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
package org.alfresco.mobile.android.application.operations.batch.file.update;

import java.io.File;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.file.FileOperationThread;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class RenameThread extends FileOperationThread<File>
{
    private static final String TAG = RenameThread.class.getName();

    protected String renamedFileName;

    private File resultFile;

    private File originalFile;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public RenameThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof RenameRequest)
        {
            this.renamedFileName = ((RenameRequest) request).getFileNewName();
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
            originalFile = new File(filePath);

            super.doInBackground();

            resultFile = new File(parentFile, renamedFileName);
            if (resultFile.exists())
            {
                throw new AlfrescoServiceException("File Already exists");
            }
            else
            {
                file.renameTo(resultFile);
            }
        }
        catch (Exception e)
        {
            result.setException(e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        result.setData(resultFile);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_STARTED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, getParentFile());
        b.putSerializable(IntentIntegrator.EXTRA_FILE, originalFile);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, getParentFile());
        b.putSerializable(IntentIntegrator.EXTRA_FILE, originalFile);
        b.putSerializable(IntentIntegrator.EXTRA_UPDATED_FILE, resultFile);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
