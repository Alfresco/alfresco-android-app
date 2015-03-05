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
package org.alfresco.mobile.android.async.workflow.task.delegate;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.workflow.task.TaskRequest;

import android.content.Context;

public class ReassignTaskRequest extends TaskRequest
{
    private static final long serialVersionUID = 1L;

    private static final String PROP_PERSONID = "personId";

    private static final String PROP_ISCLAIMED = "isClaimed";

    public static final int TYPE_ID = OperationRequestIds.ID_WORKFLOW_TASK_REASSIGN;

    final Person assignee;

    final String assigneeId;

    final Boolean isClaimed;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected ReassignTaskRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, String taskIdentifier, Task task, String assigneeId,
            Person assignee, Boolean isClaimed)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, taskIdentifier,
                task);
        this.assignee = assignee;
        this.assigneeId = assigneeId;
        this.isClaimed = isClaimed;
    }

    public void save()
    {
        persistentProperties = new HashMap<String, Serializable>();
        persistentProperties.put(PROP_PERSONID, assigneeId);
        persistentProperties.put(PROP_ISCLAIMED, isClaimed);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends TaskRequest.Builder
    {
        protected Person assignee;

        protected String assigneeId;

        protected Boolean isClaimed;

        public Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Task task, Person assignee)
        {
            super(task);
            this.assignee = assignee;
            this.assigneeId = assignee.getIdentifier();
        }

        public Builder(Task task, String assigneeId, Boolean isClaimed)
        {
            super(task);
            this.assigneeId = assigneeId;
            this.isClaimed = isClaimed;
        }

        public Builder(Task task, Person assignee, Boolean isClaimed)
        {
            super(task);
            this.assignee = assignee;
            this.assigneeId = assignee.getIdentifier();
            this.isClaimed = isClaimed;
        }

        public ReassignTaskRequest build(Context context)
        {
            return new ReassignTaskRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, taskIdentifier, task, assigneeId, assignee, isClaimed);
        }
    }

}
