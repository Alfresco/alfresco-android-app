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
package org.alfresco.mobile.android.application.fragments.account;

import java.net.MalformedURLException;
import java.net.URL;
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
import org.alfresco.mobile.android.platform.exception.AlfrescoExceptionHelper;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.ui.operation.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.squareup.otto.Subscribe;

public class AccountEditFragment extends AlfrescoFragment
{
    public static final String TAG = "AccountEditFragment";

    private Button validate;

    private String url = null, username = null, password = null, description = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountEditFragment()
    {
        setStyle(android.R.style.Theme_Holo_Light_Dialog, android.R.style.Theme_Holo_Light_Dialog);
        requiredSession = false;
        checkSession = false;
    }

    protected static AccountEditFragment newInstanceByTemplate(Bundle b)
    {
        AccountEditFragment cbf = new AccountEditFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().setTitle(R.string.account_authentication);
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }
        else
        {
            UIUtils.displayTitle(getActivity(), R.string.account_authentication,
                    !(getActivity() instanceof WelcomeActivity));
        }

        View v = inflater.inflate(R.layout.app_wizard_account_step2, container, false);

        validate = (Button) v.findViewById(R.id.next);
        validate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                validateServer();
            }
        });

        final CheckBox sw = (CheckBox) v.findViewById(R.id.repository_https);
        final EditText portForm = (EditText) v.findViewById(R.id.repository_port);
        sw.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (!sw.isChecked()
                        && (portForm.getText().toString().isEmpty() || portForm.getText().toString().equals("443")))
                {
                    portForm.setText("80");
                    AccessibilityUtils.addContentDescription(buttonView, R.string.account_https_off_hint);
                }
                else if (sw.isChecked()
                        && (portForm.getText().toString().isEmpty() || portForm.getText().toString().equals("80")))
                {
                    portForm.setText("443");
                    AccessibilityUtils.addContentDescription(buttonView, R.string.account_https_on_hint);
                }
            }
        });

        sw.setChecked(true);
        portForm.setText("443");

        // Accessibility
        if (AccessibilityUtils.isEnabled(getActivity()))
        {
            ((EditText) v.findViewById(R.id.repository_username))
                    .setHint(getString(R.string.account_username_required_hint));
            ((EditText) v.findViewById(R.id.repository_password))
                    .setHint(getString(R.string.account_password_required_hint));
            ((EditText) v.findViewById(R.id.repository_hostname))
                    .setHint(getString(R.string.account_hostname_required_hint));
            ((EditText) v.findViewById(R.id.repository_description))
                    .setHint(getString(R.string.account_description_optional_hint));
            sw.setContentDescription(getString(R.string.account_https_on_hint));
            portForm.setHint(getString(R.string.account_port_hint));
            ((EditText) v.findViewById(R.id.repository_servicedocument))
                    .setHint(getString(R.string.account_servicedocument_hint));
        }

        return v;
    }

    @Override
    public void onStart()
    {
        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_application_logo);
        }

        initForm();

        if (retrieveFormValues())
        {
            validate.setEnabled(true);
        }
        else
        {
            validate.setEnabled(false);
        }

        super.onStart();
    }

    // /////////////////////////////////////////////////////////////
    // INTERNALS
    // ////////////////////////////////////////////////////////////
    private void validateServer()
    {
        if (retrieveFormValues())
        {
            // Remove Keyboard
            UIUtils.hideKeyboard(getActivity());

            // Create AlfrescoAccount + Session
            Operator.with(getActivity()).load(new CreateAccountRequest.Builder(url, username, password, description));

            OperationWaitingDialogFragment.newInstance(CreateAccountRequest.TYPE_ID, R.drawable.ic_onpremise,
                    getString(R.string.account), getString(R.string.account_verify), null, -1, null).show(
                    getActivity().getSupportFragmentManager(), OperationWaitingDialogFragment.TAG);
        }
    }

    private void initForm()
    {
        int[] ids = new int[] { R.id.repository_username, R.id.repository_hostname, R.id.repository_password,
                R.id.repository_port };
        EditText formValue;
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
                validate.setEnabled(true);
            }
            else
            {
                validate.setEnabled(false);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            // Nothing special
        }

        @Override
        public void afterTextChanged(Editable s)
        {

        }
    };

    private boolean retrieveFormValues()
    {

        EditText formValue = (EditText) findViewByIdInternal(R.id.repository_username);
        if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
        {
            username = formValue.getText().toString();
        }
        else
        {
            AccessibilityUtils.addContentDescription(validate, R.string.account_validate_disable_hint);
            return false;
        }

        formValue = (EditText) findViewByIdInternal(R.id.repository_description);
        description = formValue.getText().toString();

        formValue = (EditText) findViewByIdInternal(R.id.repository_password);
        if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
        {
            password = formValue.getText().toString();
        }
        else
        {
            AccessibilityUtils.addContentDescription(validate, R.string.account_validate_disable_hint);
            return false;
        }

        String host;
        formValue = (EditText) findViewByIdInternal(R.id.repository_hostname);
        if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
        {
            host = formValue.getText().toString();
        }
        else
        {
            AccessibilityUtils.addContentDescription(validate, R.string.account_validate_disable_hint);
            return false;
        }

        CheckBox sw = (CheckBox) findViewByIdInternal(R.id.repository_https);
        boolean https = sw.isChecked();
        String protocol = https ? "https" : "http";

        int port;
        formValue = (EditText) findViewByIdInternal(R.id.repository_port);
        if (formValue.getText().length() > 0)
        {
            port = Integer.parseInt(formValue.getText().toString());
        }
        else
        {
            port = (protocol.equals("https")) ? 443 : 80;
        }

        formValue = (EditText) findViewByIdInternal(R.id.repository_servicedocument);
        String servicedocument = formValue.getText().toString();
        URL u;
        try
        {
            u = new URL(protocol, host, port, servicedocument);
        }
        catch (MalformedURLException e)
        {
            AccessibilityUtils.addContentDescription(validate, R.string.account_validate_disable_url_hint);
            return false;
        }

        url = u.toString();
        AccessibilityUtils.addContentDescription(validate, R.string.account_validate_hint);

        return true;
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
            b.putInt(SimpleAlertDialogFragment.ARGUMENT_ICON, R.drawable.ic_application_logo);
            b.putInt(SimpleAlertDialogFragment.ARGUMENT_TITLE, R.string.error_session_creation_title);
            b.putInt(SimpleAlertDialogFragment.ARGUMENT_POSITIVE_BUTTON, android.R.string.ok);
            b.putInt(SimpleAlertDialogFragment.ARGUMENT_MESSAGE,
                    AlfrescoExceptionHelper.getMessageId(getActivity(), event.exception));
            SimpleAlertDialogFragment.newInstance(b).show(getActivity().getSupportFragmentManager(),
                    SimpleAlertDialogFragment.TAG);
            return;
        }

        if (getActivity() instanceof MainActivity)
        {
            getActivity().getFragmentManager().popBackStack(AccountTypesFragment.TAG,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);

            if (event.data != null && !event.hasException)
            {
                long accountId = event.data.getId();

                AccountsFragment frag = (AccountsFragment) getActivity().getSupportFragmentManager().findFragmentByTag(
                        AccountsFragment.TAG);
                if (frag != null)
                {
                    frag.select(event.data);
                }
                ((BaseActivity) getActivity()).setCurrentAccount(accountId);
            }
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
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }
}
