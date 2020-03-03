/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.fragments.node.details;

import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.application.ui.form.FormManager;
import org.alfresco.mobile.android.async.tag.TagsEvent;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

/**
 * Responsible to display details of a specific Node.
 * 
 * @author Jean Marie Pascal
 */
public class NodePropertiesFragment extends NodeDetailsFragment
{
    public static final String TAG = NodePropertiesFragment.class.getName();

    private FormManager formManager;

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public NodePropertiesFragment()
    {
        layoutId = R.layout.fr_node_properties;
        reportAtCreation = false;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATE PARTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void display(Node refreshedNode)
    {
        // Log.d(TAG, "Display Node" + refreshedNode.getName());
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
            formManager = new FormManager(this, service, propertyViewGroup);
            formManager.prepare(node, parentNode);
            formManager.displayReadForm();
            // hasDisplayed = formManager.displayProperties(node, parentNode);
            hasDisplayed = true;
        }

        if (!hasDisplayed)
        {
            // Display Errors.
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onTagsEvent(TagsEvent event)
    {
        formManager.setOperationData(event.requestId, event);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends NodeDetailsFragment.Builder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
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
