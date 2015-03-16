/*******************************************************************************
 * Copyright (C) 005-014 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version .0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.api.model.config.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigConstants.ViewConfigType;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.GroupConfig;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.ViewGroupConfig;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

/**
 * @author Jean Marie Pascal
 */
public class ViewHelper extends HelperConfig
{
    private LinkedHashMap<String, Object> jsonViewConfigGroups;

    private LinkedHashMap<String, ViewConfig> viewConfigIndex;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    ViewHelper(ConfigurationImpl context, StringHelper localHelper)
    {
        super(context, localHelper);
    }

    ViewHelper(ConfigurationImpl context, StringHelper localHelper, LinkedHashMap<String, ViewConfig> viewConfigIndex)
    {
        super(context, localHelper);
        this.viewConfigIndex = viewConfigIndex;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INIT
    // ///////////////////////////////////////////////////////////////////////////
    void addViews(Map<String, Object> views)
    {
        viewConfigIndex = new LinkedHashMap<>(views.size());
        ViewConfig viewConfig;
        for (Entry<String, Object> entry : views.entrySet())
        {
            viewConfig = parse(JSONConverter.getMap(entry.getValue()), entry.getKey());
            if (viewConfig == null)
            {
                continue;
            }
            viewConfigIndex.put(viewConfig.getIdentifier(), viewConfig);
        }
    }

    void addViewGroups(List<Object> viewsGroup)
    {
        jsonViewConfigGroups = new LinkedHashMap<>(viewsGroup.size());
        String viewGroupId;
        for (Object object : viewsGroup)
        {
            viewGroupId = JSONConverter.getString(JSONConverter.getMap(object), ConfigConstants.ID_VALUE);
            if (viewGroupId == null)
            {
                continue;
            }
            jsonViewConfigGroups.put(viewGroupId, object);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasViewConfig()
    {
        return ((jsonViewConfigGroups != null && !jsonViewConfigGroups.isEmpty()) || (viewConfigIndex != null && !viewConfigIndex
                .isEmpty()));
    }

    public ViewConfig getViewById(String id)
    {
        return getViewById(id, null);
    }

    public ViewConfig getViewById(String id, ConfigScope scope)
    {
        return retrieveConfig(id, scope);
    }

    protected ViewConfig retrieveConfig(String id, ConfigScope scope)
    {
        ViewConfigImpl config = null;
        if (jsonViewConfigGroups != null && jsonViewConfigGroups.containsKey(id))
        {
            config = (ViewConfigImpl) parse(JSONConverter.getMap(jsonViewConfigGroups.get(id)), id);
        }
        else if (viewConfigIndex != null && viewConfigIndex.containsKey(id))
        {
            config = (ViewConfigImpl) viewConfigIndex.get(id);
        }
        else
        {
            return null;
        }

        // Evaluate
        if (getEvaluatorHelper() == null)
        {
            return (config.getEvaluator() == null) ? config : null;
        }
        else
        {
            if (!getEvaluatorHelper().evaluate(config.getEvaluator(), scope)) { return null; }
            if (config instanceof ViewGroupConfigImpl && ((ViewGroupConfigImpl) config).getItems() != null
                    && ((ViewGroupConfigImpl) config).getItems().size() > 0)
            {
                ((ViewGroupConfigImpl) config).setChildren(evaluateChildren(((ViewGroupConfigImpl) config).getItems()));
            }
        }
        return config;

    }

    @SuppressWarnings("unchecked")
    private ArrayList<ViewConfig> evaluateChildren(List<ViewConfig> listConfig)
    {
        if (listConfig == null) { return new ArrayList<>(0); }
        ArrayList<ViewConfig> evaluatedViews = new ArrayList<>(listConfig.size());
        boolean addViewAsChild = true;
        for (ViewConfig viewConfig : listConfig)
        {
            if (getEvaluatorHelper() == null)
            {
                addViewAsChild = (((ViewConfigImpl) viewConfig).getEvaluator() == null);
            }
            else if (!getEvaluatorHelper().evaluate(((ViewConfigImpl) viewConfig).getEvaluator(), null))
            {
                addViewAsChild = false;
            }

            if (addViewAsChild)
            {
                evaluatedViews.add(viewConfig);
                if (viewConfig instanceof ViewGroupConfig && ((GroupConfig<ViewConfig>) viewConfig).getItems() != null
                        && ((GroupConfig<ViewConfig>) viewConfig).getItems().size() > 0)
                {
                    ((ViewGroupConfigImpl) viewConfig).setChildren(evaluateChildren(((ViewGroupConfigImpl) viewConfig)
                            .getItems()));
                }
            }
            addViewAsChild = true;
        }
        return evaluatedViews;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BETA
    // ///////////////////////////////////////////////////////////////////////////
    protected static ViewConfig parseBeta(String type, Map<String, Object> json)
    {
        Map<String, Object> props = new HashMap<>(1);
        props.put(ConfigConstants.VISIBLE_VALUE, JSONConverter.getBoolean(json, ConfigConstants.VISIBLE_VALUE));
        return new ViewConfigImpl(type, null, null, null, type, props, null, null);
    }

    protected static ViewConfig createBetaRootMenu(String defaultMenuLabel, ArrayList<ViewConfig> configs)
    {
        return new ViewGroupConfigImpl(null, defaultMenuLabel, null, configs, null);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // V1.0
    // ///////////////////////////////////////////////////////////////////////////
    protected ViewConfig parse(Object object)
    {
        if (object instanceof Map)
        {
            Map<String, Object> viewMap = JSONConverter.getMap(object);
            if (viewMap.containsKey(ConfigConstants.ITEM_TYPE_VALUE))
            {

                ViewConfigType type = ViewConfigType.fromValue(JSONConverter.getString(viewMap,
                        ConfigConstants.ITEM_TYPE_VALUE));

                if (type == null)
                {
                    type = ViewConfigType.VIEW;
                }

                switch (type)
                {
                    case VIEW_ID:
                        // View is defined inside the views registry
                        return getViewById(JSONConverter.getString(viewMap, ViewConfigType.VIEW_ID.value()));
                    case VIEW_GROUP_ID:
                        // View is defined inside the view group registry
                        return getViewById(JSONConverter.getString(viewMap, ViewConfigType.VIEW_GROUP_ID.value()));
                    case VIEW:
                    default:
                        // inline definition
                        return parse(JSONConverter.getMap(JSONConverter.getMap(object).get(ConfigConstants.VIEW_VALUE)));
                }
            }
            else
            {
                return parse(JSONConverter.getMap(object), null);
            }
        }
        else if (object instanceof String)
        {
            return getViewById((String) object);
        }
        else
        {
            return null;
        }
    }

    protected ViewConfig parse(Map<String, Object> json, String identifier)
    {
        ItemConfigData data = new ItemConfigData(identifier, json, getConfiguration());

        // Forms
        ArrayList<String> formsId = null;
        if (json.containsKey(ConfigConstants.PARAMS_FORMS))
        {
            List<Object> listFormId = JSONConverter.getList(json.get(ConfigConstants.PARAMS_FORMS));
            formsId = new ArrayList<>(listFormId.size());
            for (Object formId : listFormId)
            {
                if (formId instanceof String)
                {
                    formsId.add((String) formId);
                }
            }
        }
        else
        {
            formsId = new ArrayList<>(0);
        }

        // Check if it's a group view
        LinkedHashMap<String, ViewConfig> childrenIndex = null;
        if (json.containsKey(ConfigConstants.ITEMS_VALUE))
        {
            List<Object> childrenObject = JSONConverter.getList(json.get(ConfigConstants.ITEMS_VALUE));
            LinkedHashMap<String, ViewConfig> childrenViewConfig = new LinkedHashMap<>(childrenObject.size());
            ViewConfig viewConfig = null;
            for (Object child : childrenObject)
            {
                viewConfig = parse(child);
                if (viewConfig == null)
                {
                    continue;
                }
                childrenViewConfig.put(viewConfig.getIdentifier(), viewConfig);
            }
            childrenIndex = childrenViewConfig;
            return new ViewGroupConfigImpl(data.identifier, data.iconIdentifier, data.label, data.description,
                    data.type, data.properties, childrenIndex, formsId, data.evaluatorId);
        }
        else
        {
            return new ViewConfigImpl(data.identifier, data.iconIdentifier, data.label, data.description, data.type,
                    data.properties, formsId, data.evaluatorId);
        }
    }
}
