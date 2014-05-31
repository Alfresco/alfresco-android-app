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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.workflow.task.TaskRequest;

import android.content.Context;

public class CompleteTaskRequest extends TaskRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_WORKFLOW_TASK_COMPLETE;

    final Map<String, Serializable> variables;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected CompleteTaskRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, String taskIdentifier, Task task,
            Map<String, Serializable> variables)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, taskIdentifier,
                task);
        this.variables = variables;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends TaskRequest.Builder
    {
        protected Map<String, Serializable> variables;

        public Builder()
        {
        }

        public Builder(Task task, Map<String, Serializable> variables)
        {
            super(task);
            this.variables = variables;
        }

        public CompleteTaskRequest build(Context context)
        {
            return new CompleteTaskRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, taskIdentifier, task, variables);
        }
    }
}
