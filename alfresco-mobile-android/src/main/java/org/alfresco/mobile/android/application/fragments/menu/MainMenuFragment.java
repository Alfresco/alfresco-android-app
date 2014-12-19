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
package org.alfresco.mobile.android.application.fragments.menu;

import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.RepositoryInfo;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigInfo;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.config.ConfigManager;
import org.alfresco.mobile.android.application.config.ConfigManager.ConfigurationMenuEvent;
import org.alfresco.mobile.android.application.config.manager.MainMenuConfigManager;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.fragments.accounts.AccountsAdapter;
import org.alfresco.mobile.android.application.fragments.accounts.AccountsFragment;
import org.alfresco.mobile.android.application.fragments.accounts.NetworksFragment;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.operations.OperationsFragment;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.fragments.profiles.ProfilesConfigFragment;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountCompletedEvent;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AccountsPreferences;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.sync.FavoritesSyncProvider;
import org.alfresco.mobile.android.sync.FavoritesSyncScanEvent;
import org.alfresco.mobile.android.sync.FavoritesSyncSchema;
import org.alfresco.mobile.android.sync.SyncScanInfo;
import org.alfresco.mobile.android.sync.operations.FavoriteSyncStatus;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

import com.squareup.otto.Subscribe;

public class MainMenuFragment extends AlfrescoFragment implements OnItemSelectedListener
{
    private Spinner spinnerAccount;

    private int accountIndex;

    private Button menuFavorites;

    private Button menuSlidingFavorites;

    private ConfigManager configurationManager;

    private AccountsAdapter accountsAdapter;

    public static final String TAG = MainMenuFragment.class.getName();

    public static final String SLIDING_TAG = "SlidingMenuFragment";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public MainMenuFragment()
    {
        checkSession = false;
        requiredSession = false;
    }

