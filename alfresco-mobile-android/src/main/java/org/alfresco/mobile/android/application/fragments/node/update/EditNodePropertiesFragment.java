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

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.application.ui.form.FormManager;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
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

    protected Button bcreate;

    protected ConfigManager configurationManager;

    protected ModelDefinition modelDefinition;

    protected FormManager formManager;

    protected ConfigService configService;

    protected View resultView;

    protected Boolean refresh = false;

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

        if (getDialog() != null)
        {
            getDialog().setTitle(R.string.edit_metadata);
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }

        if (getRootView() == null)
        {
            setRootView(inflater.inflate(R.layout.form_edit_properties, container, false));
        }

        if (getSession() == null) { return getRootView(); }

        // BUTTONS
        Button button = (Button) viewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (getDialog() != null)
                {
                    dismiss();
                }
                else
                {
                    getActivity().finish();
                }
            }
        });

        bcreate = (Button) viewById(R.id.validate_action);
        bcreate.setText(R.string.update);

        if (formManager == null)
        {
            // PROPERTIES
            if (configurationManager == null)
            {
                configurationManager = ConfigManager.getInstance(getActivity());
            }
            if (configurationManager != null && getAccount() != null)
            {
                configService = configurationManager.getConfig(getAccount().getId(), ConfigTypeIds.FORMS);
                configure(inflater);
            }
        }
        else
        {
            refresh = true;
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
            if (getDialog() != null)
            {
                getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, iconId);
            }
        }

        if (refresh)
        {
            // We do the refreshEditionView to display the latest value inside
            // the BaseField
            formManager.refreshViews();
            refresh = false;
        }

        super.onStart();
    }

    // //////////////////////////////////////////////////////////////////////
    // FORM GENERATOR
    // //////////////////////////////////////////////////////////////////////
    protected void configure(LayoutInflater inflater)
    {
        displayLoading();
        new Generator((ViewGroup) viewById(R.id.properties_body)).execute();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected void displayData()
    {
        hide(R.id.empty);
        hide(R.id.progressbar);
        hide(R.id.progressbar_group);
        show(R.id.form_body);
    }

    protected void displayEmptyView()
    {
        show(R.id.empty);
        hide(R.id.progressbar);
        show(R.id.progressbar_group);
        hide(R.id.form_body);
    }

    protected void displayLoading()
    {
        show(R.id.progressbar);
        show(R.id.progressbar_group);
        hide(R.id.form_body);
        hide(R.id.empty);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // AsyncTask
    // ///////////////////////////////////////////////////////////////////////////
    public class Generator extends AsyncTask<Void, Void, View>
    {
        private ViewGroup rootPropertiesView;

        public Generator(ViewGroup rootPropertiesView)
        {
            this.rootPropertiesView = rootPropertiesView;
        }

        @Override
        protected View doInBackground(Void... params)
        {
            // Solve issue during UI creation outside main thread
            if (Looper.myLooper() == null)
            {
                Looper.prepare();
            }

            if (getSession() instanceof RepositorySession)
            {
                if (node.isDocument())
                {
                    modelDefinition = getSession().getServiceRegistry().getModelDefinitionService()
                            .getDocumentTypeDefinition((Document) node);
                }
                else
                {
                    modelDefinition = getSession().getServiceRegistry().getModelDefinitionService()
                            .getFolderTypeDefinition((Folder) node);
                }
            }

            // Generating the form can be long depending on complexity of the
            // configuration & evaluator
            formManager = new FormManager(EditNodePropertiesFragment.this, configService, rootPropertiesView);
            formManager.prepare(modelDefinition, node);
            return formManager.displayEditForm();
        }

        @Override
        protected void onPostExecute(View result)
        {
            displayData();
            resultView = result;
            rootPropertiesView.removeAllViews();
            if (result != null)
            {
                rootPropertiesView.addView(result);
            }
        }
    }
}
