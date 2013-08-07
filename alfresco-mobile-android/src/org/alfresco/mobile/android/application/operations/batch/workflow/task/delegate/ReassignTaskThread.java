/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This Task is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this Task except in compliance with the License.
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
package org.alfresco.mobile.android.application.operations.batch.workflow.task.delegate;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.TaskOperationThread;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ReassignTaskThread extends TaskOperationThread<Task>
{
    private static final String TAG = ReassignTaskThread.class.getName();

    private Task updatedTask = null;

    private Map<String, Serializable> properties;

    private Person assignee;

    private String assigneeId;

    private Boolean isClaimed;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public ReassignTaskThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof ReassignTaskRequest)
        {
            this.assigneeId = ((ReassignTaskRequest) request).getAssigneeId();
            this.assignee = ((ReassignTaskRequest) request).getAssignee();
            this.isClaimed = ((ReassignTaskRequest) request).getIsClaimed();
        }
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

            if (assignee == null)
            {
                assignee = session.getServiceRegistry().getPersonService().getPerson(assigneeId);
            }

            if (assignee != null && task != null && isClaimed == null)
            {
                updatedTask = session.getServiceRegistry().getWorkflowService().reassignTask(task, assignee);
            }
            if (isClaimed)
            {
                updatedTask = session.getServiceRegistry().getWorkflowService().claimTask(task);
            }
            else if (!isClaimed)
            {
                updatedTask = session.getServiceRegistry().getWorkflowService().unClaimTask(task);
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
    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_TASK_DELEGATE_STARTED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_TASK, task);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_TASK_DELEGATE_COMPLETED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_TASK, task);
        b.putSerializable(IntentIntegrator.EXTRA_UPDATED_TASK, updatedTask);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
