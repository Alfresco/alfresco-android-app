/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
 *
 * This file is part of Alfresco Activiti Mobile for Android.
 *
 * Alfresco Activiti Mobile for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco Activiti Mobile for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.alfresco.mobile.android.application.fragments.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.signin.AccountSigninSamlFragment;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.session.CheckSessionEvent;
import org.alfresco.mobile.android.async.session.CheckSessionRequest;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.exception.AlfrescoExceptionHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.otto.Subscribe;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class AccountEditFragment extends AlfrescoFragment
{
    public static final String TAG = AccountEditFragment.class.getName();

    public static final String ARGUMENT_ACCOUNT_ID = "accountId";

    // UI references.
    private MaterialAutoCompleteTextView usernameField;

    private MaterialEditText passwordField;

    private View progressView, formView;

    private MaterialEditText alfrescoUrlField;

    private String username, password, hostname;

    private AlfrescoAccount acc;

    private Long accountId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountEditFragment()
    {
        super();
        eventBusRequired = true;
        requiredSession = false;
        screenName = AnalyticsManager.SCREEN_ACCOUNT_EDIT;
    }

    public static AccountEditFragment newInstanceByTemplate(Bundle b)
    {
        AccountEditFragment cbf = new AccountEditFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(inflater.inflate(R.layout.fr_account_signin, container, false));
        getRootView().setBackgroundColor(getResources().getColor(R.color.secondary_background));
        return getRootView();
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null)
        {
            accountId = getArguments().getLong(ARGUMENT_ACCOUNT_ID);
        }
        acc = AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(accountId);

        // It's not default we display full url in hostname
        hostname = acc.getUrl();

        // TITLE
        TextView tv = (TextView) viewById(R.id.signin_title);
        tv.setText(R.string.settings_userinfo_account_summary);

        // USERNAME
        usernameField = (MaterialAutoCompleteTextView) viewById(R.id.username);
        Account[] accounts = AccountManager.get(getActivity()).getAccounts();
        List<String> names = new ArrayList<>(accounts.length);
        String accountName;
        for (int i = 0; i < accounts.length; i++)
        {
            accountName = accounts[i].name;
            if (!TextUtils.isEmpty(accountName) && !names.contains(accountName))
            {
                names.add(accounts[i].name);
            }
        }
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, names);
        usernameField.setAdapter(adapter);
        usernameField.setText(acc.getUsername());
        usernameField.setEnabled(true);
        usernameField.setFocusable(true);

        // PASSWORD
        passwordField = (MaterialEditText) viewById(R.id.password);
        passwordField.setText(acc.getPassword());
        passwordField.setEnabled(true);
        passwordField.setFocusable(true);

        Button validate = (Button) viewById(R.id.email_sign_in_button);
        validate.setText(R.string.save);
        validate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptLogin();
            }
        });

        progressView = viewById(R.id.login_progress);
        formView = viewById(R.id.login_form);

        // Server part
        alfrescoUrlField = (MaterialEditText) viewById(R.id.signing_hostname);
        alfrescoUrlField.setText(hostname);
        alfrescoUrlField.setHint("Alfresco URL");

        // Accessibility
        if (AccessibilityUtils.isEnabled(getActivity()))
        {
            usernameField.setHint(getString(R.string.account_username_required_hint));
            passwordField.setHint(getString(R.string.account_password_required_hint));
            alfrescoUrlField.setHint(getString(R.string.account_hostname_required_hint));
        }

        show(R.id.server_form);

        // SAML Specific
        if (acc.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CMIS_SAML)
        {
            passwordField.setEnabled(false);
            passwordField.setVisibility(View.GONE);

            usernameField.setEnabled(false);
            usernameField.setVisibility(View.GONE);

            validate.setText(R.string.validate);

            show(R.id.saml_signin_panel);
            Button signIn = (Button) viewById(R.id.saml_sign_in_button);
            signIn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    AccountSigninSamlFragment.with(getActivity()).isCreation(false).account(acc).display();
                }
            });
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin()
    {
        // Reset errors.
        usernameField.setError(null);
        passwordField.setError(null);
        alfrescoUrlField.setError(null);

        // Store values at the time of the login attempt.
        username = usernameField.getText().toString();
        password = passwordField.getText().toString();
        hostname = alfrescoUrlField.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the task entered one.
        if (TextUtils.isEmpty(password))
        {
            passwordField.setError(getString(R.string.error_field_required));
            focusView = passwordField;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username))
        {
            usernameField.setError(getString(R.string.error_field_required));
            focusView = usernameField;
            cancel = true;
        }

        // Check for a valid hostname.
        if (TextUtils.isEmpty(hostname))
        {
            alfrescoUrlField.setError(getString(R.string.error_field_required));
            focusView = alfrescoUrlField;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            showProgress(true);
            connect();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show)
    {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
        formView.setVisibility(show ? View.GONE : View.VISIBLE);
        formView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                formView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void connect()
    {
        showProgress(true);

        UIUtils.hideKeyboard(getActivity(), usernameField);

        // Create AlfrescoAccount + Session
        Operator.with(getActivity(), acc).load(new CheckSessionRequest.Builder(hostname, username, password));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onCheckSessionEvent(CheckSessionEvent event)
    {
        if (event.hasException)
        {
            // Display error
            View focusView = null;
            showProgress(false);
            UIUtils.showKeyboard(getActivity(), focusView);

            if (focusView == null)
            {
                int messageId = AlfrescoExceptionHelper.getMessageId(getActivity(), event.exception);

                if (messageId == R.string.error_session_unauthorized)
                {

                    // Reload account information in case of authentication
                    // switch
                    AlfrescoAccount tmpAccount = AlfrescoAccountManager.getInstance(getActivity())
                            .retrieveAccount(acc.getId());

                    // SAML? Switch account happened
                    if (tmpAccount.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CMIS_SAML)
                    {
                        AccountSigninSamlFragment.with(getActivity()).isCreation(false).account(tmpAccount).display();
                        return;
                    }
                    else if (acc.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CMIS_SAML
                            && tmpAccount.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CMIS)
                    {
                        acc = tmpAccount;

                        passwordField.setText(acc.getUsername());
                        passwordField.setEnabled(true);
                        passwordField.setVisibility(View.VISIBLE);

                        passwordField.setText(null);
                        usernameField.setEnabled(true);
                        usernameField.setVisibility(View.VISIBLE);

                        Button validate = (Button) viewById(R.id.email_sign_in_button);
                        validate.setText(R.string.save);

                        hide(R.id.saml_signin_panel);
                    }
                }

                // Revert to Alfresco WebApp
                MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                        .title(R.string.error_session_creation_title)
                        .content(Html.fromHtml(messageId == R.string.error_unknown
                                ? String.format(getString(messageId), event.exception.getCause())
                                : getString(messageId)))
                        .positiveText(R.string.ok);
                builder.show();
            }
        }
        else
        {
            AlfrescoAccount updatedAccount = event.updatedAccount;
            // Save
            acc = AlfrescoAccountManager.getInstance(getActivity()).update(updatedAccount.getId(),
                    updatedAccount.getTitle(), updatedAccount.getUrl(), updatedAccount.getUsername(),
                    updatedAccount.getPassword(), updatedAccount.getRepositoryId(), updatedAccount.getTypeId(), null,
                    updatedAccount.getAccessToken(), updatedAccount.getRefreshToken(),
                    updatedAccount.getIsPaidAccount() ? 1 : 0);

            SessionManager.getInstance(getActivity()).saveAccount(acc);
            SessionManager.getInstance(getActivity()).saveSession(acc, event.data);

            AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_ACCOUNT,
                    AnalyticsManager.ACTION_EDIT, AnalyticsManager.LABEL_CREDENTIALS, 1, false);

            EventBusManager.getInstance()
                    .post(new LoadSessionCallBack.LoadAccountCompletedEvent(updatedAccount.getTitle(), updatedAccount));

            getActivity().onBackPressed();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
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

        public Builder accountId(Long accountId)
        {
            extraConfiguration.putLong(ARGUMENT_ACCOUNT_ID, accountId);
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
