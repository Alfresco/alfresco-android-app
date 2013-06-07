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


// ////////////////////////////////////////////////////
// Inner Class
// ////////////////////////////////////////////////////
public class OperationsGroupInfo
{
    public final OperationRequest request;

    public final int totalRequests;

    public final int pendingRequests;

    public final int failedRequests;

    public OperationsGroupInfo(OperationRequest request, int totalRequests, int pendingRequests, int failedRequests)
    {
        super();
        this.request = request;
        this.totalRequests = totalRequests;
        this.pendingRequests = pendingRequests;
        this.failedRequests = failedRequests;
    }
}
