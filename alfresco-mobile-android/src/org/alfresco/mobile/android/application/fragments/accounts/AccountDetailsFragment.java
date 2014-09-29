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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.HomeScreenActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.person.UserProfileFragment;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.DeleteAccountEvent;
import org.alfresco.mobile.android.async.account.signup.SignUpEvent;
import org.alfresco.mobile.android.async.account.signup.SignUpRequest;
import org.alfresco.mobile.android.async.account.signup.SignUpStatusEvent;
import org.alfresco.mobile.android.async.account.signup.SignUpStatusRequest;
import org.alfresco.mobile.android.async.clean.CleanSyncFavoriteRequest;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AccountsPreferences;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.data.CloudSignupRequest;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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

import com.squareup.otto.Subscribe;

/**
 * It's responsible to display the details of a specific AlfrescoAccount.
 * 
 * @author Jean Marie Pascal
 */
public class AccountDetailsFragment extends AlfrescoFragment
{
    public static final String TAG = AccountDetailsFragment.class.getName();

    public static final String ARGUMENT_ACCOUNT_ID = "accountID";

    public static final String ARGUMENT_ACCOUNT = "account";

    private String url = null, host = null, username = null, password = null, servicedocument = null,
            description = null;

    private boolean https = false;

    private int port;

    private AlfrescoAccount acc;

    private boolean isEditable = false;

    private Button validate;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountDetailsFragment()
    {
    }

    protected static AccountDetailsFragment newInstanceByTemplate(Bundle b)
    {
        AccountDetailsFragment cbf = new AccountDetailsFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null) { return null; }

        long accountId = 0;
        if (getArguments() != null)
        {
            accountId = getArguments().getLong(ARGUMENT_ACCOUNT_ID);
            acc = (AlfrescoAccount) getArguments().getSerializable(ARGUMENT_ACCOUNT);
        }

        if (acc == null)
        {
            acc = AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(accountId);
        }

        if (acc.getActivation() == null)
        {
            setRootView(inflater.inflate(R.layout.app_account_details, container, false));
            initValues();
        }

        if (isEditable)
        {
            edit();
        }

