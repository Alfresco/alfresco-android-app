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
package org.alfresco.mobile.android.application;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.accounts.fragment.AccountDetailsFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountEditFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountLoginLoaderCallback;
import org.alfresco.mobile.android.application.accounts.fragment.AccountOAuthFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountTypesFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountsLoader;
import org.alfresco.mobile.android.application.accounts.fragment.AccountsLoaderCallback;
import org.alfresco.mobile.android.application.accounts.networks.CloudNetworksFragment;
import org.alfresco.mobile.android.application.accounts.oauth.OAuthRefreshTokenCallback;
import org.alfresco.mobile.android.application.accounts.oauth.OAuthRefreshTokenLoader;
import org.alfresco.mobile.android.application.accounts.signup.CloudSignupDialogFragment;
import org.alfresco.mobile.android.application.exception.AlfrescoAppException;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.fragments.activities.ActivitiesFragment;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.browser.UploadChooseDialogFragment;
import org.alfresco.mobile.android.application.fragments.browser.local.LocalFileBrowserFragment;
import org.alfresco.mobile.android.application.fragments.comments.CommentsFragment;
import org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment;
import org.alfresco.mobile.android.application.fragments.encryption.EncryptionDialogFragment;
import org.alfresco.mobile.android.application.fragments.menu.MainMenuFragment;
import org.alfresco.mobile.android.application.fragments.properties.DetailsFragment;
import org.alfresco.mobile.android.application.fragments.search.KeywordSearch;
import org.alfresco.mobile.android.application.fragments.sites.BrowserSitesFragment;
import org.alfresco.mobile.android.application.fragments.versions.VersionFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.ReportManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.preferences.PasscodePreferences;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.AudioCapture;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.alfresco.mobile.android.application.utils.PhotoCapture;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.VideoCapture;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;

@TargetApi(11)
public class MainActivity extends Activity
{

    public static Activity activity = null;

    private static final String TAG = "MainActivity";

    private Stack<String> stackCentral = new Stack<String>();

    private Node currentNode;

    private int fragmentQueue = -1;

    private Account currentAccount;

    private PhotoCapture photoCapture = null;

    private VideoCapture videoCapture = null;

    private AudioCapture audioCapture = null;

    private List<Account> accounts;

    private Site displayFromSite = null;

    private AccountsLoaderCallback loadercallback;

    private Folder importParent;

    private int sessionState = 0;

    private int sessionStateErrorMessageId;

    public static final int SESSION_LOADING = 0;

    public static final int SESSION_ACTIVE = 1;

    public static final int SESSION_ERROR = 2;

    private static final String PARAM_ACCOUNT = "account";

    private static final String PARAM_DISPLAY_FROM_SITE = "displayFromSite";

    private static final String PARAM_IMPORT_PARENT = "importParent";

    private static final String PARAM_FRAGMENT_QUEUE = "fragmentQueue";

    private static final String PARAM_STACK_CENTRAL = "stackCentral";

    private static final String PARAM_CAPTURE_AUDIO = "audioCap";

    private static final String PARAM_CAPTURE_VIDEO = "videoCap";

    private static final String PARAM_CAPTURE_PHOTO = "photoCap";

    private static final String PREF_MIGRATION_FILES = "filesmigrated";

    // ///////////////////////////////////////////
    // INIT
    // ///////////////////////////////////////////
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        activity = this;

        super.onCreate(savedInstanceState);

