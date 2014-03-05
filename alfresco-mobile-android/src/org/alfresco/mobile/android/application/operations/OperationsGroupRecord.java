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
package org.alfresco.mobile.android.application.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.alfresco.mobile.android.application.operations.impl.AbstractOperationRequestImpl;

public class OperationsGroupRecord
{
    public final List<OperationRequest> completeRequest;

    public final List<OperationRequest> failedRequest;

    public final HashMap<String, OperationRequest> runningRequest;

    public final HashMap<String, OperationRequest> index;

    public final int totalRequests;

    public int notificationVisibility;

    public OperationsGroupRecord(int size)
    {
        this.completeRequest = new ArrayList<OperationRequest>(size);
        this.failedRequest = new ArrayList<OperationRequest>(size);
        this.runningRequest = new LinkedHashMap<String, OperationRequest>(2);
        this.totalRequests = size;
        this.index = new LinkedHashMap<String, OperationRequest>(size);
    }

    public void initIndex(List<OperationRequest> requests)
    {
        if (requests.isEmpty()) { return; }
        this.notificationVisibility = requests.get(0).getNotificationVisibility();
        for (OperationRequest operationRequest : requests)
        {
            index.put(getOperationId(operationRequest), operationRequest);
        }
    }

    public boolean hasPendingRequest()
    {
        return !index.isEmpty();
    }

    public boolean hasRunningRequest()
    {
        return !runningRequest.isEmpty();
    }

    public OperationsGroupInfo next()
    {
        if (index.isEmpty()) { return null; }
        OperationRequest request = index.remove(index.entrySet().iterator().next().getKey());
        runningRequest.put(getOperationId(request), request);
        return new OperationsGroupInfo(request, totalRequests, index.size(), failedRequest.size());
    }

    public static String getOperationId(OperationRequest operationRequest)
    {
        return ((AbstractOperationRequestImpl) operationRequest).getNotificationUri().getLastPathSegment().toString();
    }
}
