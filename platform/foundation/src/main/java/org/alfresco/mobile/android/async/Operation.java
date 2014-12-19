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

import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.async.session.LoadSessionHelper;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;

import android.content.Context;

public abstract class Operation<T> implements OperationStatus, Runnable
{
    protected Context context;

    protected AlfrescoSession session;

    protected AlfrescoAccount acc;

    protected long accountId;

    protected OperationCallback<T> listener;

    protected final OperationRequest request;

    public Future<?> future;

    public final Operator operator;

    final OperationsDispatcher dispatcher;

    private String key;

    public final OperationAction action;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public Operation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        this.context = operator.getContext();
        this.operator = operator;
        this.dispatcher = dispatcher;
        this.action = action;
        this.request = (OperationRequest) action.request;
        this.accountId = this.request.accountId;
    }

    public Operation(Context context, OperationRequest request)
    {
        this.context = context;
        this.request = (OperationRequest) request;
        this.accountId = this.request.accountId;
        this.dispatcher = null;
        this.operator = null;
        this.action = null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void run()
    {
        try
        {
            Thread.currentThread().setName("BatchOperation-" + this.getClass().getName());

            onPreExecute();
            LoaderResult<T> result = doInBackground();
            onPostExecute(result);

            if (result != null && !result.hasException())
            {
                saveStatus(Operation.STATUS_SUCCESSFUL);
                dispatcher.dispatchComplete(this);
            }
            else if (result != null && result.hasException() && result.getException() instanceof CancellationException)
            {
                saveStatus(Operation.STATUS_CANCEL);
                dispatcher.dispatchFailed(this);
            }
            else if (result == null || result.hasException())
            {
                saveStatus(Operation.STATUS_FAILED);
                dispatcher.dispatchFailed(this);
            }
        }
        catch (CancellationException e)
        {
            saveStatus(Operation.STATUS_CANCEL);
            dispatcher.dispatchFailed(this);
        }
        finally
        {
            Thread.currentThread().setName("BatchOperation-Idle");
        }
    }

    protected void onPreExecute()
    {
        checkCancel();
        saveStatus(Operation.STATUS_RUNNING);
    }

    protected abstract LoaderResult<T> doInBackground();

    protected void onPostExecute(LoaderResult<T> result)
    {

    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public boolean cancel()
    {
        if (!isCancelled())
        {
            saveStatus(STATUS_CANCEL);
        }
        return future != null && future.cancel(true);
    }

    public boolean isCancelled()
    {
        return future != null && future.isCancelled();
    }

    protected void checkCancel()
    {
        if (isCancelled()) { throw new CancellationException("User has send a Cancel Signal"); }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public OperationRequest getOperationRequest()
    {
        return request;
    }

    public String getOperationId()
    {
        if (request.notificationUri == null) { return null; }
        return request.notificationUri.getLastPathSegment();
    }

    public String getRequestId()
    {
        if (request.notificationUri == null) { return null; }
        return request.notificationUri.toString();
    }

    public void setOperationCallBack(OperationCallback<T> listener)
    {
        this.listener = listener;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNAL UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected boolean isInterrupted()
    {
        return Thread.currentThread().isInterrupted();
    }

    protected void saveStatus(int status)
    {
        if (request.notificationUri != null)
        {
            context.getContentResolver().update(request.notificationUri, request.createContentValues(status), null,
                    null);
        }
    }

    public AlfrescoSession getSession()
    {
        return session;
    }

    protected AlfrescoSession requestSession()
    {
        if (SessionManager.getInstance(context).hasSession(accountId))
        {
            return SessionManager.getInstance(context).getSession(accountId);
        }
        else
        {
            LoadSessionHelper helper = new LoadSessionHelper(context, accountId);
            session = helper.requestSession();
            SessionManager.getInstance(context).saveSession(helper.getAccount(), session);
            return session;
        }
    }

    public int getPriority()
    {
        return android.os.Process.THREAD_PRIORITY_BACKGROUND;
    }

    public String getKey()
    {
        return key;
    }

    public interface OperationCallback<T>
    {
        void onPreExecute(Operation<T> task);

        void onPostExecute(Operation<T> task, T result);

        void onError(Operation<T> task, Exception e);
    }
}
