/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.accounts.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;

@TargetApi(14)
public class WizardEditAccountFragment extends DialogFragment
{
    public static final String TAG = "EditAccountFragment";

    public WizardEditAccountFragment()
    {
        setStyle(android.R.style.Theme_Holo_Light_Dialog, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public void onStart()
    {
        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_alfresco);
        }
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().setTitle("Account Information");
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }
        else
        {
            getActivity().getActionBar().show();
            getActivity().setTitle("Account Information");
        }

        View v = inflater.inflate(R.layout.sdkapp_wizard_account_step2, container, false);

        v.setBackgroundColor(Color.parseColor("#FFFFFF"));

        Button advanced = (Button) v.findViewById(R.id.advanced);
        advanced.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                View vt = (View) findViewByIdInternal(R.id.advanced_settings);
                if (vt.getVisibility() == View.VISIBLE)
                    vt.setVisibility(View.GONE);
                else
                    vt.setVisibility(View.VISIBLE);
            }
        });

        Button step2 = (Button) v.findViewById(R.id.next);
        step2.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                validateServer(v);
            }
        });
        
        final Switch sw = (Switch) v.findViewById(R.id.repository_https);
        final EditText portForm = (EditText) v.findViewById(R.id.repository_port);
        sw.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (sw.isChecked() == false
                        && (portForm.getText().toString() == null || portForm.getText().toString().equals("443")))
                {
                    portForm.setText("80");
                }
                else if (sw.isChecked() == true
                        && (portForm.getText().toString() == null || portForm.getText().toString().equals("80")))
                {
                    portForm.setText("443");
                }
            }
        });
        
        sw.setChecked(true);
        portForm.setText("443");
        
        return v;
    }
    
    private String url = null, host = null, username = null, password = null, servicedocument = null,
            description = null;

    private boolean https = false;

    private int port;

    private void validateServer(View v)
    {
        retrieveFormValues();
        // Create Session
        AccountLoaderCallback call = new AccountLoaderCallback(getActivity(), this, url, username, password,
                description);
        LoaderManager lm = getLoaderManager();
        lm.restartLoader(SessionLoader.ID, null, call);
        lm.getLoader(SessionLoader.ID).forceLoad();
    }

    private void retrieveFormValues()
    {

        EditText form_value = (EditText) findViewByIdInternal(R.id.repository_username);
        username = form_value.getText().toString();

        form_value = (EditText) findViewByIdInternal(R.id.repository_description);
        description = form_value.getText().toString();

        form_value = (EditText) findViewByIdInternal(R.id.repository_password);
        password = form_value.getText().toString();

        form_value = (EditText) findViewByIdInternal(R.id.repository_hostname);
        if (form_value != null && form_value.getText() != null && form_value.getText().length() > 0)
            host = form_value.getText().toString();
        else
        {
            MessengerManager.showToast(getActivity(), "URL error");
            return;
        }

        Switch sw = (Switch) findViewByIdInternal(R.id.repository_https);
        https = sw.isChecked();
        String protocol = https ? "https" : "http";

        form_value = (EditText) findViewByIdInternal(R.id.repository_port);
        port = Integer.parseInt(form_value.getText().toString());

        form_value = (EditText) findViewByIdInternal(R.id.repository_servicedocument);
        servicedocument = form_value.getText().toString();
        URL u = null;
        try
        {
            u = new URL(protocol, host, port, servicedocument);
        }
        catch (MalformedURLException e)
        {
            MessengerManager.showToast(getActivity(), "URL error");
            return;
        }

        url = u.toString();
    }

    public void validateAccount()
    {
        ActionManager.actionRefresh(WizardEditAccountFragment.this, IntentIntegrator.CATEGORY_REFRESH_ALL,
                IntentIntegrator.ACCOUNT_TYPE);
        getActivity().finish();
    }

    private View findViewByIdInternal(int id)
    {
        if (getDialog() != null)
        {
            return getDialog().findViewById(id);
        }
        else
        {
            return getActivity().findViewById(id);
        }
    }
}
