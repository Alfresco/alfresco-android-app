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
package org.alfresco.mobile.android.application.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.services.ServiceRegistry;
import org.alfresco.mobile.android.api.services.impl.AlfrescoServiceRegistry;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.AccountOAuthHelper;
import org.alfresco.mobile.android.application.capture.DeviceCapture;
import org.alfresco.mobile.android.application.capture.DeviceCaptureHelper;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.fragments.accounts.AccountEditFragment;
import org.alfresco.mobile.android.application.fragments.accounts.AccountOAuthFragment;
import org.alfresco.mobile.android.application.fragments.accounts.AccountTypesFragment;
import org.alfresco.mobile.android.application.fragments.accounts.AccountsFragment;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.builder.FragmentBuilderFactory;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.help.HelpDialogFragment;
import org.alfresco.mobile.android.application.fragments.menu.MainMenuFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.fragments.sync.SyncFragment;
import org.alfresco.mobile.android.application.intent.AlfrescoIntentAPI;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.application.security.DataProtectionUserDialogFragment;
import org.alfresco.mobile.android.async.account.CreateAccountEvent;
import org.alfresco.mobile.android.async.configuration.ConfigurationEvent;
import org.alfresco.mobile.android.async.file.encryption.AccountProtectionEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountCompletedEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountErrorEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountStartedEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadInactiveAccountEvent;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.async.session.oauth.RetrieveOAuthDataEvent;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AccountsPreferences;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.ScanSnapManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.ui.RefreshFragment;
import org.alfresco.mobile.android.ui.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.ui.node.browse.NodeBrowserTemplate;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.squareup.otto.Subscribe;

