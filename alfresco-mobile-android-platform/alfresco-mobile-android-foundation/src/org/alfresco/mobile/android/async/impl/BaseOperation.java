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
package org.alfresco.mobile.android.async.impl;

import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;

import android.util.Log;

public abstract class BaseOperation<T> extends Operation<T>
{
    private static final String TAG = BaseOperation.class.getName();

    protected static final String EXCEPTION_OPERATION_CANCEL = "Operation Cancelled";

    protected boolean hasCancelled = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public BaseOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<T> doInBackground()
    {
        try
        {
            acc = AlfrescoAccountManager.getInstance(context).retrieveAccount(accountId);

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
                if (hasCancelled)
                {
                    resultStatus = STATUS_CANCEL;
                }
            }
            else
            {
                listener.onPostExecute(this, result.getData());
            }
        }
        saveStatus(resultStatus);
        // BatchOperationManager.getInstance(context).notifyCompletion(getOperationId(),
        // resultStatus);
    }

    protected void onCancelled(LoaderResult<T> result)
    {
        saveStatus(Operation.STATUS_CANCEL);
    }

    public boolean requireNetwork()
    {
        return true;
    }

    public AlfrescoAccount getAccount()
    {
        return acc;
    }
}
