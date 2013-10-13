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
package org.alfresco.mobile.android.application.operations.batch.impl;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.NotificationHelper;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationUtils;
import org.alfresco.mobile.android.application.operations.OperationsGroupResult;
import org.alfresco.mobile.android.application.operations.impl.AbstractOperationCallback;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public abstract class AbstractBatchOperationCallback<T> extends AbstractOperationCallback<T>
{
    protected int finalComplete = 0;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AbstractBatchOperationCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onPostExecution(OperationsGroupResult result)
    {
        switch (result.notificationVisibility)
        {
            case OperationRequest.VISIBILITY_NOTIFICATIONS:
                createNotification(result);
                break;
            case OperationRequest.VISIBILITY_DIALOG:
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(IntentIntegrator.ACTION_OPERATIONS_COMPLETED);
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
                OperationUtils.removeOperationUri(context, result);
                break;
            case OperationRequest.VISIBILITY_TOAST:
                MessengerManager.showToast(getBaseContext(), complete);
                OperationUtils.removeOperationUri(context, result);
                break;
            default:
                OperationUtils.removeOperationUri(context, result);
                break;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNAL UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected void createNotification(OperationsGroupResult result)
    {
        Bundle b = new Bundle();
        b.putString(NotificationHelper.ARGUMENT_TITLE, complete);
        if (result.failedRequest.isEmpty() && finalComplete != 0)
        {
            b.putString(NotificationHelper.ARGUMENT_DESCRIPTION, String.format(getBaseContext().getResources()
                    .getQuantityString(finalComplete, result.totalRequests), result.totalRequests));
        }
        else
        {
            b.putString(NotificationHelper.ARGUMENT_DESCRIPTION, String.format(getBaseContext().getResources()
                    .getQuantityString(R.plurals.batch_failed, result.failedRequest.size()), result.failedRequest.size()));
            b.putString(NotificationHelper.ARGUMENT_CONTENT_INFO, result.completeRequest.size() + "/"
                    + result.totalRequests);
            b.putInt(NotificationHelper.ARGUMENT_SMALL_ICON, R.drawable.ic_warning_light);
        }
        NotificationHelper.createNotification(getBaseContext(), getNotificationId(), b);
    }

    protected int getNotificationId()
    {
        return NotificationHelper.DEFAULT_NOTIFICATION_ID;
    }
}
