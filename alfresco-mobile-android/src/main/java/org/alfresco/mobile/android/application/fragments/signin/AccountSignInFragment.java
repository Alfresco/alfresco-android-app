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
package org.alfresco.mobile.android.application.fragments.signin;

import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.WelcomeActivity;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.CreateAccountEvent;
import org.alfresco.mobile.android.async.account.CreateAccountRequest;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.exception.AlfrescoExceptionHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.mdm.MDMManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.operation.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.otto.Subscribe;

/**
 * Fragment uses by MDM or 3rd PARTY APP to create an Alfresco Account by
 * passing informations.
 */
public class AccountSignInFragment extends AlfrescoFragment
{
    public static final String TAG = AccountSignInFragment.class.getSimpleName();

    public static final String ARGUMENT_ACCOUNT_ID = "accountID";

    private static final String ARGUMENT_ACCOUNT = "account";

    private static final String ARGUMENT_SHARE_URL = "shareUrl";

    private static final String ARGUMENT_REPO_URL = "repoUrl";

    private static final String ARGUMENT_USERNAME = "username";

    private AlfrescoAccount account;

    private String usernameIntent, shareUrlIntent, alfrescoUrlIntent;

    private MaterialEditText passwordField;

    private MaterialEditText userField;

    private MDMManager mdmManager;

    private boolean hasIntentConfig = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountSignInFragment()
    {
        requiredSession = false;
        checkSession = false;
        screenName = AnalyticsManager.SCREEN_ACCOUNT_SIGNIN;
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
        mdmManager = MDMManager.getInstance(getActivity());

        if (getArguments() != null || !getArguments().isEmpty())
        {
            account = (AlfrescoAccount) getArguments().getSerializable(ARGUMENT_ACCOUNT);
            usernameIntent = getArguments().getString(ARGUMENT_USERNAME);
            alfrescoUrlIntent = getArguments().getString(ARGUMENT_REPO_URL);
            shareUrlIntent = getArguments().getString(ARGUMENT_SHARE_URL);
            hasIntentConfig = !TextUtils.isEmpty(alfrescoUrlIntent) && !TextUtils.isEmpty(usernameIntent);
        }

        getActivity().setTitle(R.string.app_name);

        setRootView(inflater.inflate(R.layout.fr_signin, container, false));

        Button validate = (Button) viewById(R.id.next);
        validate.setEnabled(true);
        validate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                validateServer(v);
            }
        });

        userField = (MaterialEditText) viewById(R.id.signin_username);
        userField.setEnabled(false);
        userField.setHint(R.string.account_username);
        if (account != null)
        {
            userField.setText(account.getUsername());
        }
        else if (hasIntentConfig)
        {
            userField.setText(usernameIntent);
        }
        else
        {
            userField.setText(MDMManager.getInstance(getActivity()).getUsername());
        }

        try
        {
            passwordField = (MaterialEditText) viewById(R.id.signin_password);
            passwordField.setHint(R.string.account_password);
            passwordField.requestFocus();
            UIUtils.showKeyboard(getActivity(), passwordField);
        }
        catch (Exception e)
        {
            Log.e("ERROR", Log.getStackTraceString(e));
        }

        // Accessibility
        if (AccessibilityUtils.isEnabled(getActivity()))
        {
            userField.setHint(getString(R.string.account_username_required_hint));
            passwordField.setHint(getString(R.string.account_password_required_hint));
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
            String password = passwordField.getText().toString();
            String username = userField.getText().toString();

            // Creation or Update Password ?
            if (account != null)
            {
                // Update Account
                AlfrescoAccount acc = AlfrescoAccountManager.getInstance(getActivity()).update(account.getId(),
                        account.getTitle(), account.getUrl(), username, password, account.getRepositoryId(),
                        account.getTypeId(), null, account.getAccessToken(), account.getRefreshToken(),
                        account.getIsPaidAccount() ? 1 : 0);

                // Reload
                // Affect new AlfrescoAccount to activity
                ((BaseActivity) getActivity()).setCurrentAccount(acc);
                EventBusManager.getInstance().post(new RequestSessionEvent(acc, true));
                getFragmentManager().popBackStackImmediate();

                return;
            }

            String alfrescoUrl = (hasIntentConfig) ? alfrescoUrlIntent : mdmManager.getAlfrescoURL();
            String description = (hasIntentConfig) ? null : mdmManager.getDescription();

            // Create AlfrescoAccount + Session
            Operator.with(getActivity()).load(
                    new CreateAccountRequest.Builder(alfrescoUrl, username, password, description));

            OperationWaitingDialogFragment.newInstance(CreateAccountRequest.TYPE_ID, R.drawable.ic_onpremise,
                    getString(R.string.account), getString(R.string.account_verify), null, -1, null).show(
                    getActivity().getSupportFragmentManager(), OperationWaitingDialogFragment.TAG);
        }
    }

    private boolean retrieveFormValues()
    {
        return !TextUtils.isEmpty(passwordField.getText());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onAccountCreated(CreateAccountEvent event)
    {
        if (event.hasException)
        {
            int messageId = AlfrescoExceptionHelper.getMessageId(getActivity(), event.exception);
            MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                    .title(R.string.error_session_creation_title).cancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {
                            dismiss();
                        }
                    }).content(Html.fromHtml(getString(messageId))).positiveText(R.string.ok);
            builder.show();
            return;
        }

        if (getActivity() instanceof WelcomeActivity)
        {
            ConfigManager.getInstance(getActivity()).setSession(event.data.getId(),
                    SessionUtils.getSession(getActivity()));
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder account(AlfrescoAccount account)
        {
            extraConfiguration.putSerializable(ARGUMENT_ACCOUNT, account);
            return this;
        }

        public Builder username(String username)
        {
            extraConfiguration.putString(ARGUMENT_USERNAME, username);
            return this;
        }

        public Builder repoUrl(String repositoryUrl)
        {
            extraConfiguration.putString(ARGUMENT_REPO_URL, repositoryUrl);
            return this;
        }

        public Builder shareUrl(String shareUrl)
        {
            extraConfiguration.putString(ARGUMENT_SHARE_URL, shareUrl);
            return this;
        }
    }
}
