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
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

/**
 * @author Jean Marie Pascal
 */
public class IntentAPIDispatcherActivity extends BaseActivity
{
    private static final String MIMETYPE_JPG = "image/jpg";

    private static final String MIMETYPE_TXT = "text/plain";

    private File payload;

    private boolean canUpload;

    private long accountId;

    private String folderId;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        String mimetype = getIntent().getType();
        accountId = getIntent().getLongExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID, -1);
        folderId = getIntent().getStringExtra(AlfrescoIntentAPI.EXTRA_FOLDER_ID);
        canUpload = (accountId != -1 && folderId != null);

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

                startActivityForResult(intent, 1);
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
        if (canUpload)
        {
            ArrayList<File> files = new ArrayList<File>(1);
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
            requestsBuilder.add(new CreateDocumentRequest.Builder(folderId, file.getName(),
                    new ContentFileProgressImpl(file))
                    .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
        }
        String operationId = Operator.with(this, acc).load(requestsBuilder);
    }

    protected String createFilename(String prefix, String extension)
    {
        String timeStamp = new SimpleDateFormat(DeviceCapture.TIMESTAMP_PATTERN).format(new Date());

        return prefix + timeStamp + "." + extension;
    }
}
