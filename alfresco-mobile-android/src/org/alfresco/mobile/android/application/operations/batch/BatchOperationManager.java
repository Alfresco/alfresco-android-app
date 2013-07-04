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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationManager;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsGroupRecord;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.account.CreateAccountRequest;
import org.alfresco.mobile.android.application.operations.batch.account.LoadSessionRequest;
import org.alfresco.mobile.android.application.operations.batch.file.create.CreateDirectoryRequest;
import org.alfresco.mobile.android.application.operations.batch.file.delete.DeleteFileRequest;
import org.alfresco.mobile.android.application.operations.batch.file.update.RenameRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateFolderRequest;
import org.alfresco.mobile.android.application.operations.batch.node.create.RetrieveDocumentNameRequest;
import org.alfresco.mobile.android.application.operations.batch.node.delete.DeleteNodeRequest;
import org.alfresco.mobile.android.application.operations.batch.node.download.DownloadRequest;
import org.alfresco.mobile.android.application.operations.batch.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.application.operations.batch.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdatePropertiesRequest;
import org.alfresco.mobile.android.application.operations.batch.sync.CleanSyncFavoriteRequest;
import org.alfresco.mobile.android.application.operations.batch.sync.SyncFavoriteRequest;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;

public class BatchOperationManager extends OperationManager
{
    private static final String TAG = BatchOperationManager.class.getName();

    private static BatchOperationManager mInstance;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public static BatchOperationManager getInstance(Context context)
    {
        synchronized (mLock)
        {
            if (mInstance == null)
            {
                mInstance = new BatchOperationManager(context.getApplicationContext());
                context.startService(new Intent(context, BatchOperationService.class).putExtra("t", true));
            }

            return (BatchOperationManager) mInstance;
        }
    }

