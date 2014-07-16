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
package org.alfresco.mobile.android.application.fragments.node.details;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.ConfigConstants;
import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyType;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.config.ConfigManager;
import org.alfresco.mobile.android.application.config.manager.FormConfigManager;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Responsible to display details of a specific Node.
 * 
 * @author Jean Marie Pascal
 */
public class NodePropertiesFragment extends NodeDetailsFragment
{
    public static final String TAG = NodePropertiesFragment.class.getName();

    private ConfigManager configurationManager;

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public NodePropertiesFragment()
    {
        layoutId = R.layout.app_properties;
    }

    protected static NodePropertiesFragment newInstanceByTemplate(Bundle b)
    {
        NodePropertiesFragment bf = new NodePropertiesFragment();
        bf.setArguments(b);
        return bf;
    };

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(super.onCreateView(inflater, container, savedInstanceState));
        return getRootView();
    }

    @Override
    public void onResume()
    {
        if (node != null && viewById(R.id.metadata) != null
                && ((ViewGroup) viewById(R.id.metadata)).getChildCount() == 0)
        {
            // Detect if isRestrictable
            isRestrictable = node.hasAspect(ContentModel.ASPECT_RESTRICTABLE);
            display(node);
        }
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATE PARTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void display(Node refreshedNode)
    {
        Log.d(TAG, "Display Node" + refreshedNode.getName());
        // Detect if restrictable
        isRestrictable = node.hasAspect(ContentModel.ASPECT_RESTRICTABLE);
        renditionManager = RenditionManagerImpl.getInstance(getActivity());

        displayProperties();
    }

    protected void displayProperties()
    {
        ViewGroup propertyViewGroup = (ViewGroup) viewById(R.id.metadata);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Configuration available ?
        configurationManager = ConfigManager.getInstance(getActivity());
        boolean hasDisplayed = false;
        if (configurationManager != null
                && configurationManager.hasConfig(getAccount().getId())
                && configurationManager.getConfig(getAccount().getId()).getFormConfig(
                        ConfigConstants.VIEW_NODE_PROPERTIES) != null)
        {
            FormConfigManager config = new FormConfigManager(this, configurationManager.getConfig(getAccount()
                    .getId()), propertyViewGroup);
            hasDisplayed = config.displayProperties(node);
        }

        if (!hasDisplayed)
        {
            generateProperties(inflater, propertyViewGroup,
                    MetadataUtils.getPropertyLabel(ContentModel.ASPECT_GENERAL), node, ContentModel.ASPECT_GENERAL);
            generateProperties(inflater, propertyViewGroup,
                    MetadataUtils.getPropertyLabel(ContentModel.ASPECT_GEOGRAPHIC), node,
                    ContentModel.ASPECT_GEOGRAPHIC);
            generateProperties(inflater, propertyViewGroup, MetadataUtils.getPropertyLabel(ContentModel.ASPECT_EXIF),
                    node, ContentModel.ASPECT_EXIF);
            generateProperties(inflater, propertyViewGroup, MetadataUtils.getPropertyLabel(ContentModel.ASPECT_AUDIO),
                    node, ContentModel.ASPECT_AUDIO);
            generateProperties(inflater, propertyViewGroup,
                    MetadataUtils.getPropertyLabel(ContentModel.ASPECT_RESTRICTABLE), node,
                    ContentModel.ASPECT_RESTRICTABLE);
        }
    }

    private void generateProperties(LayoutInflater inflater, ViewGroup rootView, Map<String, Integer> properties,
            Node currentNode, String groupId)
    {
        ViewGroup groupview = rootView;

        if (groupId != ContentModel.ASPECT_GENERAL)
        {
            if (!currentNode.hasAspect(groupId)) { return; }
        }

        // Add Headers
        ViewGroup grouprootview = (ViewGroup) inflater.inflate(R.layout.form_header, null);
        TextView tv = (TextView) grouprootview.findViewById(R.id.title);
        tv.setText(MetadataUtils.getAspectLabel(groupId));
        groupview = (ViewGroup) grouprootview.findViewById(R.id.group_panel);
        rootView.addView(grouprootview);

        // Generate Properties
        List<View> propertyViews = createPropertiesView(inflater, properties, currentNode);
        for (View view : propertyViews)
        {
            groupview.addView(view);
        }
    }

    private List<View> createPropertiesView(LayoutInflater inflater, Map<String, Integer> properties, Node currentNode)
    {
        List<View> propertyViews = new ArrayList<View>();
        View v = null;
        // For each properties, display the line associated
        for (Entry<String, Integer> prop : properties.entrySet())
        {
            if (currentNode.getProperty(prop.getKey()) != null
                    && currentNode.getProperty(prop.getKey()).getValue() != null)
            {
                v = createPropertyView(currentNode, getString(prop.getValue()), inflater,
                        currentNode.getProperty(prop.getKey()));
                if (v != null)
                {
                    propertyViews.add(v);
                }
            }
        }

        return propertyViews;
    }

    protected View createPropertyView(Node currentNode, String propertyLabel, LayoutInflater inflater, Property property)
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

        View vr = inflater.inflate(R.layout.form_read_row, null);
        TextView tv = (TextView) vr.findViewWithTag("propertyName");
        tv.setText(propertyLabel);
        tv = (TextView) vr.findViewWithTag("propertyValue");
        tv.setText(value);
        tv.setClickable(true);
        tv.setFocusable(true);
        tv.setCustomSelectionActionModeCallback(new Callback()
        {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu)
            {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode)
            {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item)
            {
                return false;
            }
        });
        tv.setTag(currentNode);
        return vr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends NodeDetailsFragment.Builder
    {

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE FRAGMENT
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }
}
