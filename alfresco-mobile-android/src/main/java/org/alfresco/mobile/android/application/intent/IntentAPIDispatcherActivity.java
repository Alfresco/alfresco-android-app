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
package org.alfresco.mobile.android.application.intent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.capture.DeviceCapture;
import org.alfresco.mobile.android.application.editors.text.TextEditorActivity;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.async.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.intent.AlfrescoIntentAPI;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;

/**
 * @author Jean Marie Pascal
 */
public class IntentAPIDispatcherActivity extends BaseActivity
{
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final String PARAM_URI = "tmpFileUri";

    private static final String MIMETYPE_JPG = "image/jpg";

    private static final String MIMETYPE_TXT = "text/plain";

    private Uri mOutputFileUri;

    private File payload;

    private boolean canUpload;

    private long accountId;

    private String folderId;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (outState != null)
        {
            outState.putParcelable(PARAM_URI, mOutputFileUri);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        mOutputFileUri = savedInstanceState.getParcelable(PARAM_URI);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();
        String mimetype = getIntent().getType();
        accountId = getIntent().getLongExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID, -1);
        folderId = getIntent().getStringExtra(AlfrescoIntentAPI.EXTRA_FOLDER_ID);
        canUpload = (accountId != -1 && folderId != null);

        if (savedInstanceState != null)
        {
            mOutputFileUri = savedInstanceState.getParcelable(PARAM_URI);
        }

        if (mOutputFileUri != null)
        {
            // finish();
            return;
        }

        if (AlfrescoIntentAPI.ACTION_CREATE.equals(action))
        {
            if (mimetype == null)
            {
                // TODO Error message mimetype unknown
                finish();
                return;
            }

            // Image capture
            if (MIMETYPE_JPG.equals(mimetype))
            {
                // NB: Need to be updated if targetSDK >= 24
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File folder = AlfrescoStorageManager.getInstance(this).getFileInPrivateFolder("/temp");
                if (!folder.exists())
                {
                    folder.mkdirs();
                }
                payload = new File(folder.getPath(), createFilename("IMG_", "jpg"));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(payload));
                if (intent.resolveActivity(getPackageManager()) == null)
                {
                    AlfrescoNotificationManager.getInstance(this).showAlertCrouton(this,
                            getString(R.string.feature_disable));
                    return;
                }
                mOutputFileUri = Uri.fromFile(payload);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

                // Analytics
                AnalyticsHelper.reportOperationEvent(this, AnalyticsManager.CATEGORY_WIDGET,
                        AnalyticsManager.ACTION_TOOLBAR, AnalyticsManager.LABEL_TAKE_PHOTO, 1, false);

                return;
            }

