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

package org.alfresco.mobile.android.application.intent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.user.UserProfileFragment;
import org.alfresco.mobile.android.application.fragments.workflow.task.TasksFragment;
import org.alfresco.mobile.android.platform.intent.AlfrescoIntentAPI;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;

import android.content.Intent;
import android.net.Uri;
import androidx.fragment.app.FragmentActivity;

public class PublicIntentAPIUtils
{
    // ///////////////////////////////////////////////////////////////////////////
    // CREATION WITH EDITORS
    // ///////////////////////////////////////////////////////////////////////////
    public static Intent captureImageIntent()
    {
        return new Intent(AlfrescoIntentAPI.ACTION_CREATE).setType("image/jpg");
    }

    public static Intent createTextIntent()
    {
        return new Intent(AlfrescoIntentAPI.ACTION_CREATE).setType("text/plain");
    }

    public static Intent speechToTextIntent()
    {
        return createTextIntent().putExtra(AlfrescoIntentAPI.EXTRA_SPEECH2TEXT, true);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UPLOAD
    // ///////////////////////////////////////////////////////////////////////////
    public static Intent uploadFilesIntent(List<File> files, long accountId, String folderId)
    {
        ArrayList<String> filePaths = new ArrayList<String>(files.size());
        for (File file : files)
        {
            filePaths.add(file.getPath());
        }
        return uploadFilesIntent(filePaths, accountId, folderId);
    }

    public static Intent uploadFileIntent(File file, long accountId, String folderId)
    {
        ArrayList<String> filePaths = new ArrayList<String>(1);
        filePaths.add(file.getPath());
        return uploadFilesIntent(filePaths, accountId, folderId);
    }

    private static Intent uploadFilesIntent(ArrayList<String> filePaths, long accountId, String folderId)
    {
        return new Intent(AlfrescoIntentAPI.ACTION_SEND)
                .putStringArrayListExtra(PrivateIntent.EXTRA_FILE_PATH, filePaths)
                .putExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID, accountId)
                .putExtra(AlfrescoIntentAPI.EXTRA_FOLDER_ID, folderId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEW
    // ///////////////////////////////////////////////////////////////////////////
    public static Intent viewFolder(long accountId, String folderId)
    {
        Uri.Builder b = new Uri.Builder().scheme(AlfrescoIntentAPI.SCHEME).authority(AlfrescoIntentAPI.AUTHORITY_FOLDER)
                .appendPath(folderId);
        return new Intent(AlfrescoIntentAPI.ACTION_VIEW).setData(b.build()).putExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID,
                accountId);
    }

    public static Intent viewDocument(long accountId, String documentId)
    {
        Uri.Builder b = new Uri.Builder().scheme(AlfrescoIntentAPI.SCHEME)
                .authority(AlfrescoIntentAPI.AUTHORITY_DOCUMENT).appendPath(documentId);
        return new Intent(AlfrescoIntentAPI.ACTION_VIEW).setData(b.build()).putExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID,
                accountId);
    }

    public static Intent viewSite(long accountId, String siteShortName)
    {
        Uri.Builder b = new Uri.Builder().scheme(AlfrescoIntentAPI.SCHEME).authority(AlfrescoIntentAPI.AUTHORITY_SITE)
                .appendPath(siteShortName);
        return new Intent(AlfrescoIntentAPI.ACTION_VIEW).setData(b.build()).putExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID,
                accountId);
    }

    public static Intent viewFile(long accountId, File file)
    {
        Uri.Builder b = new Uri.Builder().scheme(AlfrescoIntentAPI.SCHEME).authority(AlfrescoIntentAPI.AUTHORITY_FILE)
                .appendPath(file.getPath());
        return new Intent(AlfrescoIntentAPI.ACTION_VIEW).setData(b.build()).putExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID,
                accountId);
    }

    public static Intent viewUser(long accountId, String userId)
    {
        Uri.Builder b = new Uri.Builder().scheme(AlfrescoIntentAPI.SCHEME).authority(AlfrescoIntentAPI.AUTHORITY_USER)
                .appendPath(userId);
        return new Intent(AlfrescoIntentAPI.ACTION_VIEW).setData(b.build()).putExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID,
                accountId);
    }

    public static Intent viewTasks(long accountId, Uri data)
    {
        return new Intent(AlfrescoIntentAPI.ACTION_VIEW).setData(data).putExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID,
                accountId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DISPATCH VIEW
    // ///////////////////////////////////////////////////////////////////////////
    public static void openShortcut(FragmentActivity context, Intent intent)
    {
        if (AlfrescoIntentAPI.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null)
        {
            if (AlfrescoIntentAPI.AUTHORITY_FOLDER.equals(intent.getData().getAuthority()))
            {
                DocumentFolderBrowserFragment.with(context).folderIdentifier(intent.getData().getPathSegments().get(0))
                        .shortcut(true).display();
            }
            else if (AlfrescoIntentAPI.AUTHORITY_FILE.equals(intent.getData().getAuthority()))
            {
                FileExplorerFragment.with(context).file(new File(intent.getData().getPathSegments().get(0))).display();
            }
            else if (AlfrescoIntentAPI.AUTHORITY_DOCUMENT.equals(intent.getData().getAuthority()))
            {
                NodeDetailsFragment.with(context).nodeId(intent.getData().getPathSegments().get(0)).back(true)
                        .display();
            }
            else if (AlfrescoIntentAPI.AUTHORITY_SITE.equals(intent.getData().getAuthority()))
            {
                DocumentFolderBrowserFragment.with(context).siteShortName(intent.getData().getPathSegments().get(0))
                        .back(true).display();
            }
            else if (AlfrescoIntentAPI.AUTHORITY_USER.equals(intent.getData().getAuthority()))
            {
                UserProfileFragment.with(context).personId(intent.getData().getPathSegments().get(0)).back(true)
                        .display();
            }
            else if (AlfrescoIntentAPI.AUTHORITY_TASKS.equals(intent.getData().getAuthority()))
            {
                TasksFragment.with(context).retrieveFilter(intent).back(true).display();
            }
        }
    }
}
