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
package org.alfresco.mobile.android.application.integration.node.create;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;
import org.alfresco.mobile.android.application.integration.node.AbstractUpTask;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.CipherUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Responsible to upload a document from the device to the repository.
 * 
 * @author Jean Marie Pascal
 */
public class CreateDocumentTask extends AbstractUpTask
{
    private static final String TAG = CreateDocumentTask.class.getName();

    protected boolean isCreation;

    protected List<String> tags;
    
    protected Map<String, Serializable> properties;
    
    protected Document doc = null;

    public CreateDocumentTask(Context ctx, AbstractOperationRequestImpl request)
    {
        super(ctx, request);
        if (request instanceof CreateDocumentRequest)
        {
            this.properties = ((CreateDocumentRequest) request).getProperties();
            this.isCreation = ((CreateDocumentRequest) request).isCreation();
            this.tags = ((CreateDocumentRequest) request).getTags();
        }
    }

    @Override
    protected LoaderResult<Document> doInBackground(Void... params)
    {
        LoaderResult<Document> result = null;
        try
        {
            result = super.doInBackground();

            String filename = getContentFile().getFile().getPath();
            boolean encdec = StorageManager.shouldEncryptDecrypt(context, filename);

            if (encdec)
            {
                CipherUtils.decryptFile(context, filename);
            }

            if (parentFolder != null)
            {
                // CREATE CONTENT
                doc = session.getServiceRegistry().getDocumentFolderService()
                        .createDocument(parentFolder, documentName, properties, contentFile);

                if (tags != null && !tags.isEmpty())
                {
                    session.getServiceRegistry().getTaggingService().addTags(doc, tags);
                }
            }

            if (encdec)
            {
                CipherUtils.encryptFile(context, filename, true);
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(doc);

        return result;
    }
    
    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPLOAD_START);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putString(IntentIntegrator.EXTRA_DOCUMENT_NAME, documentName);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPLOAD_COMPLETE);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, doc);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public boolean isCreation()
    {
        return isCreation;
    }
    
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }
}
