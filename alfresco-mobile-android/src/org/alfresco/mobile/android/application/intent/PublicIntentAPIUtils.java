package org.alfresco.mobile.android.application.intent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;

import android.content.Intent;
import android.net.Uri;

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
        Uri.Builder b = new Uri.Builder().scheme(AlfrescoIntentAPI.SCHEME)
                .authority(AlfrescoIntentAPI.AUTHORITY_FOLDER).appendPath(folderId);
        return new Intent(AlfrescoIntentAPI.ACTION_VIEW).setData(b.build()).putExtra(
                AlfrescoIntentAPI.EXTRA_ACCOUNT_ID, accountId);
    }

    public static Intent viewDocument(long accountId, String documentId)
    {
        Uri.Builder b = new Uri.Builder().scheme(AlfrescoIntentAPI.SCHEME)
                .authority(AlfrescoIntentAPI.AUTHORITY_DOCUMENT).appendPath(documentId);
        return new Intent(AlfrescoIntentAPI.ACTION_VIEW).setData(b.build()).putExtra(
                AlfrescoIntentAPI.EXTRA_ACCOUNT_ID, accountId);
    }

    public static Intent viewFile(long accountId, File file)
    {
        Uri.Builder b = new Uri.Builder().scheme(AlfrescoIntentAPI.SCHEME)
                .authority(AlfrescoIntentAPI.AUTHORITY_FILE).appendPath(file.getPath());
        return new Intent(AlfrescoIntentAPI.ACTION_VIEW).setData(b.build()).putExtra(
                AlfrescoIntentAPI.EXTRA_ACCOUNT_ID, accountId);
    }
}
