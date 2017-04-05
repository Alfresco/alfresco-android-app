/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.activity;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.alfresco.mobile.android.api.exceptions.AlfrescoSessionException;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.account.AccountsFragment;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.builder.FragmentBuilderFactory;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.favorite.FavoritesFragment;
import org.alfresco.mobile.android.application.fragments.node.upload.UploadFormFragment;
import org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences;
import org.alfresco.mobile.android.application.fragments.signin.AccountOAuthFragment;
import org.alfresco.mobile.android.application.fragments.signin.AccountSigninSamlFragment;
import org.alfresco.mobile.android.application.fragments.sync.SyncFragment;
import org.alfresco.mobile.android.application.intent.PublicIntentAPIUtils;
import org.alfresco.mobile.android.application.managers.NotificationManager;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodesRequest;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountCompletedEvent;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.intent.AlfrescoIntentAPI;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.node.browse.NodeBrowserTemplate;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Subscribe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Activity responsible to manage public intent from 3rd party application. This
 * activity is "open" to public Intent.
 * 
 * @author Jean Marie Pascal
 */
public class PublicDispatcherActivity extends BaseActivity
{
    private static final String TAG = PublicDispatcherActivity.class.getName();

    /** Define the type of importFolder. */
    private int uploadFolder;

    /** Define the local file to upload */
    private List<File> uploadFiles;

    protected long requestedAccountId = -1;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        displayAsDialogActivity();
        setContentView(R.layout.activitycompat_left_panel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
        {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        String action = getIntent().getAction();
        if ((Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action))
                && getFragment(UploadFormFragment.TAG) == null)
        {
            FragmentDisplayer.with(this).load(new UploadFormFragment()).back(false).animate(null)
                    .into(FragmentDisplayer.PANEL_LEFT);
            return;
        }

        if (Intent.ACTION_VIEW.equals(action)
                && AlfrescoIntentAPI.SCHEME.equals(getIntent().getData().getScheme().toLowerCase()))
        {
            managePublicIntent(null);
            return;
        }

        if (PrivateIntent.ACTION_SYNCHRO_DISPLAY.equals(action))
        {
            SyncFragment.with(this).mode(SyncFragment.MODE_PROGRESS).display();
            return;
        }

        if (PrivateIntent.ACTION_PICK_FILE.equals(action))
        {
            File f;
            if (getIntent().hasExtra(PrivateIntent.EXTRA_FOLDER))
            {
                f = (File) getIntent().getExtras().getSerializable(PrivateIntent.EXTRA_FOLDER);
                FragmentDisplayer.with(this)
                        .load(FileExplorerFragment.with(this).menuId(1).file(f).isShortCut(true)
                                .mode(ListingModeFragment.MODE_PICK).createFragment())
                        .back(false).into(FragmentDisplayer.PANEL_LEFT);
            }
        }
    }

