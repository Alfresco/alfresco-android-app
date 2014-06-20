/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.async.file.update;

import java.io.File;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.file.FileOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class RenameFileOperation extends FileOperation<File>
{
    private static final String TAG = RenameFileOperation.class.getName();

    protected String renamedFileName;

    private File resultFile;

    private File originalFile;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public RenameFileOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof RenameFileRequest)
        {
            this.renamedFileName = ((RenameFileRequest) request).newFileName;
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
    @Override
    protected void onPostExecute(LoaderResult<File> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new RenameFileEvent(getRequestId(), result, file, getParentFile()));
    }
}