/**
 * Main activity of the application.
 * 
 * @author Jean Marie Pascal
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MainActivity extends BaseActivity
{
    private static final String TAG = MainActivity.class.getName();

    // SESSION FLAG
    private static final int SESSION_LOADING = 1;

    private static final int SESSION_ACTIVE = 2;

    private static final int SESSION_INACTIVE = 4;

    private static final int SESSION_ERROR = 8;

    private int sessionState = 0;

    private int sessionStateErrorMessageId;

    private Folder importParent;

    private Node currentNode;

    // Device capture (made static as we don't seem to be getting instance state
    // back through creation).
    private static DeviceCapture capture = null;

    private int fragmentQueue = -1;

    // SLIDING MENU
    private static DrawerLayout mDrawerLayout;

    private ViewGroup mDrawer;

    /** Used to display the shortcut folder during app first init. */
    private String shortcutFolderId;

    private static ActionBarDrawerToggle mDrawerToggle;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate(savedInstanceState);

        // Loading progress
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.app_main);

        if (capture != null) capture.setActivity(this);

        if (savedInstanceState != null)
        {
            MainActivityHelper helper = new MainActivityHelper(savedInstanceState.getBundle(MainActivityHelper.TAG));
            currentAccount = helper.getCurrentAccount();
            importParent = helper.getFolder();
            fragmentQueue = helper.getFragmentQueue();

            if (helper.getDeviceCapture() != null)
            {
                capture = helper.getDeviceCapture();
                capture.setActivity(this);
            }
        }
        else
        {
            MainMenuFragment.with(this).display();
        }

        if (SessionUtils.getAccount(this) != null)
        {
            currentAccount = SessionUtils.getAccount(this);
            if (currentAccount.getIsPaidAccount()
                    && !prefs.getBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false))
            {
                // Check if we've prompted the user for Data Protection yet.
                // This is needed on new AlfrescoAccount creation, as the
                // Activity gets
                // re-created after the AlfrescoAccount is created.
                DataProtectionUserDialogFragment.newInstance(true).show(getFragmentManager(),
                        DataProtectionUserDialogFragment.TAG);

                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();
            }
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (ViewGroup) findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.open_in,
                R.string.cancel)
        {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view)
            {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView)
            {
                MainMenuFragment slidefragment = (MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG);
                if (slidefragment != null)
                {
                    slidefragment.refreshData();
                }
                invalidateOptionsMenu();
                super.onDrawerOpened(drawerView);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (AndroidVersion.isICSOrAbove())
        {
            getActionBar().setHomeButtonEnabled(true);
        }

        setProgressBarIndeterminateVisibility((getCurrentAccount() == null && getCurrentSession() == null));

        // Is it from a shortcut ?
        if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && PrivateIntent.NODE_TYPE.equals(getIntent().getType())
                && getIntent().getExtras().containsKey(PrivateIntent.EXTRA_FOLDER_ID))
        {
            shortcutFolderId = getIntent().getStringExtra(PrivateIntent.EXTRA_FOLDER_ID);
            getIntent().removeExtra(PrivateIntent.EXTRA_FOLDER_ID);
            if (getCurrentSession() != null)
            {
                DocumentFolderBrowserFragment.with(this).folderIdentifier(shortcutFolderId).shortcut(true).display();
                shortcutFolderId = null;
            }
        }
    }

    @Override
    protected void onStart()
    {
        registerPublicReceiver(new NetworkReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        super.onStart();
        AccountOAuthHelper.requestRefreshToken(getCurrentSession(), this);
        FavoritesSyncManager.getInstance(this).cronSync(currentAccount);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        checkSession();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        FavoritesSyncManager.getInstance(this).saveSyncPrepareTimestamp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.DECRYPTED)
        {
            File requiredProtectionFile = DataProtectionManager.getInstance(this).getRequiredDataProtectionFile();
            if (!FavoritesSyncManager.getInstance(this).isSyncFile(requiredProtectionFile))
            {
                DataProtectionManager.getInstance(this).checkEncrypt(getCurrentAccount(), requiredProtectionFile);
            }
        }

        if (capture != null && requestCode == capture.getRequestCode())
        {
            capture.capturedCallback(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        try
        {
            // Shortcut to display favorites panel.
            if (PrivateIntent.ACTION_SYNCHRO_DISPLAY.equals(intent.getAction()))
            {
                if (!isVisible(SyncFragment.TAG))
                {
                    SyncFragment.with(this).display();
                }
                return;
            }

            // Intent for Scan result
            // Only associated with DocumentFolderBrowserFragment
            if (PrivateIntent.ACTION_SCAN_RESULT.equals(intent.getAction()))
            {
                if (getFragment(DocumentFolderBrowserFragment.TAG) != null && intent.getExtras() != null)
                {
                    ArrayList<String> tempList = intent.getStringArrayListExtra(PrivateIntent.EXTRA_FILE_PATH);
                    if (tempList == null) { return; }
                    List<File> files = new ArrayList<File>(tempList.size());
                    int nCnt;
                    for (nCnt = tempList.size(); nCnt > 0; nCnt--)
                    {
                        files.add(new File(tempList.get(nCnt - 1)));
                    }
                    ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG)).createFiles(files);
                }
                return;
            }

            //
            if (AlfrescoIntentAPI.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null)
            {
                if (AlfrescoIntentAPI.AUTHORITY_FOLDER.equals(intent.getData().getAuthority()))
                {
                    DocumentFolderBrowserFragment.with(this)
                            .folderIdentifier(intent.getData().getPathSegments().get(0)).shortcut(true).display();
                }
                else if (AlfrescoIntentAPI.AUTHORITY_FILE.equals(intent.getData().getAuthority()))
                {
                    FileExplorerFragment.with(this).file(new File(intent.getData().getPathSegments().get(0))).display();
                }
                return;
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBundle(MainActivityHelper.TAG,
                MainActivityHelper.createBundle(outState, currentAccount, capture, fragmentQueue, importParent));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SLIDE MENU
    // ///////////////////////////////////////////////////////////////////////////
    public void toggleSlideMenu()
    {
        if (getFragment(MainMenuFragment.TAG) != null && getFragment(MainMenuFragment.TAG).isAdded()) { return; }
        if (isSlideMenuVisible())
        {
            hideSlideMenu();
        }
        else
        {
            showSlideMenu();
        }
    }

    public void showSlideMenu()
    {
        mDrawerLayout.openDrawer(mDrawer);
    }

    public void hideSlideMenu()
    {
        mDrawerLayout.closeDrawer(mDrawer);
    }

    private boolean isSlideMenuVisible()
    {
        return mDrawerLayout.isDrawerOpen(mDrawer);
    }

    public void lockSlidingMenu()
    {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void unlockSlidingMenu()
    {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void doMainMenuAction(int id)
    {
        hideSlideMenu();
        FragmentDisplayer.clearCentralPane(this);

        String type = "";
        Bundle b = null;
        fragmentQueue = -1;
        switch (id)
        {
            case R.id.menu_browse_my_sites:
                if (!checkSession(R.id.menu_browse_my_sites)) { return; }
                type = ConfigurationConstant.KEY_SITES;
                break;
            case R.id.menu_browse_root:
                if (!checkSession(R.id.menu_browse_root) || getCurrentSession() == null) { return; }
                type = ConfigurationConstant.KEY_REPOSITORY;
                break;
            case R.id.menu_browse_shared:
                if (!checkSession(R.id.menu_browse_shared)) { return; }
                type = ConfigurationConstant.KEY_REPOSITORY;
                b = new Bundle();
                b.putString(NodeBrowserTemplate.ARGUMENT_FOLDER_TYPE_ID, NodeBrowserTemplate.FOLDER_TYPE_SHARED);
                break;
            case R.id.menu_browse_userhome:
                if (!checkSession(R.id.menu_browse_userhome)) { return; }
                type = ConfigurationConstant.KEY_REPOSITORY;
                b = new Bundle();
                b.putString(NodeBrowserTemplate.ARGUMENT_FOLDER_TYPE_ID, NodeBrowserTemplate.FOLDER_TYPE_USERHOME);
                break;
            case R.id.menu_browse_activities:
                if (!checkSession(R.id.menu_browse_activities)) { return; }
                type = ConfigurationConstant.KEY_ACTIVITIES;
                break;
            case R.id.menu_search:
                if (!checkSession(R.id.menu_search)) { return; }
                type = ConfigurationConstant.KEY_SEARCH;
                break;
            case R.id.menu_favorites:
                type = ConfigurationConstant.KEY_FAVORITES;
                break;
            case R.id.menu_workflow:
                if (!checkSession(R.id.menu_workflow)) { return; }
                type = ConfigurationConstant.KEY_TASKS;
                break;
            case R.id.menu_downloads:
                if (currentAccount == null)
                {
                    AlfrescoNotificationManager.getInstance(this).showLongToast(getString(R.string.loginfirst));
                }
                else
                {
                    type = ConfigurationConstant.KEY_LOCALFILES;
                }
                break;
            case R.id.menu_notifications:
                if (currentAccount == null)
                {
                    AlfrescoNotificationManager.getInstance(this).showLongToast(getString(R.string.loginfirst));
                }
                else
                {
                    startActivity(new Intent(PrivateIntent.ACTION_DISPLAY_OPERATIONS).putExtra(
                            PrivateIntent.EXTRA_ACCOUNT_ID, currentAccount.getId()));
                }
                break;
            default:
                break;
        }

        if (type != null)
        {
            AlfrescoFragmentBuilder viewConfig = FragmentBuilderFactory.createViewConfig(this, type, null);
            if (viewConfig == null) { return; }
            if (b != null)
            {
                viewConfig.addExtra(b);
            }
            viewConfig.display();
        }

    }

    public void showMainMenuFragment(View v)
    {
        doMainMenuAction(v.getId());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SESSION MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public void setSessionState(int state)
    {
        sessionState = state;
    }

    public void setSessionErrorMessageId(int messageId)
    {
        sessionState = SESSION_ERROR;
        sessionStateErrorMessageId = messageId;
    }

    private void checkSession()
    {
        if (AlfrescoAccountManager.getInstance(this).isEmpty() && AlfrescoAccountManager.getInstance(this).hasData())
        {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }
        else if (getCurrentAccount() == null && getCurrentSession() == null)
        {
            sessionManager.loadSession();
        }
        else if (sessionState == SESSION_ERROR && getCurrentSession() == null
                && ConnectivityUtils.hasInternetAvailable(this))
        {
            sessionManager.loadSession(getCurrentAccount());
        }
        invalidateOptionsMenu();
        setProgressBarIndeterminateVisibility(getCurrentSession() == null);
    }

    private boolean checkSession(int actionMainMenuId)
    {
        switch (sessionState)
        {
            case SESSION_ERROR:
                Bundle b = new Bundle();
                b.putInt(SimpleAlertDialogFragment.ARGUMENT_ICON, R.drawable.ic_alfresco_logo);
                b.putInt(SimpleAlertDialogFragment.ARGUMENT_TITLE, R.string.error_session_creation_message);
                b.putInt(SimpleAlertDialogFragment.ARGUMENT_MESSAGE, sessionStateErrorMessageId);
                b.putInt(SimpleAlertDialogFragment.ARGUMENT_POSITIVE_BUTTON, android.R.string.ok);
                ActionUtils.actionDisplayDialog(this, b);
                return false;
            case SESSION_LOADING:
                displayWaitingDialog();
                fragmentQueue = actionMainMenuId;
                return false;
            default:
                if (!ConnectivityUtils.hasNetwork(this))
                {
                    return false;
                }
                else if (getCurrentAccount() != null && getCurrentAccount().getActivation() != null)
                {
                    AlfrescoNotificationManager.getInstance(this).showToast(R.string.account_not_activated);
                    return false;
                }
                break;
        }
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FRAGMENTS
    // ///////////////////////////////////////////////////////////////////////////
    public void displayPreferences()
    {
        if (DisplayUtils.hasCentralPane(this))
        {
            Intent i = new Intent(PrivateIntent.ACTION_DISPLAY_SETTINGS);
            i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getCurrentAccount().getId());
            startActivity(i);
        }
        else
        {
            GeneralPreferences.with(this).display();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION BAR
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (sessionState == SESSION_ERROR && getCurrentSession() == null)
        {
            MenuItem mi = menu.add(Menu.NONE, R.id.menu_account_reload, Menu.FIRST, R.string.retry_account_loading);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        if (isSlideMenuVisible() || isVisible(MainMenuFragment.TAG))
        {
            MainMenuFragment.getMenu(menu);
            return true;
        }

        if (isVisible(AccountsFragment.TAG) && !isVisible(AccountTypesFragment.TAG)
                && !isVisible(AccountEditFragment.TAG) && !isVisible(AccountOAuthFragment.TAG))
        {
            AccountsFragment.getMenu(this, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mDrawerToggle.onOptionsItemSelected(item)) { return true; }

        switch (item.getItemId())
        {
            case R.id.menu_account_reload:
                sessionManager.loadSession(getCurrentAccount());
                return true;
            case R.id.menu_device_capture_camera_photo:
            case R.id.menu_device_capture_camera_video:
            case R.id.menu_device_capture_mic_audio:
                capture = DeviceCaptureHelper.createDeviceCapture(this, item.getItemId());
                return true;

            case R.id.menu_scan_document:
                if (ScanSnapManager.getInstance(this) != null)
                {
                    ScanSnapManager.getInstance(this).startPresetChooser(this);
                }
                return true;
            case R.id.menu_refresh:
                if (getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(this)) instanceof RefreshFragment)
                {
                    ((RefreshFragment) getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(this)))
                            .refresh();
                }
                return true;
            case R.id.menu_settings:
                displayPreferences();
                hideSlideMenu();
                return true;
            case R.id.menu_help:
                HelpDialogFragment.displayHelp(this);
                hideSlideMenu();
                return true;
            case R.id.menu_about:
                AboutFragment.with(this).display();
                hideSlideMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (isSlideMenuVisible())
        {
            hideSlideMenu();
        }
        else
        {
            super.onBackPressed();
        }

        if (DisplayUtils.hasCentralPane(this))
        {
            invalidateOptionsMenu();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public Node getCurrentNode()
    {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode)
    {
        this.currentNode = currentNode;
    }

    // For Creating file in childrenbrowser
    public Folder getImportParent()
    {
        if (getFragment(DocumentFolderBrowserFragment.TAG) != null)
        {
            importParent = ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG))
                    .getImportFolder();
            if (importParent == null)
            {
                importParent = ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG))
                        .getParent();
            }
        }
        return importParent;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onRetrieveOAuthDataEvent(RetrieveOAuthDataEvent event)
    {
        AccountOAuthHelper.onNewOauthData(this, event);
    }

    @Subscribe
    public void onAccountCreated(CreateAccountEvent event)
    {
        if (event.hasException) { return; }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        AlfrescoAccount tmpAccount = AlfrescoAccountManager.getInstance(this).retrieveAccount(event.data.getId());
        if (tmpAccount.getIsPaidAccount() && !prefs.getBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false))
        {
            DataProtectionUserDialogFragment.newInstance(true).show(getFragmentManager(),
                    DataProtectionUserDialogFragment.TAG);
            prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();
        }
        return;
    }

    @Subscribe
    public void onSessionRequested(RequestSessionEvent event)
    {
        // Change activity state to loading.
        setSessionState(SESSION_LOADING);

        // Assign the account
        currentAccount = event.accountToLoad;

        if (getFragment(MainMenuFragment.TAG) != null)
        {
            ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).displayFavoriteStatut();
            ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).hideWorkflowMenu(currentAccount);
        }

        if (getFragment(MainMenuFragment.SLIDING_TAG) != null)
        {
            ((MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG)).displayFavoriteStatut();
            ((MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG)).hideWorkflowMenu(currentAccount);
        }

        // Return to root screen
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Display progress
        setProgressBarIndeterminateVisibility(true);

        // Add accountName in actionBar
        UIUtils.displayTitle(this, getString(R.string.app_name));
        // getActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Subscribe
    public void onAccountLoading(LoadAccountStartedEvent event)
    {
        setSessionState(SESSION_LOADING);
        setProgressBarIndeterminateVisibility(true);
        setCurrentAccount(event.account);
        if (event != null)
        {
            AlfrescoNotificationManager.getInstance(this).showLongToast(event.account.getTitle());
        }
    }

    @Subscribe
    public void onAccountLoaded(LoadAccountCompletedEvent event)
    {
        if (getCurrentSession() instanceof RepositorySession)
        {
            ServiceRegistry registry = getCurrentSession().getServiceRegistry();
            if (registry instanceof AlfrescoServiceRegistry)
            {
                ConfigManager config = ConfigManager.getInstance(this);
                if (!config.hasConfig(getCurrentAccount().getId()))
                {
                    config.init(getCurrentAccount());
                }

                // Check configuration
                if (((AlfrescoServiceRegistry) registry).getConfigService() == null)
                {
                    // In this case there's no configuration defined on server
                    // We remove any cached configuration
                    ConfigManager.getInstance(this).cleanCache(getCurrentAccount());
                }
                else
                {
                    config.loadRemote(getCurrentAccount().getId(),
                            ((AlfrescoServiceRegistry) registry).getConfigService());
                }

                config.setSession(getCurrentAccount().getId(), getCurrentSession());
            }

            // Display 4.2+ special folders (userhome/shared)
            if (getFragment(MainMenuFragment.TAG) != null)
            {
                ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).displayFolderShortcut(getCurrentSession());
            }

            if (getFragment(MainMenuFragment.SLIDING_TAG) != null)
            {
                ((MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG))
                        .displayFolderShortcut(getCurrentSession());
            }
        }

        if (!isCurrentAccountToLoad(event)) { return; }

        setSessionState(SESSION_ACTIVE);
        setProgressBarIndeterminateVisibility(false);
        UIUtils.displayTitle(this, getString(R.string.app_name), false);

        // Retrieve Rendition Manager associated to this account
        RenditionManagerImpl.getInstance(this).setSession(getCurrentSession());

        // Remove OAuthFragment if one
        if (getFragment(AccountOAuthFragment.TAG) != null)
        {
            getFragmentManager().popBackStack(AccountOAuthFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        removeWaitingDialog();

        // Used for launching last pressed action button from main menu
        if (shortcutFolderId != null)
        {
            DocumentFolderBrowserFragment.with(this).folderIdentifier(shortcutFolderId).shortcut(true).display();
            shortcutFolderId = null;
        }
        else if (fragmentQueue != -1)
        {
            doMainMenuAction(fragmentQueue);
        }
        fragmentQueue = -1;

        // Save latest position as default future one
        AccountsPreferences.setDefaultAccount(this, currentAccount.getId());

        // Check Last cloud session creation ==> prevent oauth token
        // expiration
        if (getCurrentSession() instanceof CloudSession)
        {
            AccountOAuthHelper.saveLastCloudLoadingTime(this);
        }
        else
        {
            AccountOAuthHelper.removeLastCloudLoadingTime(this);
        }

        // NB : temporary code ?
        // Check to see if we have an old AlfrescoAccount that needs its paid
        // network flag setting.
        if (!currentAccount.getIsPaidAccount())
        {
            boolean paidNetwork = false;
            if (getCurrentSession() instanceof CloudSession)
            {
                paidNetwork = ((CloudSession) getCurrentSession()).getNetwork().isPaidNetwork();
            }
            else
            {
                paidNetwork = getCurrentSession().getRepositoryInfo().getEdition()
                        .equals(OnPremiseConstant.ALFRESCO_EDITION_ENTERPRISE);
            }

            if (paidNetwork)
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();

                DataProtectionUserDialogFragment.newInstance(true).show(getFragmentManager(),
                        DataProtectionUserDialogFragment.TAG);

                currentAccount = AlfrescoAccountManager.getInstance(this).update(currentAccount.getId(),
                        currentAccount.getTitle(), currentAccount.getUrl(), currentAccount.getUsername(),
                        currentAccount.getPassword(), currentAccount.getRepositoryId(), currentAccount.getTypeId(),
                        currentAccount.getActivation(), currentAccount.getAccessToken(),
                        currentAccount.getRefreshToken(), 1);
            }
        }

        // Start Sync if active
        if (FavoritesSyncManager.getInstance(this).hasDisplayedActivateSync())
        {
            FavoritesSyncManager.getInstance(this).sync(currentAccount);
        }
        return;
    }

    @Subscribe
    public void onCloudAccountErrorEvent(LoadSessionCallBack.CloudAccountErrorEvent event)
    {
        if (currentAccount == null || currentAccount.getId() != event.data) { return; }

        //Display OAuth Authentication
        AccountOAuthFragment.with(MainActivity.this).account(event.account).isCreation(false).display();

        // Stop progress indication
        setProgressBarIndeterminateVisibility(false);

        invalidateOptionsMenu();
    }

    @Subscribe
    public void onAccountErrorEvent(LoadAccountErrorEvent event)
    {
        if (currentAccount == null || currentAccount.getId() != event.data) { return; }

        // Display error dialog message
        // TODO Display Errors!
        // ActionManager.actionDisplayDialog(this, intent.getExtras());

        // Change status
        setSessionErrorMessageId(event.messageId);

        // Reset currentAccount & references
        currentAccount = AlfrescoAccountManager.getInstance(this).retrieveAccount(event.data);
        sessionManager.removeAccount(currentAccount.getId());

        // Stop progress indication
        setProgressBarIndeterminateVisibility(false);

        invalidateOptionsMenu();
    }

    @Subscribe
    public void onAccountInactiveEvent(LoadInactiveAccountEvent event)
    {
        if (currentAccount.getId() != event.account.getId()) { return; }

        setSessionState(SESSION_INACTIVE);
        setProgressBarIndeterminateVisibility(false);
        AlfrescoNotificationManager.getInstance(this).showLongToast(getString(R.string.account_not_activated));
        return;
    }

    @Subscribe
    public void onAccountProtectionEvent(AccountProtectionEvent event)
    {
        removeWaitingDialog();
        if (getFragment(GeneralPreferences.TAG) != null)
        {
            ((GeneralPreferences) getFragment(GeneralPreferences.TAG)).refreshDataProtection();
        }
    }

    @Subscribe
    public void onConfigContextEvent(ConfigurationEvent event)
    {
        removeWaitingDialog();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    public class NetworkReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            try
            {
                if (ConnectivityUtils.hasInternetAvailable(context))
                {
                    if (sessionState == SESSION_ERROR && getCurrentAccount() == null && getCurrentSession() == null)
                    {
                        SessionManager.getInstance(MainActivity.this).loadSession();
                    }
                    else if (sessionState == SESSION_ERROR && getCurrentSession() == null)
                    {
                        SessionManager.getInstance(MainActivity.this).loadSession(getCurrentAccount());
                    }
                    invalidateOptionsMenu();
                }
            }
            catch (Exception e)
            {
                // Nothing special
            }
        }
    }

    // Due to dropdown the AlfrescoAccount loaded might not be the last one to
    // load.
    private boolean isCurrentAccountToLoad(Intent intent)
    {
        if (currentAccount == null) { return false; }
        if (!intent.hasExtra(PrivateIntent.EXTRA_ACCOUNT_ID)) { return false; }
        return (currentAccount.getId() == intent.getExtras().getLong(PrivateIntent.EXTRA_ACCOUNT_ID));
    }

    private boolean isCurrentAccountToLoad(LoadAccountCompletedEvent event)
    {
        if (currentAccount == null) { return false; }
        if (event == null) { return false; }
        return (currentAccount.getId() == event.account.getId());
    }
}
