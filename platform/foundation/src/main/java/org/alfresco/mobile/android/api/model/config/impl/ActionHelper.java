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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.config.ActionConfig;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigConstants.ActionConfigType;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.GroupConfig;
import org.alfresco.mobile.android.api.model.config.ViewGroupConfig;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

/**
 * @author Jean Marie Pascal
 */
public class ActionHelper extends HelperConfig
{
    private LinkedHashMap<String, Object> jsonActionConfigGroups;

    private LinkedHashMap<String, ActionConfig> actionConfigIndex;

    private LinkedHashMap<String, ActionConfig> actionIdIndex;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    ActionHelper(ConfigurationImpl context, StringHelper localHelper)
    {
        super(context, localHelper);
    }

    ActionHelper(ConfigurationImpl context, StringHelper localHelper,
            LinkedHashMap<String, ActionConfig> ActionConfigIndex)
    {
        super(context, localHelper);
        this.actionConfigIndex = ActionConfigIndex;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INIT
    // ///////////////////////////////////////////////////////////////////////////
    void addActions(Map<String, Object> views)
    {
        actionConfigIndex = new LinkedHashMap<>(views.size());
        actionIdIndex = new LinkedHashMap<>(views.size());
        ActionConfig actionConfig;
        for (Entry<String, Object> entry : views.entrySet())
        {
            actionConfig = parse(JSONConverter.getMap(entry.getValue()), entry.getKey());
            if (actionConfig == null)
            {
                continue;
            }
            actionConfigIndex.put(actionConfig.getType(), actionConfig);
            actionIdIndex.put(actionConfig.getIdentifier(), actionConfig);
        }
    }

    void addActionGroups(List<Object> viewsGroup)
    {
        jsonActionConfigGroups = new LinkedHashMap<>(viewsGroup.size());
        String viewGroupId;
        for (Object object : viewsGroup)
        {
            viewGroupId = JSONConverter.getString(JSONConverter.getMap(object), ConfigConstants.ID_VALUE);
            if (viewGroupId == null)
            {
                continue;
            }
            jsonActionConfigGroups.put(viewGroupId, object);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasActionConfig()
    {
        return ((jsonActionConfigGroups != null && !jsonActionConfigGroups.isEmpty())
                || (actionConfigIndex != null && !actionConfigIndex.isEmpty()));
    }

    public ActionConfig getActionByType(String id)
    {
        return getActionByType(id, null);
    }

    public ActionConfig getActionByType(String id, ConfigScope scope)
    {
        return retrieveConfig(id, scope);
    }

    protected ActionConfig retrieveConfig(String id, ConfigScope scope)
    {
        ActionConfigImpl config = null;
        if (jsonActionConfigGroups != null && jsonActionConfigGroups.containsKey(id))
        {
            config = (ActionConfigImpl) parse(JSONConverter.getMap(jsonActionConfigGroups.get(id)), id);
        }
        else if (actionConfigIndex != null && actionConfigIndex.containsKey(id))
        {
            config = (ActionConfigImpl) actionConfigIndex.get(id);
        }
        else if (actionIdIndex != null && actionIdIndex.containsKey(id))
        {
            config = (ActionConfigImpl) actionIdIndex.get(id);
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
            if (config instanceof ActionGroupConfigImpl && ((ActionGroupConfigImpl) config).getItems() != null
                    && ((ActionGroupConfigImpl) config).getItems().size() > 0)
            {
                ((ActionGroupConfigImpl) config)
                        .setChildren(evaluateChildren(((ActionGroupConfigImpl) config).getItems()));
            }
        }
        return config;

    }

    @SuppressWarnings("unchecked")
    private ArrayList<ActionConfig> evaluateChildren(List<ActionConfig> listConfig)
    {
        if (listConfig == null) { return new ArrayList<>(0); }
        ArrayList<ActionConfig> evaluatedViews = new ArrayList<>(listConfig.size());
        boolean addViewAsChild = true;
        for (ActionConfig ActionConfig : listConfig)
        {
            if (getEvaluatorHelper() == null)
            {
                addViewAsChild = (((ActionConfigImpl) ActionConfig).getEvaluator() == null);
            }
            else if (!getEvaluatorHelper().evaluate(((ActionConfigImpl) ActionConfig).getEvaluator(), null))
            {
                addViewAsChild = false;
            }

            if (addViewAsChild)
            {
                evaluatedViews.add(ActionConfig);
                if (ActionConfig instanceof ViewGroupConfig
                        && ((GroupConfig<ActionConfig>) ActionConfig).getItems() != null
                        && ((GroupConfig<ActionConfig>) ActionConfig).getItems().size() > 0)
                {
                    ((ActionGroupConfigImpl) ActionConfig)
                            .setChildren(evaluateChildren(((ActionGroupConfigImpl) ActionConfig).getItems()));
                }
            }
            addViewAsChild = true;
        }
        return evaluatedViews;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // V1.0
    // ///////////////////////////////////////////////////////////////////////////
    protected ActionConfig parse(Object object)
    {
        if (object instanceof Map)
        {
            Map<String, Object> viewMap = JSONConverter.getMap(object);
            if (viewMap.containsKey(ConfigConstants.ITEM_TYPE_VALUE))
            {

                ActionConfigType type = ActionConfigType
                        .fromValue(JSONConverter.getString(viewMap, ConfigConstants.ITEM_TYPE_VALUE));

                if (type == null)
                {
                    type = ActionConfigType.ACTION;
                }

                switch (type)
                {
                    case ACTION_ID:
                        // View is defined inside the views registry
                        return getActionByType(JSONConverter.getString(viewMap, ActionConfigType.ACTION_ID.value()));
                    case ACTION_GROUP_ID:
                        // View is defined inside the view group registry
                        return getActionByType(
                                JSONConverter.getString(viewMap, ActionConfigType.ACTION_GROUP_ID.value()));
                    case ACTION:
                    default:
                        // inline definition
                        return parse(
                                JSONConverter.getMap(JSONConverter.getMap(object).get(ConfigConstants.VIEW_VALUE)));
                }
            }
            else
            {
                return parse(JSONConverter.getMap(object), null);
            }
        }
        else if (object instanceof String)
        {
            return getActionByType((String) object);
        }
        else
        {
            return null;
        }
    }

    protected ActionConfig parse(Map<String, Object> json, String identifier)
    {
        ItemConfigData data = new ItemConfigData(identifier, json, getConfiguration());

        // Enable
        Boolean isEnable = true;
        if (json.containsKey(ConfigConstants.ENABLE_VALUE))
        {
            isEnable = JSONConverter.getBoolean(json, ConfigConstants.ENABLE_VALUE);
        }

        if (isEnable == null)
        {
            isEnable = true;
        }

        // Check if it's a group view
        LinkedHashMap<String, ActionConfig> childrenIndex = null;
        if (json.containsKey(ConfigConstants.ITEMS_VALUE))
        {
            List<Object> childrenObject = JSONConverter.getList(json.get(ConfigConstants.ITEMS_VALUE));
            LinkedHashMap<String, ActionConfig> childrenActionConfig = new LinkedHashMap<>(childrenObject.size());
            ActionConfig ActionConfig = null;
            for (Object child : childrenObject)
            {
                ActionConfig = parse(child);
                if (ActionConfig == null)
                {
                    continue;
                }
                childrenActionConfig.put(ActionConfig.getType(), ActionConfig);
            }
            childrenIndex = childrenActionConfig;
            return new ActionGroupConfigImpl(data.identifier, data.iconIdentifier, data.label, data.description,
                    data.type, data.properties, childrenIndex, data.evaluatorId);
        }
        else
        {
            return new ActionConfigImpl(data.identifier, data.iconIdentifier, data.label, data.description, data.type,
                    data.properties, isEnable, data.evaluatorId);
        }
    }
}
