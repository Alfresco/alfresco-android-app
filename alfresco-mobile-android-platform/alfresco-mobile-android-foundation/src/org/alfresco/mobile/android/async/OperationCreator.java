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
package org.alfresco.mobile.android.async;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OperationCreator
{
    // ////////////////////////////////////////////////////
    // MEMBERS
    // ////////////////////////////////////////////////////
    final Operator operator;

    final List<OperationRequest.OperationBuilder> data;

    final boolean isBatch;

    final List<OperationRequest> requests;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public OperationCreator(Operator operator, OperationRequest request)
    {
        if (operator.shutdown) { throw new IllegalStateException(
                "Operator instance already shut down. Cannot submit new requests."); }
        List<OperationRequest> requests = new ArrayList<OperationRequest>(1);
        requests.add(request);
        this.operator = operator;
        this.isBatch = false;
        this.requests = requests;
        this.data = null;
    }

    public OperationCreator(Operator operator, OperationRequest.OperationBuilder builder)
    {
        if (operator.shutdown) { throw new IllegalStateException(
                "Operator instance already shut down. Cannot submit new requests."); }
        List<OperationRequest.OperationBuilder> requests = new ArrayList<OperationRequest.OperationBuilder>(1);
        requests.add(builder);
        this.operator = operator;
        this.data = requests;
        this.isBatch = false;
        this.requests = null;
    }

    public OperationCreator(Operator operator, List<OperationRequest.OperationBuilder> requests)
    {
        if (operator.shutdown) { throw new IllegalStateException(
                "Operator instance already shut down. Cannot submit new requests."); }
        this.operator = operator;
        this.data = requests;
        this.isBatch = true;
        this.requests = null;
    }

    // ////////////////////////////////////////////////////
    // API
    // ////////////////////////////////////////////////////
    String execute()
    {
        String groupKey = null;
        String action = null;

        if (data == null)
        {
            if (requests.size() > 1)
            {
                groupKey = "GROUP_".concat(Long.toString(new Date().getTime()));
            }
            for (OperationRequest request : requests)
            {
                action = execute(request, groupKey, requests.size());

                // Generally it's a retry so we set to Pending state
                operator.getContext()
                        .getContentResolver()
                        .update(request.notificationUri, request.createContentValues(OperationStatus.STATUS_PENDING),
                                null, null);
            }
        }
        else
        {
            for (OperationRequest.OperationBuilder requestBuilder : data)
            {
                if (isBatch && groupKey == null)
                {
                    groupKey = "GROUP_".concat(Long.toString(new Date().getTime()));
                }
                action = execute(requestBuilder, groupKey);
            }
        }
        return (groupKey != null) ? groupKey : action;
    }

    private String execute(OperationRequest.OperationBuilder requestBuilder, String groupKey)
    {
        // Add informations
        if (operator.getAccount() != null)
        {
            requestBuilder.setAccountId(operator.getAccount().getId());
            requestBuilder.setNetworkId(operator.getAccount().getRepositoryId());
        }
        OperationRequest request = requestBuilder.build(operator.getContext());
        return execute(request, groupKey, data.size());
    }

    private String execute(OperationRequest request, String groupKey, int size)
    {
        // Save status
        request.saveStatus(operator.getContext(), OperationStatus.STATUS_PENDING);

        // Create Action
        OperationAction action = new OperationAction(operator, request, request.notificationUri.toString(), groupKey,
                size);

        // Send to background thread
        operator.enqueueAndSubmit(action);
        return action.key;
    }
}
