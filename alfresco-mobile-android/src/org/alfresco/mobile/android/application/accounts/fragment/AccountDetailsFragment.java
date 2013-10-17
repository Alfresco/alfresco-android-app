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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.accounts.AccountSchema;
import org.alfresco.mobile.android.application.accounts.signup.CloudSignupLoader;
import org.alfresco.mobile.android.application.accounts.signup.CloudSignupLoaderCallback;
import org.alfresco.mobile.android.application.accounts.signup.CloudSignupStatusLoadeCallback;
import org.alfresco.mobile.android.application.accounts.signup.CloudSignupStatusLoader;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.HomeScreenActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.person.PersonProfileFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
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

/**
 * It's responsible to display the details of a specific account.
 * 
 * @author Jean Marie Pascal
 */
public class AccountDetailsFragment extends BaseFragment
{

    public static final String TAG = "AccountDetailsFragment";

    public static final String ARGUMENT_ACCOUNT_ID = "accountID";

    private String url = null, host = null, username = null, password = null, servicedocument = null,
            description = null;

    private boolean https = false;

    private int port;

    private Account acc;

    private View vRoot;

    private boolean isEditable = false;

    private Button validate;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountDetailsFragment()
    {
    }

    public static AccountDetailsFragment newInstance(long accountID)
    {
        AccountDetailsFragment bf = new AccountDetailsFragment();
        Bundle args = new Bundle();
        args.putLong(ARGUMENT_ACCOUNT_ID, accountID);
        bf.setArguments(args);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null) { return null; }

        acc = AccountManager.retrieveAccount(getActivity(), getArguments().getLong(ARGUMENT_ACCOUNT_ID));

        if (acc.getActivation() == null)
        {
            vRoot = inflater.inflate(R.layout.app_account_details, container, false);
            initValues(vRoot);
        }
        else
        {
            vRoot = inflater.inflate(R.layout.app_cloud_signup_check, container, false);
            initAwaitingCloud(vRoot);
        }

        if (isEditable)
        {
            edit();
        }

