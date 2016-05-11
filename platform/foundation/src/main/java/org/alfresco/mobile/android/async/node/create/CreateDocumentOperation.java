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
package org.alfresco.mobile.android.async.node.create;

import java.io.File;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.UploadRetryService;
import org.alfresco.mobile.android.async.node.UpNodeOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.exception.AlfrescoAppException;
import org.alfresco.mobile.android.platform.exception.AlfrescoOfflineException;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.io.IOUtils;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.security.EncryptionUtils;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;

import android.content.ContentValues;
import android.util.Log;

public class CreateDocumentOperation extends UpNodeOperation
{
    private static final String TAG = CreateDocumentOperation.class.getName();

    protected Document doc = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateDocumentOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    // ////////////////////////////////////////////////////
    // LIFE CYCLE
    // ////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Document> doInBackground()
    {
        // long ping = System.currentTimeMillis();
        LoaderResult<Document> result = new LoaderResult<Document>();
        try
        {
            result = super.doInBackground();

            String filename = getContentFile().getFile().getPath();
            boolean encdec = DataProtectionManager.getInstance(context).isEncryptable(acc, new File(filename));
            String finalDocumentName = createUniqueName();

            // Update the document Name with the final name
            ContentValues cValues = new ContentValues();
            cValues.put(OperationSchema.COLUMN_STATUS, Operation.STATUS_RUNNING);
            cValues.put(OperationSchema.COLUMN_TITLE, finalDocumentName);
            context.getContentResolver().update(request.notificationUri, cValues, null, null);

            if (listener != null)
            {
                listener.onPreExecute(this);
            }

            if (encdec)
            {
                EncryptionUtils.decryptFile(context, filename);
            }

            if (parentFolder != null)
            {

                // Custom type ?
                if (((CreateDocumentRequest) request).type != null)
                {
                    // CREATE CONTENT
                    doc = session.getServiceRegistry().getDocumentFolderService().createDocument(parentFolder,
                            finalDocumentName, ((CreateDocumentRequest) request).properties, contentFile, null,
                            ((CreateDocumentRequest) request).type);
                }
                else
                {
                    // CREATE CONTENT
                    doc = session.getServiceRegistry().getDocumentFolderService().createDocument(parentFolder,
                            finalDocumentName, ((CreateDocumentRequest) request).properties, contentFile);
                }

                if (((CreateDocumentRequest) request).tags != null && !((CreateDocumentRequest) request).tags.isEmpty())
                {
                    session.getServiceRegistry().getTaggingService().addTags(doc,
                            ((CreateDocumentRequest) request).tags);
                }
            }
            else
            {
                if (!ConnectivityUtils.hasInternetAvailable(context))
                {
                    result.setException(new AlfrescoOfflineException("Offline"));
                }
                else
                {
                    result.setException(new AlfrescoAppException("ParentFolder is empty"));
                }
            }

            // Encrypt if necessary
            if (encdec)
            {
                EncryptionUtils.encryptFile(context, filename, true);
            }

            // Sync if necessary
            if (SyncContentManager.getInstance(context).isRootSynced(getAccount(), parentFolder))
            {
                SyncContentManager.getInstance(context).sync(AnalyticsManager.LABEL_SYNC_ACTION, getAccount());
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }

        result.setData(doc);
        // long pong = System.currentTimeMillis();
        // Log.d("[UPLOAD-BACK]", "Duration " + (pong - ping) + " ms" + "[" +
        // (contentFile.getLength() / (pong - ping))
        // + " b/s]");

        return result;
    }

    private String createUniqueName()
    {
        String fileNameWithoutExtension = ((CreateDocumentRequest) request).documentName.replaceFirst("[.][^.]+$", "");
        String fileExtension = getFileExtension(((CreateDocumentRequest) request).documentName);

        int index = 1;

        String tmpName = ((CreateDocumentRequest) request).documentName;

        while (doesExist(tmpName))
        {
            tmpName = fileNameWithoutExtension + "-" + index + fileExtension;
            index++;
        }
        return tmpName;
    }

    private static String getFileExtension(String fileName)
    {
        return "." + IOUtils.extractFileExtension(fileName);
    }

    private boolean doesExist(String documentPath)
    {
        try
        {
            Document tmpDoc = (Document) session.getServiceRegistry().getDocumentFolderService()
                    .getChildByPath(parentFolder, documentPath);
            if (tmpDoc != null) { return true; }
        }
        catch (Exception e)
        {
            return false;
        }
        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Document> result)
    {
        super.onPostExecute(result);

        EventBusManager.getInstance().post(new CreateDocumentEvent(getRequestId(), result, parentFolder));
        if (result.hasException() && result.getException() instanceof AlfrescoOfflineException)
        {
            UploadRetryService.retryDelay(context, acc != null ? acc.getId() : null, getRequestId(),
                    UploadRetryService.DEFAULT_DELAY);
        }

        if (((CreateDocumentRequest) request).isCreation)
        {
            contentFile.getFile().delete();
        }

        // Analytics
        AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                AnalyticsManager.ACTION_CREATE, doc != null ? doc.getContentStreamMimeType() : null, 1,
                result.hasException(), AnalyticsManager.INDEX_SYNCED_SIZE,
                doc != null ? doc.getContentStreamLength() : -1L);
    }
}
