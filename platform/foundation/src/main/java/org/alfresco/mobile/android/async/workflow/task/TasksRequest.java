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
package org.alfresco.mobile.android.async.workflow.task;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.ListingOperationRequest;

import android.content.Context;

public class TasksRequest extends ListingOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_WORKFLOW_TASK_LIST;

    final Process process;

    final String processId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected TasksRequest(Context context, long accountId, String networkId, int notificationVisibility, String title,
            String mimeType, int requestTypeId, ListingContext listingContext, Process process, String processId)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, listingContext);
        this.process = process;
        this.processId = processId;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return TasksRequest.class.getName();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends ListingOperationRequest.Builder
    {
        private Process process;

        private String processId;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(ListingContext listingContext)
        {
            requestTypeId = TYPE_ID;
            this.listingContext = listingContext;
        }

        public Builder(String processId)
        {
            this();
            this.processId = processId;
        }

        public TasksRequest build(Context context)
        {
            return new TasksRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, listingContext, process, processId);
        }
    }
}
