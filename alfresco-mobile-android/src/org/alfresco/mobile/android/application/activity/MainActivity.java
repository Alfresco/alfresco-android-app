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

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.AccountOAuthHelper;
import org.alfresco.mobile.android.application.capture.DeviceCapture;
import org.alfresco.mobile.android.application.capture.DeviceCaptureHelper;
import org.alfresco.mobile.android.application.config.ConfigManager;
import org.alfresco.mobile.android.application.config.async.ConfigurationEvent;
import org.alfresco.mobile.android.application.configuration.manager.ConfigurationConstant;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.fragments.accounts.AccountDetailsFragment;
import org.alfresco.mobile.android.application.fragments.accounts.AccountEditFragment;
import org.alfresco.mobile.android.application.fragments.accounts.AccountOAuthFragment;
import org.alfresco.mobile.android.application.fragments.accounts.AccountTypesFragment;
import org.alfresco.mobile.android.application.fragments.accounts.AccountsFragment;
import org.alfresco.mobile.android.application.fragments.accounts.CloudSignupDialogFragment;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.builder.FragmentBuilderFactory;
import org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.help.HelpDialogFragment;
import org.alfresco.mobile.android.application.fragments.menu.MainMenuFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.node.rendition.GalleryPreviewFragment;
import org.alfresco.mobile.android.application.fragments.person.UserProfileFragment;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.fragments.search.SearchFragment;
import org.alfresco.mobile.android.application.fragments.site.browser.BrowserSitesFragment;
import org.alfresco.mobile.android.application.fragments.sync.SyncFragment;
import org.alfresco.mobile.android.application.fragments.workflow.process.ProcessesFragment;
import org.alfresco.mobile.android.application.fragments.workflow.task.TaskDetailsFragment;
import org.alfresco.mobile.android.application.fragments.workflow.task.TasksFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.application.security.DataProtectionUserDialogFragment;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.CreateAccountEvent;
import org.alfresco.mobile.android.async.file.encryption.AccountProtectionEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountCompletedEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountErrorEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountStartedEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadInactiveAccountEvent;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.async.session.oauth.RetrieveOAuthDataEvent;
import org.alfresco.mobile.android.async.session.oauth.RetrieveOAuthDataRequest;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AccountsPreferences;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.ui.RefreshFragment;
import org.alfresco.mobile.android.ui.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.ui.node.browse.NodeBrowserTemplate;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.annotation.TargetApi;
import android.app.Activity;
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

    private static ActionBarDrawerToggle mDrawerToggle;

    private Intent callBackIntent = null;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
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

        // TODO move elsewhere ?
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

        // REDIRECT To Accounts Fragment if signup process
        if (PrivateIntent.ACTION_CHECK_SIGNUP.equals(getIntent().getAction()))
        {
            AccountsFragment.with(this).display();
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
        getActionBar().setHomeButtonEnabled(true);

        setProgressBarIndeterminateVisibility((getCurrentAccount() == null && getCurrentSession() == null));
        // Check if there is a Fujistu scanner intent coming back to us.
        checkScan();
    }

    @Override
    protected void onStart()
    {
        IntentFilter filters = new IntentFilter();
        filters.addAction(PrivateIntent.ACTION_USER_AUTHENTICATION);
        filters.addCategory(PrivateIntent.CATEGORY_OAUTH);
        filters.addCategory(PrivateIntent.CATEGORY_OAUTH_REFRESH);

        registerPrivateReceiver(new MainActivityReceiver(), filters);
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

            // Intent for CLOUD SIGN UP
            if (PrivateIntent.ACTION_CHECK_SIGNUP.equals(intent.getAction()))
            {
                FragmentDisplayer.with(this).remove(CloudSignupDialogFragment.TAG);
                AccountsFragment.with(this).display();
                return;
            }

            if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null
                    && intent.getData().getHost().equals("activate-cloud-account")
                    && getFragment(AccountDetailsFragment.TAG) != null)
            {

                ((AccountDetailsFragment) getFragment(AccountDetailsFragment.TAG)).displayOAuthFragment();
                return;
            }

            //
            if (Intent.ACTION_VIEW.equals(intent.getAction()) && PrivateIntent.NODE_TYPE.equals(intent.getType()))
            {
                if (intent.getExtras().containsKey(PrivateIntent.EXTRA_NODE))
                {
                    NodeDetailsFragment.with(this).node((Document) intent.getExtras().get(PrivateIntent.EXTRA_NODE))
                            .display();
                }
                return;
            }

            // Intent for display Sign up Dialog
            if (Intent.ACTION_VIEW.equals(intent.getAction())
                    && PrivateIntent.ALFRESCO_SCHEME_SHORT.equals(intent.getData().getScheme())
                    && PrivateIntent.CLOUD_SIGNUP_I.equals(intent.getData().getHost()))
            {
                getFragmentManager().popBackStack(AccountTypesFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                CloudSignupDialogFragment.with(this).display();
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
            startActivity(new Intent(this, HomeScreenActivity.class));
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
            MenuItem mi = menu
                    .add(Menu.NONE, MenuActionItem.ACCOUNT_RELOAD, Menu.FIRST, R.string.retry_account_loading);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        if (isSlideMenuVisible() || isVisible(MainMenuFragment.TAG))
        {
            MainMenuFragment.getMenu(menu);
            return true;
        }

        if (isVisible(TaskDetailsFragment.TAG))
        {
            ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(ProcessesFragment.TAG))
        {
            ((ProcessesFragment) getFragment(ProcessesFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(TasksFragment.TAG))
        {
            TasksFragment.getMenu(this, menu);
            return true;
        }

        if (isVisible(AccountDetailsFragment.TAG))
        {
            ((AccountDetailsFragment) getFragment(AccountDetailsFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(AccountsFragment.TAG) && !isVisible(AccountTypesFragment.TAG)
                && !isVisible(AccountEditFragment.TAG) && !isVisible(AccountOAuthFragment.TAG))
        {
            AccountsFragment.getMenu(this, menu);
            return true;
        }

        if (isVisible(BrowserSitesFragment.TAG))
        {
            BrowserSitesFragment.getMenu(this, menu);
            return true;
        }

        if (isVisible(DocumentFolderBrowserFragment.TAG))
        {
            ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(SyncFragment.TAG))
        {
            ((SyncFragment) getFragment(SyncFragment.TAG)).getMenu(menu);
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
            case MenuActionItem.ACCOUNT_RELOAD:
                sessionManager.loadSession(getCurrentAccount());
                return true;
            case MenuActionItem.MENU_PROFILE:
                UserProfileFragment.with(this).personId(getCurrentAccount().getUsername()).displayAsDialog();
                return true;

            case MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_PHOTO:
            case MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_VIDEO:
            case MenuActionItem.MENU_DEVICE_CAPTURE_MIC_AUDIO:
            case MenuActionItem.MENU_DEVICE_SCAN_DOCUMENT:
                capture = DeviceCaptureHelper.createDeviceCapture(this, item.getItemId());
                return true;

            case MenuActionItem.MENU_ACCOUNT_ADD:
                ((AccountsFragment) getFragment(AccountsFragment.TAG)).add();
                return true;

            case MenuActionItem.MENU_ACCOUNT_EDIT:
                ((AccountDetailsFragment) getFragment(AccountDetailsFragment.TAG)).edit();
                return true;

            case MenuActionItem.MENU_ACCOUNT_DELETE:
                ((AccountDetailsFragment) getFragment(AccountDetailsFragment.TAG)).delete();
                return true;

            case MenuActionItem.MENU_SEARCH_FOLDER:
                ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG)).search();
                return true;

            case MenuActionItem.MENU_SEARCH:
                SearchFragment.with(this).display();
                return true;

            case MenuActionItem.MENU_CREATE_FOLDER:
                if (getFragment(DocumentFolderBrowserFragment.TAG) != null)
                {
                    ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG)).createFolder();
                    return true;
                }
                else
                {
                    return false;
                }
            case MenuActionItem.MENU_CREATE_DOCUMENT:
                String fragmentTag = FileExplorerFragment.TAG;
                if (getFragment(DocumentFolderBrowserFragment.TAG) != null)
                {
                    fragmentTag = DocumentFolderBrowserFragment.TAG;
                }
                DocumentTypesDialogFragment dialogft = DocumentTypesDialogFragment.newInstance(currentAccount,
                        fragmentTag);
                dialogft.show(getFragmentManager(), DocumentTypesDialogFragment.TAG);
                return true;

            case MenuActionItem.MENU_UPLOAD:
                if (getFragment(DocumentFolderBrowserFragment.TAG) != null)
                {
                    Intent i = new Intent(PrivateIntent.ACTION_PICK_FILE, null, this, PublicDispatcherActivity.class);
                    i.putExtra(PrivateIntent.EXTRA_FOLDER,
                            AlfrescoStorageManager.getInstance(this).getDownloadFolder(getCurrentAccount()));
                    i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getCurrentAccount().getId());
                    getFragment(DocumentFolderBrowserFragment.TAG).startActivityForResult(i, RequestCode.FILEPICKER);
                }
                return true;
            case MenuActionItem.MENU_REFRESH:
                if (getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(this)) instanceof RefreshFragment)
                {
                    ((RefreshFragment) getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(this)))
                            .refresh();
                }
                return true;
            case MenuActionItem.MENU_DISPLAY_GALLERY:
                GalleryPreviewFragment.with(this).display();
                return true;
            case MenuActionItem.MENU_SITE_LIST_REQUEST:
                ((BrowserSitesFragment) getFragment(BrowserSitesFragment.TAG)).displayJoinSiteRequests();
                return true;
            case MenuActionItem.MENU_TASK_REASSIGN:
                ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).reassign();
                return true;
            case MenuActionItem.MENU_TASK_CLAIM:
                ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).claim();
                return true;
            case MenuActionItem.MENU_PROCESS_HISTORY:
                ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).displayHistory();
                return true;
            case MenuActionItem.MENU_TASK_UNCLAIM:
                ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).unclaim();
                return true;
            case MenuActionItem.MENU_PROCESS_DETAILS:
                ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).showProcessDiagram();
                return true;
            case MenuActionItem.MENU_SYNC_WARNING:
                ((SyncFragment) getFragment(SyncFragment.TAG)).displayWarning();
                return true;
            case MenuActionItem.MENU_SETTINGS_ID:
                displayPreferences();
                hideSlideMenu();
                return true;
            case MenuActionItem.MENU_HELP_ID:
                HelpDialogFragment.displayHelp(this);
                hideSlideMenu();
                return true;
            case MenuActionItem.MENU_ABOUT_ID:
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
            // Check if we've prompted the user for Data Protection yet.
            // This is needed on new AlfrescoAccount creation, as the Activity
            // gets
            // re-created after the AlfrescoAccount is created.
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
        if (event != null)
        {
            AlfrescoNotificationManager.getInstance(this).showLongToast(event.account.getTitle());
        }
        return;
    }

    @Subscribe
    public void onAccountLoaded(LoadAccountCompletedEvent event)
    {
        if (getCurrentSession() instanceof RepositorySession)
        {
            if (ConfigManager.getInstance(this).load(getCurrentSession()))
            {
                displayWaitingDialog();
            }
            else
            {
                LoaderResult<ConfigService> result = new LoaderResult<ConfigService>();
                result.setData(getCurrentSession().getServiceRegistry().getConfigService());
                EventBusManager.getInstance().post(new ConfigurationEvent("", result, getCurrentAccount().getId()));
            }

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

        // Retrieve Rendition Manager associated to this account
        RenditionManagerImpl.getInstance(this).setSession(getCurrentSession());

        // Remove OAuthFragment if one
        if (getFragment(AccountOAuthFragment.TAG) != null)
        {
            getFragmentManager().popBackStack(AccountOAuthFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // removeWaitingDialog();

        // Used for launching last pressed action button from main menu
        if (fragmentQueue != -1)
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
    public void onAccountErrorEvent(LoadAccountErrorEvent event)
    {
        if (currentAccount.getId() != event.data) { return; }

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
    private class MainActivityReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getAction());

            Activity activity = MainActivity.this;

            if (PrivateIntent.ACTION_USER_AUTHENTICATION.equals(intent.getAction()))
            {
                if (!isCurrentAccountToLoad(intent)) { return; }

                if (!intent.hasExtra(PrivateIntent.EXTRA_ACCOUNT_ID)) { return; }
                Long accountId = intent.getExtras().getLong(PrivateIntent.EXTRA_ACCOUNT_ID);
                AlfrescoAccount acc = AlfrescoAccountManager.getInstance(activity).retrieveAccount(accountId);

                if (intent.getCategories().contains(PrivateIntent.CATEGORY_OAUTH)
                        && getFragment(AccountOAuthFragment.TAG) == null
                        || (getFragment(AccountOAuthFragment.TAG) != null && getFragment(AccountOAuthFragment.TAG)
                                .isAdded()))
                {
                    FragmentDisplayer.with(activity).load(AccountOAuthFragment.newInstance(context, acc))
                            .into(FragmentDisplayer.PANEL_CENTRAL);
                    return;
                }

                if (intent.getCategories().contains(PrivateIntent.CATEGORY_OAUTH_REFRESH))
                {
                    Operator.with(activity).load(
                            new RetrieveOAuthDataRequest.Builder((CloudSession) getCurrentSession()));
                    return;
                }
                return;
            }

            return;
        }
    }

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

    private void checkScan()
    {
        callBackIntent = getIntent();

        if (callBackIntent != null && callBackIntent.getScheme() != null
                && callBackIntent.getScheme().compareTo("alfrescoFujitsuScanCallback") == 0)
        {
            if (capture != null)
            {
                capture.capturedCallback(capture.getRequestCode(), Activity.RESULT_OK, callBackIntent);
            }
        }
    }
}