        // Loading progress
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.app_main);

        setProgressBarIndeterminateVisibility(false);
        // Load Accounts
        if (loadercallback == null)
        {
            loadercallback = new AccountsLoaderCallback(this);
        }
        refreshAccounts();

        if (savedInstanceState != null)
        {
            currentAccount = (Account) savedInstanceState.getSerializable(PARAM_ACCOUNT);
            if (savedInstanceState.containsKey(PARAM_DISPLAY_FROM_SITE))
            {
                displayFromSite = (Site) savedInstanceState.getSerializable(PARAM_DISPLAY_FROM_SITE);
            }

            if (savedInstanceState.containsKey(PARAM_IMPORT_PARENT))
            {
                importParent = (Folder) savedInstanceState.getSerializable(PARAM_IMPORT_PARENT);
            }

            if (savedInstanceState.containsKey(PARAM_FRAGMENT_QUEUE))
            {
                fragmentQueue = savedInstanceState.getInt(PARAM_FRAGMENT_QUEUE);
                savedInstanceState.remove(PARAM_FRAGMENT_QUEUE);
            }

            audioCapture = (AudioCapture) savedInstanceState.getSerializable(PARAM_CAPTURE_AUDIO);
            if (audioCapture != null)
            {
                audioCapture.setActivity(this);
            }

            videoCapture = (VideoCapture) savedInstanceState.getSerializable(PARAM_CAPTURE_VIDEO);
            if (videoCapture != null)
            {
                videoCapture.setActivity(this);
            }

            photoCapture = (PhotoCapture) savedInstanceState.getSerializable(PARAM_CAPTURE_PHOTO);
            if (photoCapture != null)
            {
                photoCapture.setActivity(this);
            }

            String[] d = savedInstanceState.getStringArray(PARAM_STACK_CENTRAL);
            if (d != null)
            {
                List<String> list = Arrays.asList(d);
                stackCentral = new Stack<String>();
                stackCentral.addAll(list);
            }
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

        if (IntentIntegrator.ACTION_CHECK_SIGNUP.equals(getIntent().getAction()))
        {
            displayAccounts();
        }

        // Display or not Left/central panel for middle tablet.
        DisplayUtils.switchSingleOrTwo(this, false);

        // Transfer downloads to new folder structure if they haven't been
        // already.
        if (!prefs.getBoolean(PREF_MIGRATION_FILES, false))
        {
            File oldDownloads = StorageManager.getOldDownloadFolder(this);
            File newDownloads = StorageManager.getPrivateFolder(this, "", "", "");

            if (IOUtils.isFolderEmpty(oldDownloads) == false)
            {
                if (oldDownloads != null && newDownloads != null)
                {
                    IOUtils.transferFilesBackground(oldDownloads.getPath(), newDownloads.getPath(),
                            StorageManager.DLDIR, true, true);
                    prefs.edit().putBoolean(PREF_MIGRATION_FILES, true).commit();
                }
            }
            else
            {
                prefs.edit().putBoolean(PREF_MIGRATION_FILES, true).commit();
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        checkForCrashes();
        PassCodeActivity.requestUserPasscode(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        OAuthRefreshTokenCallback.requestRefreshToken(getSession(), this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (!PasscodePreferences.hasPasscodeEnable(this))
        {
            PasscodePreferences.updateLastActivityDisplay(this);
        }
    }

    // ///////////////////////////////////////////
    // HockeyApp Integration
    // ///////////////////////////////////////////
    private void checkForCrashes()
    {
        ReportManager.checkForCrashes(this);
    }

    private void checkForUpdates()
    {
        ReportManager.checkForUpdates(this);
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

        if (requestCode == PassCodeActivity.REQUEST_CODE_PASSCODE && resultCode == RESULT_CANCELED)
        {
            finish();
        }

        if (photoCapture != null && requestCode == photoCapture.getRequestCode())
        {
            photoCapture.capturedCallback(requestCode, resultCode, data);
        }
        else if (videoCapture != null && requestCode == videoCapture.getRequestCode())
        {
            videoCapture.capturedCallback(requestCode, resultCode, data);
        }
        else if (audioCapture != null && requestCode == audioCapture.getRequestCode())
        {
            audioCapture.capturedCallback(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        if (PasscodePreferences.hasPasscodeEnable(this)) { return; }

        try
        {
            Boolean backstack = false;

            // Intent after session loading
            if (IntentIntegrator.ACTION_LOAD_SESSION_FINISH.equals(intent.getAction()))
            {
                setSessionState(SESSION_ACTIVE);
                setProgressBarIndeterminateVisibility(false);

                if (getSession() instanceof RepositorySession)
                {
                    DisplayUtils.switchSingleOrTwo(this, false);
                }
                else if (getSession() instanceof CloudSession)
                {
                    DisplayUtils.switchSingleOrTwo(this, true);
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

                if (getFragment(AccountFragment.TAG) != null)
                {
                    ((AccountFragment) getFragment(AccountFragment.TAG)).refresh();
                }

                // Used for launching last pressed action button from main menu
                if (fragmentQueue != -1)
                {
                    doMainMenuAction(fragmentQueue);
                }
                fragmentQueue = -1;

                // reload account
                refreshAccounts();

                if (SessionUtils.getAccount(this) != null)
                {
                    currentAccount = SessionUtils.getAccount(this);
                }

                // Check Last cloud session creation ==> prevent oauth token expiration
                if (getSession() instanceof CloudSession)
                {
                    OAuthRefreshTokenCallback.saveLastCloudLoadingTime(this);
                }
                else
                {
                    OAuthRefreshTokenCallback.removeLastCloudLoadingTime(this);
                }

                // Check to see if we have an old account that needs its paid
                // network flag setting.
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (!currentAccount.getIsPaidAccount())
                {
                    boolean paidNetwork = false;
                    AlfrescoSession session = getSession();

                    if (session instanceof CloudSession)
                    {
                        paidNetwork = ((CloudSession) session).getNetwork().isPaidNetwork();
                    }
                    else
                    {
                        paidNetwork = session.getRepositoryInfo().getEdition()
                                .equals(OnPremiseConstant.ALFRESCO_EDITION_ENTERPRISE);
                    }

                    if (paidNetwork)
                    {
                        prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();

                        CipherUtils.EncryptionUserInteraction(activity);

                        AccountDAO serverDao = new AccountDAO(this, SessionUtils.getDataBaseManager(this).getWriteDb());
                        serverDao.update(currentAccount.getId(), currentAccount.getDescription(),
                                currentAccount.getUrl(), currentAccount.getUsername(), currentAccount.getPassword(),
                                currentAccount.getRepositoryId(), currentAccount.getTypeId(),
                                currentAccount.getActivation(), currentAccount.getAccessToken(),
                                currentAccount.getRefreshToken(), 1);
                    }
                }

                return;
            }

            // Intent for USER AUTHENTICATION
            if (IntentIntegrator.ACTION_USER_AUTHENTICATION.equals(intent.getAction()))
            {
                // if click on menu, hide the dialog
                if (getFragment(WaitingDialogFragment.TAG) != null)
                {
                    ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
                }

                if (accounts == null) { return;}
                
                for (Account account : accounts)
                {
                    if (account.getId() == intent.getExtras().getLong(IntentIntegrator.ACCOUNT_TYPE))
                    {
                        if (intent.getCategories().contains(IntentIntegrator.CATEGORY_OAUTH_REFRESH))
                        {
                            getLoaderManager().restartLoader(OAuthRefreshTokenLoader.ID, null,
                                    new OAuthRefreshTokenCallback(this, getAccount(), (CloudSession) getSession()));
                            return;
                        }
                        else if (intent.getCategories().contains(IntentIntegrator.CATEGORY_OAUTH)
                                && getFragment(AccountOAuthFragment.TAG) == null
                                || getFragment(AccountOAuthFragment.TAG).isAdded())
                        {
                            AccountOAuthFragment newFragment = AccountOAuthFragment.newInstance(account);
                            FragmentDisplayer.replaceFragment(this, newFragment, DisplayUtils.getMainPaneId(this),
                                    AccountOAuthFragment.TAG, true);
                            DisplayUtils.switchSingleOrTwo(this, true);
                            return;
                        }
                    }
                }
                return;
            }

            // Intent for Removing Fragment + eventual associated loader.
            if (IntentIntegrator.ACTION_REMOVE_FRAGMENT.equals(intent.getAction()))
            {
                EncryptionDialogFragment.removeFragment(this, intent);
                return;
            }

            // Intent for Display Errors
            if (IntentIntegrator.ACTION_DISPLAY_ERROR.equals(intent.getAction()))
            {
                if (getFragment(WaitingDialogFragment.TAG) != null)
                {
                    ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
                }
                Exception e = (Exception) intent.getExtras().getSerializable(IntentIntegrator.DISPLAY_ERROR_DATA);

                String errorMessage = getString(R.string.error_general);
                if (e instanceof AlfrescoAppException && ((AlfrescoAppException) e).isDisplayMessage())
                {
                    errorMessage = e.getMessage();
                }

                MessengerManager.showLongToast(this, errorMessage);

                CloudExceptionUtils.handleCloudException(this, e, false);

                return;
            }

            // Intent for Display Dialog
            if (IntentIntegrator.ACTION_DISPLAY_DIALOG.equals(intent.getAction()))
            {
                SimpleAlertDialogFragment.newInstance(intent.getExtras()).show(getFragmentManager(),
                        SimpleAlertDialogFragment.TAG);
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

            // DISPLAY NODE based on URL
            if (IntentIntegrator.ACTION_DISPLAY_NODE.equals(intent.getAction()))
            {
                DetailsFragment fr = (DetailsFragment) getFragment(DetailsFragment.TAG);
                if (fr != null)
                {
                    fr.refresh();
                }
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

            if (IntentIntegrator.ACTION_REFRESH.equals(intent.getAction()))
            {
                if (getFragment(WaitingDialogFragment.TAG) != null)
                {
                    ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
                }

                if (intent.getCategories().contains(IntentIntegrator.CATEGORY_REFRESH_OTHERS))
                {
                    if (IntentIntegrator.ACCOUNT_TYPE.equals(intent.getType()))
                    {
                        getFragmentManager().popBackStack(AccountDetailsFragment.TAG,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        if (getFragment(AccountFragment.TAG) != null)
                        {
                            ((AccountFragment) getFragment(AccountFragment.TAG)).refresh();
                        }
                        refreshAccounts();
                        clearScreen();
                    }
                    else if (IntentIntegrator.FILE_TYPE.equals(intent.getType()))
                    {
                        ((LocalFileBrowserFragment) getFragment(LocalFileBrowserFragment.TAG)).refresh();
                    }
                    else
                    {
                        ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).refresh();
                        FragmentDisplayer.removeFragment(this, DetailsFragment.TAG);
                        if (!DisplayUtils.hasCentralPane(this))
                        {
                            getFragmentManager().popBackStack();
                        }
                    }
                }
                else if (intent.getCategories().contains(IntentIntegrator.CATEGORY_REFRESH_ALL))
                {
                    if (IntentIntegrator.ACCOUNT_TYPE.equals(intent.getType()))
                    {
                        getFragmentManager().popBackStack(AccountTypesFragment.TAG,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE);

                        if (getFragment(AccountFragment.TAG) != null)
                        {
                            ((AccountFragment) getFragment(AccountFragment.TAG)).refresh();
                        }
                    }
                    else
                    {
                        if (getCurrentNode() != null)
                        {
                            SessionUtils.getRenditionManager(this).removeFromCache(getCurrentNode().getIdentifier());
                        }
                        if (getFragment(ChildrenBrowserFragment.TAG) != null)
                        {
                            ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).refresh();
                        }
                        if (!DisplayUtils.hasCentralPane(this))
                        {
                            backstack = true;
                            getFragmentManager().popBackStack(DetailsFragment.TAG,
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }
                        addPropertiesFragment(currentNode, getImportParent(), backstack);
                    }
                }
                else if (intent.getCategories().contains(IntentIntegrator.CATEGORY_REFRESH_DELETE))
                {
                    ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).refresh();
                    if (!DisplayUtils.hasCentralPane(this))
                    {
                        getFragmentManager().popBackStack();
                    }
                    else
                    {
                        FragmentDisplayer.removeFragment(this, DetailsFragment.TAG);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        String[] stringArray = Arrays.copyOf(stackCentral.toArray(), stackCentral.size(), String[].class);
        outState.putStringArray(PARAM_STACK_CENTRAL, stringArray);
        outState.putSerializable(PARAM_ACCOUNT, currentAccount);
        if (audioCapture != null)
        {
            outState.putSerializable(PARAM_CAPTURE_AUDIO, audioCapture);
        }

        if (videoCapture != null)
        {
            outState.putSerializable(PARAM_CAPTURE_VIDEO, videoCapture);
        }

        if (photoCapture != null)
        {
            outState.putSerializable(PARAM_CAPTURE_PHOTO, photoCapture);
        }
        outState.putInt(PARAM_FRAGMENT_QUEUE, fragmentQueue);

        if (displayFromSite != null)
        {
            outState.putSerializable(PARAM_DISPLAY_FROM_SITE, displayFromSite);
        }

        if (importParent != null)
        {
            outState.putParcelable(PARAM_IMPORT_PARENT, importParent);
        }
    }

    // ///////////////////////////////////////////
    // SWITCH ACCOUNT
    // ///////////////////////////////////////////
    public void loadAccount(Account account)
    {
        // TODO Remove this indication ?
        MessengerManager.showToast(this, getString(R.string.account_loading) + " " + account.getDescription());
        SessionUtils.setsession(this, null);
        SessionUtils.setAccount(this, account);
        if (account.getActivation() != null)
        {
            MessengerManager.showLongToast(this, getString(R.string.account_not_activated));
        }
        else
        {
            setProgressBarIndeterminateVisibility(true);
            AccountLoginLoaderCallback call = new AccountLoginLoaderCallback(this, account);
            // If the loader already exist, we do nothing.
            // It prevents to request OAuth token multiple times.
            if (getLoaderManager().getLoader(call.getAccountLoginLoaderId()) == null)
            {
                getLoaderManager().restartLoader(call.getAccountLoginLoaderId(), null, call);
            }
        }
    }

    // ///////////////////////////////////////////
    // SLIDE MENU
    // ///////////////////////////////////////////
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
                slidefragment.setAccounts(accounts);
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
        currentAccount = SessionUtils.getAccount(this);

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
                frag = ChildrenBrowserFragment.newInstance(getSession().getRootFolder());
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
            case R.id.menu_download:
                if (currentAccount == null)
                {
                    MessengerManager.showLongToast(this, getString(R.string.loginfirst));
                }
                else
                {
                    File folder = StorageManager.getDownloadFolder(this, currentAccount.getUrl(),
                            currentAccount.getUsername());
                    if (folder != null)
                    {
                        addLocalFileNavigationFragment(folder);
                    }
                    else
                    {
                        MessengerManager.showLongToast(this, getString(R.string.sdinaccessible));
                    }
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

    public void setSessionState(int state)
    {
        sessionState = state;
    }

    public void setSessionErrorMessageId(int messageId)
    {
        sessionState = SESSION_ERROR;
        sessionStateErrorMessageId = messageId;
    }

    private boolean checkSession(int actionMainMenuId)
    {
        if (sessionState == SESSION_ERROR)
        {
            Bundle b = new Bundle();
            b.putInt(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_alfresco_logo);
            b.putInt(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_session_creation_message);
            b.putInt(SimpleAlertDialogFragment.PARAM_MESSAGE, sessionStateErrorMessageId);
            b.putInt(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
            ActionManager.actionDisplayDialog(this, b);
            return false;
        }
        else if (!hasNetwork())
        {
            return false;
        }
        else if (SessionUtils.getAccount(this) != null && SessionUtils.getAccount(this).getActivation() != null)
        {
            MessengerManager.showToast(this, R.string.account_not_activated);
            return false;
        }
        else if (SessionUtils.getSession(this) == null)
        {
            displayWaitingDialog();
            fragmentQueue = actionMainMenuId;
            return false;
        }

        return true;
    }

    private void displayWaitingDialog()
    {
        new WaitingDialogFragment().show(getFragmentManager(), WaitingDialogFragment.TAG);
    }

    // ///////////////////////////////////////////
    // FRAGMENTS
    // ///////////////////////////////////////////
    public void addNavigationFragment(Folder f)
    {
        clearScreen();
        clearCentralPane();
        BaseFragment frag = ChildrenBrowserFragment.newInstance(f);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                ChildrenBrowserFragment.TAG, true);
    }

    public void addNavigationFragment(String path)
    {
        clearScreen();
        clearCentralPane();
        BaseFragment frag = ChildrenBrowserFragment.newInstance(path);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                ChildrenBrowserFragment.TAG, true);
    }

    public void addNavigationFragment(Site s)
    {
        clearScreen();
        clearCentralPane();
        setDisplayFromSite(s);
        BaseFragment frag = ChildrenBrowserFragment.newInstance(s);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                ChildrenBrowserFragment.TAG, true);
    }

    public void addLocalFileNavigationFragment(File file)
    {
        clearScreen();
        clearCentralPane();
        BaseFragment frag = LocalFileBrowserFragment.newInstance(file);
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                LocalFileBrowserFragment.TAG, true);
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
            stackCentral.push(AccountFragment.TAG);
        }
        BaseFragment frag = AccountDetailsFragment.newInstance(id);
        frag.setSession(SessionUtils.getSession(this));
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
                showDialog(GET_PDF_VIEWER);
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
        Fragment f = new AccountFragment();
        FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), AccountFragment.TAG, true);
    }

    public void displayNetworks()
    {
        if (getSession() instanceof CloudSession)
        {
            Fragment f = new CloudNetworksFragment();
            FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), CloudNetworksFragment.TAG,
                    true);
        }
    }

    // ///////////////////////////////////////////
    // UTILS FRAGMENTS
    // ///////////////////////////////////////////
    public int getFragmentPlace()
    {
        int id = R.id.left_pane_body;
        if (DisplayUtils.hasCentralPane(this))
        {
            id = R.id.central_pane_body;
        }
        return id;
    }

    public int getFragmentPlace(boolean forceRight)
    {
        int id = R.id.left_pane_body;
        if (forceRight && DisplayUtils.hasCentralPane(this))
        {
            id = R.id.central_pane_body;
        }
        return id;
    }

    public int getFragmentPlaceId()
    {
        if (DisplayUtils.hasCentralPane(this))
        {
            return DisplayUtils.getCentralFragmentId(this);
        }
        else
        {
            return DisplayUtils.getLeftFragmentId(this);
        }
    }

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

    // //////////////////////////////////////////////////////////////////////
    // ///////// ACTION BAR ///////////////////
    // //////////////////////////////////////////////////////////////////////
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

        if (isVisible(LocalFileBrowserFragment.TAG))
        {
            ((LocalFileBrowserFragment) getFragment(LocalFileBrowserFragment.TAG)).getMenu(menu);
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

        if (isVisible(AccountFragment.TAG) && !isVisible(AccountTypesFragment.TAG)
                && !isVisible(AccountEditFragment.TAG) && !isVisible(AccountOAuthFragment.TAG))
        {
            AccountFragment.getMenu(menu);
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
                    audioCapture = null;
                    videoCapture = null;
                    photoCapture = new PhotoCapture(this, parentFolder);

                    photoCapture.captureData();
                }
                return true;

            case MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_VIDEO:

                if (parentFolder != null)
                {
                    audioCapture = null;
                    photoCapture = null;
                    videoCapture = new VideoCapture(this, parentFolder);

                    videoCapture.captureData();
                }
                return true;

            case MenuActionItem.MENU_DEVICE_CAPTURE_MIC_AUDIO:
                if (parentFolder != null)
                {
                    photoCapture = null;
                    videoCapture = null;
                    audioCapture = new AudioCapture(this, parentFolder);

                    audioCapture.captureData();
                }
                return true;

            case MenuActionItem.MENU_ACCOUNT_ADD:
                ((AccountFragment) getFragment(AccountFragment.TAG)).add();
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
                ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).createFolder();
                return true;

            case MenuActionItem.MENU_CREATE_DOCUMENT:
                String fragmentTag = LocalFileBrowserFragment.TAG;
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
            case MenuActionItem.MENU_DELETE_FOLDER:
                ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).delete();
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

                if (fr instanceof AccountFragment)
                {
                    ((AccountFragment) fr).unselect();
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

    // ///////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////

    public Fragment getFragment(String tag)
    {
        return getFragmentManager().findFragmentByTag(tag);
    }

    public AlfrescoSession getSession()
    {
        return SessionUtils.getSession(this);
    }

    public Account getAccount()
    {
        return currentAccount;
    }

    public Node getCurrentNode()
    {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode)
    {
        this.currentNode = currentNode;
    }

    public void refreshAccounts()
    {
        getLoaderManager().restartLoader(AccountsLoader.ID, null, loadercallback);
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

    public static final int GET_PDF_VIEWER = 700;

    @Override
    protected Dialog onCreateDialog(int id)
    {
        AlertDialog dialog = null;
        switch (id)
        {
            case GET_PDF_VIEWER:
                dialog = new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(R.string.get_pdf_viewer)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                dialog.dismiss();
                            }
                        }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                ActionManager.getAdobeReader(MainActivity.this);
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
        }
        return super.onCreateDialog(id);
    }

    public void setAccounts(List<Account> accounts)
    {
        this.accounts = accounts;
    }

    public List<Account> getAccounts()
    {
        return accounts;
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
}
