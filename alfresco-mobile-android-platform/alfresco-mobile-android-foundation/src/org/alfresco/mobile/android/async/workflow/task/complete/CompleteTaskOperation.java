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
package org.alfresco.mobile.android.async.workflow.task.complete;

import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.api.services.impl.publicapi.PublicAPIWorkflowServiceImpl;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.workflow.task.TaskOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class CompleteTaskOperation extends TaskOperation<Task>
{
    private static final String TAG = CompleteTaskOperation.class.getName();

    private Task updatedTask = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CompleteTaskOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Task> doInBackground()
    {
        LoaderResult<Task> result = new LoaderResult<Task>();
        try
        {
            result = super.doInBackground();

            if (((CompleteTaskRequest) request).variables != null)
            {
                String transitionIdentifier = "";
                if (task.getIdentifier().startsWith(WorkflowModel.KEY_PREFIX_ACTIVITI))
                {
                    transitionIdentifier = WorkflowModel.TRANSITION_NEXT;
                }
                if (!(session.getServiceRegistry().getWorkflowService() instanceof PublicAPIWorkflowServiceImpl))
                {
                    ((CompleteTaskRequest) request).variables.put(WorkflowModel.PROP_TRANSITIONS_VALUE,
                            transitionIdentifier);
                }
                updatedTask = session.getServiceRegistry().getWorkflowService()
                        .completeTask(task, ((CompleteTaskRequest) request).variables);
            }
        }
        catch (Exception e)
        {
            result.setException(e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        result.setData(updatedTask);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Task> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new CompleteTaskEvent(getRequestId(), result));
    }
}
