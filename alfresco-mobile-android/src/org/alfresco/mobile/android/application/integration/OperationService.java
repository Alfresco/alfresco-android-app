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
package org.alfresco.mobile.android.application.integration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.mobile.android.application.integration.Operation.OperationCallBack;
import org.alfresco.mobile.android.application.integration.OperationManager.OperationGroupInfo;
import org.alfresco.mobile.android.application.integration.account.CreateAccountCallBack;
import org.alfresco.mobile.android.application.integration.account.CreateAccountRequest;
import org.alfresco.mobile.android.application.integration.account.CreateAccountTask;
import org.alfresco.mobile.android.application.integration.account.LoadSessionCallBack;
import org.alfresco.mobile.android.application.integration.account.LoadSessionRequest;
import org.alfresco.mobile.android.application.integration.account.LoadSessionTask;
import org.alfresco.mobile.android.application.integration.file.create.CreateDirectoryCallBack;
import org.alfresco.mobile.android.application.integration.file.create.CreateDirectoryRequest;
import org.alfresco.mobile.android.application.integration.file.create.CreateDirectoryTask;
import org.alfresco.mobile.android.application.integration.file.delete.DeleteFileCallback;
import org.alfresco.mobile.android.application.integration.file.delete.DeleteFileRequest;
import org.alfresco.mobile.android.application.integration.file.delete.DeleteFileTask;
import org.alfresco.mobile.android.application.integration.file.update.RenameCallback;
import org.alfresco.mobile.android.application.integration.file.update.RenameRequest;
import org.alfresco.mobile.android.application.integration.file.update.RenameTask;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationTask;
import org.alfresco.mobile.android.application.integration.node.create.CreateDocumentCallback;
import org.alfresco.mobile.android.application.integration.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.application.integration.node.create.CreateDocumentTask;
import org.alfresco.mobile.android.application.integration.node.create.CreateFolderCallBack;
import org.alfresco.mobile.android.application.integration.node.create.CreateFolderRequest;
import org.alfresco.mobile.android.application.integration.node.create.CreateFolderTask;
import org.alfresco.mobile.android.application.integration.node.delete.DeleteNodeCallback;
import org.alfresco.mobile.android.application.integration.node.delete.DeleteNodeRequest;
import org.alfresco.mobile.android.application.integration.node.delete.DeleteNodeThread;
import org.alfresco.mobile.android.application.integration.node.download.DownloadCallBack;
import org.alfresco.mobile.android.application.integration.node.download.DownloadRequest;
import org.alfresco.mobile.android.application.integration.node.download.DownloadTask;
import org.alfresco.mobile.android.application.integration.node.favorite.FavoriteNodeCallback;
import org.alfresco.mobile.android.application.integration.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.application.integration.node.favorite.FavoriteNodeTask;
import org.alfresco.mobile.android.application.integration.node.like.LikeNodeCallback;
import org.alfresco.mobile.android.application.integration.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.application.integration.node.like.LikeNodeTask;
import org.alfresco.mobile.android.application.integration.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.application.integration.node.update.UpdateContentTask;
import org.alfresco.mobile.android.application.integration.node.update.UpdatePropertiesCallback;
import org.alfresco.mobile.android.application.integration.node.update.UpdatePropertiesRequest;
import org.alfresco.mobile.android.application.integration.node.update.UpdatePropertiesTask;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class OperationService<T> extends Service
{
    private OperationManager helper;

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
        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_OPERATION_COMPLETED);
        intentFilter.addAction(IntentIntegrator.ACTION_OPERATION_STOP);
        intentFilter.addAction(IntentIntegrator.ACTION_OPERATIONS_STOP);
        intentFilter.addAction(OperationManager.ACTION_DATA_CHANGED);
        intentFilter.addAction(IntentIntegrator.ACTION_OPERATIONS_CANCEL);
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(new OperationReceiver(), intentFilter);
    }

    // ////////////////////////////////////////////////////
    // Service lifecycle
    // ////////////////////////////////////////////////////
    private void startService()
    {
        helper = OperationManager.getInstance(getBaseContext());
        executeOperation();
    }

    @SuppressWarnings({ "unchecked" })
    private void executeOperation()
    {
        if (helper == null || getBaseContext() == null)
        {
            stopSelf();
        }

        OperationGroupInfo requestInfo = (OperationGroupInfo) helper.next();
        if (requestInfo == null) { return; }

        AbstractOperationRequestImpl request = (AbstractOperationRequestImpl) requestInfo.request;
        int totalItems = requestInfo.totalRequests;
        int pendingRequest = requestInfo.pendingRequests;

        Log.d("OperationService", "Start" + requestInfo.request.getNotificationTitle());

        Operation<T> task = null;
        OperationCallBack<T> callback = null;
        switch (request.getTypeId())
        {
            case DownloadRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new DownloadTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new DownloadCallBack(getBaseContext(), totalItems, pendingRequest);
                break;
            case CreateDocumentRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new CreateDocumentTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new CreateDocumentCallback(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case UpdateContentRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new UpdateContentTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new CreateDocumentCallback(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case DeleteNodeRequest.TYPE_ID:
                // Thread or Asynctask : Thats the question !
                // task = (Operation<T>) new DeleteNodeTask(getBaseContext(), request);
                task = (Operation<T>) new DeleteNodeThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new DeleteNodeCallback(getBaseContext(), totalItems, pendingRequest);
                break;
            case LikeNodeRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new LikeNodeTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new LikeNodeCallback(getBaseContext(), totalItems, pendingRequest);
                break;
            case FavoriteNodeRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new FavoriteNodeTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new FavoriteNodeCallback(getBaseContext(), totalItems, pendingRequest);
                break;
            case CreateFolderRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new CreateFolderTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new CreateFolderCallBack(getBaseContext(), totalItems, pendingRequest);
                break;
            case LoadSessionRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new LoadSessionTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new LoadSessionCallBack(getBaseContext(), totalItems, pendingRequest);
                break;
            case CreateAccountRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new CreateAccountTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new CreateAccountCallBack(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case UpdatePropertiesRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new UpdatePropertiesTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new UpdatePropertiesCallback(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case DeleteFileRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new DeleteFileTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new DeleteFileCallback(getBaseContext(), totalItems, pendingRequest);
                break;
            case CreateDirectoryRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new CreateDirectoryTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new CreateDirectoryCallBack(getBaseContext(), totalItems, pendingRequest);
                break;
            case RenameRequest.TYPE_ID:
                task = (AbstractOperationTask<T>) new RenameTask(getBaseContext(), request);
                callback = (OperationCallBack<T>) new RenameCallback(getBaseContext(), totalItems, pendingRequest);
                break;
            default:
                break;
        }

        if (callback != null)
        {
            task.setOperationCallBack(callback);
            // TODO mulitple batchoperation == one callback.
            if (callback instanceof BatchOperationCallBack)
            {
                setBatchOperationCallBack((BatchOperationCallBack) callback);
            }
        }

        if (ConnectivityUtils.hasInternetAvailable(getBaseContext()))
        {
            if (pendingRequest == 0)
            {
                lastOperation.add(task.getOperationId());
            }
            operations.put(task.getOperationId(), task);

            if (task instanceof Thread)
            {
                ((Thread) task).start();
            }
            else if (task instanceof AbstractOperationTask)
            {
                ((AbstractOperationTask<T>) task).execute();
            }
        }
        else
        {
            getBaseContext().getContentResolver().update(request.getNotificationUri(),
                    request.createContentValues(Operation.STATUS_PAUSED), null, null);
            executeOperation();
        }
    }

    // ////////////////////////////////////////////////////
    // BroadcastReceiver
    // ////////////////////////////////////////////////////
    public class OperationReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // DATA CHANGED START
            if (OperationManager.ACTION_DATA_CHANGED.equals(intent.getAction()))
            {
                executeOperation();
                return;
            }

            // FORCE STOP
            if (IntentIntegrator.ACTION_OPERATIONS_STOP.equals(intent.getAction())
                    || IntentIntegrator.ACTION_OPERATIONS_CANCEL.equals(intent.getAction()))
            {
                for (Entry<String, Operation<T>> operation : operations.entrySet())
                {
                    if (operation.getValue() instanceof AbstractOperationTask)
                    {
                        ((AbstractOperationTask) operation.getValue()).cancel(true);
                    }
                }
                operations.clear();
                return;
            }

            if (intent.getExtras() == null) { return; }

            String operationId = (String) intent.getExtras().get(OperationManager.EXTRA_OPERATION_ID);

            // CANCEL TASK / REQUEST
            if (operationId != null && IntentIntegrator.ACTION_OPERATION_STOP.equals(intent.getAction()))
            {
                // Check OPeration in progress
                if (operations.get(operationId) != null)
                {
                    if (operations.get(operationId) instanceof AbstractOperationTask)
                    {
                        ((AbstractOperationTask) operations.get(operationId)).cancel(true);
                    }
                    operations.remove(operationId);
                }
                return;
            }

            // START NEXT TASK
            if (operationId != null && IntentIntegrator.ACTION_OPERATION_COMPLETED.equals(intent.getAction()))
            {
                operations.remove(operationId);
                if (lastOperation.contains(operationId) && callBack != null)
                {
                    callBack.onPostBatchExecution(helper.getResult(operationId));
                }
                executeOperation();
            }
        }
    }

    // ////////////////////////////////////////////////////
    // BatchOperationCallBack
    // ////////////////////////////////////////////////////
    private BatchOperationCallBack callBack;

    public interface BatchOperationCallBack
    {
        void onPostBatchExecution(OperationGroupResult result);
    }

    public void setBatchOperationCallBack(BatchOperationCallBack listener)
    {
        this.callBack = listener;
    }
}
