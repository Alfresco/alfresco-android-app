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

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyType;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.FormConfigManager;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.async.tag.TagsEvent;

import android.app.Activity;
import android.app.Fragment;
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

import com.squareup.otto.Subscribe;

/**
 * Responsible to display details of a specific Node.
 * 
 * @author Jean Marie Pascal
 */
public class NodePropertiesFragment extends NodeDetailsFragment
{
    public static final String TAG = NodePropertiesFragment.class.getName();

    private String descriptionLabel;

    private FormConfigManager config;

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
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        descriptionLabel = getResources().getString(R.string.metadata_prop_description);
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
        boolean hasDisplayed = false;
        ConfigService service = ConfigManager.getInstance(getActivity()).getConfig(getAccount().getId(),
                ConfigTypeIds.FORMS);
        if (service != null && service.getFormConfig(ConfigConstants.VIEW_NODE_PROPERTIES) != null)
        {
            config = new FormConfigManager(this, service, propertyViewGroup);
            hasDisplayed = config.displayProperties(node, parentNode);
        }

        if (!hasDisplayed)
        {
            // Display Errors.
        }

    }

    private void generateProperties(LayoutInflater inflater, ViewGroup rootView, Map<String, Integer> properties,
            Node currentNode, String groupId)
    {
        ViewGroup groupview = rootView;

        if (!groupId.equals(ContentModel.ASPECT_GENERAL))
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

        View vr = null;
        if (descriptionLabel.equals(property.getDisplayName()))
        {
            vr = inflater.inflate(R.layout.form_read_textmultiline, null);
        }
        else
        {
            vr = inflater.inflate(R.layout.form_read_row, null);
        }

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
    // REQUEST
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onTagsEvent(TagsEvent event)
    {
        config.setOperationData(event.requestId, event);
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
