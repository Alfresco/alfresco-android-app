/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
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
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.intent.PublicIntentAPIUtils;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountCompletedEvent;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.intent.AlfrescoIntentAPI;

import android.content.Intent;
import android.view.View;

import com.squareup.otto.Subscribe;

/**
 * @author Jean Marie Pascal
 */
public class ActionShortcutActivity extends BaseShortcutActivity
{
    protected int actionId = -1;

    // ///////////////////////////////////////////////////////////////////////////
    // UI Public Method
    // ///////////////////////////////////////////////////////////////////////////
    public void validateAction(View v)
    {
        DocumentFolderBrowserFragment frag = (DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG);
        if (frag == null) { return; }
        createShortcut(uploadAccount, frag.getParentFolder(), frag.getSite(), actionId);
    }

    public void setActionId(int actionId)
    {
        this.actionId = actionId;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Intent
    // ///////////////////////////////////////////////////////////////////////////
    private void createShortcut(AlfrescoAccount uploadAccount, Folder folder, Site site, int actionId)
    {
        // Intent associated to the shortcut
        Intent actionIntent = null;
        String label = null;
        int iconId = R.drawable.widget_doc;
        switch (actionId)
        {
            case R.string.action_text:
                actionIntent = PublicIntentAPIUtils.createTextIntent();
                iconId = R.drawable.widget_doc;
                label = AnalyticsManager.LABEL_CREATE_TEXT;
                break;
            case R.string.action_speech2text:
                actionIntent = PublicIntentAPIUtils.speechToTextIntent();
                iconId = R.drawable.widget_mic;
                label = AnalyticsManager.LABEL_SPEECH_2_TEXT;
                break;
            case R.string.action_photo:
                actionIntent = PublicIntentAPIUtils.captureImageIntent();
                iconId = R.drawable.widget_photo;
                label = AnalyticsManager.LABEL_TAKE_PHOTO;
                break;
            default:
                break;
        }
        actionIntent.putExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID, uploadAccount.getId()).putExtra(
                AlfrescoIntentAPI.EXTRA_FOLDER_ID, folder.getIdentifier());

        // Result Intent
        Intent shortcutIntent = new Intent();
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, actionIntent);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getName(folder, site));
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this, iconId));
        setResult(RESULT_OK, shortcutIntent);

        // Analytics
        AnalyticsHelper.reportOperationEvent(this, AnalyticsManager.CATEGORY_WIDGET, AnalyticsManager.ACTION_CREATE,
                label, 1, false);

        finish();
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
