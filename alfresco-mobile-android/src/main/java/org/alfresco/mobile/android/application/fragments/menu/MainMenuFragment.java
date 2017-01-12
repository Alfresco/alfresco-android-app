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
package org.alfresco.mobile.android.application.fragments.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.RepositoryInfo;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigInfo;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.BuildConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.configuration.MainMenuConfigManager;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.fragments.account.AccountsAdapter;
import org.alfresco.mobile.android.application.fragments.account.NetworksFragment;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.operation.OperationsFragment;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.fragments.profile.ProfilesConfigFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.application.managers.ConfigManager.ConfigurationMenuEvent;
import org.alfresco.mobile.android.async.person.AvatarEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountCompletedEvent;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AccountsPreferences;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.mdm.MDMManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentProvider;
import org.alfresco.mobile.android.sync.SyncContentScanEvent;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.SyncScanInfo;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import com.squareup.otto.Subscribe;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

public class MainMenuFragment extends AlfrescoFragment implements AdapterView.OnItemClickListener
{
    private boolean showOperationsMenu = false;

    private AccountsAdapter accountsAdapter;

    private AlfrescoAccount currentAccount;

    private ListPopupWindow listPopupWindow;

    private Button menuFavorites;

    private LinearLayout accountsSpinnerButton;

    private List<View> syncFavoritesMenuItem = new ArrayList<>();

    private ConfigManager configManager;

    private MDMManager mdmManager;

    private String mdmProfile;

    public static final String TAG = MainMenuFragment.class.getName();

