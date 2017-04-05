/*******************************************************************************
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.capture.DeviceCapture;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.configuration.features.ConfigFeatureHelper;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.fragments.account.AccountEditFragment;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.builder.FragmentBuilderFactory;
import org.alfresco.mobile.android.application.fragments.help.HelpDialogFragment;
import org.alfresco.mobile.android.application.fragments.menu.MainMenuFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.fragments.signin.AccountOAuthFragment;
import org.alfresco.mobile.android.application.fragments.signin.AccountSigninSamlFragment;
import org.alfresco.mobile.android.application.fragments.sync.SyncFragment;
import org.alfresco.mobile.android.application.fragments.sync.SyncMigrationFragment;
import org.alfresco.mobile.android.application.intent.PublicIntentAPIUtils;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.application.managers.extensions.AnalyticHelper;
import org.alfresco.mobile.android.application.security.DataProtectionUserDialogFragment;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.CreateAccountEvent;
import org.alfresco.mobile.android.async.configuration.ConfigurationEvent;
import org.alfresco.mobile.android.async.file.encryption.AccountProtectionEvent;
import org.alfresco.mobile.android.async.person.AvatarRequest;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountCompletedEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountErrorEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountStartedEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadInactiveAccountEvent;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.async.session.oauth.AccountOAuthHelper;
import org.alfresco.mobile.android.async.session.oauth.RetrieveOAuthDataEvent;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AccountsPreferences;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.favorite.FavoritesManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.mdm.MDMManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentProvider;
import org.alfresco.mobile.android.ui.RefreshFragment;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.node.browse.NodeBrowserTemplate;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Subscribe;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SyncInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * Main activity of the application.
 *
 * @author Jean Marie Pascal
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MainActivity extends BaseActivity
{
    private static final String TAG = MainActivity.class.getName();

    private int sessionState = 0;

    private int sessionStateErrorMessageId;

    private Folder importParent;

    private Node currentNode;

    private MDMManager mdmManager;

    // Device capture (made static as we don't seem to be getting instance state
    // back through creation).
    private static DeviceCapture capture = null;

    private int fragmentQueue = -1;

    private Boolean displaySync;

    // SLIDING MENU
    private static DrawerLayout mDrawerLayout;

    private ViewGroup mDrawer;

    private static ActionBarDrawerToggle mDrawerToggle;

    /** Flag to indicate account creation from the welcome screen. */
    private boolean requestSwapAccount = false;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate(savedInstanceState);

        // Loading progress
        setContentView(R.layout.app_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
        {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mdmManager = MDMManager.getInstance(this);

        if (capture != null) capture.setActivity(this);

        if (savedInstanceState != null)
        {
            MainActivityHelper helper = new MainActivityHelper(savedInstanceState.getBundle(MainActivityHelper.TAG));
            setCurrentAccount(helper.getCurrentAccount());
            importParent = helper.getFolder();
            fragmentQueue = helper.getFragmentQueue();
            sessionState = helper.getSessionState();
            sessionStateErrorMessageId = helper.getSessionErrorMessageId();

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

        // After account creation via welcome screen
        if (getCurrentAccount() != null)
        {
            requestSwapAccount = true;
            if (getCurrentAccount().getIsPaidAccount()
                    && !prefs.getBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false))
            {

                if (!mdmManager.hasConfig())
                {
                    // Check if we've prompted the user for Data Protection yet.
                    // This is needed on new AlfrescoAccount creation, as the
                    // Activity gets
                    // re-created after the AlfrescoAccount is created.
                    DataProtectionUserDialogFragment.newInstance(true).show(getSupportFragmentManager(),
                            DataProtectionUserDialogFragment.TAG);
                    prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).apply();
                }
            }
        }

        FragmentTransaction t2 = getSupportFragmentManager().beginTransaction();
        t2.replace(R.id.sliding_menu, MainMenuFragment.with(this).createFragment(), MainMenuFragment.SLIDING_TAG);
        t2.commit();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (ViewGroup) findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open_in, R.string.cancel)
        {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view)
            {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();

                // Refresh Title from Fragments
                Fragment fr = getSupportFragmentManager()
                        .findFragmentById(DisplayUtils.getLeftFragmentId(MainActivity.this));
                if (fr != null && fr instanceof AlfrescoFragment)
                {
                    ((AlfrescoFragment) fr).displayTitle();
                    if (DisplayUtils.hasCentralPane(MainActivity.this))
                    {
                        fr = getSupportFragmentManager()
                                .findFragmentById(DisplayUtils.getCentralFragmentId(MainActivity.this));
                        if (fr != null && fr instanceof AlfrescoFragment)
                        {
                            ((AlfrescoFragment) fr).displayTitle();
                        }
                    }
                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView)
            {
                MainMenuFragment slidefragment = (MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG);
                if (slidefragment != null)
                {
                    slidefragment.refreshData();
                }
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                UIUtils.displayTitle(R.string.app_name, MainActivity.this);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getAppActionBar().setDisplayHomeAsUpEnabled(true);
        getAppActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onStart()
    {
        if (AnalyticsManager.getInstance(this) != null && AnalyticsManager.getInstance(this).isEnable())
        {
            AnalyticsManager.getInstance(this).startReport(this);
        }

        registerPublicReceiver(new NetworkReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        super.onStart();
        AccountOAuthHelper.requestRefreshToken(getCurrentSession(), this);
        SyncContentManager.getInstance(this).cronSync(getCurrentAccount());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        checkSession();

        if (getFragment(MainMenuFragment.TAG) != null && requestSwapAccount)
        {
            EventBusManager.getInstance()
                    .post(new LoadAccountCompletedEvent(LoadAccountCompletedEvent.SWAP, getCurrentAccount()));
            requestSwapAccount = false;
        }

        // Is it from an alfresco shortcut ?
        PublicIntentAPIUtils.openShortcut(this, getIntent());
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SyncContentManager.getInstance(this).saveSyncPrepareTimestamp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.DECRYPTED)
        {
            File requiredProtectionFile = DataProtectionManager.getInstance(this).getRequiredDataProtectionFile();
            if (!SyncContentManager.getInstance(this).isSyncFile(requiredProtectionFile))
            {
                DataProtectionManager.getInstance(this).checkEncrypt(getCurrentAccount(), requiredProtectionFile);
            }
        }

        // Default is Cancelled as we dont provide anything back
        // If OK it means we request the helpfragment
        if (requestCode == SyncMigrationFragment.REQUEST_CODE)
        {
            displaySync = false;
            if (resultCode == RESULT_OK)
            {
                HelpDialogFragment.with(this).back(true).display();
            }
        }

        if (capture != null && requestCode == capture.getRequestCode())
        {
            capture.capturedCallback(requestCode, resultCode, data);
        }

        if (requestCode == RequestCode.SETTINGS)
        {
            if (resultCode == RequestCode.RESULT_REFRESH_SESSION)
            {
                // Refresh Accounts
                EventBusManager.getInstance().post(new RequestSessionEvent(getCurrentAccount(), true));
            }
            else
            {
                // Refresh Accounts
                ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).refreshAccount();
                ((MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG)).refreshAccount();

                if (getCurrentAccount() != null)
                {
                    // Send Event
                    // ConfigManager.getInstance(this).loadAndUseCustom(getCurrentAccount());
                    EventBusManager.getInstance()
                            .post(new ConfigManager.ConfigurationMenuEvent(getCurrentAccount().getId()));
                }
            }
        }

        if (requestCode == PassCodeActivity.REQUEST_CODE_PASSCODE && sessionState == SESSION_LOADING
                && getCurrentSession() != null)
        {
            onAccountLoaded(new LoadAccountCompletedEvent("-1", getCurrentAccount()));
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

            // Is it from an alfresco shortcut ?
            PublicIntentAPIUtils.openShortcut(this, intent);
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
        outState.putBundle(MainActivityHelper.TAG, MainActivityHelper.createBundle(outState, getCurrentAccount(),
                capture, fragmentQueue, importParent, sessionState, sessionStateErrorMessageId));
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
        invalidateOptionsMenu();
    }

    public void hideSlideMenu()
    {
        mDrawerLayout.closeDrawer(mDrawer);
        invalidateOptionsMenu();
    }

    public boolean isSlideMenuVisible()
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
                if (getCurrentAccount() == null)
                {
                    AlfrescoNotificationManager.getInstance(this).showLongToast(getString(R.string.loginfirst));
                }
                else
                {
                    type = ConfigurationConstant.KEY_LOCAL_FILES;
                }
                break;
            case R.id.menu_notifications:
                if (getCurrentAccount() == null)
                {
                    AlfrescoNotificationManager.getInstance(this).showLongToast(getString(R.string.loginfirst));
                }
                else
                {
                    startActivity(new Intent(PrivateIntent.ACTION_DISPLAY_OPERATIONS)
                            .putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getCurrentAccount().getId()));
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

    // ///////////////////////////////////////////////////////////////////////////
    // SESSION MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public void setSessionState(int state)
    {
        sessionState = state;
        sessionStateErrorMessageId = 0;
    }

    public void setSessionState(int state, int messageId)
    {
        sessionState = state;
        if (messageId == 0 || messageId == -1)
        {
            messageId = R.string.error_general;
        }
        sessionStateErrorMessageId = messageId;
    }

    public void setSessionErrorMessageId(int messageId)
    {
        setSessionState(SESSION_ERROR, messageId);
    }

    private void checkSession()
    {
        if (AlfrescoAccountManager.getInstance(this).isEmpty() && AlfrescoAccountManager.getInstance(this).hasData())
        {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }
        else if (getCurrentSession() == null)
        {
            sessionManager.loadSession(getCurrentAccount());
        }
        else if (sessionState == SESSION_ERROR && getCurrentSession() == null
                && ConnectivityUtils.hasInternetAvailable(this))
        {
            sessionManager.loadSession(getCurrentAccount());
        }
        invalidateOptionsMenu();
    }

    public boolean hasSessionAvailable()
    {
        return checkSession(0);
    }

    private boolean checkSession(int actionMainMenuId)
    {
        switch (sessionState)
        {
            case SESSION_ERROR:
                showSessionErrorAlert(sessionStateErrorMessageId, getCurrentAccount());
                return false;
            case SESSION_LOADING:
                displayWaitingDialog();
                fragmentQueue = actionMainMenuId != 0 ? actionMainMenuId : -1;
                return false;
            default:
                if (!ConnectivityUtils.hasNetwork(this))
                {
                    new MaterialDialog.Builder(this).iconRes(R.drawable.ic_application_logo)
                            .title(R.string.error_session_creation_message)
                            .content(Html.fromHtml(getString(R.string.error_session_nodata)))
                            .positiveText(android.R.string.ok).show();
                    return false;
                }
                else if (getCurrentAccount() != null && getCurrentAccount().getActivation() != null)
                {
                    // AlfrescoNotificationManager.getInstance(this).showToast(R.string.account_not_activated);
                    return false;
                }
                break;
        }
        return true;
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
            case R.id.menu_refresh:
                if (getSupportFragmentManager()
                        .findFragmentById(DisplayUtils.getLeftFragmentId(this)) instanceof RefreshFragment)
                {
                    ((RefreshFragment) getSupportFragmentManager()
                            .findFragmentById(DisplayUtils.getLeftFragmentId(this))).refresh();
                }
                return true;
            case R.id.menu_settings:
                MainMenuFragment.displayPreferences(this, getCurrentAccount().getId());
                hideSlideMenu();
                return true;
            case R.id.menu_help:
                HelpDialogFragment.with(this).back(true).display();
                hideSlideMenu();
                return true;
            case R.id.menu_about:
                AboutFragment.with(this).display();
                hideSlideMenu();
                return true;
            case R.id.menu_notifications:
                doMainMenuAction(R.id.menu_notifications);
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
    public void setCapture(DeviceCapture deviceCapture)
    {
        capture = deviceCapture;
    }

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
        setCurrentAccount(event.accountId);
        AccountOAuthHelper.onNewOauthData(this, event);
    }

    @Subscribe
    public void onAccountCreated(CreateAccountEvent event)
    {
        if (event.hasException) { return; }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        AlfrescoAccount tmpAccount = AlfrescoAccountManager.getInstance(this).retrieveAccount(event.data.getId());

        onAccountLoading(new LoadAccountStartedEvent("-1", tmpAccount));
        onAccountLoaded(new LoadAccountCompletedEvent("-1", tmpAccount));

        if (tmpAccount.getIsPaidAccount() && !prefs.getBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false))
        {
            if (mdmManager.hasConfig())
            {
                // TODO Do we want to provide different behaviours in case
                // of MDM ?
            }
            else
            {
                DataProtectionUserDialogFragment.newInstance(true).show(getSupportFragmentManager(),
                        DataProtectionUserDialogFragment.TAG);
                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).apply();
            }
        }

        setSessionState(SESSION_ACTIVE);
    }

    private void swapSession(AlfrescoAccount currentAccount)
    {
        swapSession(currentAccount, false);
    }

    private void swapSession(AlfrescoAccount currentAccount, boolean resetStack)
    {
        // Change activity state to loading.
        setSessionState(SESSION_LOADING);

        setCurrentAccount(currentAccount);

        if (getFragment(MainMenuFragment.TAG) != null)
        {
            ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).displaySyncStatut();
            ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).hideWorkflowMenu(currentAccount);
        }

        if (getFragment(MainMenuFragment.SLIDING_TAG) != null)
        {
            ((MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG)).displaySyncStatut();
            ((MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG)).hideWorkflowMenu(currentAccount);
        }

        // Return to root screen
        if (resetStack)
        {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // Display progress
        setSupportProgressBarIndeterminateVisibility(true);

        // Add accountName in actionBar
        UIUtils.displayTitle(R.string.app_name, MainActivity.this);
    }

    boolean fromSessionRequested = false;

    @Subscribe
    public void onSessionRequested(RequestSessionEvent event)
    {
        swapSession(event.accountToLoad, event.resetStack);
        fromSessionRequested = true;
    }

    @Subscribe
    public void onAccountLoading(LoadAccountStartedEvent event)
    {
        if (fromSessionRequested)
        {
            ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).refreshData();
        }
        setSessionState(SESSION_LOADING);
        setSupportProgressBarIndeterminateVisibility(true);
        invalidateOptionsMenu();
        setCurrentAccount(event.account);
        if (event != null && event.account != null)
        {
            Snackbar.make(findViewById(R.id.left_pane_body), event.account.getTitle(), Snackbar.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void onAccountLoaded(LoadAccountCompletedEvent event)
    {
        // Avoid collision with PublicDispatcherActivity when selecting an
        // account.
        Log.i(TAG, "ON Account Loaded");
        if (event.requestId == null || getCurrentSession() == null) { return; }

        if (event.requestId == LoadAccountCompletedEvent.SWAP)
        {
            swapSession(event.account);
        }

        if (fromSessionRequested)
        {
            fromSessionRequested = false;
        }

        ServiceRegistry registry = getCurrentSession().getServiceRegistry();

        ConfigManager configManager = ConfigManager.getInstance(this);
        if (!configManager.hasConfig(getCurrentAccount().getId()))
        {
            configManager.init(getCurrentAccount());
        }
        if (registry instanceof AlfrescoServiceRegistry)
        {
            // Check configuration
            if (((AlfrescoServiceRegistry) registry).getConfigService() == null)
            {
                // In this case there's no configuration defined on server
                // We remove any cached configuration
                ConfigManager.getInstance(this).cleanCache(getCurrentAccount());
            }
            else
            {
                configManager.loadRemote(getCurrentAccount().getId(),
                        ((AlfrescoServiceRegistry) registry).getConfigService());
            }
            // Check feature config
            ConfigFeatureHelper.check(this, getCurrentAccount(), getCurrentSession());
        }
        configManager.setSession(getCurrentAccount().getId(), getCurrentSession());

        // Retrieve latest avatar
        Operator.with(this).load(new AvatarRequest.Builder(getCurrentAccount().getUsername()));

        if (!isCurrentAccountToLoad(event)) { return; }

        setSessionState(SESSION_ACTIVE);
        setSupportProgressBarIndeterminateVisibility(false);
        UIUtils.displayTitle(R.string.app_name, this, false);

        // Retrieve Rendition Manager associated to this account
        RenditionManagerImpl.getInstance(this).setSession(getCurrentSession());

        // Remove OAuthFragment if one
        if (getFragment(AccountOAuthFragment.TAG) != null)
        {
            getSupportFragmentManager().popBackStack(AccountOAuthFragment.TAG,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        removeWaitingDialog();

        if (getFragment(AccountSigninSamlFragment.TAG) != null)
        {
            if (DisplayUtils.hasCentralPane(this))
            {
                FragmentDisplayer.clearCentralPane(this);
            }
            else
            {
                getSupportFragmentManager().popBackStack(AccountSigninSamlFragment.TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }

        // Used for launching last pressed action button from main menu
        if (fragmentQueue != -1)
        {
            doMainMenuAction(fragmentQueue);
        }
        fragmentQueue = -1;

        // Save latest position as default future one
        AccountsPreferences.setDefaultAccount(this, getCurrentAccount().getId());

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
        if (!getCurrentAccount().getIsPaidAccount())
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
                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).apply();

                if (mdmManager.hasConfig())
                {
                    // TODO Do we want to provide different behaviours in case
                    // of MDM ?
                }
                else
                {
                    DataProtectionUserDialogFragment.newInstance(true).show(getSupportFragmentManager(),
                            DataProtectionUserDialogFragment.TAG);
                }

                AlfrescoAccountManager.getInstance(this).update(getCurrentAccount().getId(),
                        AlfrescoAccount.ACCOUNT_IS_PAID_ACCOUNT, "true");
            }
        }

        // NB : temporary code ?
        // Display Sync Migration
        if (displaySync == null)
        {
            displaySync = SyncContentManager.displaySyncInfo(this);
        }
        if (displaySync)
        {
            startActivityForResult(new Intent(this, InfoActivity.class), SyncMigrationFragment.REQUEST_CODE);
        }

        // Analytics
        AnalyticHelper.analyzeSession(this, event.account, getCurrentSession());

        // Activate Automatic Sync for Sync Content & Favorite
        if (SyncContentManager.getInstance(this).hasActivateSync(getCurrentAccount()))
        {
            // SyncContentManager.getInstance(this).setActivateSync(getCurrentAccount(),
            // true);
            if (SyncContentManager.getInstance(this).canSync(getCurrentAccount()))
            {
                SyncContentManager.getInstance(this).sync(AnalyticsManager.LABEL_SYNC_SESSION_LOADED,
                        getCurrentAccount());

            }
        }

        FavoritesManager.getInstance(this).setActivateSync(getCurrentAccount(), true);
        if (FavoritesManager.getInstance(this).canSync(getCurrentAccount()))
        {
            FavoritesManager.getInstance(this).sync(getCurrentAccount());
        }

        // SAML Things

        invalidateOptionsMenu();
    }

    @Subscribe
    public void onCloudAccountErrorEvent(LoadSessionCallBack.CloudAccountErrorEvent event)
    {
        if (getCurrentAccount() == null || getCurrentAccount().getId() != event.data) { return; }

        // Display OAuth Authentication
        AccountOAuthFragment.with(MainActivity.this).account(event.account).isCreation(false).display();

        // Stop progress indication
        setSupportProgressBarIndeterminateVisibility(false);

        invalidateOptionsMenu();
    }

    @Subscribe
    public void onAccountErrorEvent(final LoadAccountErrorEvent event)
    {
        // Display Error Session
        showSessionErrorAlert(event.messageId, event.account);

        // Change status
        setSessionErrorMessageId(event.messageId);

        // Reset currentAccount & references
        setCurrentAccount(AlfrescoAccountManager.getInstance(this).retrieveAccount(event.data));

        // Stop progress indication
        setSupportProgressBarIndeterminateVisibility(false);

        invalidateOptionsMenu();
    }

    @Subscribe
    public void onAccountInactiveEvent(LoadInactiveAccountEvent event)
    {
        if (getCurrentAccount() == null || getCurrentAccount().getId() != event.account.getId()) { return; }

        setSessionState(SESSION_INACTIVE);
        setSupportProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();
        // AlfrescoNotificationManager.getInstance(this).showLongToast(getString(R.string.account_not_activated));
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

                    // If connectivity is better (or reopen) we can try to sync
                    // all pending request
                    if (getCurrentSession() != null && SyncContentManager.hasPendingSync(context, getCurrentAccount()))
                    {
                        if (!isSyncActive(AlfrescoAccountManager.getInstance(context)
                                .getAndroidAccount(getCurrentAccount().getId()), SyncContentProvider.AUTHORITY))
                        {
                            SyncContentManager.getInstance(context).sync(AnalyticsManager.LABEL_SYNC_NETWORK,
                                    getCurrentAccount());
                        }
                    }

                }
            }
            catch (Exception e)
            {
                // Nothing special
                Log.d(TAG, Log.getStackTraceString(e));
            }
        }
    }

    private static boolean isSyncActive(Account account, String authority)
    {
        for (SyncInfo syncInfo : ContentResolver.getCurrentSyncs())
        {
            if (syncInfo.account.equals(account) && syncInfo.authority.equals(authority)) { return true; }
        }
        return false;
    }

    private boolean isCurrentAccountToLoad(LoadAccountCompletedEvent event)
    {
        return getCurrentAccount() != null && event != null && (getCurrentAccount().getId() == event.account.getId());
    }

    private void showSessionErrorAlert(int messageId, final AlfrescoAccount account)
    {

        // General Errors
        // Display error dialog message
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this).iconRes(R.drawable.ic_application_logo)
                .title(R.string.error_session_creation_message).positiveText(android.R.string.ok);

        if (messageId == 0 || messageId == -1)
        {
            messageId = R.string.error_general;
        }
        builder.content(Html.fromHtml(getString(messageId)));

        if (messageId == R.string.error_session_unauthorized)
        {
            if (account.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CMIS_SAML)
            {
                builder.content(Html.fromHtml(getString(R.string.error_session_expired)));
            }

            builder.negativeText(R.string.sign_in);
            builder.onNegative(new MaterialDialog.SingleButtonCallback()
            {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                {
                    if (account.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CMIS_SAML)
                    {
                        // SAML Exception ?
                        AccountSigninSamlFragment.with(MainActivity.this).isCreation(false).account(account).display();
                    }
                    else
                    {
                        AccountEditFragment.with(MainActivity.this).accountId(account.getId()).display();
                    }
                }
            });
        }

        builder.show();
    }
}
