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
package org.alfresco.mobile.android.async.workflow;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.ListingOperationRequest;

import android.content.Context;

public class ItemsRequest extends ListingOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_WORKFLOW_ATTACHMENT_LIST;

    final Task task;

    final Process process;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected ItemsRequest(Context context, long accountId, String networkId, int notificationVisibility, String title,
            String mimeType, int requestTypeId, ListingContext listingContext, Task task, Process process)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, listingContext);
        this.task = task;
        this.process = process;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return ItemsRequest.class.getName();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends ListingOperationRequest.Builder
    {
        private Task task;

        private Process process;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Process process, Task task)
        {
            requestTypeId = TYPE_ID;
            this.process = process;
            this.task = task;
        }

        public ItemsRequest build(Context context)
        {
            return new ItemsRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, listingContext, task, process);
        }
    }
}
