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
package org.alfresco.mobile.android.application.operations.sync;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.Operation.OperationCallBack;
import org.alfresco.mobile.android.application.operations.OperationsGroupInfo;
import org.alfresco.mobile.android.application.operations.sync.impl.AbstractSyncOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.sync.impl.AbstractSyncOperationThread;
import org.alfresco.mobile.android.application.operations.sync.node.delete.SyncDeleteCallback;
import org.alfresco.mobile.android.application.operations.sync.node.delete.SyncDeleteRequest;
import org.alfresco.mobile.android.application.operations.sync.node.delete.SyncDeleteThread;
import org.alfresco.mobile.android.application.operations.sync.node.download.SyncDownloadCallBack;
import org.alfresco.mobile.android.application.operations.sync.node.download.SyncDownloadRequest;
import org.alfresco.mobile.android.application.operations.sync.node.download.SyncDownloadThread;
import org.alfresco.mobile.android.application.operations.sync.node.update.SyncUpdateCallback;
import org.alfresco.mobile.android.application.operations.sync.node.update.SyncUpdateRequest;
import org.alfresco.mobile.android.application.operations.sync.node.update.SyncUpdateThread;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class SynchroService<T> extends Service
{
    private SynchroManager syncManager;

    private Map<String, Operation<T>> operations = new HashMap<String, Operation<T>>();

    private Set<String> lastOperation = new HashSet<String>();

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null && intent.getExtras() != null)
        {
            startService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // ////////////////////////////////////////////////////
    // LIFECYCLE
    // ////////////////////////////////////////////////////
    @Override
    public void onCreate()
    {
        super.onCreate();

        // Broadcast Receiver
        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_SYNCHRO_COMPLETED);
        intentFilter.addAction(IntentIntegrator.ACTION_SYNCHRO_STOP);
        intentFilter.addAction(IntentIntegrator.ACTION_SYNCHROS_STOP);
        intentFilter.addAction(SynchroManager.ACTION_START_SYNC);
        intentFilter.addAction(IntentIntegrator.ACTION_SYNCHROS_CANCEL);
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(new OperationReceiver(), intentFilter);
    }

    // ////////////////////////////////////////////////////
    // Service lifecycle
    // ////////////////////////////////////////////////////
    private void startService()
    {
        syncManager = SynchroManager.getInstance(getBaseContext());
        executeOperation();
    }

    @SuppressWarnings({ "unchecked" })
    private void executeOperation()
    {
        if (syncManager == null || getBaseContext() == null)
        {
            stopSelf();
            return;
        }

        OperationsGroupInfo requestInfo = (OperationsGroupInfo) syncManager.next();
        if (requestInfo == null) { stopSelf(); return;}

        AbstractSyncOperationRequestImpl request = (AbstractSyncOperationRequestImpl) requestInfo.request;
        int totalItems = requestInfo.totalRequests;
        int pendingRequest = requestInfo.pendingRequests;

        Log.d("OperationService", "Start : " + requestInfo.request.getNotificationTitle());

        Operation<T> task = null;
        OperationCallBack<T> callback = null;
        int parallelOperation = 1;
        switch (request.getTypeId())
        {
            case SyncDownloadRequest.TYPE_ID:
                task = (Operation<T>) new SyncDownloadThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new SyncDownloadCallBack(getBaseContext(), totalItems, pendingRequest);
                parallelOperation = 4;
                break;
            case SyncDeleteRequest.TYPE_ID:
                task = (Operation<T>) new SyncDeleteThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new SyncDeleteCallback(getBaseContext(), totalItems, pendingRequest);
                parallelOperation = 3;
                break;
            case SyncUpdateRequest.TYPE_ID:
                task = (Operation<T>) new SyncUpdateThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new SyncUpdateCallback(getBaseContext(), totalItems, pendingRequest);
                parallelOperation = 2;
                break;
            default:
                break;
        }

        if (callback != null)
        {
            task.setOperationCallBack(callback);
        }

        if (ConnectivityUtils.hasInternetAvailable(getBaseContext()))
        {
            if (pendingRequest == 0)
            {
                lastOperation.add(task.getOperationId());
            }
            operations.put(task.getOperationId(), task);

            if (operations.size() < parallelOperation && requestInfo.pendingRequests > 0)
            {
                executeOperation();
            }
            ((Thread) task).start();
        }
        else
        {
            syncManager.pause(Integer.parseInt(request.getNotificationUri().getLastPathSegment().toString()));
            executeOperation();
        }
    }

    // ////////////////////////////////////////////////////
    // BroadcastReceiver
    // ////////////////////////////////////////////////////
    public class OperationReceiver extends BroadcastReceiver
    {
        @SuppressWarnings("rawtypes")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // DATA CHANGED START
            if (SynchroManager.ACTION_START_SYNC.equals(intent.getAction()))
            {
                executeOperation();
                return;
            }

            // FORCE STOP
            if (IntentIntegrator.ACTION_SYNCHROS_STOP.equals(intent.getAction())
                    || IntentIntegrator.ACTION_SYNCHROS_CANCEL.equals(intent.getAction()))
            {
                for (Entry<String, Operation<T>> operation : operations.entrySet())
                {
                    ((AbstractSyncOperationThread) operation.getValue()).interrupt();
                }
                operations.clear();
                return;
            }

            if (intent.getExtras() == null) { return; }

            String operationId = (String) intent.getExtras().get(SynchroManager.EXTRA_SYNCHRO_ID);

            // CANCEL TASK / REQUEST
            if (operationId != null && IntentIntegrator.ACTION_SYNCHRO_STOP.equals(intent.getAction()))
            {
                // Check OPeration in progress
                if (operations.get(operationId) != null)
                {
                    ((AbstractSyncOperationThread) operations.get(operationId)).interrupt();
                    operations.remove(operationId);
                }
                return;
            }

            // START NEXT TASK
            if (operationId != null && IntentIntegrator.ACTION_SYNCHRO_COMPLETED.equals(intent.getAction()))
            {
                if (syncManager.isLastOperation(operationId) && operations.get(operationId) != null)
                {
                    ((AbstractSyncOperationThread) operations.get(operationId)).executeGroupCallback(syncManager
                            .getResult(operationId));
                }
                operations.remove(operationId);
                executeOperation();
            }
        }
    }
}
