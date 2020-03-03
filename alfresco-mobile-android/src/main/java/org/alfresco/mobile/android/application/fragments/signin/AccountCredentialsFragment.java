/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.mobile.android.application.fragments.signin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.CreateAccountEvent;
import org.alfresco.mobile.android.async.account.CreateAccountRequest;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.exception.AlfrescoExceptionHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.otto.Subscribe;

public class AccountCredentialsFragment extends AlfrescoFragment
{
    public static final String TAG = AccountCredentialsFragment.class.getName();

    private static final String ARGUMENT_HOSTNAME = "hostnameView";

    // UI references.
    private MaterialAutoCompleteTextView usernameField;

    private MaterialEditText passwordField;

    private View progressView, formView;

    private MaterialEditText alfrescoField;

    private String username, password, alfrescoUrlValue;

    private boolean https;

    private Uri endpoint;

    private AlfrescoAccount acc;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountCredentialsFragment()
    {
        super();
        eventBusRequired = true;
        requiredSession = false;
        screenName = AnalyticsManager.SCREEN_ACCOUNT_USER;
    }

    public static AccountCredentialsFragment newInstanceByTemplate(Bundle b)
    {
        AccountCredentialsFragment cbf = new AccountCredentialsFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(inflater.inflate(R.layout.fr_account_signin, container, false));
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
            alfrescoUrlValue = getArguments().getString(ARGUMENT_HOSTNAME);
            endpoint = TextUtils.isEmpty(alfrescoUrlValue) ? null : Uri.parse(alfrescoUrlValue);
        }

        // We retrieve emails from accounts.
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

        passwordField = (MaterialEditText) viewById(R.id.password);

        Button signin = (Button) viewById(R.id.email_sign_in_button);
        signin.setOnClickListener(new View.OnClickListener()
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
        alfrescoField = (MaterialEditText) viewById(R.id.signing_hostname);
        alfrescoField.setText(alfrescoUrlValue);

        // Accessibility
        if (AccessibilityUtils.isEnabled(getActivity()))
        {
            usernameField.setHint(getString(R.string.account_username_required_hint));
            passwordField.setHint(getString(R.string.account_password_required_hint));
            alfrescoField.setHint(getString(R.string.account_hostname_required_hint));
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
        alfrescoField.setError(null);

        // Store values at the time of the login attempt.
        username = usernameField.getText().toString();
        password = passwordField.getText().toString();
        alfrescoUrlValue = alfrescoField.getText().toString();

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
        if (TextUtils.isEmpty(alfrescoUrlValue))
        {
            alfrescoField.setError(getString(R.string.error_field_required));
            focusView = alfrescoField;
            cancel = true;
        }

        // Check URL
        if (TextUtils.isEmpty(alfrescoUrlValue))
        {
            alfrescoField.setError(getString(R.string.error_invalid_url));
            focusView = alfrescoField;
            cancel = true;
        }
        else
        {
            endpoint = Uri.parse(alfrescoUrlValue);
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
        UIUtils.hideKeyboard(getActivity(), usernameField);

        // Create AlfrescoAccount + Session
        Operator.with(getActivity())
                .load(new CreateAccountRequest.Builder(endpoint.toString(), username, password, null));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onAccountCreated(CreateAccountEvent event)
    {
        if (event.hasException)
        {
            View focusView = null;
            showProgress(false);
            UIUtils.showKeyboard(getActivity(), focusView);

            if (focusView == null)
            {
                int messageId = AlfrescoExceptionHelper.getMessageId(getActivity(), event.exception);
                // Revert to Alfresco WebApp
                MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                        .title(R.string.error_session_creation_title)
                        .content(Html.fromHtml(messageId == R.string.error_unknown
                                ? String.format(getString(messageId), event.exception.getCause())
                                : getString(messageId)))
                        .positiveText(R.string.ok);
                builder.show();
                show(R.id.server_form);

                if (messageId == R.string.error_session_unauthorized)
                {
                    hide(R.id.server_form);
                }
            }
        }
        else
        {
            AlfrescoAccount acc = event.data;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (acc.getIsPaidAccount() && !prefs.getBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false))
            {
                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).apply();
            }
            AccountNameFragment.with(getActivity()).accountId(acc.getId()).back(false).display();
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

        public Builder hostname(String processId)
        {
            extraConfiguration.putString(ARGUMENT_HOSTNAME, processId);
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
