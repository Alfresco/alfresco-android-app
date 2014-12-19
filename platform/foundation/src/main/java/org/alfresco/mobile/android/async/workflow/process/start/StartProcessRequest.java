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
package org.alfresco.mobile.android.async.workflow.process.start;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.ContentValues;
import android.content.Context;

public class StartProcessRequest extends BaseOperationRequest
{
    public static final int TYPE_ID = OperationRequestIds.ID_WORKFLOW_PROCESS_START;

    final Map<String, Serializable> variables;

    private static final long serialVersionUID = 1L;

    final ProcessDefinition processDefinition;

    final List<Person> assignees;

    final List<Document> items;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected StartProcessRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, ProcessDefinition processDefinition,
            List<Person> assignees, Map<String, Serializable> variables, List<Document> items)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.processDefinition = processDefinition;
        this.assignees = assignees;
        this.variables = variables;
        this.items = items;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return StartProcessRequest.class.getName();
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        return cValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        private Map<String, Serializable> variables;

        protected ProcessDefinition processDefinition;

        protected List<Person> assignees;

        protected List<Document> items;

        public Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(ProcessDefinition processDefinition, List<Person> assignees,
                Map<String, Serializable> variables, List<Document> items)
        {
            this();
            this.processDefinition = processDefinition;
            this.assignees = assignees;
            this.variables = variables;
            this.items = items;
            this.requestTypeId = TYPE_ID;
        }

        public StartProcessRequest build(Context context)
        {
            return new StartProcessRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, processDefinition, assignees, variables, items);
        }
    }
}
