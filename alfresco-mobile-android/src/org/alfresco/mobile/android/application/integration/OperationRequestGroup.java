package org.alfresco.mobile.android.application.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.accounts.Account;

import android.content.Context;

public class OperationRequestGroup implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    protected static final long DEFAULT_ACCOUNT_ID = -1;

    private long accountIdentifier;

    private String tenantIdentifier;

    private Context context;

    private List<OperationRequest> requests = new ArrayList<OperationRequest>();

    public OperationRequestGroup(Context context, long accountIdentifier, String tenantIdentifier)
    {
        this.accountIdentifier = accountIdentifier;
        this.tenantIdentifier = tenantIdentifier;
        this.context = context;
    }

    /**
     * Use for account independent request.
     */
    public OperationRequestGroup(Context context)
    {
        this(context, DEFAULT_ACCOUNT_ID, null);
    }
    
    public OperationRequestGroup(Context context, Account account)
    {
        this(context, account.getId(), account.getRepositoryId());
    }

    public void enqueue(OperationRequest request)
    {
        requests.add(request.setAccountId(accountIdentifier).setNetworkId(tenantIdentifier));
    }

    public Context getContext()
    {
        return context;
    }

    public List<OperationRequest> getRequests()
    {
        return requests;
    }
}
