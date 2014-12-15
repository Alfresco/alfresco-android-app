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
package org.alfresco.mobile.android.async;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.alfresco.mobile.android.async.Operation.OperationCallback;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

@SuppressWarnings("rawtypes")
public class OperationsDispatcher
{

    // ////////////////////////////////////////////////////
    // REGISTRY
    // ////////////////////////////////////////////////////

    private static final int AIRPLANE_MODE_ON = 1;

    private static final int AIRPLANE_MODE_OFF = 0;

    static final int REQUEST_SUBMIT = 1;

    static final int REQUEST_CANCEL = 2;

    static final int OPERATION_COMPLETE = 4;

    static final int OPERATION_FAILED = 6;

    static final int OPERATION_DELAY_NEXT_BATCH = 7;

    static final int OPERATION_BATCH_COMPLETE = 8;

    static final int NETWORK_STATE_CHANGE = 9;

    static final int AIRPLANE_MODE_CHANGE = 10;

    // ////////////////////////////////////////////////////
    // CONSTANTS
    // ////////////////////////////////////////////////////
    static final String THREAD_PREFIX = "BatchOperation-";

    private static final String DISPATCHER_THREAD_NAME = "Dispatcher";

    // ////////////////////////////////////////////////////
    // MEMBERS
    // ////////////////////////////////////////////////////
    final DispatcherThread dispatcherThread;

    final Context context;

    final ExecutorService service;

    final ExecutorService longRunningService;

    final Map<String, Operation> operationMap;

    final Map<String, List<Operation>> operationCompleteGroupIndex;

    final Map<String, List<Operation>> operationInProgressGroupIndex;

    final DispatcherHandler handler;

    final Handler mainThreadHandler;

    final NetworkBroadcastReceiver receiver;

    NetworkInfo networkInfo;

    boolean airplaneMode;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    OperationsDispatcher(Context context, ExecutorService service, ExecutorService longRunningService,
            Handler mainThreadHandler)
    {
        this.dispatcherThread = new DispatcherThread();
        this.dispatcherThread.start();
        this.context = context;
        this.service = service;
        this.longRunningService = longRunningService;
        this.operationMap = new LinkedHashMap<String, Operation>();
        this.operationCompleteGroupIndex = new LinkedHashMap<String, List<Operation>>(2);
        this.operationInProgressGroupIndex = new LinkedHashMap<String, List<Operation>>(2);
        this.handler = new DispatcherHandler(dispatcherThread.getLooper(), this);
        this.mainThreadHandler = mainThreadHandler;
        this.receiver = new NetworkBroadcastReceiver(this.context);
        receiver.register();
    }

    // ////////////////////////////////////////////////////
    // LIFE CYCLE
    // ////////////////////////////////////////////////////
    void shutdown()
    {
        service.shutdown();
        dispatcherThread.quit();
        receiver.unregister();
    }

    // ////////////////////////////////////////////////////
    // DISPATCH
    // ////////////////////////////////////////////////////
    public void dispatchSubmit(OperationAction action)
    {
        handler.sendMessage(handler.obtainMessage(REQUEST_SUBMIT, action));
    }

    public void dispatchCancel(String operationId)
    {
        handler.sendMessage(handler.obtainMessage(REQUEST_CANCEL, operationId));
    }

    public void dispatchFailed(Operation hunter)
    {
        handler.sendMessage(handler.obtainMessage(OPERATION_FAILED, hunter));
    }

    public void dispatchComplete(Operation hunter)
    {
        handler.sendMessage(handler.obtainMessage(OPERATION_COMPLETE, hunter));
    }

    public void dispatchNetworkStateChange(NetworkInfo info)
    {
        handler.sendMessage(handler.obtainMessage(NETWORK_STATE_CHANGE, info));
    }

    public void dispatchAirplaneModeChange(boolean airplaneMode)
    {
        handler.sendMessage(handler.obtainMessage(AIRPLANE_MODE_CHANGE, airplaneMode ? AIRPLANE_MODE_ON
                : AIRPLANE_MODE_OFF, 0));
    }

    // ////////////////////////////////////////////////////
    // PERFORM
    // ////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    public void performSubmit(OperationAction action)
    {
        if (service.isShutdown()) { return; }

        Operation operation = OperationsFactory.getTask(action.operator, this, action);
        OperationCallback callback = OperationsFactory.getCallBack(action.operator, this, action);

        boolean isBatch = !TextUtils.isEmpty(action.groupKey);
        if (isBatch && !operationCompleteGroupIndex.containsKey(action.groupKey))
        {
            operationCompleteGroupIndex.put(action.groupKey, new ArrayList<Operation>(action.requestNumber));
            operationInProgressGroupIndex.put(action.groupKey, new ArrayList<Operation>(action.requestNumber));
        }

        if (operation != null)
        {
            Log.d("Operations", "[Start] " + operation.getOperationId() + " " + action.request.getClass().getName());
            operation.setOperationCallBack(callback);

            if (action.request.isLongRunning())
            {
                operation.future = longRunningService.submit(operation);
            }
            else
            {
                operation.future = service.submit(operation);
            }
            operationMap.put(action.key, operation);
            if (isBatch)
            {
                operationInProgressGroupIndex.get(operation.action.groupKey).add(operation);
            }
        }
    }