            // Image capture
            if (MIMETYPE_TXT.equals(mimetype))
            {
                boolean isSpeechToText = getIntent().getBooleanExtra(AlfrescoIntentAPI.EXTRA_SPEECH2TEXT, false);
                File file = null;
                try
                {
                    File tmpFolder = AlfrescoStorageManager.getInstance(this).getFileInPrivateFolder("/temp");
                    if (!tmpFolder.exists())
                    {
                        tmpFolder.mkdirs();
                    }
                    file = new File(tmpFolder, createFilename("NOTE_", "txt"));
                    file.createNewFile();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                Intent intent = new Intent(this, TextEditorActivity.class);
                intent.setAction(PrivateIntent.ACTION_CREATE_TEXT);
                intent.putExtra(AlfrescoIntentAPI.EXTRA_SPEECH2TEXT, isSpeechToText);
                intent.putExtra(PrivateIntent.EXTRA_FILE, file);
                intent.putExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID, accountId);
                intent.putExtra(AlfrescoIntentAPI.EXTRA_FOLDER_ID, folderId);
                intent.setType("text/plain");
                startActivity(intent);
                finish();

                // Analytics
                if (isSpeechToText)
                {
                    AnalyticsHelper.reportOperationEvent(this, AnalyticsManager.CATEGORY_WIDGET,
                            AnalyticsManager.ACTION_TOOLBAR, AnalyticsManager.LABEL_SPEECH_2_TEXT, 1, false);
                }
                else
                {
                    AnalyticsHelper.reportOperationEvent(this, AnalyticsManager.CATEGORY_WIDGET,
                            AnalyticsManager.ACTION_TOOLBAR, AnalyticsManager.LABEL_CREATE_TEXT, 1, false);
                }

                return;
            }
        }

        if (AlfrescoIntentAPI.ACTION_SEND.equals(action))
        {
            // Retrieve files
            ArrayList<String> tempList = getIntent().getStringArrayListExtra(PrivateIntent.EXTRA_FILE_PATH);
            if (tempList == null) { return; }
            List<File> files = new ArrayList<File>(tempList.size());
            int nCnt;
            for (nCnt = tempList.size(); nCnt > 0; nCnt--)
            {
                files.add(new File(tempList.get(nCnt - 1)));
            }

            // Folder destination has been provided
            // We send directly
            if (accountId != -1 && folderId != null)
            {
                AlfrescoAccount acc = AlfrescoAccountManager.getInstance(this).retrieveAccount(accountId);

                List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(files.size());
                for (File file : files)
                {
                    requestsBuilder.add(new CreateDocumentRequest.Builder(folderId, file.getName(),
                            new ContentFileProgressImpl(file))
                                    .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
                }
                String operationId = Operator.with(this, acc).load(requestsBuilder);
            }
            else
            {
                // DIsplay Upload UI
                ActionUtils.actionSendDocumentsToAlfresco(this, files);
            }

            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        payload = new File(mOutputFileUri.getPath());

        if (canUpload && requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            ArrayList<File> files = new ArrayList<>(1);
            files.add(payload);
            send(files);
        }
        else
        {
            ActionUtils.actionSendDocumentToAlfresco(this, payload);
        }
        finish();
    }

    private void send(ArrayList<File> files)
    {
        AlfrescoAccount acc = AlfrescoAccountManager.getInstance(this).retrieveAccount(accountId);

        List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(files.size());
        for (File file : files)
        {
            requestsBuilder
                    .add(new CreateDocumentRequest.Builder(folderId, file.getName(), new ContentFileProgressImpl(file))
                            .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
        }
        String operationId = Operator.with(this, acc).load(requestsBuilder);
    }

    protected String createFilename(String prefix, String extension)
    {
        String timeStamp = new SimpleDateFormat(DeviceCapture.TIMESTAMP_PATTERN).format(new Date());

        return prefix + timeStamp + "." + extension;
    }

    private void openPhotoChooser()
    {
        // Determine Uri of camera image to save
        File folder = AlfrescoStorageManager.getInstance(this).getFileInPrivateFolder("/temp");
        if (!folder.exists())
        {
            folder.mkdirs();
        }
        payload = new File(folder.getPath(), createFilename("IMG_", "jpg"));
        mOutputFileUri = Uri.fromFile(payload);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam)
        {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[] {}));

        startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE);
    }

    /*
     * @Override public void onActivityResult(int requestCode, int resultCode,
     * Intent data) { Uri selectedImageUri = null; //Log.v(TAG,
     * "#onActivityResult req: " + requestCode); if (resultCode ==
     * Activity.RESULT_OK) { if (requestCode == REQUEST_IMAGE_CAPTURE) { final
     * boolean isCamera; if (data == null) { isCamera = true; } else { final
     * String action = data.getAction(); if (action == null) { isCamera = false;
     * } else { isCamera =
     * action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); } } if
     * (isCamera) { selectedImageUri = mOutputFileUri; } else { selectedImageUri
     * = data == null ? null : data.getData(); } if (selectedImageUri != null) {
     * //showImageTaken(selectedImageUri); ArrayList<File> files = new
     * ArrayList<>(1); files.add(new File(selectedImageUri.getPath()));
     * send(files); } else { // TODO: show no image data received message } } }
     * finish(); }
     */

    @Override
    public void setSessionState(int state)
    {

    }
}
