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
package org.alfresco.mobile.android.application.operations.batch.workflow.task;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;

import android.content.Context;
import android.util.Log;

public abstract class TaskOperationThread<T> extends AbstractBatchOperationThread<T>
{
    private static final String TAG = TaskOperationThread.class.getName();

    protected String taskIdentifier;

    protected Task task;

    protected Folder parentFolder;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public TaskOperationThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof TaskOperationRequest)
        {
            this.taskIdentifier = ((TaskOperationRequest) request).getTaskIdentifier();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<T> doInBackground()
    {
        try
        {
            super.doInBackground();

            try
            {
                if (taskIdentifier != null)
                {
                    task = session.getServiceRegistry().getWorkflowService().getTask(taskIdentifier);
                }
            }
            catch (AlfrescoServiceException e)
            {
                // Do Nothing
            }

            if (listener != null)
            {
                listener.onPreExecute(this);
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<T>();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Task getTask()
    {
        return task;
    }
}
