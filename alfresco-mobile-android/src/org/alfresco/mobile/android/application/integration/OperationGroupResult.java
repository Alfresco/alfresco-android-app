package org.alfresco.mobile.android.application.integration;

import java.util.List;

public class OperationGroupResult
{
    public final List<OperationRequest> completeRequest;

    public final List<OperationRequest> failedRequest;

    public final int totalRequests;

    public final int notificationVisibility;

    public OperationGroupResult(int notificationVisibility, List<OperationRequest> completeRequest,
            List<OperationRequest> failedRequest)
    {
        super();
        this.notificationVisibility = notificationVisibility;
        this.completeRequest = completeRequest;
        this.failedRequest = failedRequest;
        this.totalRequests = completeRequest.size() + failedRequest.size();
    }
}