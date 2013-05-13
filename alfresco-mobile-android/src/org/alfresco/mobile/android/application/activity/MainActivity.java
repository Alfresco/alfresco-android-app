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
package org.alfresco.mobile.android.application.activity;

import java.io.File;
import java.util.Stack;

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.accounts.fragment.AccountDetailsFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountEditFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountOAuthFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountTypesFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountsFragment;
import org.alfresco.mobile.android.application.accounts.networks.CloudNetworksFragment;
import org.alfresco.mobile.android.application.accounts.oauth.OAuthRefreshTokenCallback;
import org.alfresco.mobile.android.application.accounts.oauth.OAuthRefreshTokenLoader;
import org.alfresco.mobile.android.application.accounts.signup.CloudSignupDialogFragment;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.fragments.activities.ActivitiesFragment;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.browser.UploadChooseDialogFragment;
import org.alfresco.mobile.android.application.fragments.comments.CommentsFragment;
import org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment;
import org.alfresco.mobile.android.application.fragments.encryption.EncryptionDialogFragment;
import org.alfresco.mobile.android.application.fragments.favorites.FavoritesFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerHelper;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerMenuFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.LibraryFragment;
import org.alfresco.mobile.android.application.fragments.help.HelpDialogFragment;
import org.alfresco.mobile.android.application.fragments.menu.MainMenuFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.properties.DetailsFragment;
import org.alfresco.mobile.android.application.fragments.search.KeywordSearch;
import org.alfresco.mobile.android.application.fragments.sites.BrowserSitesFragment;
import org.alfresco.mobile.android.application.fragments.versions.VersionFragment;
import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.integration.capture.AudioCapture;
import org.alfresco.mobile.android.application.integration.capture.DeviceCapture;
import org.alfresco.mobile.android.application.integration.capture.PhotoCapture;
import org.alfresco.mobile.android.application.integration.capture.VideoCapture;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.ReportManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.preferences.PasscodePreferences;
import org.alfresco.mobile.android.application.security.CipherUtils;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;

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

    // MANAGE FRAGMENT STACK CENTRAL
    private Stack<String> stackCentral = new Stack<String>();

    // Available objects for fragments
    private Site displayFromSite = null;

    private Folder importParent;

    private Node currentNode;

    // Device capture
    private DeviceCapture capture;

    private int fragmentQueue = -1;

    private boolean activateCheckPasscode = false;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        activateCheckPasscode = false;

        super.onCreate(savedInstanceState);

        // Check intent
        if (getIntent().hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
        {
            long accountId = getIntent().getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID);
            currentAccount = AccountManager.retrieveAccount(this, accountId);
        }

        // Loading progress
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.app_main);

        setProgressBarIndeterminateVisibility(false);

        if (savedInstanceState != null)
        {
            MainActivityHelper helper = new MainActivityHelper(savedInstanceState.getBundle(MainActivityHelper.TAG));
            currentAccount = helper.getCurrentAccount();
            displayFromSite = helper.getSite();
            importParent = helper.getFolder();
            fragmentQueue = helper.getFragmentQueue();
            if (helper.getDeviceCapture() != null)
            {
                capture = helper.getDeviceCapture();
                capture.setActivity(this);
            }
            stackCentral = helper.getStackCentral();
        }
        else
        {
            displayMainMenu();
        }

        if (SessionUtils.getAccount(this) != null)
        {
            currentAccount = SessionUtils.getAccount(this);
            if (currentAccount.getIsPaidAccount() == true)
            {
                // Check if we've prompted the user for Data Protection yet.
                // This is needed on new account creation, as the Activity gets
                // re-created after the account is created.
                CipherUtils.EncryptionUserInteraction(this);

                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();
            }
        }

        initActionBar();
        checkForUpdates();

        // REDIRECT To Accounts Fragment if signup process
        if (IntentIntegrator.ACTION_CHECK_SIGNUP.equals(getIntent().getAction()))
        {
            displayAccounts();
        }

        // Display or not Left/central panel for middle tablet.
        DisplayUtils.switchSingleOrTwo(this, false);

        // TODO FIXME Remove it!
        // Clean all operations
        OperationSchema.reset(ApplicationManager.getInstance(this).getDatabaseManager().getWriteDb());
    }

    @Override
    protected void onStart()
    {
        IntentFilter filters = new IntentFilter();
        filters.addAction(IntentIntegrator.ACTION_LOAD_ACCOUNT);
        filters.addAction(IntentIntegrator.ACTION_RELOAD_ACCOUNT);
        filters.addAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_STARTED);
        filters.addAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED);
        filters.addAction(IntentIntegrator.ACTION_ACCOUNT_INACTIVE);
        filters.addAction(IntentIntegrator.ACTION_USER_AUTHENTICATION);
        filters.addCategory(IntentIntegrator.CATEGORY_OAUTH);
        filters.addCategory(IntentIntegrator.CATEGORY_OAUTH_REFRESH);
        filters.addAction(IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED);
        filters.addAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_ERROR);
        registerPrivateReceiver(new MainActivityReceiver(), filters);

        super.onStart();
        OAuthRefreshTokenCallback.requestRefreshToken(getCurrentSession(), this);
        PassCodeActivity.requestUserPasscode(this);
        activateCheckPasscode = PasscodePreferences.hasPasscodeEnable(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        checkForCrashes();
        checkSession();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (!activateCheckPasscode)
        {
            PasscodePreferences.updateLastActivity(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PublicIntent.REQUESTCODE_DECRYPTED)
        {
            String filename = PreferenceManager.getDefaultSharedPreferences(this).getString(
                    GeneralPreferences.REQUIRES_ENCRYPT, "");
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            EncryptionDialogFragment fragment = EncryptionDialogFragment.encrypt(filename);
            fragmentTransaction.add(fragment, fragment.getFragmentTransactionTag());
            fragmentTransaction.commit();
        }

        if (requestCode == PassCodeActivity.REQUEST_CODE_PASSCODE)
        {
            if (resultCode == RESULT_CANCELED)
            {
                finish();
            }
            else
            {
                activateCheckPasscode = true;
            }
        }

        if (capture != null && requestCode == capture.getRequestCode())
        {
            capture.capturedCallback(requestCode, resultCode, data);
        }
    }

    // TODO remove All this fonction and replace by broadcast.
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        try
        {
            // Intent for Removing Fragment + eventual associated loader.
            if (IntentIntegrator.ACTION_REMOVE_FRAGMENT.equals(intent.getAction()))
            {
                EncryptionDialogFragment.removeFragment(this, intent);
                return;
            }

            // Intent for CLOUD SIGN UP
            if (IntentIntegrator.ACTION_CHECK_SIGNUP.equals(intent.getAction()))
            {
                FragmentDisplayer.removeFragment(this, CloudSignupDialogFragment.TAG);
                displayAccounts();
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
            if (Intent.ACTION_VIEW.equals(intent.getAction()) && IntentIntegrator.NODE_TYPE.equals(intent.getType()))
            {
                if (intent.getExtras().containsKey(IntentIntegrator.EXTRA_NODE))
                {
                    BaseFragment frag = DetailsFragment.newInstance((Document) intent.getExtras().get(
                            IntentIntegrator.EXTRA_NODE));
                    frag.setSession(SessionUtils.getSession(this));
                    FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), DetailsFragment.TAG, false);
                }
                return;
            }

            // Intent for display Sign up Dialog
            if (Intent.ACTION_VIEW.equals(intent.getAction())
                    && IntentIntegrator.ALFRESCO_SCHEME_SHORT.equals(intent.getData().getScheme())
                    && IntentIntegrator.CLOUD_SIGNUP_I.equals(intent.getData().getHost()))
            {
                getFragmentManager().popBackStack(AccountTypesFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                CloudSignupDialogFragment newFragment = new CloudSignupDialogFragment();
                FragmentDisplayer.replaceFragment(this, newFragment, DisplayUtils.getFragmentPlace(this),
                        CloudSignupDialogFragment.TAG, true);
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

        outState.putBundle(MainActivityHelper.TAG, MainActivityHelper.createBundle(outState, stackCentral,
                currentAccount, capture, fragmentQueue, displayFromSite, importParent));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // HockeyApp Integration
    // ///////////////////////////////////////////////////////////////////////////
    private void checkForCrashes()
    {
        ReportManager.checkForCrashes(this);
    }

    private void checkForUpdates()
    {
        ReportManager.checkForUpdates(this);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SLIDE MENU
    // ///////////////////////////////////////////////////////////////////////////
    public void toggleSlideMenu()
    {
        if (getFragment(MainMenuFragment.TAG) != null && getFragment(MainMenuFragment.TAG).isAdded()) { return; }
        View slideMenu = findViewById(R.id.slide_pane);
        if (slideMenu.getVisibility() == View.VISIBLE)
        {
            hideSlideMenu();
        }
        else
        {
            MainMenuFragment slidefragment = (MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG);
            if (slidefragment != null)
            {
                slidefragment.refreshData();
            }
            showSlideMenu();
        }
    }

    private void hideSlideMenu()
    {
        View slideMenu = findViewById(R.id.slide_pane);
        slideMenu.setVisibility(View.GONE);
        slideMenu.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rbm_out_to_left));
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private boolean isSlideMenuVisible()
    {
        return findViewById(R.id.slide_pane).getVisibility() == View.VISIBLE;
    }

    private void showSlideMenu()
    {
        View slideMenu = findViewById(R.id.slide_pane);
        slideMenu.setVisibility(View.VISIBLE);
        slideMenu.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rbm_in_from_left));
        getActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void doMainMenuAction(int id)
    {
        BaseFragment frag = null;

        View slideMenu = findViewById(R.id.slide_pane);
        if (slideMenu.getVisibility() == View.VISIBLE)
        {
            hideSlideMenu();
        }

        if (DisplayUtils.hasCentralPane(this))
        {
            clearCentralPane();
        }

        switch (id)
        {
            case R.id.menu_browse_my_sites:
                if (!checkSession(R.id.menu_browse_my_sites)) { return; }
                frag = BrowserSitesFragment.newInstance();
                FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                        BrowserSitesFragment.TAG, true);
                break;
            case R.id.menu_browse_root:
                if (!checkSession(R.id.menu_browse_root)) { return; }
                setDisplayFromSite(null);
                frag = ChildrenBrowserFragment.newInstance(getCurrentSession().getRootFolder());
                frag.setSession(SessionUtils.getSession(this));
                FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                        ChildrenBrowserFragment.TAG, true);
                break;
            case R.id.menu_browse_activities:
                if (!checkSession(R.id.menu_browse_activities)) { return; }
                frag = ActivitiesFragment.newInstance();
                FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                        ActivitiesFragment.TAG, true);
                break;
            case R.id.menu_search:
                if (!checkSession(R.id.menu_search)) { return; }
                FragmentDisplayer.replaceFragment(this, DisplayUtils.getLeftFragmentId(this), KeywordSearch.TAG, true);
                break;
            case R.id.menu_favorites:
                if (!checkSession(R.id.menu_favorites)) { return; }
                frag = FavoritesFragment.newInstance(FavoritesFragment.MODE_DOCUMENTS);
                FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                        FavoritesFragment.TAG, true);
                break;
            case R.id.menu_documents:
                if (currentAccount == null)
                {
                    MessengerManager.showLongToast(this, getString(R.string.loginfirst));
                }
                else
                {
                    addLocalFileNavigationFragment();
                }
                break;
            case R.id.menu_prefs:
                displayPreferences();
                break;
            case R.id.menu_about:
                displayAbout();
                break;
            case R.id.menu_help:
                displayHelp();
                break;

            default:
                break;
        }
    }

    public void showMainMenuFragment(View v)
    {
        DisplayUtils.hideLeftTitlePane(this);
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
        if (accountManager.isEmpty() && accountManager.hasData())
        {
            startActivity(new Intent(this, HomeScreenActivity.class));
            finish();
            return;
        }
        else if (getCurrentAccount() == null && getCurrentSession() == null)
        {
            ActionManager.loadAccount(this, accountManager.getDefaultAccount());
        }
        invalidateOptionsMenu();
    }

    private boolean checkSession(int actionMainMenuId)
    {
        switch (sessionState)
        {
            case SESSION_ERROR:
                Bundle b = new Bundle();
                b.putInt(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_alfresco_logo);
                b.putInt(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_session_creation_message);
                b.putInt(SimpleAlertDialogFragment.PARAM_MESSAGE, sessionStateErrorMessageId);
                b.putInt(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
                ActionManager.actionDisplayDialog(this, b);
                return false;
            case SESSION_LOADING:
                displayWaitingDialog();
                fragmentQueue = actionMainMenuId;
                return false;
            default:
                if (!hasNetwork())
                {
                    return false;
                }
                else if (getCurrentAccount() != null && getCurrentAccount().getActivation() != null)
                {
                    MessengerManager.showToast(this, R.string.account_not_activated);
                    return false;
                }
                break;
        }
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FRAGMENTS
    // ///////////////////////////////////////////////////////////////////////////
    public void addNavigationFragment(Folder f)
    {
        clearScreen();
        clearCentralPane();
        super.addNavigationFragment(f);
    }

    public void addNavigationFragment(String path)
    {
        clearScreen();
        clearCentralPane();
        super.addBrowserFragment(path);
    }

    public void addNavigationFragment(Site s)
    {
        clearScreen();
        clearCentralPane();
        setDisplayFromSite(s);
        super.addNavigationFragment(s);
    }

    public void addLocalFileNavigationFragment()
    {
        clearScreen();
        clearCentralPane();

        if (DisplayUtils.hasCentralPane(this))
        {
            BaseFragment frag = FileExplorerMenuFragment.newInstance();
            FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                    FileExplorerMenuFragment.TAG, true);
        }
        else
        {
            FileExplorerHelper.setSelection(this);
            FileExplorerHelper.displayNavigationMode(this, ListingModeFragment.MODE_LISTING);
        }
    }

    public void addLocalFileNavigationFragment(File file)
    {
        Boolean b = DisplayUtils.hasCentralPane(this) ? false : true;
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(FileExplorerFragment.TAG);
        }
        BaseFragment frag = FileExplorerFragment.newInstance(file);
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getMainPaneId(this), FileExplorerFragment.TAG, b);
    }

    public void addLocalFileNavigationFragment(int mediaType)
    {
        Boolean b = DisplayUtils.hasCentralPane(this) ? false : true;
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(FileExplorerFragment.TAG);
        }
        LibraryFragment frag = LibraryFragment.newInstance(mediaType);
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getMainPaneId(this), LibraryFragment.TAG, b);
    }

    public void addPropertiesFragment(Node n, Folder parentFolder, boolean forceBackStack)
    {
        clearCentralPane();
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(DetailsFragment.TAG);
        }
        BaseFragment frag = DetailsFragment.newInstance(n, parentFolder);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), DetailsFragment.TAG, forceBackStack);

    }

    public void addPropertiesFragment(String nodeIdentifier)
    {
        Boolean b = DisplayUtils.hasCentralPane(this) ? false : true;
        clearCentralPane();
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(DetailsFragment.TAG);
        }
        BaseFragment frag = DetailsFragment.newInstance(nodeIdentifier);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), DetailsFragment.TAG, b);
    }

    public void addPropertiesFragment(Node n)
    {
        Boolean b = DisplayUtils.hasCentralPane(this) ? false : true;
        addPropertiesFragment(n, getImportParent(), b);
    }

    public void addComments(Node n)
    {
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.push(CommentsFragment.TAG);
        }
        BaseFragment frag = CommentsFragment.newInstance(n);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(true), CommentsFragment.TAG, true);
        ((View) findViewById(getFragmentPlace(true)).getParent()).setVisibility(View.VISIBLE);
    }

    public void addVersions(Document d)
    {
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.push(VersionFragment.TAG);
        }
        BaseFragment frag = VersionFragment.newInstance(d);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(true), VersionFragment.TAG, true);
        ((View) findViewById(getFragmentPlace(true)).getParent()).setVisibility(View.VISIBLE);
    }

    public void addAccountDetails(long id)
    {
        Boolean b = DisplayUtils.hasCentralPane(this) ? false : true;
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(AccountsFragment.TAG);
        }
        BaseFragment frag = AccountDetailsFragment.newInstance(id);
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getMainPaneId(this), AccountDetailsFragment.TAG, b);
    }

    public void displayHelp()
    {
        String pathHelpGuideFile = null;
        try
        {
            long lastUpdate = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).lastUpdateTime;
            // Check last update time of the app and compare to an
            // existing (or not) help guide.
            File assetFolder = StorageManager.getAssetFolder(this);
            String helpGuideName = getString(R.string.asset_folder_prefix) + "_" + getString(R.string.help_user_guide);
            File helpGuideFile = new File(assetFolder, helpGuideName);

            if (!helpGuideFile.exists() || helpGuideFile.lastModified() < lastUpdate)
            {
                String assetfilePath = getString(R.string.help_path) + helpGuideName;
                org.alfresco.mobile.android.api.utils.IOUtils.copyFile(getAssets().open(assetfilePath), helpGuideFile);
            }

            pathHelpGuideFile = helpGuideFile.getPath();

            if (!ActionManager.launchPDF(this, pathHelpGuideFile))
            {
                new HelpDialogFragment().show(getFragmentManager(), HelpDialogFragment.TAG);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Unable to open help guide.");
        }
    }

    public void displayAbout()
    {
        clearScreen();
        clearCentralPane();
        if (getFragment(AboutFragment.TAG) != null)
        {
            // If reclick on About and if is visible, it removes the
            // AboutFragment.
            getFragmentManager().popBackStack(AboutFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        else
        {
            Fragment f = new AboutFragment();
            FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getMainPaneId(this), AboutFragment.TAG, true);
            if (DisplayUtils.hasCentralPane(this))
            {
                stackCentral.push(AboutFragment.TAG);
            }
        }
        DisplayUtils.switchSingleOrTwo(this, true);
    }

    public void displayPreferences()
    {
        clearScreen();
        clearCentralPane();
        if (getFragment(GeneralPreferences.TAG) != null)
        {
            getFragmentManager().popBackStack(GeneralPreferences.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        else
        {
            Fragment f = new GeneralPreferences();
            FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getMainPaneId(this), GeneralPreferences.TAG, true);
            if (DisplayUtils.hasCentralPane(this))
            {
                stackCentral.push(GeneralPreferences.TAG);
            }
        }
        DisplayUtils.switchSingleOrTwo(this, true);
    }

    public void displayMainMenu()
    {
        Fragment f = new MainMenuFragment();
        FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), MainMenuFragment.TAG, false);
        hideSlideMenu();
    }

    public void displayAccounts()
    {
        Fragment f = new AccountsFragment();
        FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), AccountsFragment.TAG, true);
    }

    public void displayNetworks()
    {
        if (getCurrentSession() instanceof CloudSession)
        {
            Fragment f = new CloudNetworksFragment();
            FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), CloudNetworksFragment.TAG,
                    true);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS FRAGMENTS
    // ///////////////////////////////////////////////////////////////////////////
    public void clearScreen()
    {
        if (DisplayUtils.hasCentralPane(this))
        {
            FragmentDisplayer.removeFragment(this, DisplayUtils.getCentralFragmentId(this));
        }
        if (DisplayUtils.hasLeftPane(this))
        {
            DisplayUtils.show(DisplayUtils.getLeftPane(this));
        }
    }

    private void clearCentralPane()
    {
        FragmentDisplayer.removeFragment(this, stackCentral);
        stackCentral.clear();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION BAR
    // ///////////////////////////////////////////////////////////////////////////
    @TargetApi(14)
    private void initActionBar()
    {
        try
        {
            ActionBar bar = getActionBar();
            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);

            if (DisplayUtils.hasCentralPane(this))
            {
                bar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO, ActionBar.DISPLAY_USE_LOGO);
            }
            else
            {
                bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_USE_LOGO);
            }

            if (AndroidVersion.isICSOrAbove())
            {
                bar.setHomeButtonEnabled(true);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        if (isVisible(FileExplorerFragment.TAG))
        {
            ((FileExplorerFragment) getFragment(FileExplorerFragment.TAG)).getMenu(menu);
        }

        if (isVisible(ActivitiesFragment.TAG))
        {
            ((ActivitiesFragment) getFragment(ActivitiesFragment.TAG)).getMenu(menu);
        }

        if (isVisible(AccountDetailsFragment.TAG))
        {
            ((AccountDetailsFragment) getFragment(AccountDetailsFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(AccountsFragment.TAG) && !isVisible(AccountTypesFragment.TAG)
                && !isVisible(AccountEditFragment.TAG) && !isVisible(AccountOAuthFragment.TAG))
        {
            AccountsFragment.getMenu(menu);
            return true;
        }

        if (isVisible(BrowserSitesFragment.TAG))
        {
            BrowserSitesFragment.getMenu(menu);
            return true;
        }

        if (isVisible(DetailsFragment.TAG))
        {
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(ChildrenBrowserFragment.TAG))
        {
            getActionBar().setDisplayShowTitleEnabled(false);
            ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(FavoritesFragment.TAG))
        {
            getActionBar().setDisplayShowTitleEnabled(false);
            FavoritesFragment.getMenu(menu);
            return true;
        }

        return true;
    }

    private boolean isVisible(String tag)
    {
        return getFragmentManager().findFragmentByTag(tag) != null
                && getFragmentManager().findFragmentByTag(tag).isAdded();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Not in all case we have ChildrenBrowserFragment displayed
        Folder parentFolder = null;
        if (((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)) != null)
        {
            parentFolder = ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).getParent();
        }

        switch (item.getItemId())
        {
            case MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_PHOTO:
                if (parentFolder != null)
                {
                    capture = new PhotoCapture(this, parentFolder);
                    capture.captureData();
                }
                return true;

            case MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_VIDEO:

                if (parentFolder != null)
                {
                    capture = new VideoCapture(this, parentFolder);
                    capture.captureData();
                }
                return true;

            case MenuActionItem.MENU_DEVICE_CAPTURE_MIC_AUDIO:
                if (parentFolder != null)
                {
                    capture = new AudioCapture(this, parentFolder);
                    capture.captureData();
                }
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
                FragmentDisplayer.replaceFragment(this,
                        KeywordSearch.newInstance(getImportParent(), isDisplayFromSite()), getFragmentPlace(false),
                        KeywordSearch.TAG, true);
                return true;

            case MenuActionItem.MENU_SEARCH:
                FragmentDisplayer.replaceFragment(this, new KeywordSearch(), getFragmentPlace(), KeywordSearch.TAG,
                        true);
                return true;

            case MenuActionItem.MENU_CREATE_FOLDER:
                if (getFragment(ChildrenBrowserFragment.TAG) != null)
                {
                    ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).createFolder();
                }
                else if (getFragment(FileExplorerFragment.TAG) != null)
                {
                    ((FileExplorerFragment) getFragment(FileExplorerFragment.TAG)).createFolder();
                }
                return true;
                
            case MenuActionItem.MENU_SHORTCUT:
                BaseFragment frag = FileExplorerMenuFragment.newInstance();
                frag.show(getFragmentManager(), FileExplorerMenuFragment.TAG);
                return true;

            case MenuActionItem.MENU_CREATE_DOCUMENT:
                String fragmentTag = FileExplorerFragment.TAG;
                if (getFragment(ChildrenBrowserFragment.TAG) != null)
                {
                    fragmentTag = ChildrenBrowserFragment.TAG;
                }
                DocumentTypesDialogFragment dialogft = DocumentTypesDialogFragment.newInstance(currentAccount,
                        fragmentTag);
                dialogft.show(getFragmentManager(), DocumentTypesDialogFragment.TAG);
                return true;

            case MenuActionItem.MENU_UPLOAD:
                UploadChooseDialogFragment dialog = UploadChooseDialogFragment.newInstance(currentAccount);
                dialog.show(getFragmentManager(), UploadChooseDialogFragment.TAG);
                return true;
            case MenuActionItem.MENU_REFRESH:
                ((RefreshFragment) getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(this)))
                        .refresh();
                return true;

            case MenuActionItem.MENU_SHARE:
                ((DetailsFragment) getFragment(DetailsFragment.TAG)).share();
                return true;
            case MenuActionItem.MENU_OPEN_IN:
                ((DetailsFragment) getFragment(DetailsFragment.TAG)).openin();
                return true;
            case MenuActionItem.MENU_DOWNLOAD:
                ((DetailsFragment) getFragment(DetailsFragment.TAG)).download();
                return true;
            case MenuActionItem.MENU_UPDATE:
                UploadChooseDialogFragment dialogu = UploadChooseDialogFragment.newInstance(currentAccount,
                        DetailsFragment.TAG);
                dialogu.show(getFragmentManager(), UploadChooseDialogFragment.TAG);
                return true;
            case MenuActionItem.MENU_EDIT:
                ((DetailsFragment) getFragment(DetailsFragment.TAG)).edit();
                return true;
            case MenuActionItem.MENU_COMMENT:
                ((DetailsFragment) getFragment(DetailsFragment.TAG)).comment();
                return true;
            case MenuActionItem.MENU_VERSION_HISTORY:
                ((DetailsFragment) getFragment(DetailsFragment.TAG)).versions();
                return true;
            case MenuActionItem.MENU_TAGS:
                ((DetailsFragment) getFragment(DetailsFragment.TAG)).tags();
                return true;
            case MenuActionItem.MENU_DELETE:
                ((DetailsFragment) getFragment(DetailsFragment.TAG)).delete();
                return true;
            case MenuActionItem.MENU_SITE_LIST_REQUEST:
                ((BrowserSitesFragment) getFragment(BrowserSitesFragment.TAG)).displayJoinSiteRequests();
                return true;
            case MenuActionItem.ABOUT_ID:
                displayAbout();
                DisplayUtils.switchSingleOrTwo(this, true);
                return true;
            case android.R.id.home:
                // app icon in action bar clicked; go home
                toggleSlideMenu();
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
        else if (getResources().getBoolean(R.bool.tablet_middle))
        {
            // Sepcific case where we want to display one or two panes.
            if (getFragmentManager().findFragmentById(DisplayUtils.getCentralFragmentId(this)) == null)
            {
                super.onBackPressed();
            }
            else
            {
                DisplayUtils.getLeftPane(this).setVisibility(View.VISIBLE);
                DisplayUtils.getCentralPane(this).setVisibility(View.GONE);

                Fragment fr = getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(this));

                boolean backStack = true;

                if (fr instanceof FileExplorerMenuFragment)
                {
                    backStack = false;
                }

                if (fr instanceof AccountsFragment)
                {
                    ((AccountsFragment) fr).unselect();
                    backStack = false;
                }

                if (fr instanceof ChildrenBrowserFragment)
                {
                    ((ChildrenBrowserFragment) fr).unselect();
                    backStack = false;
                }

                if (fr instanceof KeywordSearch)
                {
                    ((KeywordSearch) fr).unselect();
                    backStack = false;
                }

                if (fr instanceof ActivitiesFragment)
                {
                    backStack = false;
                }

                if (fr instanceof FavoritesFragment)
                {
                    backStack = false;
                }

                // Special case : if Activities Fragment
                if (backStack)
                {
                    getFragmentManager().popBackStack();
                }
                else
                {
                    FragmentDisplayer.remove(this,
                            getFragmentManager().findFragmentById(DisplayUtils.getCentralFragmentId(this)), false);
                }
            }
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

    private boolean hasNetwork()
    {
        if (!ConnectivityUtils.hasInternetAvailable(this))
        {
            Bundle b = new Bundle();
            b.putInt(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_network_title);
            b.putInt(SimpleAlertDialogFragment.PARAM_MESSAGE, R.string.error_network_details);
            b.putInt(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
            ActionManager.actionDisplayDialog(this, b);
            return false;
        }
        else
        {
            return true;
        }
    }

    public Site isDisplayFromSite()
    {
        return displayFromSite;
    }

    // For dropdown view in childrenbrowser
    public void setDisplayFromSite(Site site)
    {
        this.displayFromSite = site;
    }

    // For Creating file in childrenbrowser
    public Folder getImportParent()
    {
        if (getFragment(ChildrenBrowserFragment.TAG) != null)
        {
            importParent = ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).getImportFolder();
            if (importParent == null)
            {
                importParent = ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).getParent();
            }
        }
        return importParent;
    }

    public boolean hasActivateCheckPasscode()
    {
        return activateCheckPasscode;
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

            if (IntentIntegrator.ACTION_LOAD_ACCOUNT.equals(intent.getAction())
                    || IntentIntegrator.ACTION_RELOAD_ACCOUNT.equals(intent.getAction()))
            {
                // Change activity state to loading.
                setSessionState(SESSION_LOADING);

                if (!intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID)) { return; }

                // Assign the account
                currentAccount = AccountManager.retrieveAccount(context,
                        intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));

                // Return to root screen
                activity.getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                // Display progress
                activity.setProgressBarIndeterminateVisibility(true);

                return;
            }

            if (IntentIntegrator.ACTION_LOAD_ACCOUNT_STARTED.equals(intent.getAction()))
            {
                setSessionState(SESSION_LOADING);
                activity.setProgressBarIndeterminateVisibility(true);
                if (intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    Account acc = AccountManager.retrieveAccount(context,
                            intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
                    MessengerManager.showLongToast(activity, acc.getDescription());
                }
                return;
            }

            if (IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED.equals(intent.getAction()))
            {
                if (!isCurrentAccountToLoad(intent)) { return; }

                setSessionState(SESSION_ACTIVE);
                setProgressBarIndeterminateVisibility(false);

                // Affect session
                // currentSession = getCurrentSession()

                if (getCurrentSession() instanceof RepositorySession)
                {
                    DisplayUtils.switchSingleOrTwo(activity, false);
                }
                else if (getCurrentSession() instanceof CloudSession)
                {
                    DisplayUtils.switchSingleOrTwo(activity, true);
                }

                // Remove OAuthFragment if one
                if (getFragment(AccountOAuthFragment.TAG) != null)
                {
                    getFragmentManager().popBackStack(AccountOAuthFragment.TAG,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }

                if (getFragment(WaitingDialogFragment.TAG) != null)
                {
                    ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
                }

                // Used for launching last pressed action button from main menu
                if (fragmentQueue != -1)
                {
                    doMainMenuAction(fragmentQueue);
                }
                fragmentQueue = -1;

                // Save latest position as default future one
                AccountsPreferences.setDefaultAccount(activity, currentAccount.getId());

                // TODO Move to sessionManager/AccountManager ???
                // Check Last cloud session creation ==> prevent oauth token
                // expiration
                if (getCurrentSession() instanceof CloudSession)
                {
                    OAuthRefreshTokenCallback.saveLastCloudLoadingTime(activity);
                }
                else
                {
                    OAuthRefreshTokenCallback.removeLastCloudLoadingTime(activity);
                }

                // NB : temporary code ?
                // Check to see if we have an old account that needs its paid
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
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                        prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();

                        CipherUtils.EncryptionUserInteraction(activity);

                        currentAccount = accountManager.update(currentAccount.getId(), currentAccount.getDescription(),
                                currentAccount.getUrl(), currentAccount.getUsername(), currentAccount.getPassword(),
                                currentAccount.getRepositoryId(), currentAccount.getTypeId(),
                                currentAccount.getActivation(), currentAccount.getAccessToken(),
                                currentAccount.getRefreshToken(), 1);
                    }
                }
                return;
            }

            if (IntentIntegrator.ACTION_LOAD_ACCOUNT_ERROR.equals(intent.getAction()))
            {
                if (!isCurrentAccountToLoad(intent)) { return; }

                // Display error dialog message
                ActionManager.actionDisplayDialog(context, intent.getExtras());

                // Change status
                setSessionErrorMessageId(intent.getExtras().getInt(SimpleAlertDialogFragment.PARAM_MESSAGE));

                // Reset currentAccount & references
                if (intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    currentAccount = AccountManager.retrieveAccount(context,
                            intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
                    applicationManager.removeAccount(currentAccount.getId());
                }

                // Stop progress indication
                activity.setProgressBarIndeterminateVisibility(false);
                return;
            }

            if (IntentIntegrator.ACTION_ACCOUNT_INACTIVE.equals(intent.getAction()))
            {
                if (!isCurrentAccountToLoad(intent)) { return; }

                setSessionState(SESSION_INACTIVE);
                activity.setProgressBarIndeterminateVisibility(false);
                MessengerManager.showLongToast(activity, getString(R.string.account_not_activated));
                return;
            }

            if (IntentIntegrator.ACTION_USER_AUTHENTICATION.equals(intent.getAction()))
            {
                if (!isCurrentAccountToLoad(intent)) { return; }

                if (!intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID)) { return; }
                Long accountId = intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID);
                Account acc = AccountManager.retrieveAccount(activity, accountId);

                if (intent.getCategories().contains(IntentIntegrator.CATEGORY_OAUTH)
                        && getFragment(AccountOAuthFragment.TAG) == null
                        || getFragment(AccountOAuthFragment.TAG).isAdded())
                {
                    AccountOAuthFragment newFragment = AccountOAuthFragment.newInstance(acc);
                    FragmentDisplayer.replaceFragment(activity, newFragment, DisplayUtils.getMainPaneId(activity),
                            AccountOAuthFragment.TAG, true);
                    DisplayUtils.switchSingleOrTwo(activity, true);
                    return;
                }

                if (intent.getCategories().contains(IntentIntegrator.CATEGORY_OAUTH_REFRESH))
                {
                    getLoaderManager().restartLoader(OAuthRefreshTokenLoader.ID, null,
                            new OAuthRefreshTokenCallback(activity, acc, (CloudSession) getCurrentSession()));
                    return;
                }
                return;
            }

            return;
        }
    }

    // Due to dropdown the account loaded might not be the last one to load.
    private boolean isCurrentAccountToLoad(Intent intent)
    {
        if (currentAccount == null) { return false; }
        if (!intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID)) { return false; }
        return (currentAccount.getId() == intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
    }
}
