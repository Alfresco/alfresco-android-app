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

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.impl.AbstractOperationThread;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

public abstract class AbstractSyncOperationThread<T> extends AbstractOperationThread<T> implements SyncOperation<T>
{
    private static final String TAG = AbstractSyncOperationThread.class.getName();
    
    /** Flag to indicate if status must be persisted in the contentProvider. */
    protected boolean saveStatus = true;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AbstractSyncOperationThread(Context context, OperationRequest request)
    {
        super(context, request);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<T> doInBackground()
    {
        try
        {
            acc = AccountManager.retrieveAccount(context, accountId);

            session = requestSession();
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<T>();
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
        SynchroManager.getInstance(context).notifyCompletion(context, getOperationId(), resultStatus);
    }

    protected void onCancelled(LoaderResult<T> result)
    {
        saveStatus(Operation.STATUS_CANCEL);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNAL UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected void saveStatus(int status)
    {
        if (saveStatus && request.getNotificationUri() != null)
        {
            context.getContentResolver().update(request.getNotificationUri(),
                    ((AbstractSyncOperationRequestImpl) request).createContentValues(status), null, null);
        }
    }

    protected void requestUserInteraction(Uri localUri, int reasonId)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(BatchOperationSchema.COLUMN_STATUS, SyncOperation.STATUS_REQUEST_USER);
        cValues.put(BatchOperationSchema.COLUMN_REASON, reasonId);
        context.getContentResolver().update(localUri, cValues, null, null);
    }
}
