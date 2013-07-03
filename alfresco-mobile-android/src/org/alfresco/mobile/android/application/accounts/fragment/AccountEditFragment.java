/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.accounts.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.account.CreateAccountRequest;
import org.alfresco.mobile.android.application.operations.batch.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class AccountEditFragment extends DialogFragment
{
    public static final String TAG = "AccountEditFragment";

    private Button validate;

    private String url = null, host = null, username = null, password = null, servicedocument = null,
            description = null;

    private int port;

    private AccountsReceiver receiver;

    public AccountEditFragment()
    {
        setStyle(android.R.style.Theme_Holo_Light_Dialog, android.R.style.Theme_Holo_Light_Dialog);
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
            UIUtils.displayTitle(getActivity(), R.string.account_authentication);
        }

        View v = inflater.inflate(R.layout.app_wizard_account_step2, container, false);

        validate = (Button) v.findViewById(R.id.next);
        validate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                validateServer(v);
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
                }
                else if (sw.isChecked()
                        && (portForm.getText().toString().isEmpty() || portForm.getText().toString().equals("80")))
                {
                    portForm.setText("443");
                }
            }
        });

        sw.setChecked(true);
        portForm.setText("443");

        return v;
    }

    @Override
    public void onStart()
    {
        if (receiver == null)
        {
            receiver = new AccountsReceiver();
            IntentFilter filters = new IntentFilter(IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filters);
        }

        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_alfresco);
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

    @Override
    public void onPause()
    {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onPause();
    }

    // /////////////////////////////////////////////////////////////
    // INTERNALS
    // ////////////////////////////////////////////////////////////
    private void validateServer(View v)
    {
        if (retrieveFormValues())
        {
            // Remove Keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            // Create Account + Session
            OperationsRequestGroup group = new OperationsRequestGroup(getActivity());
            group.enqueue(new CreateAccountRequest(url, username, password, description)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
            BatchOperationManager.getInstance(getActivity()).enqueue(group);

            if (getActivity() instanceof MainActivity)
            {
                OperationWaitingDialogFragment.newInstance(FavoriteNodeRequest.TYPE_ID, R.drawable.ic_onpremise,
                        getString(R.string.account), getString(R.string.account_verify), null, -1).show(
                        getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
            }
        }
    }

    private void initForm()
    {
        int[] ids = new int[] { R.id.repository_username, R.id.repository_hostname, R.id.repository_password,
                R.id.repository_port };
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
            // TODO Auto-generated method stub

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
            return false;
        }

        formValue = (EditText) findViewByIdInternal(R.id.repository_hostname);
        if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
        {
            host = formValue.getText().toString();
        }
        else
        {
            return false;
        }

        CheckBox sw = (CheckBox) findViewByIdInternal(R.id.repository_https);
        boolean https = sw.isChecked();
        String protocol = https ? "https" : "http";

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
        servicedocument = formValue.getText().toString();
        URL u = null;
        try
        {
            u = new URL(protocol, host, port, servicedocument);
        }
        catch (MalformedURLException e)
        {
            return false;
        }

        url = u.toString();

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
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    private class AccountsReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED.equals(intent.getAction()) && getActivity() instanceof MainActivity)
            {
                getActivity().getFragmentManager().popBackStack(AccountTypesFragment.TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);

                if (intent.getExtras() != null && intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    long accountId = intent.getLongExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, -1);

                    AccountsFragment frag = (AccountsFragment) getActivity().getFragmentManager().findFragmentByTag(
                            AccountsFragment.TAG);
                    if (frag != null)
                    {
                        frag.select(accountId);
                    }
                    ((BaseActivity) getActivity()).setCurrentAccount(accountId);
                }
            }
        }
    }
}
