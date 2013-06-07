/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.node.create;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CreateFolderThread extends AbstractBatchOperationThread<Folder>
{
    /** Parent Folder object of the new folder. */
    protected Folder parentFolder;

    protected String parentFolderIdentifier;

    protected String folderName;

    protected Map<String, Serializable> properties;

    protected Folder folder = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateFolderThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof CreateFolderRequest)
        {
            this.parentFolderIdentifier = ((CreateFolderRequest) request).getParentFolderIdentifier();
            this.properties = ((CreateFolderRequest) request).getProperties();
            this.folderName = ((CreateFolderRequest) request).getFolderName();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Folder> doInBackground()
    {
        LoaderResult<Folder> result = new LoaderResult<Folder>();

        try
        {
            session = SessionUtils.getSession(context, accountId);
            parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                    .getNodeByIdentifier(parentFolderIdentifier);
            
            if (listener != null)
            {
                listener.onPreExecute(this);
            }

            if (parentFolder != null)
            {
                folder = session.getServiceRegistry().getDocumentFolderService()
                        .createFolder(parentFolder, folderName, properties);
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(folder);

        return result;
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Folder getParentFolder()
    {
        return parentFolder;
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
        broadcastIntent.setAction(IntentIntegrator.ACTION_CREATE_FOLDER_STARTED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_CREATE_FOLDER_COMPLETED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_CREATED_FOLDER, folder);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
