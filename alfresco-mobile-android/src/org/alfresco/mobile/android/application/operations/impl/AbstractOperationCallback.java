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

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.NotificationHelper;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsGroupCallBack;
import org.alfresco.mobile.android.application.operations.OperationsGroupRecord;
import org.alfresco.mobile.android.application.operations.OperationsGroupResult;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public abstract class AbstractOperationCallback<T> implements Operation.OperationCallBack<T>, OperationsGroupCallBack
{
    private static final String TAG = AbstractOperationCallback.class.getName();
    
    protected static final int PROGRESS_MAX = 100;

    protected Context context;

    protected int totalItems;

    protected int pendingItems;

    protected String inProgress;

    protected String complete;

    protected int notificationVisibility;

    protected OperationsGroupRecord groupRecord;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AbstractOperationCallback(Context context, int totalItems, int pendingItems)
    {
        this.context = context;
        this.totalItems = totalItems;
        this.pendingItems = pendingItems;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onPreExecute(Operation<T> task)
    {
        notificationVisibility = ((AbstractOperationRequestImpl) task.getOperationRequest())
                .getNotificationVisibility();

        if (task.getStartBroadCastIntent() != null)
        {
            LocalBroadcastManager.getInstance(context).sendBroadcast(task.getStartBroadCastIntent());
        }

        switch (notificationVisibility)
        {
            case OperationRequest.VISIBILITY_NOTIFICATIONS:
                groupRecord = BatchOperationManager.getInstance(context).getOperationGroup(task.getOperationId());
                if (groupRecord.totalRequests == 1)
                {
                    NotificationHelper.createProgressNotification(getBaseContext(), getNotificationId(), task
                            .getOperationRequest().getNotificationTitle(), inProgress,
                            groupRecord.completeRequest.size() + "/" + groupRecord.totalRequests, 0, PROGRESS_MAX);
                }
                else
                {
                    NotificationHelper
                            .createIndeterminateNotification(getBaseContext(), getNotificationId(), inProgress, String
                                    .format(getBaseContext().getResources().getQuantityString(
                                            R.plurals.batch_in_progress, groupRecord.runningRequest.size()),
                                            groupRecord.runningRequest.size() + ""), groupRecord.completeRequest.size()
                                    + "/" + groupRecord.totalRequests);
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onPostExecute(Operation<T> task, T results)
    {
        if (task.getCompleteBroadCastIntent() != null)
        {
            LocalBroadcastManager.getInstance(context).sendBroadcast(task.getCompleteBroadCastIntent());
        }

        switch (notificationVisibility)
        {
            case OperationRequest.VISIBILITY_NOTIFICATIONS:
                groupRecord = BatchOperationManager.getInstance(context).getOperationGroup(task.getOperationId());
                if (groupRecord.totalRequests == 1)
                {
                    NotificationHelper.createIndeterminateNotification(getBaseContext(), getNotificationId(), task
                            .getOperationRequest().getNotificationTitle(), complete, totalItems - pendingItems + "/"
                            + totalItems);
                }
                else
                {
                    NotificationHelper
                            .createIndeterminateNotification(getBaseContext(), getNotificationId(), inProgress, String
                                    .format(getBaseContext().getResources().getQuantityString(
                                            R.plurals.batch_in_progress, groupRecord.runningRequest.size()),
                                            groupRecord.runningRequest.size() + ""), groupRecord.completeRequest.size()
                                    + "/" + groupRecord.totalRequests);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressUpdate(Operation<T> task, Long values)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onError(Operation<T> task, Exception e)
    {
        Log.e(TAG, Log.getStackTraceString(e));
    }

    @Override
    public void onPostExecution(OperationsGroupResult result)
    {
        switch (result.notificationVisibility)
        {
            case OperationRequest.VISIBILITY_NOTIFICATIONS:
                Bundle b = new Bundle();
                b.putString(NotificationHelper.ARGUMENT_TITLE, complete);
                NotificationHelper.createNotification(getBaseContext(), getNotificationId(), b);
                break;
            case OperationRequest.VISIBILITY_DIALOG:
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(IntentIntegrator.ACTION_OPERATIONS_COMPLETED);
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
                removeOperationUri(result);
                break;
            case OperationRequest.VISIBILITY_TOAST:
                MessengerManager.showToast(getBaseContext(), "Operation complete");
                removeOperationUri(result);
                break;
            default:
                removeOperationUri(result);
                break;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    protected Context getBaseContext()
    {
        return context;
    }

    protected void removeOperationUri(OperationsGroupResult result)
    {
        Uri operationUri = null;
        for (OperationRequest operationRequest : result.completeRequest)
        {
            operationUri = ((AbstractOperationRequestImpl) operationRequest).getNotificationUri();
            context.getContentResolver().delete(operationUri, null, null);
        }

        for (OperationRequest operationRequest : result.failedRequest)
        {
            operationUri = ((AbstractOperationRequestImpl) operationRequest).getNotificationUri();
            context.getContentResolver().delete(operationUri, null, null);
        }
    }

    protected String formatText(int textId, String text)
    {
        return String.format(getBaseContext().getString(textId), text);
    }

    protected abstract int getNotificationId();
}
