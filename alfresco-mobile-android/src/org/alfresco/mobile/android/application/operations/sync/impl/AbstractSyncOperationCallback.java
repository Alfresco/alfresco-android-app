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
package org.alfresco.mobile.android.application.operations.sync.impl;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.NotificationHelper;
import org.alfresco.mobile.android.application.operations.OperationsGroupResult;
import org.alfresco.mobile.android.application.operations.impl.AbstractOperationCallback;

import android.content.Context;
import android.os.Bundle;

public abstract class AbstractSyncOperationCallback<T> extends AbstractOperationCallback<T>
{
    
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AbstractSyncOperationCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onPostExecution(OperationsGroupResult result)
    {
        Bundle b = new Bundle();
        b.putString(NotificationHelper.ARGUMENT_TITLE, getBaseContext().getString(R.string.sync_complete));
        if (result.failedRequest.isEmpty())
        {
            b.putString(
                    NotificationHelper.ARGUMENT_DESCRIPTION,
                    String.format(
                            getBaseContext().getResources().getQuantityString(R.plurals.sync_complete_description,
                                    result.totalRequests), result.totalRequests));
        }
        else
        {
            b.putString(NotificationHelper.ARGUMENT_DESCRIPTION, result.failedRequest.size() + "/"
                    + result.totalRequests);
        }
        NotificationHelper.createNotification(getBaseContext(), getNotificationId(), b);
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // INTERNAL UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected int getNotificationId()
    {
        return NotificationHelper.DEFAULT_NOTIFICATION_SYNC_ID;
    }

}