    protected static MainMenuFragment newInstanceByTemplate(Bundle b)
    {
        MainMenuFragment cbf = new MainMenuFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(inflater.inflate(R.layout.app_main_menu, container, false));

        spinnerAccount = (Spinner) getRootView().findViewById(R.id.accounts_spinner);
        spinnerAccount.setOnItemSelectedListener(this);

        menuFavorites = (Button) viewById(R.id.menu_favorites);

        if (SLIDING_TAG.equals(getTag()))
        {
            menuSlidingFavorites = (Button) viewById(R.id.menu_favorites);
            setRetainInstance(true);
        }

        configurationManager = ConfigManager.getInstance(getActivity());
        if (configurationManager != null && getAccount() != null
                && configurationManager.hasConfig(getAccount().getId()))
        {
            configure(configurationManager.getConfig(getAccount().getId()));
        }
        else
        {
            display();
        }

        return getRootView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // retrieve accounts
        configurationManager = ConfigManager.getInstance(getActivity());
        AlfrescoAccount acc = getAccount();
        if (acc == null)
        {
            acc = AlfrescoAccountManager.getInstance(getActivity()).getDefaultAccount();
        }
        if (configurationManager != null && acc != null && configurationManager.hasConfig(acc.getId()))
        {
            configure(configurationManager.getConfig(acc.getId()));
        }
        else if (configurationManager != null && acc != null)
        {
            // Configuration
            configurationManager.init(acc);
            configure(configurationManager.getConfig(acc.getId()));
        }

        refresh();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (isAdded() && TAG.equals(getTag())
                && getActivity().getFragmentManager().findFragmentByTag(GeneralPreferences.TAG) == null
                && getActivity().getFragmentManager().findFragmentByTag(AboutFragment.TAG) == null)
        {
            FragmentDisplayer.clearCentralPane(getActivity());
        }

        UIUtils.displayTitle(getActivity(), R.string.app_name);
        EventBusManager.getInstance().register(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        if (TAG.equals(getTag()))
        {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
            getActivity().getActionBar().setHomeButtonEnabled(false);
            if (getActivity() instanceof MainActivity)
            {
                ((MainActivity) getActivity()).lockSlidingMenu();
            }
        }
        displayFavoriteStatut();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (!isVisible() && TAG.equals(getTag()))
        {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            getActivity().getActionBar().setHomeButtonEnabled(true);
            if (getActivity() instanceof MainActivity)
            {
                ((MainActivity) getActivity()).unlockSlidingMenu();
            }
        }
    }

    @Override
    public void onStop()
    {
        EventBusManager.getInstance().unregister(this);
        super.onStop();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DPUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public void refreshData()
    {
        refresh();
        displayFavoriteStatut();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DROPDOWN EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
    {
        AlfrescoAccount acc = (AlfrescoAccount) parentView.getItemAtPosition(position);
        int accountId = (int) acc.getId();

        switch (accountId)
        {
            case AccountsAdapter.PROFILES_ITEM:
                ProfilesConfigFragment.with(getActivity()).display();
                hideSlidingMenu(false);
                refresh();
                break;
            case AccountsAdapter.NETWORK_ITEM:
                NetworksFragment.with(getActivity()).display();
                hideSlidingMenu(false);
                refresh();
                break;
            case AccountsAdapter.MANAGE_ITEM:
                AccountsFragment.with(getActivity()).display();
                hideSlidingMenu(false);
                refresh();
                break;

            default:
                AlfrescoAccount currentAccount = SessionUtils.getAccount(getActivity());
                if (currentAccount != null && currentAccount.getId() != accountId)
                {
                    hideSlidingMenu(true);

                    // Switch to current account
                    if (getActivity() instanceof AlfrescoActivity)
                    {
                        ((AlfrescoActivity) getActivity()).setCurrentAccount(accountId);
                        UIUtils.displayTitle(getActivity(), getString(R.string.app_name), false);
                    }

                    // Request session loading for the selected AlfrescoAccount.
                    SessionManager.getInstance(getActivity()).loadSession(
                            AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(accountId));
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private void configure(ConfigService configService)
    {
        if (configService != null && configService.hasViewConfig())
        {
            String profileId = configurationManager.getCurrentProfileId();
            if (profileId == null)
            {
                profileId = configService.getDefaultProfile().getIdentifier();
            }
            if (configService.getDefaultProfile().getRootViewId() != null
                    && configService.getViewConfig(configService.getProfile(profileId).getRootViewId(),
                            configurationManager.getCurrentScope()) != null)
            {
                // Configuration (Internal or from Server)
                DisplayUtils.hide(viewById(R.id.main_menu_group));
                MainMenuConfigManager config = new MainMenuConfigManager(getActivity(), configService,
                        (ViewGroup) getRootView());
                config.createMenu();
            }
            else if (configService.getConfigInfo() == null
                    || (configService.getConfigInfo() != null && ConfigInfo.SCHEMA_VERSION_BETA.equals(configService
                            .getConfigInfo().getSchemaVersion())))
            {
                // BETA
                hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_ACTIVITIES), R.id.menu_browse_activities);
                hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_REPOSITORY), R.id.menu_browse_root);
                hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_SITES), R.id.menu_browse_my_sites);
                hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_TASKS), R.id.menu_workflow);
                hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_FAVORITES), R.id.menu_favorites);
                hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_SEARCH), R.id.menu_search);
                hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_LOCAL_FILES), R.id.menu_downloads);
                hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_NOTIFICATIONS), R.id.menu_notifications);
                hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_SHARED), R.id.menu_browse_shared, true);
                hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_MYFILES), R.id.menu_browse_userhome,
                        true);
            }
            else
            {
                display();
            }
        }
        else
        {
            display();
        }
    }

    private void display()
    {
        AlfrescoAccount acc = SessionUtils.getAccount(getActivity());

        DisplayUtils.show(viewById(R.id.main_menu_group));
        show(R.id.menu_browse_activities);
        show(R.id.menu_browse_root);
        show(R.id.menu_browse_my_sites);
        if (acc != null && acc.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CLOUD)
        {
            hide(R.id.menu_workflow);
        }
        else
        {
            show(R.id.menu_workflow);
        }
        show(R.id.menu_favorites);
        show(R.id.menu_search);
        show(R.id.menu_downloads);
        if (OperationsFragment.canDisplay(getActivity(), acc))
        {
            show(R.id.menu_notifications);
        }
        else
        {
            hide(R.id.menu_notifications);
        }
        displayFolderShortcut(SessionUtils.getSession(getActivity()));
    }

    private void hideOrDisplay(ViewConfig viewConfig, int viewId)
    {
        hideOrDisplay(viewConfig, viewId, false);
    }

    private void hideOrDisplay(ViewConfig viewConfig, int viewId, boolean forceHide)
    {
        if (viewConfig != null && viewConfig.getParameter(ConfigConstants.VISIBLE_VALUE) != null
                && viewConfig.getParameter(ConfigConstants.VISIBLE_VALUE) instanceof Boolean)
        {
            if ((Boolean) viewConfig.getParameter(ConfigConstants.VISIBLE_VALUE))
            {
                show(viewId);
            }
            else
            {
                hide(viewId);
            }
        }
        else
        {
            if (forceHide)
            {
                hide(viewId);
            }
            else
            {
                show(viewId);
            }
        }
    }

    private void refresh()
    {
        AlfrescoAccount currentAccount = SessionUtils.getAccount(getActivity());
        if (currentAccount == null)
        {
            currentAccount = AccountsPreferences.getDefaultAccount(getActivity());
        }

        if (currentAccount == null) { return; }

        // We retrieve index of the current account to select it
        List<AlfrescoAccount> list = AlfrescoAccountManager.retrieveAccounts(getActivity());
        for (int i = 0; i < list.size(); i++)
        {
            if (currentAccount.getId() == list.get(i).getId())
            {
                accountIndex = i;
                break;
            }
        }

        // We add all extra parameters at the end of the list.
        if (currentAccount.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CLOUD)
        {
            list.add(new AlfrescoAccount(AccountsAdapter.NETWORK_ITEM, getString(R.string.cloud_networks_switch), null,
                    null, null, null, "0", null, "false"));
        }

        if (configurationManager != null && configurationManager.getConfig(currentAccount.getId()) != null
                && configurationManager.getConfig(currentAccount.getId()).getProfiles() != null)
        {
            list.add(new AlfrescoAccount(AccountsAdapter.PROFILES_ITEM, getString(R.string.profiles_switch), null,
                    null, null, null, "0", null, "false"));
        }

        list.add(new AlfrescoAccount(AccountsAdapter.MANAGE_ITEM, getString(R.string.manage_accounts), null, null,
                null, null, "0", null, "false"));

        // Init the adapter and create the menu
        accountsAdapter = new AccountsAdapter(getActivity(), list, R.layout.app_account_list_row, null);
        spinnerAccount.setAdapter(accountsAdapter);
        spinnerAccount.setSelection(accountIndex);

        if (OperationsFragment.canDisplay(getActivity(), currentAccount))
        {
            show(R.id.menu_notifications);
        }
        else
        {
            hide(R.id.menu_notifications);
        }
    }

    private void hideSlidingMenu(boolean goHome)
    {
        if (SLIDING_TAG.equals(getTag()))
        {
            if (goHome)
            {
                getFragmentManager().popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }

    public void hideWorkflowMenu(AlfrescoAccount currentAccount)
    {
        if (getRootView() == null || viewById(R.id.menu_workflow) == null || currentAccount == null) { return; }
        if (currentAccount.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CLOUD
                || currentAccount.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_TEST_OAUTH)
        {
            hide(R.id.menu_workflow);
        }
        else
        {
            show(R.id.menu_workflow);
        }
        displayFolderShortcut(SessionUtils.getSession(getActivity(), currentAccount.getId()));
    }

    public void displayFolderShortcut(AlfrescoSession alfSession)
    {
        if (alfSession != null)
        {
            RepositoryInfo repoInfo = alfSession.getRepositoryInfo();
            boolean globalCheck = repoInfo.getMajorVersion() > 4;
            boolean global42Check = repoInfo.getMajorVersion() == 4 && repoInfo.getMinorVersion() > 2;
            boolean enterpriseCheck = repoInfo.getMajorVersion() >= 4 && repoInfo.getMinorVersion() >= 2
                    && repoInfo.getEdition().equals(OnPremiseConstant.ALFRESCO_EDITION_ENTERPRISE);
            boolean communityCheck = repoInfo.getMajorVersion() >= 4 && repoInfo.getMinorVersion() >= 2
                    && repoInfo.getEdition().equals(OnPremiseConstant.ALFRESCO_EDITION_COMMUNITY)
                    && (repoInfo.getVersion().contains(".e") || repoInfo.getVersion().contains(".f"));
            if (globalCheck || global42Check || enterpriseCheck || communityCheck)
            {
                show(R.id.menu_browse_shared);
                show(R.id.menu_browse_userhome);
                return;
            }
        }
        if (getRootView() != null)
        {
            hide(R.id.menu_browse_shared);
            hide(R.id.menu_browse_userhome);
        }
    }

    public void displayFavoriteStatut()
    {
        Cursor statutCursor = null;
        Drawable icon = getActivity().getResources().getDrawable(R.drawable.ic_favorite);
        Drawable statut = null;

        try
        {
            AlfrescoAccount acc = SessionUtils.getAccount(getActivity());
            Boolean hasSynchroActive = FavoritesSyncManager.getInstance(getActivity()).hasActivateSync(acc);

            long startTimeStamp = FavoritesSyncManager.getInstance(getActivity()).getStartSyncPrepareTimestamp(acc);
            long finalTimeStamp = FavoritesSyncManager.getInstance(getActivity()).getSyncPrepareTimestamp(acc);

            // Sync Prepare in Progress ?
            if (startTimeStamp > finalTimeStamp)
            {
                // Sync Prepare in progress
                statut = getActivity().getResources().getDrawable(R.drawable.ic_action_reload);
            }
            else
            {
                // Sync Prepare done

                // Is there a policy warning ?
                SyncScanInfo syncScanInfo = SyncScanInfo.getLastSyncScanData(getActivity(), acc);
                if (syncScanInfo != null && syncScanInfo.hasWarning())
                {
                    // ==> Sync requires a user input
                    statut = getActivity().getResources().getDrawable(R.drawable.ic_warning_light);
                }
            }

            // Is there a doc warning ?
            if (hasSynchroActive && acc != null)
            {
                statutCursor = getActivity().getContentResolver().query(
                        FavoritesSyncProvider.CONTENT_URI,
                        FavoritesSyncSchema.COLUMN_ALL,
                        FavoritesSyncProvider.getAccountFilter(acc) + " AND " + FavoritesSyncSchema.COLUMN_STATUS
                                + " == " + FavoriteSyncStatus.STATUS_REQUEST_USER, null, null);
                if (statutCursor.getCount() > 0)
                {
                    statut = getActivity().getResources().getDrawable(R.drawable.ic_warning_light);
                }
                statutCursor.close();
            }
            else
            {
                statut = null;
            }

            if (menuSlidingFavorites != null)
            {
                menuSlidingFavorites.setCompoundDrawablesWithIntrinsicBounds(icon, null, statut, null);
            }
            menuFavorites.setCompoundDrawablesWithIntrinsicBounds(icon, null, statut, null);
        }
        catch (Exception e)
        {

        }
        finally
        {
            if (statutCursor != null)
            {
                statutCursor.close();
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // OVERFLOW MENU
    // ///////////////////////////////////////////////////////////////////////////
    public static void getMenu(Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, R.id.menu_settings, Menu.FIRST, R.string.menu_prefs);
        mi.setIcon(R.drawable.ic_settings_light);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        mi = menu.add(Menu.NONE, R.id.menu_help, Menu.FIRST + 1, R.string.menu_help);
        mi.setIcon(R.drawable.ic_help);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        mi = menu.add(Menu.NONE, R.id.menu_about, Menu.FIRST + 2, R.string.menu_about);
        mi.setIcon(R.drawable.ic_about_light);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onAccountLoaded(LoadAccountCompletedEvent event)
    {
        refresh();
    }

    @Subscribe
    public void onConfigureMenuEvent(ConfigurationMenuEvent event)
    {
        Log.d("EVENT", "Receive onConfigureMenuEvent");

        configurationManager = ConfigManager.getInstance(getActivity());
        if (configurationManager != null && configurationManager.hasConfig(event.accountId))
        {
            refresh();
            configure(configurationManager.getConfig(event.accountId));
        }
        else
        {
            display();
        }
    }

    @Subscribe
    public void onSyncCompleted(FavoritesSyncScanEvent event)
    {
        displayFavoriteStatut();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
            this.hasBackStack = false;
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
    }

}