    @Override
    protected void onStart()
    {
        getAppActionBar().setDisplayHomeAsUpEnabled(true);
        super.onStart();
        PassCodeActivity.requestUserPasscode(this);
        activateCheckPasscode = PasscodePreferences.hasPasscodeEnable(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (!activateCheckPasscode)
        {
            PasscodePreferences.updateLastActivityDisplay(this);
        }
    }

    @Override
    protected void onStop()
    {
        if (receiver != null)
        {
            broadcastManager.unregisterReceiver(receiver);
            receiver = null;
        }
        super.onStop();
    }

    private boolean validateIntentId()
    {
        List<String> pathSegments = getIntent().getData().getPathSegments();
        if (pathSegments == null || pathSegments.isEmpty())
        {
            NotificationManager.getInstance(this).showLongToast(R.string.public_url_wrong_format);
            finish();
            return false;
        }

        if (!AlfrescoIntentAPI.ID.equals(pathSegments.get(0)))
        {
            NotificationManager.getInstance(this).showLongToast(R.string.public_url_file_missing_id);
            finish();
            return false;
        }

        return true;
    }

    private boolean validateIntentFilter()
    {
        List<String> pathSegments = getIntent().getData().getPathSegments();
        if (pathSegments == null || pathSegments.isEmpty())
        {
            NotificationManager.getInstance(this).showLongToast(R.string.public_url_wrong_format);
            finish();
            return false;
        }

        if (!AlfrescoIntentAPI.FILTER.equals(pathSegments.get(0)))
        {
            NotificationManager.getInstance(this).showLongToast(R.string.public_url_tasks_missing_filter);
            finish();
            return false;
        }

        return true;
    }

    private String retrieveNodeRef()
    {
        String nodeRefIntent = getIntent().getData().getLastPathSegment();
        if (NodeRefUtils.isIdentifier(nodeRefIntent))
        {
            return NodeRefUtils.createNodeRefByIdentifier(getIntent().getData().getLastPathSegment());
        }
        else if (NodeRefUtils.isNodeRef(nodeRefIntent))
        {
            return nodeRefIntent;
        }
        else
        {
            NotificationManager.getInstance(this).showLongToast(R.string.public_url_noderef_format);
            finish();
            return null;
        }
    }

    public void managePublicIntent(AlfrescoAccount accountSelected)
    {
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())
                && AlfrescoIntentAPI.SCHEME.equals(getIntent().getData().getScheme().toLowerCase()))
        {
            Intent i = null;
            try
            {
                // Check Hostname
                // If no Hostname we just open the App
                String hostname = getIntent().getData().getHost();
                if (TextUtils.isEmpty(hostname))
                {
                    i = new Intent(this, MainActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }

                // If multiple account we have to request the user to select one
                if (AlfrescoAccountManager.getInstance(this).hasMultipleAccount() && accountSelected == null)
                {

                    FragmentDisplayer.with(this).animate(null).load(AccountsFragment.with(this).createFragment())
                            .back(false).into(FragmentDisplayer.PANEL_LEFT);
                    return;
                }

                // Check URL Pattern
                // We support only ids for the moment
                // alfresco://document/id/<objectId>
                AlfrescoAccount acc = accountSelected != null ? accountSelected : getCurrentAccount();
                if (acc == null)
                {
                    acc = AlfrescoAccountManager.getInstance(this).getDefaultAccount();
                }

                if (AlfrescoIntentAPI.AUTHORITY_DOCUMENT.equals(hostname))
                {
                    // Check Id
                    if (!validateIntentId()) { return; }
                    if (retrieveNodeRef() == null) { return; }

                    i = PublicIntentAPIUtils.viewDocument(acc.getId(), retrieveNodeRef());
                }
                else if (AlfrescoIntentAPI.AUTHORITY_FOLDER.equals(hostname))
                {
                    // Check Id
                    if (!validateIntentId()) { return; }
                    if (retrieveNodeRef() == null) { return; }

                    i = PublicIntentAPIUtils.viewFolder(acc.getId(), retrieveNodeRef());
                }
                else if (AlfrescoIntentAPI.AUTHORITY_SITE.equals(hostname))
                {
                    // Check Id
                    if (!validateIntentId()) { return; }
                    i = PublicIntentAPIUtils.viewSite(acc.getId(), getIntent().getData().getLastPathSegment());
                }
                else if (AlfrescoIntentAPI.AUTHORITY_USER.equals(hostname))
                {
                    // Check Id
                    if (!validateIntentId()) { return; }
                    i = PublicIntentAPIUtils.viewUser(acc.getId(), getIntent().getData().getLastPathSegment());
                }
                else if (AlfrescoIntentAPI.AUTHORITY_TASKS.equals(hostname))
                {
                    if (!validateIntentFilter()) { return; }
                    i = PublicIntentAPIUtils.viewTasks(acc.getId(), getIntent().getData());
                }
                else
                {
                    i = new Intent(this, MainActivity.class);
                }
            }
            catch (Exception e)
            {
                i = new Intent(this, MainActivity.class);
            }

            startActivity(i);
            finish();
            return;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI Public Method
    // ///////////////////////////////////////////////////////////////////////////
    public void doCancel(View v)
    {
        finish();
    }

    public void validateAction(View v)
    {
        ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG)).createFiles(uploadFiles);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        if (isVisible(DocumentFolderBrowserFragment.TAG))
        {
            ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG)).getMenu(menu);
            return true;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                if (getIntent() != null && PrivateIntent.ACTION_PICK_FILE.equals(getIntent().getAction()))
                {
                    finish();
                }
                else
                {
                    Intent i = new Intent(this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public void setUploadFolder(int uploadFolderType)
    {
        this.uploadFolder = uploadFolderType;
    }

    public void setUploadFile(List<File> localFile)
    {
        this.uploadFiles = localFile;
    }

    // ////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////
    @Subscribe
    public void onAccountLoaded(LoadAccountCompletedEvent event)
    {
        // If the session is available, display the view associated
        // (repository, sites, downloads, favorites).
        if (event == null || event.account == null) { return; }
        if (requestedAccountId != -1 && requestedAccountId != event.account.getId()) { return; }
        requestedAccountId = -1;

        setSupportProgressBarIndeterminateVisibility(false);

        // Remove OAuthFragment if one
        if (getFragment(AccountOAuthFragment.TAG) != null)
        {
            getSupportFragmentManager().popBackStack(AccountOAuthFragment.TAG,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        if (getFragment(AccountSigninSamlFragment.TAG) != null)
        {
            getSupportFragmentManager().popBackStack(AccountSigninSamlFragment.TAG,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        removeWaitingDialog();

        // Upload process : Display the view where the user wants to upload
        // files.
        if (getCurrentSession() == null) { return; }
        String type = null;
        HashMap<String, Object> props = new HashMap<String, Object>();
        switch (uploadFolder)
        {
            case R.string.menu_browse_sites:
                type = ConfigurationConstant.KEY_SITE_BROWSER;
                break;
            case R.string.menu_browse_root:
                type = ConfigurationConstant.KEY_REPOSITORY;
                break;
            case R.string.menu_favorites_folder:
                FavoritesFragment.with(this).setMode(FavoriteNodesRequest.MODE_FOLDERS).display();
                return;
            case R.string.menu_browse_userhome:
                type = ConfigurationConstant.KEY_REPOSITORY;
                props.put(NodeBrowserTemplate.ARGUMENT_FOLDER_TYPE_ID, NodeBrowserTemplate.FOLDER_TYPE_USERHOME);
                break;
            default:
                break;
        }

        if (type != null)
        {
            AlfrescoFragmentBuilder viewConfig = FragmentBuilderFactory.createViewConfig(this, type, props);
            if (viewConfig == null) { return; }
            viewConfig.display();
        }
    }

    @Subscribe
    public void onSessionRequested(RequestSessionEvent event)
    {
        requestedAccountId = event.accountToLoad.getId();
        setCurrentAccount(event.accountToLoad);
        displayWaitingDialog();
    }

    @Subscribe
    public void onAccountErrorEvent(LoadSessionCallBack.LoadAccountErrorEvent event)
    {
        // SAML Exception ?
        if (event.exception instanceof AlfrescoSessionException
                && event.account.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CMIS_SAML)
        {
            AccountSigninSamlFragment.with(this).isCreation(false).account(event.account).display();
            removeWaitingDialog();
        }
        else
        {
            // General Errors
            // Display error dialog message
            new MaterialDialog.Builder(this).iconRes(R.drawable.ic_application_logo)
                    .title(R.string.error_session_creation_message).content(Html.fromHtml(getString(event.messageId)))
                    .positiveText(android.R.string.ok).show();
        }

        // Reset currentAccount & references
        setCurrentAccount(AlfrescoAccountManager.getInstance(this).retrieveAccount(event.data));

        // Stop progress indication
        setSupportProgressBarIndeterminateVisibility(false);

        invalidateOptionsMenu();
    }
}
