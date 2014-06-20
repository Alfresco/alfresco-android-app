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

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;
import org.alfresco.mobile.android.application.utils.IOUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class RetrieveDocumentNameThread extends AbstractBatchOperationThread<String>
{
    private static final String TAG = RetrieveDocumentNameThread.class.getName();

    /** Parent Folder object of the new folder. */
    protected Folder parentFolder;

    protected String parentFolderIdentifier;

    /** Name of the future document. */
    protected String documentName;
    
    protected String finalDocumentName;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public RetrieveDocumentNameThread(Context ctx, AbstractBatchOperationRequestImpl request)
    {
        super(ctx, request);
        if (request instanceof RetrieveDocumentNameRequest)
        {
            this.parentFolderIdentifier = ((RetrieveDocumentNameRequest) request).getParentFolderIdentifier();
            this.documentName = ((RetrieveDocumentNameRequest) request).getDocumentName();
        }
    }

    // ////////////////////////////////////////////////////
    // LIFE CYCLE
    // ////////////////////////////////////////////////////
    @Override
    protected LoaderResult<String> doInBackground()
    {
        LoaderResult<String> result = new LoaderResult<String>();
        try
        {
            result = super.doInBackground();
            
            parentFolder = retrieveParentFolder();
            
            finalDocumentName = createUniqueName();
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }

        result.setData(finalDocumentName);

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
    
    private Folder retrieveParentFolder()
    {
        if (parentFolder == null && parentFolderIdentifier != null)
        {
            parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                    .getNodeByIdentifier(parentFolderIdentifier);
        }
        return parentFolder;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Folder getParentFolder()
    {
        return parentFolder;
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_RETRIEVE_NAME_COMPLETED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putString(IntentIntegrator.EXTRA_DOCUMENT_NAME, finalDocumentName);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
