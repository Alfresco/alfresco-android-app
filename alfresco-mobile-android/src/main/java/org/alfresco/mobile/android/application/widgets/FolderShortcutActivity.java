/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.widgets;

import java.io.File;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.intent.PublicIntentAPIUtils;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountCompletedEvent;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;

import android.content.Intent;
import android.view.View;

import com.squareup.otto.Subscribe;

/**
 * @author Jean Marie Pascal
 */
public class FolderShortcutActivity extends BaseShortcutActivity
{
    // ///////////////////////////////////////////////////////////////////////////
    // UI Public Method
    // ///////////////////////////////////////////////////////////////////////////
    public void validateAction(View v)
    {
        Folder parentFolder = ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG))
                .getParentFolder();
        createShortcut(uploadAccount, parentFolder);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Intent
    // ///////////////////////////////////////////////////////////////////////////
    public void createShortcut(AlfrescoAccount uploadAccount, File file)
    {
        createShortcut(file.getName(), PublicIntentAPIUtils.viewFile(uploadAccount.getId(), file));
    }
    
    private void createShortcut(AlfrescoAccount uploadAccount, Folder folder)
    {
        createShortcut(folder.getName(), PublicIntentAPIUtils.viewFolder(uploadAccount.getId(), folder.getIdentifier()));
    }
    
    private void createShortcut(String shortcutName, Intent shortcutIntent){
        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this, R.drawable.widget_folder));
        setResult(RESULT_OK, addIntent);
        finish();
        return;
    }

    // ////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////
    @Subscribe
    public void onAccountLoaded(LoadAccountCompletedEvent event)
    {
        super.onAccountLoaded(event);
    }

    @Subscribe
    public void onSessionRequested(RequestSessionEvent event)
    {
        super.onSessionRequested(event);
    }
}
