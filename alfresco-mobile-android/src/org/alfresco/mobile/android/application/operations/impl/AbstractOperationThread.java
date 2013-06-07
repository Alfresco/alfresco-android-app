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
package org.alfresco.mobile.android.application.operations.impl;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsGroupCallBack;
import org.alfresco.mobile.android.application.operations.OperationsGroupResult;
import org.alfresco.mobile.android.application.operations.batch.account.LoadSessionHelper;

import android.content.Context;
import android.content.Intent;

public abstract class AbstractOperationThread<T> extends Thread implements Operation<T>
{
    protected Context context;

    protected AlfrescoSession session;

    protected Account acc;

    protected long accountId;

    protected OperationCallBack<T> listener;

    protected AbstractOperationRequestImpl request;
    
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AbstractOperationThread(Context context, OperationRequest request)
    {
        super();
        this.context = context;
        this.request = (AbstractOperationRequestImpl) request;
        this.accountId = this.request.getAccountId();
    }
 
    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void run()
    {
        onPreExecute();
        LoaderResult<T> result = doInBackground();
        onPostExecute(result);
    }
    
    protected void onPreExecute()
    {
        saveStatus(Operation.STATUS_RUNNING);
    }
    
    protected abstract LoaderResult<T> doInBackground();
    
    protected void onPostExecute(LoaderResult<T> result)
    {
        //Must be implemented in subclass
    }
    
    protected void onCancelled(LoaderResult<T> result)
    {
        saveStatus(Operation.STATUS_CANCEL);
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    
    @Override
    public Intent getStartBroadCastIntent()
    {
        return null;
    }
    
    @Override
    public Intent getCompleteBroadCastIntent()
    {
        return null;
    }
    
    public void executeGroupCallback(OperationsGroupResult result)
    {
        if (listener instanceof OperationsGroupCallBack)
        {
            ((OperationsGroupCallBack) listener).onPostExecution(result);
        }
    }
    
    public OperationRequest getOperationRequest()
    {
        return request;
    }

    public String getOperationId()
    {
        return request.getNotificationUri().getLastPathSegment();
    }

    
    public void setOperationCallBack(OperationCallBack<T> listener)
    {
        this.listener = listener;
    }
    // ///////////////////////////////////////////////////////////////////////////
    // INTERNAL UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected void saveStatus(int status)
    {
        if (request.getNotificationUri() != null)
        {
            context.getContentResolver().update(request.getNotificationUri(), request.createContentValues(status),
                    null, null);
        }
    }
    
    public AlfrescoSession getSession()
    {
        return session;
    }
    
    protected AlfrescoSession requestSession()
    {
        if (ApplicationManager.getInstance(context).hasSession(accountId))
        {
            return ApplicationManager.getInstance(context).getSession(accountId);
        }
        else
        {
            LoadSessionHelper helper = new LoadSessionHelper(context, accountId);
            session = helper.requestSession();
            ApplicationManager.getInstance(context).saveSession(helper.getAccount(), session);
            return session;
        }
    }

    
}
