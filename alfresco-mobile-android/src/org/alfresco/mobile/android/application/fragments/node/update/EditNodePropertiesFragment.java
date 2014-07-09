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

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.TypeDefinition;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.config.ConfigManager;
import org.alfresco.mobile.android.application.config.manager.FormConfigManager;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.definition.TypeDefinitionRequest;
import org.alfresco.mobile.android.async.node.update.UpdateNodeRequest;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

public abstract class EditNodePropertiesFragment extends AlfrescoFragment
{
    public static final String TAG = "UpdateNodeDialogFragment";

    protected static final String ARGUMENT_NODE = "node";

    protected Node node;

    private Button bcreate;

    protected ConfigManager configurationManager;

    protected TypeDefinition typeDefinition;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public EditNodePropertiesFragment()
    {
        eventBusRequired = true;
    }

    public static Bundle createBundle(Node node)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        return args;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        node = (Node) getArguments().getSerializable(ARGUMENT_NODE);

        getDialog().setTitle(R.string.edit_metadata);
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        setRootView(inflater.inflate(R.layout.config_edit_properties, container, false));
        if (getSession() == null) { return getRootView(); }

        // BUTTONS
        Button button = (Button) viewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                EditNodePropertiesFragment.this.dismiss();
            }
        });

        bcreate = (Button) viewById(R.id.validate_action);
        bcreate.setText(R.string.update);
        bcreate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                updateNode();
            }
        });

        // PROPERTIES
        configurationManager = ConfigManager.getInstance(getActivity());
        AlfrescoAccount acc = getAccount();
        if (acc == null)
        {
            acc = AlfrescoAccountManager.getInstance(getActivity()).getDefaultAccount();
        }
        if (configurationManager != null && acc != null && configurationManager.hasConfig(acc.getId()))
        {
            configure(inflater, configurationManager.getConfig(acc.getId()));
        }
        else if (configurationManager != null && acc != null)
        {
            // Configuration
            configurationManager.init(acc);
            configure(inflater, configurationManager.getConfig(acc.getId()));
        }

        return getRootView();
    }

    @Override
    public void onStart()
    {
        if (node != null)
        {
            int iconId = R.drawable.mime_folder;
            if (node.isDocument())
            {
                iconId = MimeTypeManager.getInstance(getActivity()).getIcon(node.getName());
            }
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, iconId);
        }
        super.onStart();

        EventBusManager.getInstance().register(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        EventBusManager.getInstance().unregister(this);
    }

    // //////////////////////////////////////////////////////////////////////
    // FORM GENERATOR
    // //////////////////////////////////////////////////////////////////////
    protected void configure(LayoutInflater inflater, ConfigService config)
    {
        if (typeDefinition != null)
        {
            // Properties View
            ViewGroup rootPropertiesView = (ViewGroup) viewById(R.id.properties_body);

            FormConfigManager formConfig = new FormConfigManager(getActivity(), config, rootPropertiesView);
            formConfig.displayEditForm(typeDefinition, node);
        }
        else
        {
            Operator.with(getActivity()).load(
                    new TypeDefinitionRequest.Builder((node.isDocument()) ? TypeDefinitionRequest.DOCUMENT
                            : TypeDefinitionRequest.FOLDER, node.getType()));
            displayLoading();
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    protected void updateNode()
    {
        Map<String, Serializable> props = new HashMap<String, Serializable>(2);
        new UpdateNodeRequest.Builder(node, props);
        bcreate.setEnabled(false);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected void displayData()
    {
        hide(R.id.empty);
        hide(R.id.progressbar);
        show(R.id.form_body);
    }

    protected void displayEmptyView()
    {
        show(R.id.empty);
        hide(R.id.progressbar);
        hide(R.id.form_body);
    }

    protected void displayLoading()
    {
        show(R.id.progressbar);
        hide(R.id.form_body);
        hide(R.id.empty);
    }
}