        return getRootView();
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
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), getText(R.string.accounts_details) + " : " + acc.getTitle());
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
    private void initValues()
    {
        URL tmprUrl = null;
        try
        {
            tmprUrl = new URL(acc.getUrl());
        }
        catch (MalformedURLException e)
        {
            AlfrescoNotificationManager.getInstance(getActivity()).showToast(R.string.error_account_url);
            return;
        }

        show(R.id.account_authentication);

        if (acc.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CLOUD)
        {
            hide(R.id.advanced);
            hide(R.id.advanced_settings);
            hide(R.id.repository_https_group);
            hide(R.id.repository_hostname_group);
            hide(R.id.repository_username_group);
            hide(R.id.repository_password_group);
        }
        else if (acc.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_TEST_BASIC
                || acc.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_TEST_OAUTH)
        {
            hide(R.id.repository_password_group);
        }
        else
        {
            show(R.id.advanced_settings);
        }

        Button advanced = (Button) viewById(R.id.browse_document);
        advanced.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Affect new AlfrescoAccount to activity
                ((BaseActivity) getActivity()).setCurrentAccount(acc);

                // Request or create new session for the AlfrescoAccount.
                // TODO Replace by AccountManager or SessionManager
                EventBusManager.getInstance().post(new RequestSessionEvent(acc, true));
            }
        });

        advanced = (Button) viewById(R.id.my_profile);
        advanced.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                UserProfileFragment.with(getActivity()).personId(acc.getUsername()).displayAsDialog();
            }
        });
        displayProfileButton();

        // Init values
        EditText formValue = (EditText) viewById(R.id.repository_hostname);
        formValue.setText(tmprUrl.getHost());
        formValue.setEnabled(isEditable);

        formValue = (EditText) viewById(R.id.repository_username);
        formValue.setText(acc.getUsername());
        formValue.setEnabled(isEditable);

        formValue = (EditText) viewById(R.id.repository_description);
        formValue.setText(acc.getTitle());
        formValue.setEnabled(isEditable);

        formValue = (EditText) viewById(R.id.repository_password);
        formValue.setText(acc.getPassword());
        formValue.setEnabled(isEditable);

        // TODO Switch widget ?
        final CheckBox sw = (CheckBox) viewById(R.id.repository_https);
        sw.setChecked(tmprUrl.getProtocol().equals("https"));
        sw.setEnabled(isEditable);

        final EditText portForm = (EditText) viewById(R.id.repository_port);
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

        formValue = (EditText) viewById(R.id.repository_port);
        if (tmprUrl.getPort() != -1)
        {
            formValue.setText(tmprUrl.getPort() + "");
        }
        else
        {
            formValue.setText(tmprUrl.getDefaultPort() + "");
        }
        formValue.setEnabled(isEditable);

        formValue = (EditText) viewById(R.id.repository_servicedocument);
        formValue.setText(tmprUrl.getPath());
        formValue.setEnabled(isEditable);

        // Accessibility
        if (AccessibilityUtils.isEnabled(getActivity()))
        {
            ((EditText) viewById(R.id.repository_username)).setHint(getString(R.string.account_username_required_hint));
            ((EditText) viewById(R.id.repository_password)).setHint(getString(R.string.account_password_required_hint));
            ((EditText) viewById(R.id.repository_hostname)).setHint(getString(R.string.account_hostname_required_hint));
            ((EditText) viewById(R.id.repository_description))
                    .setHint(getString(R.string.account_description_optional_hint));
            AccessibilityUtils.addContentDescription(sw, sw.isChecked() ? R.string.account_https_on_hint
                    : R.string.account_https_off_hint);
            portForm.setHint(getString(R.string.account_port_hint));
            ((EditText) viewById(R.id.repository_servicedocument))
                    .setHint(getString(R.string.account_servicedocument_hint));
        }
    }

    private boolean retrieveFormValues()
    {
        // Check values
        EditText formValue = (EditText) viewById(R.id.repository_hostname);
        if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
        {
            host = formValue.getText().toString();
        }
        else
        {
            AccessibilityUtils.addContentDescription(validate, R.string.account_validate_disable_hint);
            return false;
        }

        formValue = (EditText) viewById(R.id.repository_username);
        if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
        {
            username = formValue.getText().toString();
        }
        else
        {
            AccessibilityUtils.addContentDescription(validate, R.string.account_validate_disable_hint);
            return false;
        }

        formValue = (EditText) viewById(R.id.repository_description);
        description = formValue.getText().toString();

        if (acc.getTypeId() != AlfrescoAccount.TYPE_ALFRESCO_CLOUD
                && acc.getTypeId() != AlfrescoAccount.TYPE_ALFRESCO_TEST_OAUTH)
        {
            formValue = (EditText) viewById(R.id.repository_password);
            if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
            {
                password = formValue.getText().toString();
            }
            else
            {
                AccessibilityUtils.addContentDescription(validate, R.string.account_validate_disable_hint);
                return false;
            }
        }

        CheckBox sw = (CheckBox) viewById(R.id.repository_https);
        https = sw.isChecked();
        String protocol = https ? "https" : "http";

        formValue = (EditText) viewById(R.id.repository_port);
        if (formValue.getText().length() > 0)
        {
            port = Integer.parseInt(formValue.getText().toString());
        }
        else
        {
            port = (protocol.equals("https")) ? 443 : 80;
        }

        formValue = (EditText) viewById(R.id.repository_servicedocument);
        servicedocument = formValue.getText().toString();
        URL u = null;
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

    private void initForm()
    {
        int[] ids = new int[] { R.id.repository_username, R.id.repository_password, R.id.repository_hostname,
                R.id.repository_servicedocument, R.id.repository_description, R.id.repository_port };
        EditText formValue = null;
        for (int i = 0; i < ids.length; i++)
        {
            formValue = (EditText) viewById(ids[i]);
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
            formValue = (EditText) viewById(ids[i]);
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
        if (isEditable) { return; }

        isEditable = true;
        initValues();
        initForm();

        validate = (Button) viewById(R.id.validate_account);
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
                acc = AlfrescoAccountManager.getInstance(getActivity()).update(
                        getArguments().getLong(ARGUMENT_ACCOUNT_ID), description, (url != null) ? url : acc.getUrl(),
                        username, password, acc.getRepositoryId(), Integer.valueOf((int) acc.getTypeId()), null,
                        acc.getAccessToken(), acc.getRefreshToken(), acc.getIsPaidAccount() ? 1 : 0);

                initValues();
                show(R.id.browse_document);
                displayProfileButton();
                hide(R.id.cancel_account);
                v.setVisibility(View.GONE);
            }
        });

        Button b = (Button) viewById(R.id.cancel_account);
        b.setVisibility(View.VISIBLE);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isEditable = false;
                validate.setEnabled(false);
                removeFormWatcher();
                initValues();
                show(R.id.browse_document);
                displayProfileButton();
                v.setVisibility(View.GONE);
                hide(R.id.validate_account);
            }
        });

        hide(R.id.browse_document);
        hide(R.id.profile_group);
    }

    public void delete()
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean dataProtectionDeletion = true;

        // Prepare deletion : Check if the account we want to delete is the
        // latest paid account.
        if (DataProtectionManager.getInstance(getActivity()).hasDataProtectionEnable())
        {
            // List all accounts.
            List<AlfrescoAccount> accounts = AlfrescoAccountManager.retrieveAccounts(getActivity());
            for (AlfrescoAccount alfrescoAccount : accounts)
            {
                // Ignore the account we want to delete
                if (alfrescoAccount.getId() == acc.getId())
                {
                    continue;
                }

                // If there's one account with paid service, data protection is
                // still valid
                if (alfrescoAccount.getIsPaidAccount() == true)
                {
                    dataProtectionDeletion = false;
                    break;
                }
            }
        }
        else
        {
            dataProtectionDeletion = false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final File folder = AlfrescoStorageManager.getInstance(getActivity()).getPrivateFolder("", null);
        if (dataProtectionDeletion && folder != null)
        {
            builder.setTitle(R.string.delete);
            builder.setMessage(String.format(getResources().getString(R.string.delete_description_data_protection),
                    acc.getTitle()));
            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int item)
                {
                    DataProtectionManager.getInstance(getActivity()).decrypt(acc);

                    Editor edit = prefs.edit();

                    // Unflag this so that on next (first) addition of a new
                    // paid account, they will get prompted again.
                    DataProtectionManager.getInstance(getActivity()).setDataProtectionUserRequested(false);
                    // Last paid service removed, so unflag that we've accessed
                    // paid services.
                    edit.putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false);
                    // Turn off data protection
                    DataProtectionManager.getInstance(getActivity()).setDataProtectionEnable(false);
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
                    acc.getTitle()));
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

    private void deleteAccount()
    {
        // List all accounts.
        List<AlfrescoAccount> accounts = AlfrescoAccountManager.retrieveAccounts(getActivity());

        // Remove all Sync
        if (acc == null) { return; }
        Operator.with(getActivity()).load(new CleanSyncFavoriteRequest.Builder(acc, true));

        // Delete Account from AccountManager
        AccountManager.get(getActivity()).removeAccount(
                AlfrescoAccountManager.getInstance(getActivity()).getAndroidAccount(acc.getId()), null, null);

        // Send the event
        EventBusManager.getInstance().post(new DeleteAccountEvent(acc));

        // In case where currentAccount is the one deleted.
        SessionManager.getInstance(getActivity()).removeAccount(acc.getId());

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
            ((BaseActivity) getActivity()).setCurrentAccount(AlfrescoAccountManager.getInstance(getActivity())
                    .getDefaultAccount());
        }

        // UI Management
        if (accounts.size() - 1 > 0)
        {
            // There's still other account.
            // Remove Details panel
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.with(getActivity()).remove(AccountDetailsFragment.TAG);
            }
            else
            {
                getFragmentManager().popBackStack(AccountDetailsFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
        else
        {
            // If no AlfrescoAccount left, we remove all preferences
            // Remove preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Editor editor = sharedPref.edit();
            editor.clear();
            editor.commit();

            // Redirect to HomeScreenActivity
            getActivity().startActivity(new Intent(getActivity(), HomeScreenActivity.class));
            getActivity().finish();
        }
    }

    public void displayOAuthFragment()
    {
        AccountOAuthFragment.with(getActivity()).display();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private void displayProfileButton()
    {
        if (SessionManager.getInstance(getActivity()).hasSession(acc.getId()))
        {
            show(R.id.profile_group);
        }
        else
        {
            hide(R.id.profile_group);
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
                    String.format(getString(R.string.account_edit_hint), acc.getTitle()));
            mi.setIcon(R.drawable.ic_edit);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_ACCOUNT_DELETE, Menu.FIRST + MenuActionItem.MENU_ACCOUNT_DELETE,
                String.format(getString(R.string.account_delete_hint), acc.getTitle()));
        mi.setIcon(R.drawable.ic_delete);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onCloudSignUpStatusEvent(SignUpStatusEvent event)
    {
        Boolean hasData = event.data;
        if (event.hasException)
        {
            Log.e(TAG, Log.getStackTraceString(event.exception));
            AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(getActivity().getString(R.string.error_general));
        }
        else if (hasData)
        {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(PrivateIntent.ALFRESCO_SCHEME_SHORT
                    + "://activate-cloud-account/" + event.signUpRequest.getIdentifier()));
            getActivity().startActivity(i);
        }
        else
        {
            AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(getString(R.string.account_not_activated_description));
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
            templateArguments = new String[] {};
            hasBackStack = false;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder accountId(long accountId)
        {
            extraConfiguration.putLong(ARGUMENT_ACCOUNT_ID, accountId);
            return this;
        }

        public Builder account(AlfrescoAccount account)
        {
            extraConfiguration.putSerializable(ARGUMENT_ACCOUNT, account);
            return this;
        }

    }

}
