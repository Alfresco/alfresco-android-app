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
package org.alfresco.mobile.android.application.operations.batch.node.create;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.node.AbstractUpThread;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.security.EncryptionUtils;
import org.alfresco.mobile.android.application.utils.IOUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CreateDocumentThread extends AbstractUpThread
{
    private static final String TAG = CreateDocumentThread.class.getName();

    protected boolean isCreation;

    protected List<String> tags;

    protected Map<String, Serializable> properties;

    protected Document doc = null;

    private String finalDocumentName = null;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public CreateDocumentThread(Context ctx, AbstractBatchOperationRequestImpl request)
    {
        super(ctx, request);
        if (request instanceof CreateDocumentRequest)
        {
            this.properties = ((CreateDocumentRequest) request).getProperties();
            this.isCreation = ((CreateDocumentRequest) request).isCreation();
            this.tags = ((CreateDocumentRequest) request).getTags();
        }
    }

    // ////////////////////////////////////////////////////
    // LIFE CYCLE
    // ////////////////////////////////////////////////////
    public void run()
    {
        LoaderResult<Document> result = doInBackground();
        onPostExecute(result);
    }

    @Override
    protected LoaderResult<Document> doInBackground()
    {
        LoaderResult<Document> result = new LoaderResult<Document>();
        try
        {

            OperationCallBack<Document> tmpListener = listener;
            setOperationCallBack(null);
            result = super.doInBackground();
            setOperationCallBack(tmpListener);

            String filename = getContentFile().getFile().getPath();
            boolean encdec = DataProtectionManager.getInstance(context).isEncryptable(acc, new File(filename));
            finalDocumentName = createUniqueName();

            // Update Request
            if (request instanceof CreateDocumentRequest)
            {
                ((CreateDocumentRequest) request).setDocumentName(finalDocumentName);
            }

            // Update the document Name with the final name
            ContentValues cValues = new ContentValues();
            cValues.put(BatchOperationSchema.COLUMN_STATUS, Operation.STATUS_RUNNING);
            cValues.put(BatchOperationSchema.COLUMN_TITLE, finalDocumentName);
            context.getContentResolver().update(request.getNotificationUri(), cValues, null, null);

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

                // CREATE CONTENT
                doc = session.getServiceRegistry().getDocumentFolderService()
                        .createDocument(parentFolder, finalDocumentName, properties, contentFile);

                if (tags != null && !tags.isEmpty())
                {
                    session.getServiceRegistry().getTaggingService().addTags(doc, tags);
                }
            }

            if (encdec)
            {
                EncryptionUtils.encryptFile(context, filename, true);
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }

        result.setData(doc);

        return result;
    }

    private String createUniqueName()
    {
        String fileNameWithoutExtension = documentName.replaceFirst("[.][^.]+$", "");
        String fileExtension = getFileExtension(documentName);

        int index = 1;

        String tmpName = documentName;

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
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean isCreation()
    {
        return isCreation;
    }

    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPLOAD_STARTED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putString(IntentIntegrator.EXTRA_DOCUMENT_NAME, finalDocumentName);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPLOAD_COMPLETED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, doc);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
