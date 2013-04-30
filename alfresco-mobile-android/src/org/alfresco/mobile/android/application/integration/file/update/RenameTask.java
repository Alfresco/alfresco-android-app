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
package org.alfresco.mobile.android.application.integration.file.update;

import java.io.File;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationTask;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class RenameTask extends AbstractOperationTask<File>
{
    private static final String TAG = RenameTask.class.getName();

    /** Parent Folder object of the new folder. */
    protected File parentFolder;

    protected String parentFolderIdentifier;

    protected String renamedFileName;

    private String filePath;
    
    private File originalFile;
    
    private File resultFile;

    public RenameTask(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof RenameRequest)
        {
            this.renamedFileName = ((RenameRequest) request).getFileName();
            this.filePath = ((RenameRequest) request).getFilePath();
            this.originalFile = new File(filePath);
            this.parentFolder = originalFile.getParentFile();
        }
    }

    @Override
    protected LoaderResult<File> doInBackground(Void... params)
    {
        LoaderResult<File> result = new LoaderResult<File>();

        try
        {
            if (listener != null)
            {
                listener.onPreExecute(this);
            }
            
            resultFile = new File(parentFolder, renamedFileName);
            originalFile.renameTo(resultFile);
        }
        catch (Exception e)
        {
            result.setException(e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        result.setData(resultFile);

        return result;
    }

    public File getParentFolder()
    {
        return parentFolder;
    }

    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_STARTED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putSerializable(IntentIntegrator.EXTRA_FILE, originalFile);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putSerializable(IntentIntegrator.EXTRA_FILE, originalFile);
        b.putSerializable(IntentIntegrator.EXTRA_UPDATED_FILE, resultFile);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
