/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.application.configuration.manager;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyType;
import org.alfresco.mobile.android.api.model.config.ConfigContext;
import org.alfresco.mobile.android.application.R;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FormConfigurator extends BaseConfigurator
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTANT
    // ///////////////////////////////////////////////////////////////////////////
    private static final Map<String, String> CONVERT = new HashMap<String, String>()
    {
        private static final long serialVersionUID = 1L;
        {
            put("size", PropertyIds.CONTENT_STREAM_LENGTH);
            put("mimetype", PropertyIds.CONTENT_STREAM_MIME_TYPE);
        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // MEMBERS
    // ///////////////////////////////////////////////////////////////////////////
    private Node currentNode;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public FormConfigurator(Activity activity, ConfigContext configurationContext, ViewGroup vRoot,
            Node currentNode)
    {
        super(activity, configurationContext);
        /*this.rootConfiguration = retrieveConfigurationByPath(configurationContext.getJson(),
                ConfigurationConstant.PATH_DEFAULT_DETAILS);*/
        this.vRoot = vRoot;
        this.currentNode = currentNode;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public void displayProperties(LayoutInflater inflater)
    {
        generatePropertiesByModel(inflater);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DRIVEN BY OBJECT
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Driven by the object ==> Type and Aspects Error can occurs in term of
     * subtype
     * 
     * @param inflater
     */
    private void generateProperties(LayoutInflater inflater)
    {
        Map<String, Object> properties;
        for (Entry<String, Object> menuConfig : rootConfiguration.entrySet())
        {
            // Retrieve Configuration with pointer resolution
           /* properties = retrieveAllConfiguration(menuConfig);

            // Hook view
            ViewGroup groupview = vRoot;

            // Type
            String type = currentNode.getType();

            if (properties.containsKey(type))
            {
                generateProperties(inflater, groupview, properties, currentNode, type);
            }

            // Aspects
            for (String aspect : currentNode.getAspects())
            {
                generateProperties(inflater, groupview, properties, currentNode, aspect);
            }

            properties.clear();*/
        }
    }

    private void generateProperties(LayoutInflater inflater, ViewGroup rootView, Map<String, Object> properties,
            Node currentNode, String groupId)
    {
        ViewGroup groupview = rootView;
        Bundle b = new Bundle();
        b.putSerializable(EVALUATOR_ARGUMENT_NODE, currentNode);

        if (properties.containsKey(groupId) && doesRespectEvaluators(properties, b))
        {

            // Retrieve list of properties
            Map<String, Object> props = JSONConverter.getMap(properties.get(groupId));

            // Add Headers
            if (props.containsKey(LABEL_ID))
            {
                ViewGroup grouprootview = (ViewGroup) inflater.inflate(R.layout.sdk_property_title, null);
                TextView tv = (TextView) grouprootview.findViewById(R.id.title);
                tv.setText(JSONConverter.getString(props, LABEL_ID));
                groupview = (ViewGroup) grouprootview.findViewById(R.id.group_panel);
                vRoot.addView(grouprootview);
            }

            // Generate Properties
            List<View> propertyViews = createPropertiesView(inflater, properties, currentNode);
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // DRIVEN BY MODEL
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Driven by the model. We try to display as much as we can
     * 
     * @param inflater
     */
    private void generatePropertiesByModel(LayoutInflater inflater)
    {
        Map<String, Object> properties;
        for (Entry<String, Object> menuConfig : rootConfiguration.entrySet())
        {
            // Retrieve Configuration with pointer resolution
            /*properties = retrieveAllConfiguration(menuConfig);

            // Hook view
            ViewGroup groupview = vRoot;

            for (Entry<String, Object> items : properties.entrySet())
            {
                if (items.getValue() instanceof Map)
                {
                    generateProperties(inflater, groupview, JSONConverter.getMap(items.getValue()), currentNode);
                }
            }*/

        }
    }

    private void generateProperties(LayoutInflater inflater, ViewGroup rootView, Map<String, Object> properties,
            Node currentNode)
    {
        ViewGroup groupview = rootView;
        Bundle b = new Bundle();
        b.putSerializable(EVALUATOR_ARGUMENT_NODE, currentNode);

        if (doesRespectEvaluators(properties, b))
        {
            // Generate Properties
            List<View> propertyViews = createPropertiesView(inflater, properties, currentNode);

            // Add Headers
            if (properties.containsKey(LABEL_ID) && !propertyViews.isEmpty())
            {
                ViewGroup grouprootview = (ViewGroup) inflater.inflate(R.layout.sdk_property_title, null);
                TextView tv = (TextView) grouprootview.findViewById(R.id.title);
                tv.setText(JSONConverter.getString(properties, LABEL_ID));
                groupview = (ViewGroup) grouprootview.findViewById(R.id.group_panel);
                vRoot.addView(grouprootview);
            }

            // Attach views
            for (View view : propertyViews)
            {
                groupview.addView(view);
            }
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
    private List<View> createPropertiesView(LayoutInflater inflater, Map<String, Object> properties, Node currentNode)
    {
        List<View> propertyViews = new ArrayList<View>();
        // For each properties, display the line associated
        for (Entry<String, Object> prop : properties.entrySet())
        {
            if (prop.getValue() instanceof HashMap)
            {
                // Check Visibility
                Map<String, Object> propertyConfiguration = JSONConverter.getMap(properties.get(prop.getKey()));
                if (JSONConverter.getBoolean(propertyConfiguration, VISIBILITY) == null
                        || !JSONConverter.getBoolean(propertyConfiguration, VISIBILITY))
                {
                    continue;
                }

                String key = prop.getKey();

                // Check
                if (CONVERT.containsKey(key))
                {
                    key = CONVERT.get(key);
                }

                // Add Line
                Property nodeProp = currentNode.getProperty(key);
                if (nodeProp.getValue() != null)
                {
                    View v = createPropertyView(inflater, nodeProp.getDisplayName(), nodeProp, currentNode);
                    if (v != null)
                    {
                        propertyViews.add(v);
                    }
                }
            }
        }

        return propertyViews;
    }

    protected View createPropertyView(LayoutInflater inflater, String propertyLabel, Property property, Node currentNode)
    {
        String value = null;
        if (PropertyType.DATETIME.equals(property.getType()))
        {
            value = DateFormat.getMediumDateFormat(getActivity()).format(
                    ((GregorianCalendar) property.getValue()).getTime())
                    + " "
                    + DateFormat.getTimeFormat(getActivity()).format(
                            ((GregorianCalendar) property.getValue()).getTime());
        }
        else if (property.getValue().toString() != null && !property.getValue().toString().isEmpty())
        {
            value = property.getValue().toString();
        }

        if (value == null) { return null; }

        View vr = inflater.inflate(R.layout.sdk_property_row, null);
        TextView tv = (TextView) vr.findViewWithTag("propertyName");
        tv.setText(propertyLabel);
        tv = (TextView) vr.findViewWithTag("propertyValue");
        tv.setText(value);
        tv.setClickable(true);
        tv.setFocusable(true);
        tv.setTag(currentNode);
        return vr;
    }
}
