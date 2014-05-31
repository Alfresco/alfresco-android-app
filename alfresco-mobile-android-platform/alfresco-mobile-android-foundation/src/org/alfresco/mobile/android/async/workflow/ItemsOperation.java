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
package org.alfresco.mobile.android.async.workflow;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class ItemsOperation extends ListingOperation<PagingResult<Document>>
{
    private static final String TAG = ItemsOperation.class.getName();

    private Task task;

    private Process process;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public ItemsOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof ItemsRequest)
        {
            this.task = ((ItemsRequest) request).task;
            this.process = ((ItemsRequest) request).process;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<PagingResult<Document>> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<PagingResult<Document>> result = new LoaderResult<PagingResult<Document>>();
            PagingResult<Document> pagingResult = null;

            try
            {
                if (task != null)
                {
                    pagingResult = session.getServiceRegistry().getWorkflowService().getDocuments(task, listingContext);
                }
                else if (process != null)
                {
                    pagingResult = session.getServiceRegistry().getWorkflowService()
                            .getDocuments(process, listingContext);
                }
            }
            catch (AlfrescoServiceException e)
            {
                Log.w(TAG, Log.getStackTraceString(e));
                result.setException(e);
            }

            result.setData(pagingResult);

            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<PagingResult<Document>>();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<PagingResult<Document>> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new ItemsEvent(getRequestId(), result, task, process));
    }
}