    private BatchOperationManager(Context applicationContext)
    {
        super(applicationContext);
        mAppContext.registerReceiver(new NetworkReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    // ////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ////////////////////////////////////////////////////
    public void enqueue(OperationsRequestGroup group)
    {
        Uri notificationUri = null;
        OperationsGroupRecord groupRecord = new OperationsGroupRecord(group.getRequests().size());
        for (OperationRequest request : group.getRequests())
        {
            if (((AbstractBatchOperationRequestImpl) request).getNotificationUri() == null)
            {
                notificationUri = mAppContext.getContentResolver().insert(BatchOperationContentProvider.CONTENT_URI,
                        ((AbstractBatchOperationRequestImpl) request).createContentValues(Operation.STATUS_PENDING));
                ((AbstractBatchOperationRequestImpl) request).setNotificationUri(notificationUri);
            }
            indexOperationGroup.put(OperationsGroupRecord.getOperationId(request), groupRecord);
        }
        groupRecord.initIndex(group.getRequests());
        operationsGroups.add(groupRecord);
        notifyDataChanged();
    }

    public void retry(long[] ids)
    {

        Map<String, OperationsRequestGroup> groups = new HashMap<String, OperationsRequestGroup>(ids.length);

        OperationsRequestGroup group = null;
        Cursor cursor = null;
        Uri uri = null;
        AbstractBatchOperationRequestImpl request = null;

        for (int i = 0; i < ids.length; i++)
        {
            uri = getUri(ids[i]);
            cursor = mAppContext.getContentResolver().query(uri, BatchOperationSchema.COLUMN_ALL, null, null, null);
            cursor.moveToFirst();
            int requestId = cursor.getInt(BatchOperationSchema.COLUMN_REQUEST_TYPE_ID);
            switch (requestId)
            {
                case DownloadRequest.TYPE_ID:
                    request = new DownloadRequest(cursor);
                    break;
                case CreateDocumentRequest.TYPE_ID:
                    request = new CreateDocumentRequest(cursor);
                    break;
                case UpdateContentRequest.TYPE_ID:
                    request = new UpdateContentRequest(cursor);
                    break;
                case DeleteNodeRequest.TYPE_ID:
                    request = new DeleteNodeRequest(cursor);
                    break;
                case LikeNodeRequest.TYPE_ID:
                    request = new LikeNodeRequest(cursor);
                    break;
                case FavoriteNodeRequest.TYPE_ID:
                    request = new FavoriteNodeRequest(cursor);
                    break;
                case CreateFolderRequest.TYPE_ID:
                    request = new CreateFolderRequest(cursor);
                    break;
                case LoadSessionRequest.TYPE_ID:
                    request = new LoadSessionRequest(cursor);
                    break;
                case CreateAccountRequest.TYPE_ID:
                    // request = new CreateAccountRequest(cursor);
                    break;
                case UpdatePropertiesRequest.TYPE_ID:
                    request = new UpdatePropertiesRequest(cursor);
                    break;
                case DeleteFileRequest.TYPE_ID:
                    request = new DeleteFileRequest(cursor);
                    break;
                case CreateDirectoryRequest.TYPE_ID:
                    request = new CreateDirectoryRequest(cursor);
                    break;
                case RenameRequest.TYPE_ID:
                    request = new RenameRequest(cursor);
                    break;
                case SyncFavoriteRequest.TYPE_ID:
                    request = new SyncFavoriteRequest(cursor);
                    break;
                case CleanSyncFavoriteRequest.TYPE_ID:
                    // request = new UnSyncFavoriteRequest(cursor);
                    break;
                case RetrieveDocumentNameRequest.TYPE_ID:
                    request = new RetrieveDocumentNameRequest(cursor);
                    break;
                default:
                    break;
            }

            if (request != null)
            {
                ((AbstractBatchOperationRequestImpl) request).setNotificationUri(uri);

                mAppContext.getContentResolver().update(uri,
                        ((AbstractBatchOperationRequestImpl) request).createContentValues(Operation.STATUS_PENDING),
                        null, null);

                if (groups.containsKey(request.getAccountId() + "***" + request.getNetworkId()))
                {
                    group = groups.get(request.getAccountId() + "***" + request.getNetworkId());
                }
                else
                {
                    group = new OperationsRequestGroup(mAppContext, request.getAccountId(), request.getNetworkId());
                    groups.put(request.getAccountId() + "***" + request.getNetworkId(), group);
                }
                group.enqueue(request);
            }
            else
            {
                Log.d(TAG, "Unable to retry for" + ids[i]);
            }
        }

        for (Entry<String, OperationsRequestGroup> groupSet : groups.entrySet())
        {
            enqueue(groupSet.getValue());
        }
        if (cursor != null)
        {
            cursor.close();
        }
    }

    public void retry(long id)
    {
        retry(new long[] { id });
    }

    // ////////////////////////////////////////////////////
    // Utils
    // ////////////////////////////////////////////////////
    private static Uri getNotificationUri(OperationRequest operationRequest)
    {
        return ((AbstractBatchOperationRequestImpl) operationRequest).getNotificationUri();
    }

    // ////////////////////////////////////////////////////
    // Broadcast Receiver
    // ////////////////////////////////////////////////////
    protected IntentFilter getIntentFilter()
    {
        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_OPERATION_COMPLETED);
        intentFilter.addAction(IntentIntegrator.ACTION_OPERATION_STOP);
        intentFilter.addAction(IntentIntegrator.ACTION_OPERATIONS_STOP);
        intentFilter.addAction(IntentIntegrator.ACTION_OPERATIONS_CANCEL);
        intentFilter.addAction(IntentIntegrator.ACTION_OPERATION_PAUSE);

        return intentFilter;
    }

    @Override
    protected BroadcastReceiver getReceiver()
    {
        return new OperationHelperReceiver();
    }

    private class OperationHelperReceiver extends BroadcastReceiver
    {
        private OperationRequest request;

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (IntentIntegrator.ACTION_OPERATIONS_CANCEL.equals(intent.getAction()))
            {
                OperationsGroupRecord group = null;
                for (int i = 0; i < operationsGroups.size(); i++)
                {
                    group = operationsGroups.get(i);
                    for (Entry<String, OperationRequest> requestEntry : group.runningRequest.entrySet())
                    {
                        try
                        {
                            context.getContentResolver().update(
                                    getNotificationUri(requestEntry.getValue()),
                                    ((AbstractBatchOperationRequestImpl) requestEntry.getValue())
                                            .createContentValues(Operation.STATUS_CANCEL), null, null);
                        }
                        catch (Exception e)
                        {
                            continue;
                        }
                        group.failedRequest.add(requestEntry.getValue());
                    }
                    group.runningRequest.clear();

                    for (Entry<String, OperationRequest> requestEntry : group.index.entrySet())
                    {
                        try
                        {
                            context.getContentResolver().update(
                                    getNotificationUri(requestEntry.getValue()),
                                    ((AbstractBatchOperationRequestImpl) requestEntry.getValue())
                                            .createContentValues(Operation.STATUS_CANCEL), null, null);
                        }
                        catch (Exception e)
                        {
                            continue;
                        }
                        group.failedRequest.add(requestEntry.getValue());
                    }
                    group.index.clear();
                }
                operationsGroups.clear();
                return;
            }

            if (IntentIntegrator.ACTION_OPERATIONS_STOP.equals(intent.getAction()))
            {
                OperationsGroupRecord group = null;
                for (int i = 0; i < operationsGroups.size(); i++)
                {
                    group = operationsGroups.get(i);
                    for (Entry<String, OperationRequest> requestEntry : group.runningRequest.entrySet())
                    {
                        try
                        {
                            context.getContentResolver().delete(
                                    ((AbstractBatchOperationRequestImpl) requestEntry.getValue()).getNotificationUri(),
                                    null, null);
                        }
                        catch (Exception e)
                        {
                            continue;
                        }
                        group.failedRequest.add(requestEntry.getValue());
                    }
                    group.runningRequest.clear();

                    for (Entry<String, OperationRequest> requestEntry : group.index.entrySet())
                    {
                        try
                        {
                            context.getContentResolver().delete(
                                    ((AbstractBatchOperationRequestImpl) requestEntry.getValue()).getNotificationUri(),
                                    null, null);
                        }
                        catch (Exception e)
                        {
                            continue;
                        }
                        group.failedRequest.add(requestEntry.getValue());
                    }
                    group.index.clear();


                    for (OperationRequest operationRequest : group.completeRequest)
                    {
                        context.getContentResolver()
                                .delete(((AbstractBatchOperationRequestImpl) operationRequest).getNotificationUri(),
                                        null, null);
                    }

                }
                return;
            }

            if (intent.getExtras() == null) { return; }

            String operationId = (String) intent.getExtras().get(EXTRA_OPERATION_ID);

            OperationsGroupRecord currentGroup = getOperationGroup(operationId);

            // ADD
            if (operationId != null && IntentIntegrator.ACTION_OPERATION_COMPLETED.equals(intent.getAction()))
            {
                int operationStatus = (int) intent.getExtras().getInt(EXTRA_OPERATION_RESULT);
                if (currentGroup.runningRequest.containsKey(operationId))
                {
                    OperationRequest op = currentGroup.runningRequest.remove(operationId);
                    switch (operationStatus)
                    {
                        case Operation.STATUS_SUCCESSFUL:
                            currentGroup.completeRequest.add(op);
                            break;
                        case Operation.STATUS_PAUSED:
                        case Operation.STATUS_CANCEL:
                        case Operation.STATUS_FAILED:
                            currentGroup.failedRequest.add(op);
                            break;
                        default:
                            break;
                    }
                }
                return;
            }

            // STOP & DISPATCH
            if (operationId != null && IntentIntegrator.ACTION_OPERATION_STOP.equals(intent.getAction()))
            {
                if (currentGroup.runningRequest.containsKey(operationId))
                {
                    request = currentGroup.runningRequest.remove(operationId);
                }
                else if (currentGroup.index.containsKey(operationId))
                {
                    request = currentGroup.index.remove(operationId);
                }

                context.getContentResolver().update(getNotificationUri(request),
                        ((AbstractBatchOperationRequestImpl) request).createContentValues(Operation.STATUS_CANCEL),
                        null, null);
                currentGroup.failedRequest.add(request);
                return;
            }

            // PAUSE
            if (operationId != null && IntentIntegrator.ACTION_OPERATION_PAUSE.equals(intent.getAction()))
            {
                if (currentGroup.runningRequest.containsKey(operationId))
                {
                    request = currentGroup.runningRequest.remove(operationId);
                }
                else if (currentGroup.index.containsKey(operationId))
                {
                    request = currentGroup.index.remove(operationId);
                }

                Log.d(TAG, "PAUSED : " + getNotificationUri(request));
                context.getContentResolver().update(getNotificationUri(request),
                        ((AbstractBatchOperationRequestImpl) request).createContentValues(Operation.STATUS_PAUSED),
                        null, null);
                currentGroup.index.put(operationId, request);
                if (!currentGroup.hasRunningRequest())
                {
                    operationsGroups.remove(currentGroup);
                }
                
                return;
            }
        }
    }

