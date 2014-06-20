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
package org.alfresco.mobile.android.application.fragments.accounts;

import java.util.Map;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.signup.SignUpEvent;
import org.alfresco.mobile.android.async.account.signup.SignUpRequest;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.data.CloudSignupRequest;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

public class CloudSignupDialogFragment extends AlfrescoFragment
{
    public static final String TAG = CloudSignupDialogFragment.class.getName();

    private static final int PASSWORD_LENGTH_MIN = 6;

    private String firstName;

    private String lastName;

    private String emailAddress;

    private String password;

    private Button signup;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public CloudSignupDialogFragment()
    {
        setStyle(android.R.style.Theme_Holo_Light_Dialog, android.R.style.Theme_Holo_Light_Dialog);
        requiredSession = false;
        checkSession = false;

    }

    protected static CloudSignupDialogFragment newInstanceByTemplate(Bundle b)
    {
        CloudSignupDialogFragment cbf = new CloudSignupDialogFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
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

        setRootView(inflater.inflate(R.layout.app_cloud_signup, container, false));

        TextView t2 = (TextView) viewById(R.id.cloud_signup_hint);
        t2.setMovementMethod(LinkMovementMethod.getInstance());

        signup = (Button) viewById(R.id.cloud_signup_action);
        signup.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signup(v);
            }
        });

        return getRootView();
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

    // //////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // //////////////////////////////////////////////////////////////////////
    public void signup(View v)
    {
        if (retrieveFormValues())
        {
            Operator.with(getActivity()).load(
                    new SignUpRequest.Builder(firstName, lastName, emailAddress, password,
                            getString(R.string.signup_key)));
            if (getActivity() instanceof BaseActivity)
            {
                ((BaseActivity) getActivity()).displayWaitingDialog();
            }
        }
    }

    public void displayAccounts(long id)
    {
        Intent i = new Intent(getActivity(), MainActivity.class);
        i.setAction(PrivateIntent.ACTION_CHECK_SIGNUP);
        i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, id);
        getActivity().startActivity(i);
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
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

        if (password.length() < PASSWORD_LENGTH_MIN || confirm.length() < PASSWORD_LENGTH_MIN
                || !confirm.equals(password)) { return false; }

        formValue = (EditText) findViewByIdInternal(R.id.cloud_signup_email);
        emailAddress = formValue.getText().toString();

        // Simplify email adress validation at the minimum.
        // No need of complex regxp
        if (emailAddress.length() <= 2 || !emailAddress.contains("@")) { return false; }

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

        if (AccessibilityUtils.isEnabled(getActivity()))
        {
            AccessibilityUtils.addHint(viewById(R.id.cloud_signup_firstname), R.string.cloud_signup_firstname_hint);
            AccessibilityUtils.addHint(viewById(R.id.cloud_signup_lastname), R.string.cloud_signup_lastname_hint);
            AccessibilityUtils.addHint(viewById(R.id.cloud_signup_email), R.string.cloud_signup_email_hint);
            AccessibilityUtils.addHint(viewById(R.id.cloud_signup_password), R.string.cloud_signup_password_hint_long);
            AccessibilityUtils.addHint(viewById(R.id.cloud_signup_confirm), R.string.cloud_signup_confirm_hint);
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

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onCloudSignUpEvent(SignUpEvent event)
    {
        CloudSignupRequest request = event.data;
        if (request != null)
        {
            AlfrescoAccount acc = AlfrescoAccountManager.getInstance(getActivity()).create(
                    getString(R.string.account_default_cloud), SessionManager.getInstance(getActivity()).getSignUpHostname(), emailAddress,
                    password, "", String.valueOf(AlfrescoAccount.TYPE_ALFRESCO_CLOUD),
                    request.getIdentifier() + "?key=" + request.getRegistrationKey(), null, null, "0");

            if (acc != null)
            {
                displayAccounts(acc.getId());
            }

        }
        else if (event.hasException)
        {
            Exception e = event.exception;
            int errorMessageId = R.string.error_general;

            if (e instanceof AlfrescoServiceException
                    && ((AlfrescoServiceException) e).getErrorCode() == CloudSignupRequest.SESSION_SIGNUP_ERROR
                    && ((AlfrescoServiceException) e).getMessage().contains("Invalid Email Address"))
            {
                errorMessageId = R.string.cloud_signup_error_email;
            }

            Log.e(TAG, Log.getStackTraceString(e));
            Bundle b = new Bundle();
            b.putInt(SimpleAlertDialogFragment.ARGUMENT_TITLE, R.string.cloud_signup_error_email_title);
            b.putInt(SimpleAlertDialogFragment.ARGUMENT_MESSAGE, errorMessageId);
            b.putInt(SimpleAlertDialogFragment.ARGUMENT_POSITIVE_BUTTON, android.R.string.ok);
            ActionUtils.actionDisplayDialog(getActivity(), b);
        }

        if (getActivity() instanceof BaseActivity)
        {
            ((BaseActivity) getActivity()).removeWaitingDialog();
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
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }
}
