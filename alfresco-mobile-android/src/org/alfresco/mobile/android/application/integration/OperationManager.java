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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;
import org.alfresco.mobile.android.application.integration.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.application.integration.node.create.CreateFolderRequest;
import org.alfresco.mobile.android.application.integration.node.delete.DeleteNodeRequest;
import org.alfresco.mobile.android.application.integration.node.download.DownloadRequest;
import org.alfresco.mobile.android.application.integration.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.application.integration.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.application.integration.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;

public class OperationManager
{
    private static final String TAG = OperationManager.class.getName();

    private final Context mAppContext;

    private final ArrayList<OperationGroupRecord> operationsGroups = new ArrayList<OperationGroupRecord>();

    private OperationGroupRecord currentOperationGroup;

    private static final Object mLock = new Object();

    private static OperationManager mInstance;

    private static HashMap<String, OperationGroupRecord> indexOperationGroup = new HashMap<String, OperationGroupRecord>(
            2);

    public static OperationManager getInstance(Context context)
    {
        synchronized (mLock)
        {
            if (mInstance == null)
            {
                mInstance = new OperationManager(context.getApplicationContext());
                context.startService(new Intent(context, OperationService.class).putExtra("t", true));
            }

            return mInstance;
        }
    }

    private OperationManager(Context applicationContext)
    {
        mAppContext = applicationContext;
        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_OPERATION_COMPLETE);
        intentFilter.addAction(IntentIntegrator.ACTION_OPERATION_STOP);
        intentFilter.addAction(IntentIntegrator.ACTION_OPERATIONS_STOP);
        intentFilter.addAction(IntentIntegrator.ACTION_OPERATIONS_CANCEL);
        LocalBroadcastManager.getInstance(mAppContext).registerReceiver(new OperationHelperReceiver(), intentFilter);

        // TODO unregister
        mAppContext.registerReceiver(new NetworkReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void enqueue(OperationRequestGroup group)
    {
        Uri notificationUri = null;
        OperationGroupRecord groupRecord = new OperationGroupRecord(group.getRequests().size());
        for (OperationRequest request : group.getRequests())
        {
            if (((AbstractOperationRequestImpl) request).getNotificationUri() == null)
            {
                notificationUri = mAppContext.getContentResolver().insert(OperationContentProvider.CONTENT_URI,
                        ((AbstractOperationRequestImpl) request).createContentValues(Operation.STATUS_PENDING));
                ((AbstractOperationRequestImpl) request).setNotificationUri(notificationUri);
            }
            indexOperationGroup.put(getOperationId(request), groupRecord);
        }
        groupRecord.initIndex(group.getRequests());
        operationsGroups.add(groupRecord);
        notifyDataChanged(mAppContext);
    }

    public void retry(String id)
    {
        Uri uri = Uri.parse(OperationContentProvider.CONTENT_URI + "/" + id);
        Cursor cursor = mAppContext.getContentResolver().query(uri, OperationSchema.COLUMN_ALL, null, null, null);
        cursor.moveToFirst();
        int requestId = cursor.getInt(OperationSchema.COLUMN_REQUEST_TYPE_ID);
        AbstractOperationRequestImpl request = null;
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
            default:
                break;
        }

        ((AbstractOperationRequestImpl) request).setNotificationUri(uri);

        OperationRequestGroup group = new OperationRequestGroup(mAppContext, request.getAccountId(),
                request.getNetworkId());
        group.enqueue(request);
        enqueue(group);
        cursor.close();
    }

    public OperationGroupInfo next()
    {
        if (currentOperationGroup == null && operationsGroups.isEmpty()) { return null; }

        if (currentOperationGroup == null && !operationsGroups.isEmpty())
        {
            currentOperationGroup = operationsGroups.remove(0);
        }

        if (currentOperationGroup.hasPendingRequest())
        {
            OperationGroupInfo info = currentOperationGroup.next();
            return info;
        }
        else
        {
            currentOperationGroup = null;
            return next();
        }
    }

