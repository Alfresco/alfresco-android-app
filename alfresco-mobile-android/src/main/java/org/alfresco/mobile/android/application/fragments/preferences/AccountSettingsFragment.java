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

package org.alfresco.mobile.android.application.fragments.preferences;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.BaseAppCompatActivity;
import org.alfresco.mobile.android.application.activity.WelcomeActivity;
import org.alfresco.mobile.android.application.configuration.features.SyncCellularConfigFeature;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.account.AccountEditFragment;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.config.MenuConfigFragment;
import org.alfresco.mobile.android.application.fragments.user.UserProfileFragment;
import org.alfresco.mobile.android.application.fragments.utils.EditTextDialogFragment;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.application.managers.extensions.AnalyticHelper;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.DeleteAccountEvent;
import org.alfresco.mobile.android.async.clean.CleanSyncFavoriteRequest;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AccountsPreferences;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesCheckboxViewHolder;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import com.afollestad.materialdialogs.MaterialDialog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Manage global application preferences.
 * 
 * @author Jean Marie Pascal
 */
public class AccountSettingsFragment extends AlfrescoFragment implements EditTextDialogFragment.onEditTextFragment
{
    public static final String TAG = AccountSettingsFragment.class.getName();

    private static final String ARGUMENT_ACCOUNT_ID = "accountId";

    private static final int ACCOUNT_ID = 1;

    private Long accountId = null;

    private boolean isLatest = false;

    private MaterialDialog progressdialog;

    private AlfrescoAccount account;

    private TwoLinesViewHolder menuCustomizationVH;

    private TwoLinesCheckboxViewHolder syncFavoritesVH;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountSettingsFragment()
    {
        setHasOptionsMenu(true);
        requiredSession = false;
        screenName = AnalyticsManager.SCREEN_SETTINGS_ACCOUNT;
    }

