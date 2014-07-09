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
package org.alfresco.mobile.android.application.fragments.node.update;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.definition.TypeDefinitionEvent;
import org.alfresco.mobile.android.async.node.update.UpdateNodeRequest;
import org.alfresco.mobile.android.platform.utils.BundleUtils;

import com.squareup.otto.Subscribe;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditPropertiesDialogFragment extends EditNodePropertiesFragment
{
    public static final String TAG = EditPropertiesDialogFragment.class.getName();

    protected static final String ARGUMENT_FOLDER = "folder";

    private Folder folder;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    protected EditPropertiesDialogFragment()
    {
    }

    public static EditPropertiesDialogFragment newInstanceByTemplate(Bundle b)
    {
        EditPropertiesDialogFragment adf = new EditPropertiesDialogFragment();
        adf.setArguments(b);
        return adf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        folder = (Folder) getArguments().getSerializable(ARGUMENT_FOLDER);
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    protected void updateNode(EditText tv, EditText desc, Button bcreate)
    {
        bcreate.setEnabled(false);

        Map<String, Serializable> props = new HashMap<String, Serializable>(2);
        props.put(ContentModel.PROP_NAME, tv.getText().toString().trim());
        if (desc.getText() != null && desc.getText().length() > 0)
        {
            props.put(ContentModel.PROP_DESCRIPTION, desc.getText().toString());
        }

        Operator.with(getActivity()).load(new UpdateNodeRequest.Builder(folder, node, props));
        dismiss();
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(TypeDefinitionEvent event)
    {
        if (event.hasException)
        {
            displayEmptyView();
            ((TextView) viewById(R.id.empty_text)).setText(R.string.empty_child);
        }
        else if (getActivity() != null)
        {
            displayData();
            typeDefinition = event.data;
            configure(LayoutInflater.from(getActivity()), configurationManager.getConfig(getAccount().getId()));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS & HELPERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            templateArguments = new String[] {};
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder parentFolder(Folder folder)
        {
            BundleUtils.addIfNotNull(extraConfiguration, ARGUMENT_FOLDER, folder);
            return this;
        }

        public Builder node(Node node)
        {
            BundleUtils.addIfNotNull(extraConfiguration, ARGUMENT_NODE, node);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }

}
