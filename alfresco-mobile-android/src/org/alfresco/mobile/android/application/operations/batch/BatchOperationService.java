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
package org.alfresco.mobile.android.application.operations.batch;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.Operation.OperationCallBack;
import org.alfresco.mobile.android.application.operations.OperationsGroupInfo;
import org.alfresco.mobile.android.application.operations.OperationsGroupRecord;
import org.alfresco.mobile.android.application.operations.batch.account.CreateAccountCallBack;
import org.alfresco.mobile.android.application.operations.batch.account.CreateAccountRequest;
import org.alfresco.mobile.android.application.operations.batch.account.CreateAccountThread;
import org.alfresco.mobile.android.application.operations.batch.account.LoadSessionCallBack;
import org.alfresco.mobile.android.application.operations.batch.account.LoadSessionRequest;
import org.alfresco.mobile.android.application.operations.batch.account.LoadSessionThread;
import org.alfresco.mobile.android.application.operations.batch.configuration.ConfigurationOperationCallBack;
import org.alfresco.mobile.android.application.operations.batch.configuration.ConfigurationOperationRequest;
import org.alfresco.mobile.android.application.operations.batch.configuration.ConfigurationOperationThread;
import org.alfresco.mobile.android.application.operations.batch.file.create.CreateDirectoryCallBack;
import org.alfresco.mobile.android.application.operations.batch.file.create.CreateDirectoryRequest;
import org.alfresco.mobile.android.application.operations.batch.file.create.CreateDirectoryThread;
import org.alfresco.mobile.android.application.operations.batch.file.delete.DeleteFileCallback;
import org.alfresco.mobile.android.application.operations.batch.file.delete.DeleteFileRequest;
import org.alfresco.mobile.android.application.operations.batch.file.delete.DeleteFileThread;
import org.alfresco.mobile.android.application.operations.batch.file.encryption.DataProtectionCallback;
import org.alfresco.mobile.android.application.operations.batch.file.encryption.DataProtectionRequest;
import org.alfresco.mobile.android.application.operations.batch.file.encryption.FileProtectionThread;
import org.alfresco.mobile.android.application.operations.batch.file.encryption.FolderProtectionThread;
import org.alfresco.mobile.android.application.operations.batch.file.update.RenameCallback;
import org.alfresco.mobile.android.application.operations.batch.file.update.RenameRequest;
import org.alfresco.mobile.android.application.operations.batch.file.update.RenameThread;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateDocumentCallback;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateDocumentThread;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateFolderCallBack;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateFolderRequest;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateFolderThread;
import org.alfresco.mobile.android.application.operations.batch.node.create.RetrieveDocumentNameCallBack;
import org.alfresco.mobile.android.application.operations.batch.node.create.RetrieveDocumentNameRequest;
import org.alfresco.mobile.android.application.operations.batch.node.create.RetrieveDocumentNameThread;
import org.alfresco.mobile.android.application.operations.batch.node.delete.DeleteNodeCallback;
import org.alfresco.mobile.android.application.operations.batch.node.delete.DeleteNodeRequest;
import org.alfresco.mobile.android.application.operations.batch.node.delete.DeleteNodeThread;
import org.alfresco.mobile.android.application.operations.batch.node.download.DownloadCallBack;
import org.alfresco.mobile.android.application.operations.batch.node.download.DownloadRequest;
import org.alfresco.mobile.android.application.operations.batch.node.download.DownloadThread;
import org.alfresco.mobile.android.application.operations.batch.node.favorite.FavoriteNodeCallback;
import org.alfresco.mobile.android.application.operations.batch.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.application.operations.batch.node.favorite.FavoriteNodeThread;
import org.alfresco.mobile.android.application.operations.batch.node.like.LikeNodeCallback;
import org.alfresco.mobile.android.application.operations.batch.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.application.operations.batch.node.like.LikeNodeThread;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdateContentCallback;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdateContentThread;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdatePropertiesCallback;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdatePropertiesRequest;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdatePropertiesThread;
import org.alfresco.mobile.android.application.operations.batch.sync.CleanSyncFavoriteRequest;
import org.alfresco.mobile.android.application.operations.batch.sync.CleanSyncFavoriteThread;
import org.alfresco.mobile.android.application.operations.batch.sync.SyncCallBack;
import org.alfresco.mobile.android.application.operations.batch.sync.SyncFavoriteRequest;
import org.alfresco.mobile.android.application.operations.batch.sync.SyncFavoriteThread;
import org.alfresco.mobile.android.application.operations.batch.workflow.process.start.StartProcessCallback;
import org.alfresco.mobile.android.application.operations.batch.workflow.process.start.StartProcessRequest;
import org.alfresco.mobile.android.application.operations.batch.workflow.process.start.StartProcessThread;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.complete.CompleteTaskCallback;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.complete.CompleteTaskRequest;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.complete.CompleteTaskThread;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.delegate.ReassignTaskCallback;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.delegate.ReassignTaskRequest;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.delegate.ReassignTaskThread;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class BatchOperationService<T> extends Service
{
    private BatchOperationManager batchManager;

    private Map<String, Operation<T>> operations = new HashMap<String, Operation<T>>();

    private Set<String> lastOperation = new HashSet<String>();

    private int parallelOperation;

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
        intentFilter.addAction(BatchOperationManager.ACTION_DATA_CHANGED);
        intentFilter.addAction(IntentIntegrator.ACTION_OPERATIONS_CANCEL);
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(new OperationReceiver(), intentFilter);
    }

    // ////////////////////////////////////////////////////
    // Service lifecycle
    // ////////////////////////////////////////////////////
    private void startService()
    {
        batchManager = BatchOperationManager.getInstance(getBaseContext());
        executeOperation();
    }

    @SuppressWarnings({ "unchecked" })
    private void executeOperation()
    {
        if (batchManager == null || getBaseContext() == null)
        {
            stopSelf();
            return;
        }

        OperationsGroupInfo requestInfo = (OperationsGroupInfo) batchManager.next();
        if (requestInfo == null)
        {
            stopSelf();
            return;
        }

        AbstractBatchOperationRequestImpl request = (AbstractBatchOperationRequestImpl) requestInfo.request;
        int totalItems = requestInfo.totalRequests;
        int pendingRequest = requestInfo.pendingRequests;

        Log.d("OperationService", "Start : " + requestInfo.request.getNotificationTitle());

        AbstractBatchOperationThread<T> task = null;
        OperationCallBack<T> callback = null;
        parallelOperation = 1;
        switch (request.getTypeId())
        {
            case DownloadRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new DownloadThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new DownloadCallBack(getBaseContext(), totalItems, pendingRequest);
                parallelOperation = 4;
                break;
            case CreateDocumentRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new CreateDocumentThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new CreateDocumentCallback(getBaseContext(), totalItems,
                        pendingRequest);
                parallelOperation = 4;
                break;
            case UpdateContentRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new UpdateContentThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new UpdateContentCallback(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case DeleteNodeRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new DeleteNodeThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new DeleteNodeCallback(getBaseContext(), totalItems, pendingRequest);
                parallelOperation = 4;
                break;
            case LikeNodeRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new LikeNodeThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new LikeNodeCallback(getBaseContext(), totalItems, pendingRequest);
                parallelOperation = 4;
                break;
            case FavoriteNodeRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new FavoriteNodeThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new FavoriteNodeCallback(getBaseContext(), totalItems, pendingRequest);
                parallelOperation = 1;
                break;
            case CreateFolderRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new CreateFolderThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new CreateFolderCallBack(getBaseContext(), totalItems, pendingRequest);
                break;
            case LoadSessionRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new LoadSessionThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new LoadSessionCallBack(getBaseContext(), totalItems, pendingRequest);
                break;
            case CreateAccountRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new CreateAccountThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new CreateAccountCallBack(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case UpdatePropertiesRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new UpdatePropertiesThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new UpdatePropertiesCallback(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case DeleteFileRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new DeleteFileThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new DeleteFileCallback(getBaseContext(), totalItems, pendingRequest);
                break;
            case CreateDirectoryRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new CreateDirectoryThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new CreateDirectoryCallBack(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case RenameRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new RenameThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new RenameCallback(getBaseContext(), totalItems, pendingRequest);
                break;
            case SyncFavoriteRequest.TYPE_ID:
                parallelOperation = 1;
                task = (AbstractBatchOperationThread<T>) new SyncFavoriteThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new SyncCallBack(getBaseContext(), totalItems, pendingRequest);
                break;
            case CleanSyncFavoriteRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new CleanSyncFavoriteThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new SyncCallBack(getBaseContext(), totalItems, pendingRequest);
                break;
            case RetrieveDocumentNameRequest.TYPE_ID:
                task = (AbstractBatchOperationThread<T>) new RetrieveDocumentNameThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new RetrieveDocumentNameCallBack(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case StartProcessRequest.TYPE_ID:
                parallelOperation = 1;
                task = (AbstractBatchOperationThread<T>) new StartProcessThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new StartProcessCallback(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case CompleteTaskRequest.TYPE_ID:
                parallelOperation = 1;
                task = (AbstractBatchOperationThread<T>) new CompleteTaskThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new CompleteTaskCallback(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case ReassignTaskRequest.TYPE_ID:
                parallelOperation = 1;
                task = (AbstractBatchOperationThread<T>) new ReassignTaskThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new ReassignTaskCallback(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case DataProtectionRequest.TYPE_ID:
                parallelOperation = 2;
                if (new File(((DataProtectionRequest) request).getFilePath()).isDirectory())
                {
                    task = (AbstractBatchOperationThread<T>) new FolderProtectionThread(getBaseContext(), request);
                }
                else
                {
                    task = (AbstractBatchOperationThread<T>) new FileProtectionThread(getBaseContext(), request);
                }
                callback = (OperationCallBack<T>) new DataProtectionCallback(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            case ConfigurationOperationRequest.TYPE_ID:
                parallelOperation = 1;
                task = (AbstractBatchOperationThread<T>) new ConfigurationOperationThread(getBaseContext(), request);
                callback = (OperationCallBack<T>) new ConfigurationOperationCallBack(getBaseContext(), totalItems,
                        pendingRequest);
                break;
            default:
                break;
        }

        if (callback != null)
        {
            task.setOperationCallBack(callback);
        }

        if ((task.requireNetwork() && ConnectivityUtils.hasInternetAvailable(getBaseContext()))
                || !task.requireNetwork())
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
            batchManager.pause(Integer.parseInt(request.getNotificationUri().getLastPathSegment().toString()));
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
            if (BatchOperationManager.ACTION_DATA_CHANGED.equals(intent.getAction()))
            {
                if (operations.size() < parallelOperation)
                {
                    executeOperation();
                }
                return;
            }

            // FORCE STOP
            if (IntentIntegrator.ACTION_OPERATIONS_STOP.equals(intent.getAction())
                    || IntentIntegrator.ACTION_OPERATIONS_CANCEL.equals(intent.getAction()))
            {
                for (Entry<String, Operation<T>> operation : operations.entrySet())
                {
                    ((AbstractBatchOperationThread) operation.getValue()).interrupt();
                }
                operations.clear();
                return;
            }

            if (intent.getExtras() == null) { return; }

            String operationId = (String) intent.getExtras().get(BatchOperationManager.EXTRA_OPERATION_ID);

            // CANCEL TASK / REQUEST
            if (operationId != null && IntentIntegrator.ACTION_OPERATION_STOP.equals(intent.getAction()))
            {
                // Check OPeration in progress
                if (operations.get(operationId) != null)
                {
                    ((AbstractBatchOperationThread) operations.get(operationId)).interrupt();
                    operations.remove(operationId);
                }
                return;
            }

            // START NEXT TASK
            if (operationId != null && IntentIntegrator.ACTION_OPERATION_COMPLETED.equals(intent.getAction()))
            {
                if (batchManager.isLastOperation(operationId) && operations.get(operationId) != null)
                {
                    OperationsGroupRecord group = batchManager.getOperationGroup(operationId);
                    ((AbstractBatchOperationThread) operations.get(operationId)).executeGroupCallback(batchManager
                            .getResult(operationId));
                }
                operations.remove(operationId);
                executeOperation();
            }
        }
    }
}