        return vRoot;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume()
    {
        DisplayUtils.hideLeftTitlePane(getActivity());
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), getText(R.string.accounts_details) + " : " + acc.getDescription());
        }
        getActivity().invalidateOptionsMenu();
        super.onResume();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        getActivity().invalidateOptionsMenu();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
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

    private void initValues(final View v)
    {
        URL tmprUrl = null;
        try
        {
            tmprUrl = new URL(acc.getUrl());
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
                // Affect new account to activity
                ((BaseActivity) getActivity()).setCurrentAccount(acc);

                // Request or create new session for the account.
                ActionManager.reloadAccount(getActivity(), acc);
            }
        });

        advanced = (Button) v.findViewById(R.id.my_profile);
        advanced.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                PersonProfileFragment.newInstance(acc.getUsername()).show(getFragmentManager(),
                        PersonProfileFragment.TAG);
            }
        });
        displayProfileButton();

        // Init values
        EditText formValue = (EditText) v.findViewById(R.id.repository_hostname);
        formValue.setText(tmprUrl.getHost());
        formValue.setEnabled(isEditable);

        formValue = (EditText) v.findViewById(R.id.repository_username);
        formValue.setText(acc.getUsername());
        formValue.setEnabled(isEditable);

        formValue = (EditText) v.findViewById(R.id.repository_description);
        formValue.setText(acc.getDescription());
        formValue.setEnabled(isEditable);

        formValue = (EditText) v.findViewById(R.id.repository_password);
        formValue.setText(acc.getPassword());
        formValue.setEnabled(isEditable);

        // TODO Switch widget ?
        final CheckBox sw = (CheckBox) v.findViewById(R.id.repository_https);
        sw.setChecked(tmprUrl.getProtocol().equals("https"));
        sw.setEnabled(isEditable);
        final EditText portForm = (EditText) v.findViewById(R.id.repository_port);
        sw.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (!sw.isChecked()
                        && (portForm.getText().toString() == null || portForm.getText().toString().equals("443")))
                {
                    portForm.setText("80");
                }
                else if (sw.isChecked()
                        && (portForm.getText().toString() == null || portForm.getText().toString().equals("80")))
                {
                    portForm.setText("443");
                }
            }
        });

        formValue = (EditText) v.findViewById(R.id.repository_port);
        if (tmprUrl.getPort() != -1)
        {
            formValue.setText(tmprUrl.getPort() + "");
        }
        else
        {
            formValue.setText(tmprUrl.getDefaultPort() + "");
        }
        formValue.setEnabled(isEditable);

        formValue = (EditText) v.findViewById(R.id.repository_servicedocument);
        formValue.setText(tmprUrl.getPath());
        formValue.setEnabled(isEditable);
    }

    private boolean retrieveFormValues()
    {
        // Check values
        EditText formValue = (EditText) vRoot.findViewById(R.id.repository_hostname);
        if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
        {
            host = formValue.getText().toString();
        }
        else
        {
            return false;
        }

        formValue = (EditText) vRoot.findViewById(R.id.repository_username);
        if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
        {
            username = formValue.getText().toString();
        }
        else
        {
            return false;
        }

        formValue = (EditText) vRoot.findViewById(R.id.repository_description);
        description = formValue.getText().toString();

        if (acc.getTypeId() != Account.TYPE_ALFRESCO_CLOUD && acc.getTypeId() != Account.TYPE_ALFRESCO_TEST_OAUTH)
        {
            formValue = (EditText) vRoot.findViewById(R.id.repository_password);
            if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
            {
                password = formValue.getText().toString();
            }
            else
            {
                return false;
            }
        }

        CheckBox sw = (CheckBox) vRoot.findViewById(R.id.repository_https);
        https = sw.isChecked();
        String protocol = https ? "https" : "http";

        formValue = (EditText) vRoot.findViewById(R.id.repository_port);
        if (formValue.getText().length() > 0)
        {
            port = Integer.parseInt(formValue.getText().toString());
        }
        else
        {
            port = (protocol.equals("https")) ? 443 : 80;
        }

        formValue = (EditText) vRoot.findViewById(R.id.repository_servicedocument);
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

    private void initForm()
    {
        int[] ids = new int[] { R.id.repository_username, R.id.repository_password, R.id.repository_hostname,
                R.id.repository_servicedocument, R.id.repository_description, R.id.repository_port };
        EditText formValue = null;
        for (int i = 0; i < ids.length; i++)
        {
            formValue = (EditText) vRoot.findViewById(ids[i]);
            formValue.addTextChangedListener(watcher);
        }
    }
    
    private void removeFormWatcher()
    {
        int[] ids = new int[] { R.id.repository_username, R.id.repository_password, R.id.repository_hostname,
                R.id.repository_servicedocument, R.id.repository_description, R.id.repository_port };
        EditText formValue = null;
        for (int i = 0; i < ids.length; i++)
        {
            formValue = (EditText) vRoot.findViewById(ids[i]);
            formValue.removeTextChangedListener(watcher);
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

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////

    public void edit()
    {
        if (isEditable){
          return;
        }

        isEditable = true;
        initValues(vRoot);
        initForm();
        
        validate = (Button) vRoot.findViewById(R.id.validate_account);
        validate.setVisibility(View.VISIBLE);
        validate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isEditable = false;
                removeFormWatcher();
                validate.setEnabled(false);
                retrieveFormValues();
                acc = AccountManager.update(getActivity(), getArguments().getLong(ARGUMENT_ACCOUNT_ID), description,
                        (url != null) ? url : acc.getUrl(), username, password, acc.getRepositoryId(),
                        Integer.valueOf((int) acc.getTypeId()), null, acc.getAccessToken(), acc.getRefreshToken(),
                        acc.getIsPaidAccount() ? 1 : 0);

                initValues(vRoot);
                vRoot.findViewById(R.id.browse_document).setVisibility(View.VISIBLE);
                displayProfileButton();
                vRoot.findViewById(R.id.cancel_account).setVisibility(View.GONE);
                v.setVisibility(View.GONE);
            }
        });

        Button b = (Button) vRoot.findViewById(R.id.cancel_account);
        b.setVisibility(View.VISIBLE);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isEditable = false;
                validate.setEnabled(false);
                removeFormWatcher();
                initValues(vRoot);
                vRoot.findViewById(R.id.browse_document).setVisibility(View.VISIBLE);
                displayProfileButton();
                v.setVisibility(View.GONE);
                vRoot.findViewById(R.id.validate_account).setVisibility(View.GONE);
            }
        });

        vRoot.findViewById(R.id.browse_document).setVisibility(View.GONE);
        vRoot.findViewById(R.id.profile_group).setVisibility(View.GONE);
    }

    public void delete()
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean dataProtectionDeletion = false;

        // Prepare deletion : Check if
        if (prefs.getBoolean(GeneralPreferences.PRIVATE_FOLDERS, false))
        {
            Cursor cursor = getActivity().getContentResolver().query(
                    AccountManager.CONTENT_URI,
                    AccountManager.COLUMN_ALL,
                    AccountSchema.COLUMN_ID + "!=" + acc.getId() + " AND " + AccountSchema.COLUMN_IS_PAID_ACCOUNT
                            + " = 1", null, null);
            if (cursor.getCount() == 0)
            {
                dataProtectionDeletion = true;
            }
            cursor.close();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final File folder = StorageManager.getPrivateFolder(getActivity(), "", null);
        if (dataProtectionDeletion && folder != null)
        {
            builder.setTitle(R.string.delete);
            builder.setMessage(String.format(getResources().getString(R.string.delete_description_data_protection),
                    acc.getDescription()));
            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int item)
                {
                    DataProtectionManager.getInstance(getActivity()).decrypt(acc);

                    Editor edit = prefs.edit();

                    // Unflag this so that on next (first) addition of a new
                    // paid account, they will get prompted again.
                    edit.putBoolean(GeneralPreferences.ENCRYPTION_USER_INTERACTION, false);
                    // Last paid service removed, so unflag that we've accessed
                    // paid services.
                    edit.putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false);
                    // Turn off data protection
                    edit.putBoolean(GeneralPreferences.PRIVATE_FOLDERS, false);
                    edit.commit();

                    deleteAccount();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int item)
                {
                    dialog.dismiss();
                }
            });
        }
        else
        {
            builder.setTitle(R.string.delete);
            builder.setMessage(String.format(getResources().getQuantityString(R.plurals.delete_items, 1),
                    acc.getDescription()));
            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int item)
                {
                    deleteAccount();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int item)
                {
                    dialog.dismiss();
                }
            });
        }

        AlertDialog alert = builder.create();
        alert.show();
    }

    // TODO move to mainActivity + broadcast !
    private void deleteAccount()
    {
        getActivity().getContentResolver().delete(AccountManager.getUri(acc.getId()), null, null);

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                new Intent(IntentIntegrator.ACTION_DELETE_ACCOUNT_COMPLETED));

        // In case where currentAccount is the one deleted.
        ApplicationManager.getInstance(getActivity()).removeAccount(acc.getId());

        if (SessionUtils.getAccount(getActivity()) != null
                && SessionUtils.getAccount(getActivity()).getId() == acc.getId())
        {
            ((BaseActivity) getActivity()).setCurrentAccount(null);
            SharedPreferences settings = getActivity().getSharedPreferences(AccountsPreferences.ACCOUNT_PREFS, 0);
            long id = settings.getLong(AccountsPreferences.ACCOUNT_DEFAULT, -1);
            if (id == acc.getId())
            {
                settings.edit().putLong(AccountsPreferences.ACCOUNT_DEFAULT, -1).commit();
            }
            ((BaseActivity) getActivity()).setCurrentAccount(AccountManager.getInstance(getActivity())
                    .getDefaultAccount());
        }

        Cursor cursor = getActivity().getContentResolver().query(AccountManager.CONTENT_URI, AccountManager.COLUMN_ALL,
                null, null, null);
        if (cursor.getCount() > 0)
        {
            // Remove Details panel
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.removeFragment(getActivity(), AccountDetailsFragment.TAG);
            }
            else
            {
                getFragmentManager().popBackStack(AccountDetailsFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
        else
        {
            // If no account left, we remove all preferences
            // Remove preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Editor editor = sharedPref.edit();
            editor.clear();
            editor.commit();

            // Redirect to HomeScreenActivity
            getActivity().startActivity(new Intent(getActivity(), HomeScreenActivity.class));
            getActivity().finish();
        }
        cursor.close();
    }

    public void displayOAuthFragment()
    {
        AccountOAuthFragment newFragment = AccountOAuthFragment.newInstance(acc);
        FragmentDisplayer.replaceFragment(getActivity(), newFragment, DisplayUtils.getMainPaneId(getActivity()),
                AccountOAuthFragment.TAG, true);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private void displayProfileButton()
    {
        if (ApplicationManager.getInstance(getActivity()).hasSession(acc.getId()))
        {
            vRoot.findViewById(R.id.profile_group).setVisibility(View.VISIBLE);
        }
        else
        {
            vRoot.findViewById(R.id.profile_group).setVisibility(View.GONE);
        }
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
                    R.string.edit);
            mi.setIcon(R.drawable.ic_edit);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_ACCOUNT_DELETE, Menu.FIRST + MenuActionItem.MENU_ACCOUNT_DELETE,
                R.string.delete);
        mi.setIcon(R.drawable.ic_delete);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }
}
