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
package org.alfresco.mobile.android.application.integration.file.delete;

import java.io.File;
import java.io.IOException;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.integration.file.FileOperationTask;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class DeleteFileTask extends FileOperationTask<Void>
{
    private static final String TAG = DeleteFileTask.class.getName();

    private File parentFile;

    public DeleteFileTask(Context ctx, AbstractOperationRequestImpl request)
    {
        super(ctx, request);
    }

    @Override
    protected LoaderResult<Void> doInBackground(Void... params)
    {
        Log.d(TAG, "Start Delete");

        LoaderResult<Void> result = null;
        try
        {
            result = super.doInBackground();
            parentFile = file.getParentFile();

            if (file.isDirectory())
            {
                if (!deleteDirectory(file)) { throw new IOException("Unable to delete the file"); }
            }
            else if (!file.delete()) { throw new IOException("Unable to delete the file"); }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            if (result == null)
            {
                result = new LoaderResult<Void>();
            }
            result.setException(e);
        }

        return result;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_DELETE_COMPLETE);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, parentFile);
        b.putSerializable(IntentIntegrator.EXTRA_FILE, file);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    private static boolean deleteDirectory(File path)
    {
        if (path.exists())
        {
            File[] files = path.listFiles();
            if (files == null) { return true; }
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory())
                {
                    deleteDirectory(files[i]);
                }
                else
                {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
