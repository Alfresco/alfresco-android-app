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

import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;
import org.alfresco.mobile.android.platform.provider.MapUtil;

import android.content.ContentValues;
import android.content.Context;

public class TaskRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    final Task task;

    final String taskIdentifier;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected TaskRequest(Context context, long accountId, String networkId, int notificationVisibility, String title,
            String mimeType, int requestTypeId, String taskIdentifier, Task task)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.task = task;
        this.taskIdentifier = taskIdentifier;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return TaskRequest.class.getName();
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_NODE_ID, taskIdentifier);
        if (persistentProperties != null && !persistentProperties.isEmpty())
        {
            cValues.put(OperationSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        }
        return cValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        protected String taskIdentifier;

        protected Task task;

        public Builder()
        {
        }

        public Builder(Task task)
        {
            this();
            this.task = task;
            this.taskIdentifier = task.getIdentifier();
        }

        public TaskRequest build(Context context)
        {
            return new TaskRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, taskIdentifier, task);
        }
    }
}
