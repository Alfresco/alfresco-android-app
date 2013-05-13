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
package org.alfresco.mobile.android.application.integration.file.create;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationTask;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CreateDirectoryTask extends AbstractOperationTask<File>
{
    /** Parent Folder object of the new folder. */
    protected File parentFolder;

    protected String parentFolderIdentifier;

    protected String folderName;

    protected Map<String, Serializable> properties;

    protected File folder = null;

    public CreateDirectoryTask(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof CreateDirectoryRequest)
        {
            this.parentFolderIdentifier = ((CreateDirectoryRequest) request).getFilePath();
            this.folderName = ((CreateDirectoryRequest) request).getFolderName();
        }
    }

    @Override
    protected LoaderResult<File> doInBackground(Void... params)
    {
        LoaderResult<File> result = new LoaderResult<File>();

        try
        {
            parentFolder = new File(parentFolderIdentifier);
            
            if (listener != null)
            {
                listener.onPreExecute(this);
            }

            if (parentFolder != null)
            {
                folder = new File(parentFolder, folderName);
                folder.mkdirs();
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(folder);

        return result;
    }

    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_CREATE_FOLDER_START);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_CREATE_FOLDER_COMPLETE);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putSerializable(IntentIntegrator.EXTRA_CREATED_FOLDER, folder);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public File getParentFolder()
    {
        return parentFolder;
    }

    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

}
