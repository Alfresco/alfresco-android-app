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
package org.alfresco.mobile.android.application.fragments.account;

import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.WelcomeActivity;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.application.ui.form.views.EditTextFieldView;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.CreateAccountEvent;
import org.alfresco.mobile.android.async.account.CreateAccountRequest;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.exception.AlfrescoExceptionHelper;
import org.alfresco.mobile.android.platform.extensions.MobileIronManager;
import org.alfresco.mobile.android.platform.mdm.MDMConstants;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.ui.operation.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.squareup.otto.Subscribe;

public class AccountSignInFragment extends AlfrescoFragment
{
    public static final String TAG = "AccountEditFragment";

    public static final String ARGUMENT_ACCOUNT_ID = "accountID";

    public static final String ARGUMENT_ACCOUNT = "account";

    private Button validate;

    private AlfrescoAccount account;

    private EditTextFieldView passwordField;

    private EditTextFieldView userField;

    private MobileIronManager mdmManager;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountSignInFragment()
    {
        requiredSession = false;
        checkSession = false;
    }

    protected static AccountSignInFragment newInstanceByTemplate(Bundle b)
    {
        AccountSignInFragment cbf = new AccountSignInFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mdmManager = MobileIronManager.getInstance(getActivity());

        if (getArguments() != null || !getArguments().isEmpty())
        {
            account = (AlfrescoAccount) getArguments().getSerializable(ARGUMENT_ACCOUNT);
        }

        getActivity().setTitle(R.string.app_name);

        setRootView(inflater.inflate(R.layout.app_signin, container, false));

        validate = (Button) viewById(R.id.next);
        validate.setEnabled(true);
        validate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                validateServer(v);
            }
        });

        userField = (EditTextFieldView) viewById(R.id.signin_username);
        userField.setReadOnly(true);
        userField.setHint(getString(R.string.account_username));
        if (account != null)
        {
            userField.setText(account.getUsername());
        }
        else
        {
            userField.setText((String) MobileIronManager.getInstance(getActivity()).getConfig(
                    MDMConstants.ALFRESCO_USERNAME));
        }

        try
        {
            passwordField = (EditTextFieldView) viewById(R.id.signin_password);
            passwordField.setHint(getString(R.string.account_password));
            passwordField.setMandatory(true);
            passwordField.enablePassword(true);
            passwordField.requestFocus();
            UIUtils.showKeyboard(getActivity(), passwordField);
        }
        catch (Exception e)
        {
            Log.e("ERROR", Log.getStackTraceString(e));
        }

        return getRootView();
    }

    // /////////////////////////////////////////////////////////////
    // INTERNALS
    // ////////////////////////////////////////////////////////////
    private void validateServer(View v)
    {
        if (retrieveFormValues())
        {

            // Remove Keyboard
            UIUtils.hideKeyboard(getActivity());

            // Retrieve user/pass information
            String password = passwordField.getText();
            String username = userField.getText();

            // Creation or Update Password ?
            if (account != null)
            {
                // Update Account
                AlfrescoAccount acc = AlfrescoAccountManager.getInstance(getActivity()).update(account.getId(),
                        account.getTitle(), account.getUrl(), username, password, account.getRepositoryId(),
                        Integer.valueOf((int) account.getTypeId()), null, account.getAccessToken(),
                        account.getRefreshToken(), account.getIsPaidAccount() ? 1 : 0);

                // Reload
                // Affect new AlfrescoAccount to activity
                ((BaseActivity) getActivity()).setCurrentAccount(acc);
                EventBusManager.getInstance().post(new RequestSessionEvent(acc, true));
                getFragmentManager().popBackStackImmediate();

                return;
            }

            // Retrieve MDM information
            String repositoryURL = (String) mdmManager.getConfig(MDMConstants.ALFRESCO_REPOSITORY_URL);
            String description = (String) mdmManager.getConfig(MDMConstants.ALFRESCO_DISPLAY_NAME);

            // Create AlfrescoAccount + Session
            Operator.with(getActivity()).load(
                    new CreateAccountRequest.Builder(repositoryURL, username, password, description));

            OperationWaitingDialogFragment.newInstance(CreateAccountRequest.TYPE_ID, R.drawable.ic_onpremise,
                    getString(R.string.account), getString(R.string.account_verify), null, -1, null).show(
                    getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
        }
    }

    private boolean retrieveFormValues()
    {
        return !TextUtils.isEmpty(passwordField.getValue());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onAccountCreated(CreateAccountEvent event)
    {
        if (event.hasException)
        {
            ((AlfrescoActivity) getActivity()).removeWaitingDialog();
            Bundle b = new Bundle();
            b.putInt(SimpleAlertDialogFragment.ARGUMENT_ICON, R.drawable.ic_alfresco);
            b.putInt(SimpleAlertDialogFragment.ARGUMENT_TITLE, R.string.error_session_creation_title);
            b.putInt(SimpleAlertDialogFragment.ARGUMENT_POSITIVE_BUTTON, android.R.string.ok);
            b.putInt(SimpleAlertDialogFragment.ARGUMENT_MESSAGE,
                    AlfrescoExceptionHelper.getMessageId(getActivity(), event.exception));
            SimpleAlertDialogFragment.newInstance(b).show(getActivity().getFragmentManager(),
                    SimpleAlertDialogFragment.TAG);
            return;
        }

        if (getActivity() instanceof WelcomeActivity)
        {
            ConfigManager.getInstance(getActivity()).setSession(event.data.getId(),
                    SessionUtils.getSession(getActivity()));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
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
        // CREATE
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder account(AlfrescoAccount account)
        {
            extraConfiguration.putSerializable(ARGUMENT_ACCOUNT, account);
            return this;
        }
    }
}