    public OperationGroupResult getResult(String operationId)
    {
        if (indexOperationGroup.get(operationId) != null)
        {
            OperationGroupRecord group = indexOperationGroup.get(operationId);
            return new OperationGroupResult(group.notificationVisibility, group.completeRequest, group.failedRequest);
        }
        else
        {
            return null;
        }
    }

    // ////////////////////////////////////////////////////
    // Utils
    // ////////////////////////////////////////////////////
    private static String getOperationId(OperationRequest operationRequest)
    {
        return ((AbstractOperationRequestImpl) operationRequest).getNotificationUri().getLastPathSegment().toString();
    }

    private static Uri getNotificationUri(OperationRequest operationRequest)
    {
        return ((AbstractOperationRequestImpl) operationRequest).getNotificationUri();
    }

    // ////////////////////////////////////////////////////
    // Broadcast
    // ////////////////////////////////////////////////////
    public static final String EXTRA_OPERATION_ID = "taskId";

    public static final String EXTRA_OPERATION_RESULT = "resultOperation";

    public static final String ACTION_DATA_CHANGED = "startOperationService";

    public static void notifyCompletion(Context c, String operationId, int status)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_OPERATION_COMPLETE);
        i.putExtra(EXTRA_OPERATION_ID, operationId);
        i.putExtra(EXTRA_OPERATION_RESULT, status);
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    public static void notifyDataChanged(Context c)
    {
        Intent i = new Intent(ACTION_DATA_CHANGED);
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    public static void forceStop(Context c, int operationId)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_OPERATION_STOP);
        i.putExtra(EXTRA_OPERATION_ID, operationId + "");
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    public static void forceStop(Context c)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_OPERATIONS_STOP);
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    public static void cancelAll(Context c)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_OPERATIONS_CANCEL);
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    // ////////////////////////////////////////////////////
    // Broadcast Receiver
    // ////////////////////////////////////////////////////
    private class OperationHelperReceiver extends BroadcastReceiver
    {
        private OperationRequest request;

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (IntentIntegrator.ACTION_OPERATIONS_CANCEL.equals(intent.getAction()) && currentOperationGroup != null)
            {
                for (Entry<String, OperationRequest> requestEntry : currentOperationGroup.index.entrySet())
                {
                    try
                    {
                        context.getContentResolver().update(
                                getNotificationUri(requestEntry.getValue()),
                                ((AbstractOperationRequestImpl) requestEntry.getValue())
                                        .createContentValues(Operation.STATUS_CANCEL), null, null);
                    }
                    catch (Exception e)
                    {
                        continue;
                    }
                    currentOperationGroup.failedRequest.add(requestEntry.getValue());
                }
                // TODO Send to callback ?
                operationsGroups.clear();
                currentOperationGroup = null;
                return;
            }

            if (IntentIntegrator.ACTION_OPERATIONS_STOP.equals(intent.getAction()) && currentOperationGroup != null)
            {
                for (Entry<String, OperationRequest> requestEntry : currentOperationGroup.index.entrySet())
                {
                    try
                    {
                        context.getContentResolver().delete(
                                ((AbstractOperationRequestImpl) requestEntry.getValue()).getNotificationUri(), null,
                                null);
                    }
                    catch (IllegalArgumentException e)
                    {
                        continue;
                    }
                    currentOperationGroup.failedRequest.add(requestEntry.getValue());
                }

                for (OperationRequest request : currentOperationGroup.completeRequest)
                {
                    context.getContentResolver().delete(((AbstractOperationRequestImpl) request).getNotificationUri(),
                            null, null);
                }
                return;
            }

            if (intent.getExtras() == null) { return; }

            String operationId = (String) intent.getExtras().get(EXTRA_OPERATION_ID);

            if (currentOperationGroup == null) { return; }

            // ADD
            if (operationId != null && IntentIntegrator.ACTION_OPERATION_COMPLETE.equals(intent.getAction()))
            {
                int operationStatus = (int) intent.getExtras().getInt(EXTRA_OPERATION_RESULT);
                if (currentOperationGroup.runningRequest.containsKey(operationId))
                {
                    OperationRequest op = currentOperationGroup.runningRequest.remove(operationId);
                    switch (operationStatus)
                    {
                        case Operation.STATUS_SUCCESSFUL:
                            currentOperationGroup.completeRequest.add(op);
                            break;
                        case Operation.STATUS_PAUSED:
                        case Operation.STATUS_CANCEL:
                        case Operation.STATUS_FAILED:
                            currentOperationGroup.failedRequest.add(op);
                            break;
                        default:
                            break;
                    }
                }

                if (currentOperationGroup.index.isEmpty() && currentOperationGroup.runningRequest.isEmpty())
                {
                    currentOperationGroup = null;
                }
                return;
            }

            // STOP & DISPATCH
            if (operationId != null && IntentIntegrator.ACTION_OPERATION_STOP.equals(intent.getAction()))
            {
                if (currentOperationGroup.runningRequest.containsKey(operationId))
                {
                    request = currentOperationGroup.runningRequest.remove(operationId);
                }
                else if (currentOperationGroup.index.containsKey(operationId))
                {
                    request = currentOperationGroup.index.remove(operationId);
                }

                context.getContentResolver().update(getNotificationUri(request),
                        ((AbstractOperationRequestImpl) request).createContentValues(Operation.STATUS_CANCEL), null,
                        null);
                currentOperationGroup.failedRequest.add(request);
            }
        }
    }

    public class NetworkReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (ConnectivityUtils.isWifiAvailable(context))
            {
                String[] projection = { OperationSchema.COLUMN_ID };
                String selection = OperationSchema.COLUMN_STATUS + "=" + Operation.STATUS_PAUSED;
                Cursor cursor = context.getContentResolver().query(OperationContentProvider.CONTENT_URI, projection,
                        selection, null, null);

                while (cursor.moveToNext())
                {
                    retry(cursor.getLong(OperationSchema.COLUMN_ID_ID) + "");
                }
            }
        }
    }

    // ////////////////////////////////////////////////////
    // Inner Class
    // ////////////////////////////////////////////////////
    private static class OperationGroupRecord
    {
        final List<OperationRequest> completeRequest;

        final List<OperationRequest> failedRequest;

        final HashMap<String, OperationRequest> runningRequest;

        final HashMap<String, OperationRequest> index;

        final int totalRequests;

        int notificationVisibility;

        OperationGroupRecord(int size)
        {
            this.completeRequest = new ArrayList<OperationRequest>(size);
            this.failedRequest = new ArrayList<OperationRequest>(size);
            this.runningRequest = new LinkedHashMap<String, OperationRequest>(2);
            this.totalRequests = size;
            this.index = new LinkedHashMap<String, OperationRequest>(size);
        }

        void initIndex(List<OperationRequest> requests)
        {
            this.notificationVisibility = requests.get(0).getNotificationVisibility();
            for (OperationRequest operationRequest : requests)
            {
                index.put(getOperationId(operationRequest), operationRequest);
            }
        }

        public boolean hasPendingRequest()
        {
            return !index.isEmpty();
        }

        public OperationGroupInfo next()
        {
            if (index.isEmpty()) { return null; }
            OperationRequest request = index.remove(index.entrySet().iterator().next().getKey());
            runningRequest.put(getOperationId(request), request);
            return new OperationGroupInfo(request, totalRequests, index.size(), failedRequest.size());
        }
    }

    protected static class OperationGroupInfo
    {
        public final OperationRequest request;

        public final int totalRequests;

        public final int pendingRequests;

        public final int failedRequests;

        public OperationGroupInfo(OperationRequest request, int totalRequests, int pendingRequests, int failedRequests)
        {
            super();
            this.request = request;
            this.totalRequests = totalRequests;
            this.pendingRequests = pendingRequests;
            this.failedRequests = failedRequests;
        }
    }
}
