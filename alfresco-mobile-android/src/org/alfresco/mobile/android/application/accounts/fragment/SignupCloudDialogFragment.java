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

import org.alfresco.mobile.android.api.asynchronous.CloudSignupLoader;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignupCloudDialogFragment extends DialogFragment
{
    public static final String TAG = "SignupCloudDialogFragment";

    private String firstName;

    private String lastName;

    private String emailAddress;

    private String password;

    private String apiKey;

    public SignupCloudDialogFragment()
    {
        setStyle(android.R.style.Theme_Holo_Dialog, android.R.style.Theme_Holo_Dialog);
    }

    @Override
    public void onStart()
    {
        getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_cloud);
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(R.string.sign_up_cloud);
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        View v = inflater.inflate(R.layout.app_cloud_signup, container, false);

        TextView t2 = (TextView) v.findViewById(R.id.cloud_signup_hint);
        t2.setMovementMethod(LinkMovementMethod.getInstance());

        Button signup = (Button) v.findViewById(R.id.cloud_signup_action);
        signup.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signup(v);
            }
        });
        
        
        /*((EditText) v.findViewById(R.id.cloud_signup_firstname)).setText("Jean Marie");
        ((EditText) v.findViewById(R.id.cloud_signup_email)).setText("jeanmarie.pascal@neuf.fr");
        ((EditText) v.findViewById(R.id.cloud_signup_lastname)).setText("PASCAL");
        ((EditText) v.findViewById(R.id.cloud_signup_password)).setText("password");
        ((EditText) v.findViewById(R.id.cloud_signup_confirm)).setText("password");*/

        return v;
    }

    public void signup(View v)
    {
        if (retrieveFormValues())
        {
            SignupCloudLoaderCallback call = new SignupCloudLoaderCallback(getActivity(), this, firstName, lastName,
                    emailAddress, password, apiKey, "Alfresco Cloud");
            LoaderManager lm = getLoaderManager();
            lm.restartLoader(CloudSignupLoader.ID, null, call);
            lm.getLoader(CloudSignupLoader.ID).forceLoad();
        }
    }

    private boolean retrieveFormValues()
    {

        EditText form_value = (EditText) getDialog().findViewById(R.id.cloud_signup_firstname);
        firstName = form_value.getText().toString();

        if (firstName.length() == 0)
        {
            MessengerManager.showToast(getActivity(), "Password error");
            return false;
        }

        form_value = (EditText) getDialog().findViewById(R.id.cloud_signup_lastname);
        lastName = form_value.getText().toString();

        if (lastName.length() == 0)
        {
            MessengerManager.showToast(getActivity(), "Password error");
            return false;
        }

        form_value = (EditText) getDialog().findViewById(R.id.cloud_signup_password);
        password = form_value.getText().toString();

        form_value = (EditText) getDialog().findViewById(R.id.cloud_signup_password);
        String confirm = form_value.getText().toString();

        if (!confirm.equals(password) && password.length() <= 6 && confirm.length() <= 6)
        {
            MessengerManager.showToast(getActivity(), "Password error");
            return false;
        }

        form_value = (EditText) getDialog().findViewById(R.id.cloud_signup_email);
        emailAddress = form_value.getText().toString();

        if (emailAddress.length() == 0)
        {
            MessengerManager.showToast(getActivity(), "Email error");
            return false;
        }

        return true;
    }
    
    public void displayAccounts(){
        Intent i = new Intent(getActivity(), MainActivity.class);
        i.setAction(IntentIntegrator.ACTION_CHECK_SIGNUP);
        getActivity().startActivity(i);
    }
}
