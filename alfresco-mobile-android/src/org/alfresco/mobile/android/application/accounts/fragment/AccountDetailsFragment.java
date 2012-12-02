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
package org.alfresco.mobile.android.application.accounts.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.alfresco.mobile.android.application.HomeScreenActivity;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.accounts.signup.CloudSignupLoader;
import org.alfresco.mobile.android.application.accounts.signup.CloudSignupLoaderCallback;
import org.alfresco.mobile.android.application.accounts.signup.CloudSignupStatusLoadeCallback;
import org.alfresco.mobile.android.application.accounts.signup.CloudSignupStatusLoader;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class AccountDetailsFragment extends BaseFragment
{

    public static final String TAG = "AccountDetailsFragment";

    public static final String ARGUMENT_ACCOUNT_ID = "accountID";

    private String url = null, host = null, username = null, password = null, servicedocument = null,
            description = null;

    private boolean https = false;

    private int port;

    private Account acc;

    private AccountDAO accountDao;

    private View vRoot;

    public AccountDetailsFragment()
    {
    }

    public static AccountDetailsFragment newInstance(long accountID)
    {
        AccountDetailsFragment bf = new AccountDetailsFragment();
        bf.setArguments(createBundleArgs(accountID));
        return bf;
    }

    public static Bundle createBundleArgs(long accountID)
    {
        Bundle args = new Bundle();
        args.putLong(ARGUMENT_ACCOUNT_ID, accountID);
        return args;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null) { return null; }

        accountDao = new AccountDAO(getActivity(), SessionUtils.getDataBaseManager(getActivity()).getWriteDb());
        acc = accountDao.findById(getArguments().getLong(ARGUMENT_ACCOUNT_ID));

        if (acc == null) { return null; }

        if (acc.getActivation() == null)
        {
            vRoot = inflater.inflate(R.layout.app_account_details, container, false);
            initValues(vRoot, false);
        }
        else
        {
            vRoot = inflater.inflate(R.layout.app_cloud_signup_check, container, false);
            initAwaitingCloud(vRoot);
        }
        return vRoot;
    }

    private void initAwaitingCloud(final View v)
    {
        TextView tv = (TextView) v.findViewById(R.id.sign_up_cloud_email);
        tv.setText(tv.getText() + " " + acc.getUsername());

        tv = (TextView) v.findViewById(R.id.sign_up_cloud_email_having_trouble);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        Button btn = (Button) v.findViewById(R.id.cloud_signup_refresh);
        btn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CloudSignupStatusLoadeCallback call = new CloudSignupStatusLoadeCallback(getActivity(),
                        AccountDetailsFragment.this, acc);
                LoaderManager lm = getLoaderManager();
                lm.restartLoader(CloudSignupStatusLoader.ID, null, call);
            }
        });

        btn = (Button) v.findViewById(R.id.cloud_signup_resend);
        btn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CloudSignupLoaderCallback call = new CloudSignupLoaderCallback(getActivity(),
                        AccountDetailsFragment.this, null, null, acc.getUsername(), null, null);
                LoaderManager lm = getLoaderManager();
                lm.restartLoader(CloudSignupLoader.ID, null, call);
            }
        });
    }

    private void initValues(final View v, boolean isEditable)
    {
        URL url = null;
        try
        {
            url = new URL(acc.getUrl());
        }
        catch (MalformedURLException e)
        {
            MessengerManager.showToast(getActivity(), R.string.error_account_url);
            return;
        }

        v.findViewById(R.id.account_authentication).setVisibility(View.VISIBLE);

        if (acc.getTypeId() == Account.TYPE_ALFRESCO_CLOUD)
        {
            v.findViewById(R.id.advanced).setVisibility(View.GONE);
            v.findViewById(R.id.advanced_settings).setVisibility(View.GONE);
            v.findViewById(R.id.repository_https_group).setVisibility(View.GONE);
            v.findViewById(R.id.repository_hostname_group).setVisibility(View.GONE);
            v.findViewById(R.id.repository_username_group).setVisibility(View.GONE);
            v.findViewById(R.id.repository_password_group).setVisibility(View.GONE);
        }
        else if (acc.getTypeId() == Account.TYPE_ALFRESCO_TEST_BASIC
                || acc.getTypeId() == Account.TYPE_ALFRESCO_TEST_OAUTH)
        {
            v.findViewById(R.id.repository_password_group).setVisibility(View.GONE);
        }
        else
        {
            v.findViewById(R.id.advanced_settings).setVisibility(View.VISIBLE);
        }

        Button advanced = (Button) v.findViewById(R.id.browse_document);
        advanced.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ((MainActivity) getActivity()).loadAccount(acc);
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        // Init values
        EditText form_value = (EditText) v.findViewById(R.id.repository_hostname);
        form_value.setText(url.getHost());
        form_value.setEnabled(isEditable);

        form_value = (EditText) v.findViewById(R.id.repository_username);
        form_value.setText(acc.getUsername());
        form_value.setEnabled(isEditable);

        form_value = (EditText) v.findViewById(R.id.repository_description);
        form_value.setText(acc.getDescription());
        form_value.setEnabled(isEditable);

        form_value = (EditText) v.findViewById(R.id.repository_password);
        form_value.setText(acc.getPassword());
        form_value.setEnabled(isEditable);

        // TODO Switch widget ?
        final CheckBox sw = (CheckBox) v.findViewById(R.id.repository_https);
        sw.setChecked(url.getProtocol().equals("https"));
        sw.setEnabled(isEditable);
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

        form_value = (EditText) v.findViewById(R.id.repository_port);
        if (url.getPort() != -1)
        {
            form_value.setText(url.getPort() + "");
        }
        else
        {
            form_value.setText(url.getDefaultPort() + "");
        }
        form_value.setEnabled(isEditable);

        form_value = (EditText) v.findViewById(R.id.repository_servicedocument);
        form_value.setText(url.getPath());
        form_value.setEnabled(isEditable);

    }

    private boolean retrieveFormValues()
    {
        // Check values
        EditText form_value = (EditText) vRoot.findViewById(R.id.repository_hostname);
        if (form_value != null && form_value.getText() != null && form_value.getText().length() > 0)
        {
            host = form_value.getText().toString();
        }
        else
        {
            return false;
        }

        form_value = (EditText) vRoot.findViewById(R.id.repository_username);
        if (form_value != null && form_value.getText() != null && form_value.getText().length() > 0)
        {
            username = form_value.getText().toString();
        }
        else
        {
            return false;
        }

        form_value = (EditText) vRoot.findViewById(R.id.repository_description);
        description = form_value.getText().toString();

        form_value = (EditText) vRoot.findViewById(R.id.repository_password);
        password = form_value.getText().toString();

        CheckBox sw = (CheckBox) vRoot.findViewById(R.id.repository_https);
        https = sw.isChecked();
        String protocol = https ? "https" : "http";

        form_value = (EditText) vRoot.findViewById(R.id.repository_port);
        if (form_value.getText().length() > 0)
        {
            port = Integer.parseInt(form_value.getText().toString());
        }
        else
        {
            port = (protocol.equals("https")) ? 443 : 80;
        }

        form_value = (EditText) vRoot.findViewById(R.id.repository_servicedocument);
        servicedocument = form_value.getText().toString();
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart()
    {
        DisplayUtils.hideLeftTitlePane(getActivity());
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            getActivity().setTitle(getText(R.string.accounts_details) + " : " + acc.getDescription());
        }
        getActivity().invalidateOptionsMenu();
        super.onStart();
    }

    private void initForm()
    {
        int[] ids = new int[] { R.id.repository_username, R.id.repository_hostname, R.id.repository_port };
        EditText form_value = null;
        for (int i = 0; i < ids.length; i++)
        {
            form_value = (EditText) vRoot.findViewById(ids[i]);
            form_value.addTextChangedListener(watcher);
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

    private Button validate;

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////

    public void edit()
    {
        initValues(vRoot, true);
        initForm();

        validate = (Button) vRoot.findViewById(R.id.validate_account);
        validate.setVisibility(View.VISIBLE);
        validate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                retrieveFormValues();
                if (accountDao.update(acc.getId(), description, url, username, password, acc.getRepositoryId(),
                        Integer.valueOf((int) acc.getTypeId()), null, acc.getAccessToken(), acc.getRefreshToken()))
                {
                    acc = accountDao.findById(getArguments().getLong(ARGUMENT_ACCOUNT_ID));
                    initValues(vRoot, false);
                    vRoot.findViewById(R.id.browse_document).setVisibility(View.VISIBLE);
                    vRoot.findViewById(R.id.cancel_account).setVisibility(View.GONE);
                    v.setVisibility(View.GONE);
                }
            }
        });

        Button b = (Button) vRoot.findViewById(R.id.cancel_account);
        b.setVisibility(View.VISIBLE);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                initValues(vRoot, false);
                vRoot.findViewById(R.id.browse_document).setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
                vRoot.findViewById(R.id.validate_account).setVisibility(View.GONE);
            }
        });

        vRoot.findViewById(R.id.browse_document).setVisibility(View.GONE);
    }

    public void delete()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.action_delete);
        builder.setMessage(getResources().getString(R.string.action_delete_desc) + " " + acc.getDescription());
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                accountDao.delete(acc.getId());
                // In case where currentAccount is the one deleted.
                if (SessionUtils.getAccount(getActivity()) != null
                        && SessionUtils.getAccount(getActivity()).getId() == acc.getId())
                {
                    SessionUtils.setAccount(getActivity(), null);
                }

                if (!accountDao.findAll().isEmpty())
                {
                    ActionManager.actionRefresh(AccountDetailsFragment.this, IntentIntegrator.CATEGORY_REFRESH_OTHERS,
                            IntentIntegrator.ACCOUNT_TYPE);
                }
                else
                {
                    getActivity().finish();
                    startActivityForResult(new Intent(getActivity(), HomeScreenActivity.class), 1);
                }

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void displayOAuthFragment()
    {
        AccountOAuthFragment newFragment = AccountOAuthFragment.newInstance(acc);
        FragmentDisplayer.replaceFragment(getActivity(), newFragment, DisplayUtils.getMainPaneId(getActivity()),
                AccountOAuthFragment.TAG, true);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        getActivity().invalidateOptionsMenu();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public void getMenu(Menu menu)
    {
        MenuItem mi;

        if (acc.getActivation() == null)
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_ACCOUNT_EDIT, Menu.FIRST + MenuActionItem.MENU_ACCOUNT_EDIT,
                    R.string.action_edit);
            mi.setIcon(R.drawable.ic_edit);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_ACCOUNT_DELETE, Menu.FIRST + MenuActionItem.MENU_ACCOUNT_DELETE,
                R.string.action_delete);
        mi.setIcon(R.drawable.ic_delete);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }
}
