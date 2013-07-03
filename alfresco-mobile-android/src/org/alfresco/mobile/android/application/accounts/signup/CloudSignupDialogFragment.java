/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.accounts.signup;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.HomeScreenActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.UIUtils;

import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CloudSignupDialogFragment extends DialogFragment
{
    public static final String TAG = "SignupCloudDialogFragment";
    
    private static final int PASSWORD_LENGTH_MIN = 6;

    private String firstName;

    private String lastName;

    private String emailAddress;

    private String password;

    private Button signup;

    public CloudSignupDialogFragment()
    {
        setStyle(android.R.style.Theme_Holo_Light_Dialog, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public void onStart()
    {
        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_cloud);
        }

        initForm();

        if (retrieveFormValues())
        {
            signup.setEnabled(true);
        }
        else
        {
            signup.setEnabled(false);
        }

        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(true);
        if (getDialog() != null)
        {
            getDialog().setTitle(R.string.sign_up_cloud);
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }
        else
        {
            UIUtils.displayTitle(getActivity(), R.string.sign_up_cloud);
        }

        View v = inflater.inflate(R.layout.app_cloud_signup, container, false);

        TextView t2 = (TextView) v.findViewById(R.id.cloud_signup_hint);
        t2.setMovementMethod(LinkMovementMethod.getInstance());

        signup = (Button) v.findViewById(R.id.cloud_signup_action);
        signup.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signup(v);
            }
        });

        return v;
    }

    public void signup(View v)
    {
        if (retrieveFormValues())
        {
            CloudSignupLoaderCallback call = new CloudSignupLoaderCallback(getActivity(), this, firstName, lastName,
                    emailAddress, password, "Alfresco Cloud");
            LoaderManager lm = getLoaderManager();
            lm.restartLoader(CloudSignupLoader.ID, null, call);
            lm.getLoader(CloudSignupLoader.ID).forceLoad();
        }
    }

    private boolean retrieveFormValues()
    {

        EditText formValue = (EditText) findViewByIdInternal(R.id.cloud_signup_firstname);
        firstName = formValue.getText().toString();

        if (firstName.length() == 0) { return false; }

        formValue = (EditText) findViewByIdInternal(R.id.cloud_signup_lastname);
        lastName = formValue.getText().toString();

        if (lastName.length() == 0) { return false; }

        formValue = (EditText) findViewByIdInternal(R.id.cloud_signup_password);
        password = formValue.getText().toString();

        formValue = (EditText) findViewByIdInternal(R.id.cloud_signup_confirm);
        String confirm = formValue.getText().toString();

        if (password.length() < PASSWORD_LENGTH_MIN || confirm.length() < PASSWORD_LENGTH_MIN || !confirm.equals(password)) { return false; }

        formValue = (EditText) findViewByIdInternal(R.id.cloud_signup_email);
        emailAddress = formValue.getText().toString();

        if (emailAddress.length() == 0 || !emailAddress.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) { return false; }

        return true;
    }

    private void initForm()
    {
        int[] ids = new int[] { R.id.cloud_signup_firstname, R.id.cloud_signup_lastname, R.id.cloud_signup_password,
                R.id.cloud_signup_confirm, R.id.cloud_signup_email };
        EditText formValue = null;
        for (int i = 0; i < ids.length; i++)
        {
            formValue = (EditText) findViewByIdInternal(ids[i]);
            formValue.addTextChangedListener(watcher);
        }
    }

    private TextWatcher watcher = new TextWatcher()
    {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if (retrieveFormValues())
            {
                signup.setEnabled(true);
            }
            else
            {
                signup.setEnabled(false);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            // DO Nothing
        }

        @Override
        public void afterTextChanged(Editable s)
        {

        }
    };

    public void displayAccounts()
    {
        Intent i = new Intent(getActivity(), MainActivity.class);
        i.setAction(IntentIntegrator.ACTION_CHECK_SIGNUP);
        getActivity().startActivity(i);
        if (getActivity() instanceof HomeScreenActivity)
        {
            getActivity().finish();
        }
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