    public void performCancel(String operationId)
    {
        if (operationId == null) { return; }
        if (operationId.startsWith("GROUP_"))
        {
            List<Operation> operations = operationInProgressGroupIndex.get(operationId);
            if (operations != null)
            {
                List<Operation> cancelledOperations = new ArrayList<Operation>(operations);
                operations.clear();
                operationInProgressGroupIndex.remove(operationId);
                for (Operation operation : cancelledOperations)
                {
                    operation.cancel();
                    performComplete(operation);
                }
            }
        }
        else
        {
            Operation operation = operationMap.get(operationId);
            if (operation != null)
            {
                operation.cancel();
                performComplete(operation);
            }
        }
    }

    public void performComplete(Operation operation)
    {
        operationMap.remove(operation.getKey());
        if (TextUtils.isEmpty(operation.action.groupKey))
        {
            EventBusManager.getInstance().post(new BatchOperationEvent(operation));
            if (operation.request.notificationVisibility != OperationRequest.VISIBILITY_NOTIFICATIONS)
            {
                OperationsUtils.removeOperationUri(context, operation.action.request);
            }
        }
        else
        {
            batch(operation);
        }
    }

    void performBatchComplete(String key)
    {
        List<Operation> batch = operationCompleteGroupIndex.remove(key);
        List<Operation> copy = new ArrayList<Operation>(batch);
        batch.clear();
        mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(OPERATION_BATCH_COMPLETE, copy));
    }

    private void batch(Operation operation)
    {
        List<Operation> completedBatchOperation = operationCompleteGroupIndex.get(operation.action.groupKey);
        if (completedBatchOperation != null)
        {
            List<Operation> batchInProgress = operationInProgressGroupIndex.get(operation.action.groupKey);
            if (batchInProgress != null)
            {
                batchInProgress.remove(operation);
            }
            completedBatchOperation.add(operation);
            if (completedBatchOperation.size() == operation.action.requestNumber
                    && !handler.hasMessages(OPERATION_DELAY_NEXT_BATCH))
            {
                handler.sendMessage(handler.obtainMessage(OPERATION_DELAY_NEXT_BATCH, operation.action.groupKey));
            }
        }
    }

    void performNetworkStateChange(NetworkInfo info)
    {
        networkInfo = info;
        if (service instanceof OperationsPoolExecutor)
        {
            ((OperationsPoolExecutor) service).adjustThreadCount(info);
        }
        if (longRunningService instanceof OperationsPoolExecutor)
        {
            ((OperationsPoolExecutor) longRunningService).adjustThreadCount(info);
        }
    }

    // ////////////////////////////////////////////////////
    // HANDLER THREAD
    // ////////////////////////////////////////////////////
    static class DispatcherThread extends HandlerThread
    {
        DispatcherThread()
        {
            super(THREAD_PREFIX + DISPATCHER_THREAD_NAME, THREAD_PRIORITY_BACKGROUND);
        }
    }

    // ////////////////////////////////////////////////////
    // HANDLER
    // ////////////////////////////////////////////////////
    private static class DispatcherHandler extends Handler
    {
        private final OperationsDispatcher dispatcher;

        public DispatcherHandler(Looper looper, OperationsDispatcher dispatcher)
        {
            super(looper);
            this.dispatcher = dispatcher;
        }

        @Override
        public void handleMessage(final Message msg)
        {
            switch (msg.what)
            {
                case REQUEST_SUBMIT:
                {
                    OperationAction action = (OperationAction) msg.obj;
                    dispatcher.performSubmit(action);
                    break;
                }
                case REQUEST_CANCEL:
                {
                    String operationId = (String) msg.obj;
                    dispatcher.performCancel(operationId);
                    break;
                }
                case OPERATION_FAILED:
                {
                    Operation operation = (Operation) msg.obj;
                    dispatcher.performComplete(operation);
                    break;
                }
                case OPERATION_COMPLETE:
                {
                    Operation operation = (Operation) msg.obj;
                    dispatcher.performComplete(operation);
                    break;
                }
                case OPERATION_DELAY_NEXT_BATCH:
                {
                    dispatcher.performBatchComplete((String) msg.obj);
                    break;
                }
                case NETWORK_STATE_CHANGE:
                {
                    NetworkInfo info = (NetworkInfo) msg.obj;
                    dispatcher.performNetworkStateChange(info);
                    break;
                }
                default:
                    break;
            }
        }
    }

    // ////////////////////////////////////////////////////
    // NetworkBroadcastReceiver
    // ////////////////////////////////////////////////////
    private class NetworkBroadcastReceiver extends BroadcastReceiver
    {
        private static final String EXTRA_AIRPLANE_STATE = "state";

        private final ConnectivityManager connectivityManager;

        NetworkBroadcastReceiver(Context context)
        {
            connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        }

        void register()
        {
            boolean shouldScanState = service instanceof OperationsPoolExecutor && //
                    ConnectivityUtils.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_AIRPLANE_MODE_CHANGED);
            if (shouldScanState)
            {
                filter.addAction(CONNECTIVITY_ACTION);
            }
            context.registerReceiver(this, filter);
        }

        void unregister()
        {
            context.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            // On some versions of Android this may be called with a null Intent
            if (null == intent) { return; }

            String action = intent.getAction();
            Bundle extras = intent.getExtras();

            if (ACTION_AIRPLANE_MODE_CHANGED.equals(action))
            {
                dispatchAirplaneModeChange(extras.getBoolean(EXTRA_AIRPLANE_STATE, false));
            }
            else if (CONNECTIVITY_ACTION.equals(action))
            {
                dispatchNetworkStateChange(connectivityManager.getActiveNetworkInfo());
            }
        }
    }
}