    protected static AccountSettingsFragment newInstanceByTemplate(Bundle b)
    {
        AccountSettingsFragment cbf = new AccountSettingsFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(inflater.inflate(R.layout.fr_settings_account, container, false));

        account = null;
        if (getArguments() != null)
        {
            accountId = BundleUtils.getLong(getArguments(), ARGUMENT_ACCOUNT_ID);
            account = AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(accountId);
        }

        // TITLE
        UIUtils.displayTitle(getActivity(), getString(R.string.settings_account));

        if (account == null)
        {
            hide(R.id.settings_account_info_container);
            return getRootView();
        }

        // User Info
        TwoLinesViewHolder vh;
        if (account != null && account.getTypeId() != AlfrescoAccount.TYPE_ALFRESCO_CLOUD)
        {
            vh = new TwoLinesViewHolder(viewById(R.id.settings_account_info));
            vh.topText.setText(R.string.settings_userinfo_account);
            vh.bottomText.setText(R.string.settings_userinfo_account_summary);
            vh.icon.setVisibility(View.GONE);
            viewById(R.id.settings_account_info_container).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    AccountEditFragment.with(getActivity()).accountId(accountId).display();
                }
            });
        }
        else
        {
            hide(R.id.settings_account_info_container);
        }

        // Account Name
        vh = new TwoLinesViewHolder(viewById(R.id.settings_account_name));
        vh.topText.setText(R.string.settings_userinfo_account_name);
        vh.bottomText.setText(R.string.settings_userinfo_account_name_summary);
        vh.icon.setVisibility(View.GONE);
        viewById(R.id.settings_account_name_container).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EditTextDialogFragment.with(getActivity()).fieldId(ACCOUNT_ID).tag(getTag()).value(account.getTitle())
                        .displayAsDialog();
            }
        });

        recreate();

        getActivity().invalidateOptionsMenu();

        return getRootView();
    }

    public void onCreate(Bundle savedInstanceState)
    {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (AlfrescoAccountManager.getInstance(getActivity()).isEmpty()
                && AlfrescoAccountManager.getInstance(getActivity()).hasData())
        {
            dismiss();
            getActivity().finish();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNAL
    // ///////////////////////////////////////////////////////////////////////////
    private void recreate()
    {
        // Preferences
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Account Name
        account = AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(accountId);
        ((TextView) viewById(R.id.settings_account_name_title))
                .setText(account.getTitle() + "  (" + account.getUsername() + ")");

        // CUSTOMIZATION
        menuCustomizationVH = HolderUtils.configure(viewById(R.id.settings_custom_menu_manage),
                getString(R.string.settings_custom_menu_manage), getString(R.string.settings_custom_menu_summary), -1);
        HolderUtils.makeMultiLine(menuCustomizationVH.bottomText, 3);

        if (ConfigManager.getInstance(getActivity()).hasRemoteConfig(account.getId())
                && ConfigManager.getInstance(getActivity()).getRemoteConfig(account.getId()).hasViewConfig())
        {
            viewById(R.id.settings_custom_menu_manage_container).setEnabled(false);
            menuCustomizationVH.bottomText.setText(R.string.settings_custom_menu_disable);
        }
        else
        {
            viewById(R.id.settings_custom_menu_manage_container).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    MenuConfigFragment.with(getActivity()).accountId(accountId).display();
                }
            });
        }

        // SYNC
        Boolean syncEnable = SyncContentManager.getInstance(getActivity()).hasActivateSync(account);
        Boolean syncWifiEnable = SyncContentManager.getInstance(getActivity()).hasWifiOnlySync(account);

        if (!syncEnable)
        {
            viewById(R.id.settings_sync_container).setVisibility(View.GONE);
        }
        else if (syncEnable)
        {
            viewById(R.id.settings_sync_container).setVisibility(View.VISIBLE);
            syncFavoritesVH = HolderUtils.configure(viewById(R.id.favorite_sync_wifi),
                    getString(R.string.settings_favorite_sync_data),
                    getString(R.string.settings_favorite_sync_data_all), syncWifiEnable);

            syncFavoritesVH.choose.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    SyncContentManager.getInstance(getActivity()).setWifiOnlySync(account,
                            syncFavoritesVH.choose.isChecked());
                }
            });

            SyncCellularConfigFeature feature = new SyncCellularConfigFeature(getActivity());
            if (feature.isProtected(account))
            {
                syncFavoritesVH.bottomText.setText(R.string.mdm_managed);
                syncFavoritesVH.choose.setEnabled(false);
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.clear();
        MenuItem mi = menu.add(Menu.NONE, R.id.menu_account_delete, Menu.FIRST + 10, R.string.delete);
        mi.setIcon(R.drawable.ic_clear_grey);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        if (SessionManager.getInstance(getActivity()).hasSession(account.getId()))
        {
            mi = menu.add(Menu.NONE, R.id.my_profile, Menu.FIRST, R.string.my_profile);
            mi.setIcon(R.drawable.ic_account_circle_grey);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.my_profile:
                UserProfileFragment.with(getActivity()).accountId(accountId).personId(account.getUsername()).display();
                return true;
            case R.id.menu_account_delete:
                delete();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
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
                if (alfrescoAccount.getId() == account.getId())
                {
                    continue;
                }

                // If there's one account with paid service, data protection is
                // still valid
                if (alfrescoAccount.getIsPaidAccount())
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

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());

        final File folder = AlfrescoStorageManager.getInstance(getActivity()).getPrivateFolder("", null);
        if (dataProtectionDeletion && folder != null)
        {
            builder.title(R.string.delete);
            builder.content(String.format(getResources().getString(R.string.delete_description_data_protection),
                    account.getTitle()));

            builder.positiveText(R.string.confirm).negativeText(R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback()
                    {
                        @Override
                        public void onPositive(MaterialDialog dialog)
                        {
                            DataProtectionManager.getInstance(getActivity()).decrypt(account);

                            SharedPreferences.Editor edit = prefs.edit();

                            // Unflag this so that on next (first) addition of a
                            // new
                            // paid account, they will get prompted again.
                            DataProtectionManager.getInstance(getActivity()).setDataProtectionUserRequested(false);
                            // Last paid service removed, so unflag that we've
                            // accessed
                            // paid services.
                            edit.putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false);
                            // Turn off data protection
                            DataProtectionManager.getInstance(getActivity()).setDataProtectionEnable(false);
                            edit.apply();

                            deleteAccount();
                        }
                    });
        }
        else
        {
            builder.title(R.string.delete);
            builder.content(
                    String.format(getResources().getQuantityString(R.plurals.delete_items, 1), account.getTitle()));
            builder.positiveText(R.string.confirm).negativeText(R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback()
                    {
                        @Override
                        public void onPositive(MaterialDialog dialog)
                        {
                            deleteAccount();
                        }
                    });
        }

        builder.show();
    }

    private void deleteAccount()
    {
        // List all accounts.
        List<AlfrescoAccount> accounts = AlfrescoAccountManager.retrieveAccounts(getActivity());

        // Remove all Sync
        if (account == null) { return; }
        Operator.with(getActivity()).load(new CleanSyncFavoriteRequest.Builder(account, true));

        // Delete Account from AccountManager
        AccountManager.get(getActivity()).removeAccount(
                AlfrescoAccountManager.getInstance(getActivity()).getAndroidAccount(account.getId()), null, null);

        // Analytics
        AnalyticsHelper
                .reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_ACCOUNT,
                        AnalyticsManager.ACTION_DELETE, AnalyticsHelper.getAccountType(getAccount().getTypeId()),
                        1, false);

        // In case where currentAccount is the one deleted.
        SessionManager.getInstance(getActivity()).removeAccount(account.getId());

        // Send the event
        EventBusManager.getInstance().post(new DeleteAccountEvent(account));

        AlfrescoAccount newAccount = AlfrescoAccountManager.getInstance(getActivity()).getDefaultAccount();

        if (newAccount != null && newAccount.getId() == account.getId())
        {
            SharedPreferences settings = getActivity().getSharedPreferences(AccountsPreferences.ACCOUNT_PREFS, 0);
            long id = settings.getLong(AccountsPreferences.ACCOUNT_DEFAULT, -1);
            if (id == account.getId())
            {
                settings.edit().putLong(AccountsPreferences.ACCOUNT_DEFAULT, -1).apply();
            }
        }

        // UI Management
        if (accounts.size() - 1 > 0)
        {
            for (AlfrescoAccount acc : accounts)
            {
                if (acc.getId() == account.getId())
                {
                    accounts.remove(acc);
                    break;
                }
            }

            newAccount = accounts.get(0);
            SessionManager.getInstance(getActivity()).saveAccount(newAccount);
            SharedPreferences settings = getActivity().getSharedPreferences(AccountsPreferences.ACCOUNT_PREFS, 0);
            settings.edit().putLong(AccountsPreferences.ACCOUNT_DEFAULT, newAccount.getId()).apply();
            setCurrentAccount(newAccount);

            if (SessionManager.getInstance(getActivity()).hasSession(newAccount.getId()))
            {
                SessionManager.getInstance(getActivity()).getCurrentSession();
            }
            else
            {
                SessionManager.getInstance(getActivity()).loadSession(newAccount);
            }

            // There's still other account.
            // Remove Details panel
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.with(getActivity()).remove(AccountSettingsFragment.TAG);
            }
            else
            {
                getFragmentManager().popBackStack(AccountSettingsFragment.TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
        else
        {
            // If no AlfrescoAccount left, we remove all preferences
            // Remove preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPref.edit().clear().apply();

            // Redirect to HomeScreenActivity
            getActivity().startActivity(new Intent(getActivity(), WelcomeActivity.class));
            getActivity().finish();
        }

        // Clear Analytics Info for deleted account
        AnalyticHelper.cleanOpt(getActivity(), account);
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // PICKER CALLBACK
    // //////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onTextEdited(int id, String newValue)
    {
        Account androidAccount = AlfrescoAccountManager.getInstance(getActivity()).getAndroidAccount(accountId);
        AccountManager manager = AccountManager.get(getActivity());
        manager.setUserData(androidAccount, AlfrescoAccount.ACCOUNT_NAME, newValue);
        account = AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(accountId);
        SessionManager.getInstance(getActivity()).saveSession(account,
                SessionManager.getInstance(getActivity()).getSession(account.getId()));
        SessionManager.getInstance(getActivity()).saveAccount(account);
        setCurrentAccount(account);

        AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_ACCOUNT,
                AnalyticsManager.ACTION_EDIT, AnalyticsManager.LABEL_NAME, 1, false);

        recreate();
    }

    @Override
    public void onTextClear(int valueId)
    {
        Account androidAccount = AlfrescoAccountManager.getInstance(getActivity()).getAndroidAccount(accountId);
        AccountManager manager = AccountManager.get(getActivity());
        if (account.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CLOUD)
        {
            manager.setUserData(androidAccount, AlfrescoAccount.ACCOUNT_NAME,
                    getString(R.string.account_default_cloud));
        }
        else if (account.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CMIS)
        {
            manager.setUserData(androidAccount, AlfrescoAccount.ACCOUNT_NAME,
                    getString(R.string.account_default_onpremise));
        }
        manager.setUserData(androidAccount, AlfrescoAccount.ACCOUNT_NAME,
                getString(R.string.account_default_onpremise));
        account = AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(accountId);
        SessionManager.getInstance(getActivity()).saveSession(account,
                SessionManager.getInstance(getActivity()).getSession(account.getId()));
        SessionManager.getInstance(getActivity()).saveAccount(account);
        setCurrentAccount(account);

        recreate();
    }

    private void setCurrentAccount(AlfrescoAccount account)
    {
        if (getActivity() instanceof BaseAppCompatActivity)
        {
            ((BaseAppCompatActivity) getActivity()).setCurrentAccount(account);
        }
        else
        {
            ((BaseActivity) getActivity()).setCurrentAccount(account);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONFIG
    // ///////////////////////////////////////////////////////////////////////////
    private void getSyncWifi()
    {
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

        public Builder accountId(long accountId)
        {
            extraConfiguration.putLong(ARGUMENT_ACCOUNT_ID, accountId);
            return this;
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