    public static final String SLIDING_TAG = "SlidingMenuFragment";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public MainMenuFragment()
    {
        checkSession = false;
        requiredSession = false;
        setHasOptionsMenu(true);
        reportAtCreation = false;
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

        accountsSpinnerButton = (LinearLayout) viewById(R.id.accounts_spinner_button);

        menuFavorites = (Button) viewById(R.id.menu_favorites);

        if (SLIDING_TAG.equals(getTag()))
        {
            setRetainInstance(true);
        }

        return getRootView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // retrieve accounts
        configManager = ConfigManager.getInstance(getActivity());
        // Todo replace elsewhere & improve request numbers?
        mdmManager = MDMManager.getInstance(getActivity());
        if (mdmManager != null)
        {
            mdmManager.requestConfig(getActivity(), BuildConfig.APPLICATION_ID);
        }

        currentAccount = getAccount();
        if (currentAccount == null)
        {
            currentAccount = AlfrescoAccountManager.getInstance(getActivity()).getDefaultAccount();
        }
        if (configManager != null && currentAccount != null)
        {
            // Configuration
            configManager.init(currentAccount);
            configure(configManager.getConfig(currentAccount.getId(), ConfigTypeIds.VIEWS));
        }

        viewById(R.id.menu_settings).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayPreferences((BaseActivity) getActivity(), getAccount().getId());
                hideSlidingMenu(false);
            }
        });

        viewById(R.id.menu_help).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(getActivity(), PrivateDialogActivity.class);
                i.setAction(PrivateIntent.ACTION_DISPLAY_HELP);
                startActivity(i);
            }
        });

        refresh();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (isAdded() && TAG.equals(getTag())
                && getActivity().getSupportFragmentManager().findFragmentByTag(GeneralPreferences.TAG) == null
                && getActivity().getSupportFragmentManager().findFragmentByTag(AboutFragment.TAG) == null)
        {
            FragmentDisplayer.clearCentralPane(getActivity());
        }

        UIUtils.displayTitle(R.string.app_name, (AppCompatActivity) getActivity(), !TAG.equals(getTag()));
        EventBusManager.getInstance().register(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onResume()
    {
        super.onResume();
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        if (TAG.equals(getTag()))
        {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setHomeButtonEnabled(false);
            if (getActivity() instanceof MainActivity)
            {
                ((MainActivity) getActivity()).lockSlidingMenu();
            }
        }
        displaySyncStatut();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onPause()
    {
        super.onPause();
        if (!isVisible() && TAG.equals(getTag()))
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
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
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public void refreshAccount()
    {
        currentAccount = SessionManager.getInstance(getActivity()).getCurrentAccount();
        if (currentAccount == null) { return; }
        if (configManager.getConfig(currentAccount.getId()) != null)
        {
            refreshConfiguration(currentAccount.getId());
        }
        else
        {
            configManager = ConfigManager.getInstance(getActivity());
            refresh();
            if (configManager.getConfig(currentAccount.getId()) == null)
            {
                configManager.init(currentAccount);
            }
            MainMenuConfigManager config = new MainMenuConfigManager(getActivity(),
                    configManager.getConfig(currentAccount.getId()), (ViewGroup) getRootView());
            config.createMenu();
        }
        refresh();
        displaySyncStatut();
    }

    public void refreshData()
    {
        refresh();
        displaySyncStatut();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DROPDOWN EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        AlfrescoAccount acc = (AlfrescoAccount) parent.getItemAtPosition(position);
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
                displayPreferences((BaseActivity) getActivity(), getAccount().getId());
                hideSlidingMenu(false);
                refresh();
                break;
            default:
                if (currentAccount != null && currentAccount.getId() != accountId)
                {
                    if (getActivity() instanceof AlfrescoActivity)
                    {
                        ((TextView) accountsSpinnerButton.findViewById(R.id.accounts_spinner_title))
                                .setText(acc.getTitle());
                        ((AlfrescoActivity) getActivity()).swapAccount(acc);
                        if (SLIDING_TAG.equals(getTag()))
                        {
                            ((MainMenuFragment) ((AlfrescoActivity) getActivity()).getFragment(TAG)).swapAccount();
                        }
                        else
                        {
                            ((MainMenuFragment) ((AlfrescoActivity) getActivity()).getFragment(SLIDING_TAG))
                                    .swapAccount();
                        }
                    }
                }
                break;
        }

        listPopupWindow.dismiss();
        listPopupWindow = null;
    }

    public void swapAccount()
    {
        AlfrescoAccount tmpAccount = getAccount();
        if (tmpAccount != null && currentAccount != null && tmpAccount.getId() != currentAccount.getId())
        {
            currentAccount = tmpAccount;
            refresh();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private void configure(ConfigService configService)
    {
        if (configService == null || !configService.hasViewConfig()) { return; }

        // Default only available since configuration 0.1
        if (configService.getConfigInfo() == null || (configService.getConfigInfo() != null
                && ConfigInfo.SCHEMA_VERSION_BETA.equals(configService.getConfigInfo().getSchemaVersion())))
        {
            // BETA
            DisplayUtils.hide(viewById(R.id.custom_menu_group));
            DisplayUtils.show(viewById(R.id.main_menu_group));
            hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_ACTIVITIES), R.id.menu_browse_activities);
            hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_REPOSITORY), R.id.menu_browse_root);
            hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_SITES), R.id.menu_browse_my_sites);
            hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_TASKS), R.id.menu_workflow);
            hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_FAVORITES), R.id.menu_favorites);
            hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_SEARCH), R.id.menu_search);
            hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_LOCAL_FILES), R.id.menu_downloads);
            hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_NOTIFICATIONS), R.id.menu_notifications);
            hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_SHARED), R.id.menu_browse_shared, true);
            hideOrDisplay(configService.getViewConfig(ConfigConstants.MENU_MYFILES), R.id.menu_browse_userhome, true);
        }
        else
        {
            // Configuration (Internal or from Server)
            DisplayUtils.show(viewById(R.id.custom_menu_group));
            DisplayUtils.hide(viewById(R.id.main_menu_group));
            MainMenuConfigManager config = new MainMenuConfigManager(getActivity(), configService,
                    (ViewGroup) getRootView());
            config.createMenu();
            syncFavoritesMenuItem.addAll(config.getViewsByType(ConfigConstants.VIEW_MODEL_SYNC));
        }
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
        if (currentAccount == null)
        {
            currentAccount = AccountsPreferences.getDefaultAccount(getActivity());
            if (currentAccount == null) { return; }
        }

        List<AlfrescoAccount> list = AlfrescoAccountManager.retrieveAccounts(getActivity());
        // We add all extra parameters at the end of the list.
        if (currentAccount.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CLOUD)
        {
            list.add(new AlfrescoAccount(AccountsAdapter.NETWORK_ITEM, getString(R.string.cloud_networks_switch), null,
                    null, null, null, "0", null, "false"));
        }

        // We add profiles management option if at least there's 2 profiles
        // available
        if (configManager != null && configManager.getConfig(currentAccount.getId()) != null
                && configManager.getConfig(currentAccount.getId()).getProfiles().size() > 1)
        {
            // If MDM enforce a specific profile
            String mdmProfile = null;
            if (mdmManager != null && mdmManager.hasConfig())
            {
                mdmProfile = mdmManager.getProfile();
            }

            if (mdmProfile == null || mdmProfile.isEmpty())
            {
                list.add(new AlfrescoAccount(AccountsAdapter.PROFILES_ITEM, getString(R.string.profiles_switch), null,
                        null, null, null, "0", null, "false"));
            }
            else if (mdmProfile != null && !mdmProfile.equals(configManager.getCurrentProfileId()))
            {
                configManager.swapProfile(currentAccount, mdmManager.getProfile());
            }
        }

        list.add(new AlfrescoAccount(AccountsAdapter.MANAGE_ITEM, getString(R.string.manage_accounts), null, null, null,
                null, "0", null, "false"));

        // Init the adapter and create the menu
        if (accountsAdapter == null)
        {
            accountsAdapter = new AccountsAdapter(getActivity(), list, R.layout.row_single_line, null);
        }
        else
        {
            accountsAdapter.clear();
            accountsAdapter.addAll(list);
        }

        accountsAdapter.setNotifyOnChange(false);

        ((TextView) accountsSpinnerButton.findViewById(R.id.accounts_spinner_title)).setText(currentAccount.getTitle());
        AccountsAdapter.displayAvatar(getActivity(), currentAccount, R.drawable.ic_account_light,
                ((ImageView) accountsSpinnerButton.findViewById(R.id.accounts_spinner_icon)));
        accountsSpinnerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (listPopupWindow != null)
                {
                    listPopupWindow.dismiss();
                    listPopupWindow = null;
                }
                else
                {
                    listPopupWindow = new ListPopupWindow(getActivity());
                    GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                            new int[] { 0xFF282828, 0xFF282828 });
                    gd.setCornerRadius(0f);
                    listPopupWindow.setBackgroundDrawable(gd);
                    listPopupWindow.setAnchorView(accountsSpinnerButton);
                    listPopupWindow.setAdapter(accountsAdapter);
                    listPopupWindow.setOnItemClickListener(MainMenuFragment.this);
                    listPopupWindow.setWidth(ListPopupWindow.WRAP_CONTENT);
                    listPopupWindow.show();
                }
            }
        });

        if (OperationsFragment.canDisplay(getActivity(), currentAccount))
        {
            show(R.id.menu_notifications);
            showOperationsMenu = true;
        }
        else
        {
            hide(R.id.menu_notifications);
            showOperationsMenu = false;
        }
        getActivity().invalidateOptionsMenu();
    }

    private void hideSlidingMenu(boolean goHome)
    {
        if (SLIDING_TAG.equals(getTag()))
        {
            if (goHome)
            {
                getFragmentManager().popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            if (getActivity() instanceof MainActivity)
            {
                ((MainActivity) getActivity()).hideSlideMenu();
                FragmentDisplayer.clearCentralPane(getActivity());
            }
        }
    }

    private void refreshConfiguration(Long accountId)
    {
        configManager = ConfigManager.getInstance(getActivity());
        if (configManager != null && configManager.hasConfig(accountId))
        {
            refresh();
            configure(configManager.getConfig(accountId));
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
        if (alfSession == null || alfSession instanceof CloudSession)
        {
            if (getRootView() != null)
            {
                hide(R.id.menu_browse_shared);
                hide(R.id.menu_browse_userhome);
            }
        }
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
            }
        }
    }

    public void displaySyncStatut()
    {
        // TODO Check refactoring for independent sync menu
        Cursor statutCursor = null;
        Drawable icon = getActivity().getResources().getDrawable(R.drawable.ic_sync_dark);
        Drawable statut = null;

        try
        {
            Boolean hasSynchroActive = SyncContentManager.getInstance(getActivity()).hasActivateSync(currentAccount);

            long startTimeStamp = SyncContentManager.getInstance(getActivity())
                    .getStartSyncPrepareTimestamp(currentAccount);
            long finalTimeStamp = SyncContentManager.getInstance(getActivity()).getSyncPrepareTimestamp(currentAccount);

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
                SyncScanInfo syncScanInfo = SyncScanInfo.getLastSyncScanData(getActivity(), currentAccount);
                if (syncScanInfo != null && syncScanInfo.hasWarning())
                {
                    // ==> Sync requires a user input
                    statut = getActivity().getResources().getDrawable(R.drawable.ic_warning_light);
                }
            }

            // Is there a doc warning ?
            if (hasSynchroActive && currentAccount != null)
            {
                statutCursor = getActivity().getContentResolver().query(SyncContentProvider.CONTENT_URI,
                        SyncContentSchema.COLUMN_ALL, SyncContentProvider.getAccountFilter(currentAccount) + " AND "
                                + SyncContentSchema.COLUMN_STATUS + " == " + SyncContentStatus.STATUS_REQUEST_USER,
                        null, null);
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

            menuFavorites.setCompoundDrawablesWithIntrinsicBounds(icon, null, statut, null);
            for (View v : syncFavoritesMenuItem)
            {
                if (v instanceof Button)
                {
                    ((Button) v).setCompoundDrawablesWithIntrinsicBounds(icon, null, statut, null);
                }
            }

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
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.clear();

        if (showOperationsMenu)
        {
            MenuItem mi = menu.add(Menu.NONE, R.id.menu_notifications, Menu.FIRST, R.string.notifications);
            mi.setIcon(R.drawable.ic_events_dark);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }

    public static void displayPreferences(FragmentActivity context, Long accountId)
    {
        if (DisplayUtils.hasCentralPane(context))
        {
            Intent i = new Intent(PrivateIntent.ACTION_DISPLAY_SETTINGS);
            i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, accountId);
            context.startActivityForResult(i, RequestCode.SETTINGS);
        }
        else
        {
            GeneralPreferences.with(context).display();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onAccountLoaded(LoadAccountCompletedEvent event)
    {
        currentAccount = event.account;
        refresh();
    }

    @Subscribe
    public void onAvatarLoaded(AvatarEvent event)
    {
        refresh();
    }

    @Subscribe
    public void onConfigureMenuEvent(ConfigurationMenuEvent event)
    {
        if (event.accountId >= 0)
        {
            refreshConfiguration(event.accountId);
        }
    }

    @Subscribe
    public void onConfigureProfileEvent(ConfigManager.ConfigurationProfileEvent event)
    {
        configManager = ConfigManager.getInstance(getActivity());
        refresh();
        MainMenuConfigManager config = new MainMenuConfigManager(getActivity(),
                configManager.getConfig(event.accountId), (ViewGroup) getRootView());
        config.createMenu();
    }

    @Subscribe
    public void onSyncCompleted(SyncContentScanEvent event)
    {
        displaySyncStatut();
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
            this.hasBackStack = false;
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
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
        }
    }

}
