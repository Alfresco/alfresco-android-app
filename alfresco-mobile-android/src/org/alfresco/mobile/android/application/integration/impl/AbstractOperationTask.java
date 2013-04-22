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
import android.os.AsyncTask;

public abstract class AbstractOperationTask<T> extends AsyncTask<Void, Long, LoaderResult<T>> implements Operation<T>
{
    protected Context context;

    protected AlfrescoSession session;

    protected long accountId;

    protected OperationCallBack<T> listener;

    protected AbstractOperationRequestImpl request;

    public AbstractOperationTask(Context context, OperationRequest request)
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

    public Intent getCompleteBroadCastIntent()
    {
        return null;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        saveStatus(Operation.STATUS_RUNNING);
    }

    @Override
    protected void onPostExecute(LoaderResult<T> result)
    {
        super.onPostExecute(result);
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

    @Override
    protected void onCancelled(LoaderResult<T> result)
    {
        saveStatus(Operation.STATUS_CANCEL);
        super.onCancelled(result);
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