    public class NetworkReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Cursor cursor = null;
            try
            {
                if (ConnectivityUtils.isWifiAvailable(context))
                {
                    // BATCH OPERATIONS
                    String[] projection = { BatchOperationSchema.COLUMN_ID };
                    String selection = BatchOperationSchema.COLUMN_STATUS + "=" + Operation.STATUS_PAUSED;
                    cursor = context.getContentResolver().query(BatchOperationContentProvider.CONTENT_URI, projection,
                            selection, null, null);

                    if (cursor.getCount() == 0) { return; }
                    long[] ids = new long[cursor.getCount()];
                    int i = 0;
                    while (cursor.moveToNext())
                    {
                        ids[i] = cursor.getLong(BatchOperationSchema.COLUMN_ID_ID);
                        i++;
                    }
                    retry(ids);
                }
            }
            catch (Exception e)
            {
                // TODO: handle exception
            }
            finally
            {
                if (cursor != null)
                {
                    cursor.close();
                }
            }

        }
    }

    // ////////////////////////////////////////////////////
    // Static Method - Shortcut
    // ////////////////////////////////////////////////////
    public static Uri getUri(long id)
    {
        return Uri.parse(BatchOperationContentProvider.CONTENT_URI + "/" + id);
    }

    public static ContentValues createDefaultContentValues()
    {
        ContentValues cValues = new ContentValues();
        cValues.put(BatchOperationSchema.COLUMN_ACCOUNT_ID, -1);
        cValues.put(BatchOperationSchema.COLUMN_TENANT_ID, "");
        cValues.put(BatchOperationSchema.COLUMN_STATUS, -1);
        cValues.put(BatchOperationSchema.COLUMN_REASON, -1);
        cValues.put(BatchOperationSchema.COLUMN_REQUEST_TYPE, -1);
        cValues.put(BatchOperationSchema.COLUMN_TITLE, "");
        cValues.put(BatchOperationSchema.COLUMN_NOTIFICATION_VISIBILITY, OperationRequest.VISIBILITY_NOTIFICATIONS);
        cValues.put(BatchOperationSchema.COLUMN_NODE_ID, "");
        cValues.put(BatchOperationSchema.COLUMN_PARENT_ID, "");
        cValues.put(BatchOperationSchema.COLUMN_MIMETYPE, "");
        cValues.put(BatchOperationSchema.COLUMN_PROPERTIES, "");
        cValues.put(BatchOperationSchema.COLUMN_TOTAL_SIZE_BYTES, -1);
        cValues.put(BatchOperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, -1);
        cValues.put(BatchOperationSchema.COLUMN_LOCAL_URI, "");
        return cValues;
    }

}
