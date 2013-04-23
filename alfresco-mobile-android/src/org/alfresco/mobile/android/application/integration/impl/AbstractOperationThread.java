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
package org.alfresco.mobile.android.application.integration.impl;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.OperationManager;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.account.LoadSessionHelper;

import android.content.Context;
import android.content.Intent;

public abstract class AbstractOperationThread<T> extends Thread implements Operation<T>
{
    protected Context context;

    protected AlfrescoSession session;

    protected long accountId;

    protected OperationCallBack<T> listener;

    protected AbstractOperationRequestImpl request;
    
    
    public AbstractOperationThread(Context context, OperationRequest request)
    {
        super();
        this.context = context;
        this.request = (AbstractOperationRequestImpl) request;
        this.accountId = this.request.getAccountId();
    }
 
    public void setOperationCallBack(OperationCallBack<T> listener)
    {
        this.listener = listener;
    }
    
    
    public void run()
    {
        onPreExecute();
        LoaderResult<T> result = doInBackground();
        onPostExecute(result);
    }
    
    
    protected abstract LoaderResult<T> doInBackground();
    
    public Intent getCompleteBroadCastIntent()
    {
        return null;
    }
 
    protected void onPreExecute()
    {
        saveStatus(Operation.STATUS_RUNNING);
    }
    
    protected void onPostExecute(LoaderResult<T> result)
    {
        int resultStatus = Operation.STATUS_SUCCESSFUL;
        if (listener != null)
        {
            if (result.hasException())
            {
                listener.onError(this, result.getException());
                resultStatus = STATUS_FAILED;
            }
            else
            {
                listener.onPostExecute(this, result.getData());
            }
        }
        saveStatus(resultStatus);
        OperationManager.notifyCompletion(context, getOperationId(), resultStatus);
    }
    
    protected void onCancelled(LoaderResult<T> result)
    {
        saveStatus(Operation.STATUS_CANCEL);
    }
    
    public AlfrescoSession getSession()
    {
        return session;
    }

    public OperationRequest getOperationRequest()
    {
        return request;
    }

    public String getOperationId()
    {
        return request.getNotificationUri().getLastPathSegment();
    }

    protected void saveStatus(int status)
    {
        if (request.getNotificationUri() != null)
        {
            context.getContentResolver().update(request.getNotificationUri(), request.createContentValues(status),
                    null, null);
        }
    }

    @Override
    public Intent getStartBroadCastIntent()
    {
        return null;
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
