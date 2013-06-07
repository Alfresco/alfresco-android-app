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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.accounts.Account;

import android.content.Context;

public class OperationsRequestGroup implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    protected static final long DEFAULT_ACCOUNT_ID = -1;

    private long accountIdentifier;

    private String tenantIdentifier;

    private Context context;

    private List<OperationRequest> requests = new ArrayList<OperationRequest>();

    public OperationsRequestGroup(Context context, long accountIdentifier, String tenantIdentifier)
    {
        this.accountIdentifier = accountIdentifier;
        this.tenantIdentifier = tenantIdentifier;
        this.context = context;
    }

    /**
     * Use for account independent request.
     */
    public OperationsRequestGroup(Context context)
    {
        this(context, DEFAULT_ACCOUNT_ID, null);
    }
    
    public OperationsRequestGroup(Context context, Account account)
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
