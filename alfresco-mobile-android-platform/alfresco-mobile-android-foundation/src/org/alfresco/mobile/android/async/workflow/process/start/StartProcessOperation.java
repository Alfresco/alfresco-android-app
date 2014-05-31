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
package org.alfresco.mobile.android.async.workflow.process.start;

import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class StartProcessOperation extends BaseOperation<Process>
{
    private static final String TAG = StartProcessOperation.class.getName();

    protected Process process = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public StartProcessOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Process> doInBackground()
    {
        LoaderResult<Process> result = new LoaderResult<Process>();

        try
        {
            result = super.doInBackground();
            process = session
                    .getServiceRegistry()
                    .getWorkflowService()
                    .startProcess(((StartProcessRequest) request).processDefinition,
                            ((StartProcessRequest) request).assignees, ((StartProcessRequest) request).variables,
                            ((StartProcessRequest) request).items);
        }
        catch (Exception e)
        {
            result.setException(e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        result.setData(process);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Process> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new StartProcessEvent(getRequestId(), result));
    }
}
