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
package org.alfresco.mobile.android.application.operations;

import java.util.ArrayList;
import java.util.HashMap;

import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class OperationManager
{
    //private static final String TAG = OperationManager.class.getName();

    protected final Context mAppContext;

    protected final ArrayList<OperationsGroupRecord> operationsGroups = new ArrayList<OperationsGroupRecord>();

    protected static final Object mLock = new Object();

    protected static HashMap<String, OperationsGroupRecord> indexOperationGroup = new HashMap<String, OperationsGroupRecord>();

    protected LocalBroadcastManager localBroadcastManager;

    protected abstract IntentFilter getIntentFilter();

    protected OperationManager(Context applicationContext)
    {
        mAppContext = applicationContext;
        IntentFilter intentFilter = getIntentFilter();
        localBroadcastManager = LocalBroadcastManager.getInstance(mAppContext);
        localBroadcastManager.registerReceiver(getReceiver(), intentFilter);
    }

    protected abstract BroadcastReceiver getReceiver();

    public abstract void enqueue(OperationsRequestGroup group);

    public abstract void retry(long id);

    public boolean isLastOperation(String operationId)
    {
        if (indexOperationGroup.get(operationId) != null)
        {
            OperationsGroupRecord group = indexOperationGroup.get(operationId);
            return !(group.hasPendingRequest() || group.hasRunningRequest());
        }
        return true;
    }
    
    public OperationsGroupRecord getOperationGroup(String operationId)
    {
        return indexOperationGroup.get(operationId);
    }
    
    public OperationsGroupRecord removeOperationGroup(String operationId)
    {
        return indexOperationGroup.get(operationId);
    }
    
    public OperationsGroupInfo next(){
        OperationsGroupRecord group = null;
        ArrayList<Integer> ints = new ArrayList<Integer>();
        for (int i = 0; i < operationsGroups.size(); i++)
        {
            group = operationsGroups.get(i);
            if (group.hasPendingRequest())
            {
                return group.next();
            }
            if (!group.hasPendingRequest() && !group.hasRunningRequest())
            {
                ints.add(i);
            }
        }
        
        for (Integer index : ints)
        {
            operationsGroups.remove(index);
        }
       
       return null;
    }

    public OperationsGroupResult getResult(String operationId)
    {
        if (indexOperationGroup.get(operationId) != null)
        {
            OperationsGroupRecord group = indexOperationGroup.get(operationId);
            return new OperationsGroupResult(group.notificationVisibility, group.completeRequest, group.failedRequest);
        }
        else
        {
            return null;
        }
    }

    // ////////////////////////////////////////////////////
    // Broadcast
    // ////////////////////////////////////////////////////
    public static final String EXTRA_OPERATION_ID = "taskId";

    public static final String EXTRA_OPERATION_RESULT = "resultOperation";

    public static final String ACTION_DATA_CHANGED = "startOperationService";

    public void notifyCompletion(String operationId, int status)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_OPERATION_COMPLETED);
        i.putExtra(EXTRA_OPERATION_ID, operationId);
        i.putExtra(EXTRA_OPERATION_RESULT, status);
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(i);
    }

    public void notifyDataChanged()
    {
        Intent i = new Intent(ACTION_DATA_CHANGED);
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(i);
    }

    public void forceStop(int operationId)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_OPERATION_STOP);
        i.putExtra(EXTRA_OPERATION_ID, operationId + "");
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(i);
    }

    public void pause(int operationId)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_OPERATION_PAUSE);
        i.putExtra(EXTRA_OPERATION_ID, operationId + "");
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(i);
    }

    public void forceStop()
    {
        Intent i = new Intent(IntentIntegrator.ACTION_OPERATIONS_STOP);
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(i);
    }

    public void cancelAll(Context c)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_OPERATIONS_CANCEL);
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }
}
