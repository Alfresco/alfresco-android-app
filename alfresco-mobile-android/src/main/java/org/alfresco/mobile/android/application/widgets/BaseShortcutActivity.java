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
package org.alfresco.mobile.android.application.widgets;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.fragments.account.AccountOAuthFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.favorite.FavoritesFragment;
import org.alfresco.mobile.android.application.fragments.site.browser.BrowserSitesPagerFragment;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodesRequest;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountCompletedEvent;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;

import com.squareup.otto.Subscribe;

/**
 * @author Jean Marie Pascal
 */
public class BaseShortcutActivity extends BaseActivity
{
    protected long requestedAccountId = -1;

    /** Define the type of importFolder. */
    protected int rootFolderTypeId;

    protected AlfrescoAccount uploadAccount;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        displayAsDialogActivity();
        setContentView(R.layout.app_left_panel);

        DocumentFolderPickerFragment.with(this).display();
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
        // TO Override by children class
    }

    public void setUploadFolder(int rootFolderTypeId)
    {
        this.rootFolderTypeId = rootFolderTypeId;
    }

    public void setUploadAccount(AlfrescoAccount account)
    {
        this.uploadAccount = account;
    }

    protected String getName(Folder folder, Site selectedSite)
    {
        String shortcutName = (folder.getProperty(PropertyIds.PATH).getValue());
        if (TextUtils.isEmpty(shortcutName))
        {
            shortcutName = folder.getName();
        }
        else
        {
            shortcutName = (selectedSite != null && shortcutName.startsWith("/Sites") && shortcutName
                    .endsWith("documentLibrary")) ? selectedSite.getTitle() : folder.getName();
        }
        return shortcutName;
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

        setProgressBarIndeterminateVisibility(false);

        // Remove OAuthFragment if one
        if (getFragment(AccountOAuthFragment.TAG) != null)
        {
            getSupportFragmentManager()
                    .popBackStack(AccountOAuthFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        removeWaitingDialog();

        // Upload process : Display the view where the user wants to upload
        // files.
        if (getCurrentSession() != null && rootFolderTypeId == R.string.menu_browse_sites)
        {
            BrowserSitesPagerFragment.with(this).display();
        }
        else if (getCurrentSession() != null && rootFolderTypeId == R.string.menu_browse_root)
        {
            DocumentFolderBrowserFragment.with(this).folder(getCurrentSession().getRootFolder()).display();
        }
        else if (getCurrentSession() != null && rootFolderTypeId == R.string.menu_favorites_folder)
        {
            FavoritesFragment.with(this).setMode(FavoriteNodesRequest.MODE_FOLDERS).display();
        }
    }

    @Subscribe
    public void onSessionRequested(RequestSessionEvent event)
    {
        requestedAccountId = event.accountToLoad.getId();
        currentAccount = event.accountToLoad;
        displayWaitingDialog();
    }
}
