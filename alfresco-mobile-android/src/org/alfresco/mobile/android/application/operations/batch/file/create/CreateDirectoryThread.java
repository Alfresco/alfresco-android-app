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
package org.alfresco.mobile.android.application.operations.batch.file.create;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.file.FileOperationThread;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CreateDirectoryThread extends FileOperationThread<File>
{
    protected String folderName;

    protected Map<String, Serializable> properties;

    protected File newFolderFile = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateDirectoryThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof CreateDirectoryRequest)
        {
            this.folderName = ((CreateDirectoryRequest) request).getFolderName();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<File> doInBackground()
    {
        LoaderResult<File> result = new LoaderResult<File>();

        try
        {
            super.doInBackground();
            
            if (file != null)
            {
                newFolderFile = new File(file, folderName);
                newFolderFile.mkdirs();
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(newFolderFile);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
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
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, file);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_CREATE_FOLDER_COMPLETED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, file);
        b.putSerializable(IntentIntegrator.EXTRA_CREATED_FOLDER, newFolderFile);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

   

}
