/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.fragments.favorites.SyncScanInfo;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationManager;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsGroupRecord;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.sync.CleanSyncFavoriteRequest;
import org.alfresco.mobile.android.application.operations.batch.sync.SyncPrepareRequest;
import org.alfresco.mobile.android.application.operations.sync.impl.AbstractSyncOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.sync.node.delete.SyncDeleteRequest;
import org.alfresco.mobile.android.application.operations.sync.node.download.SyncDownloadRequest;
import org.alfresco.mobile.android.application.operations.sync.node.update.SyncUpdateRequest;
import org.alfresco.mobile.android.application.operations.sync.utils.NodeSyncPlaceHolder;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;
import org.alfresco.mobile.android.application.utils.CursorUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public final class SynchroManager extends OperationManager
{
    private static final String TAG = SynchroManager.class.getName();

    private static SynchroManager mInstance;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public static SynchroManager getInstance(Context context)
    {
        synchronized (mLock)
        {
            if (mInstance == null)
            {
                mInstance = new SynchroManager(context.getApplicationContext());
                context.startService(new Intent(context, SynchroService.class).putExtra("t", true));
            }

            return (SynchroManager) mInstance;
        }
    }

    private SynchroManager(Context applicationContext)
    {
        super(applicationContext);
        mAppContext.registerReceiver(new NetworkReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    // ////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ////////////////////////////////////////////////////
    public void enqueue(OperationsRequestGroup group)
    {
        OperationsGroupRecord groupRecord = new OperationsGroupRecord(group.getRequests().size());
        for (OperationRequest request : group.getRequests())
        {
            indexOperationGroup.put(OperationsGroupRecord.getOperationId(request), groupRecord);
        }
        groupRecord.initIndex(group.getRequests());
        operationsGroups.add(groupRecord);
        notifyDataChanged(mAppContext);
    }

    public void retry(long[] ids)
    {
        Map<String, OperationsRequestGroup> groups = new HashMap<String, OperationsRequestGroup>(ids.length);

        OperationsRequestGroup group = null;
        Cursor cursor = null;
        Uri uri = null;
        AbstractSyncOperationRequestImpl request = null;

        for (int i = 0; i < ids.length; i++)
        {
            uri = getUri(ids[i]);
            cursor = mAppContext.getContentResolver().query(uri, SynchroSchema.COLUMN_ALL, null, null, null);
            cursor.moveToFirst();
            int requestId = cursor.getInt(SynchroSchema.COLUMN_REQUEST_TYPE_ID);
            switch (requestId)
            {
                case SyncDeleteRequest.TYPE_ID:
                    request = new SyncDeleteRequest(cursor);
                    break;
                case SyncDownloadRequest.TYPE_ID:
                    request = new SyncDownloadRequest(cursor);
                    break;
                case SyncUpdateRequest.TYPE_ID:
                    request = new SyncUpdateRequest(cursor);
                    break;
                default:
                    break;
            }

            if (request != null)
            {
                ((AbstractSyncOperationRequestImpl) request).setNotificationUri(uri);

                mAppContext.getContentResolver().update(uri,
                        ((AbstractSyncOperationRequestImpl) request).createContentValues(Operation.STATUS_PENDING),
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
        CursorUtils.closeCursor(cursor);
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
        return ((AbstractSyncOperationRequestImpl) operationRequest).getNotificationUri();
    }

    // ////////////////////////////////////////////////////
    // EVENTS
    // ////////////////////////////////////////////////////
    public static final String EXTRA_SYNCHRO_ID = "synchroId";

    public static final String EXTRA_SYNCHRO_RESULT = "synchroResult";

    public static final String ACTION_START_SYNC = "startSyncService";

    private static final String LAST_SYNC_ACTIVATED_AT = "LastSyncDateTime";

    private static final String LAST_START_SYNC_PREPARE = "LastSyncPrepareDateTime";

    public void notifyCompletion(Context c, String operationId, int status)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_SYNCHRO_COMPLETED);
        i.putExtra(EXTRA_SYNCHRO_ID, operationId);
        i.putExtra(EXTRA_SYNCHRO_RESULT, status);
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    public void notifyDataChanged(Context c)
    {
        Intent i = new Intent(ACTION_START_SYNC);
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    public void forceStop(Context c, int operationId)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_SYNCHRO_STOP);
        i.putExtra(EXTRA_SYNCHRO_ID, operationId + "");
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    public void forceStop(Context c)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_SYNCHROS_STOP);
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    public void cancelAll(Context c)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_SYNCHROS_CANCEL);
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    public void pause(int operationId)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_SYNCHRO_PAUSE);
        i.putExtra(EXTRA_SYNCHRO_ID, operationId + "");
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(i);
    }

    // ////////////////////////////////////////////////////
    // Broadcast Receiver
    // ////////////////////////////////////////////////////
    protected IntentFilter getIntentFilter()
    {
        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_SYNCHRO_COMPLETED);
        intentFilter.addAction(IntentIntegrator.ACTION_SYNCHRO_STOP);
        intentFilter.addAction(IntentIntegrator.ACTION_SYNCHROS_STOP);
        intentFilter.addAction(IntentIntegrator.ACTION_SYNCHROS_CANCEL);
        intentFilter.addAction(IntentIntegrator.ACTION_SYNCHRO_PAUSE);
        intentFilter.addAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
        intentFilter.addAction(IntentIntegrator.ACTION_DELETE_COMPLETED);
        intentFilter.addAction(IntentIntegrator.ACTION_FAVORITE_COMPLETED);
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
            Log.d(TAG, intent.getAction());

            if (intent.getExtras() != null
                    && (IntentIntegrator.ACTION_DELETE_COMPLETED.equals(intent.getAction())
                            || IntentIntegrator.ACTION_UPDATE_COMPLETED.equals(intent.getAction()) || IntentIntegrator.ACTION_FAVORITE_COMPLETED
                                .equals(intent.getAction())))
            {
                Bundle b = intent.getExtras().getParcelable(IntentIntegrator.EXTRA_DATA);
                if (b == null) { return; }
                if (b.getSerializable(IntentIntegrator.EXTRA_FOLDER) instanceof File) { return; }

                if (intent.getAction().equals(IntentIntegrator.ACTION_FAVORITE_COMPLETED))
                {
                    Node node = (Node) b.getParcelable(IntentIntegrator.EXTRA_NODE);
                    Account acc = SessionUtils.getAccount(mAppContext);

                    SyncScanInfo lastScanSyncInfo = SyncScanInfo.getLastSyncScanData(context, acc);
                    // The preceding scan returns an error/warning
                    // We await the next user/automatic scan before redid it
                    if (lastScanSyncInfo != null && lastScanSyncInfo.getScanResponse() == SyncScanInfo.RESPONSE_AWAIT) { return; }

                    if (acc != null && node != null && canSync(acc))
                    {
                        if (b.containsKey(IntentIntegrator.EXTRA_BATCH_FAVORITE)
                                && b.getBoolean(IntentIntegrator.EXTRA_BATCH_FAVORITE))
                        {
                            sync(acc);
                        }
                        else
                        {
                            sync(acc, node);
                        }
                    }
                    return;
                }

                if (intent.getAction().equals(IntentIntegrator.ACTION_DELETE_COMPLETED))
                {
                    Node node = (Node) b.getParcelable(IntentIntegrator.EXTRA_DOCUMENT);
                    Cursor favoriteCursor = mAppContext.getContentResolver().query(
                            SynchroProvider.CONTENT_URI,
                            SynchroSchema.COLUMN_ALL,
                            SynchroSchema.COLUMN_NODE_ID + " LIKE '"
                                    + NodeRefUtils.getCleanIdentifier(node.getIdentifier()) + "%'", null, null);
                    if (favoriteCursor.getCount() == 1 && favoriteCursor.moveToNext())
                    {
                        Account acc = AccountManager.retrieveAccount(mAppContext,
                                favoriteCursor.getLong(SynchroSchema.COLUMN_ACCOUNT_ID_ID));
                        if (canSync(acc))
                        {
                            SynchroManager.getInstance(mAppContext).sync(acc);
                        }
                    }
                    CursorUtils.closeCursor(favoriteCursor);
                    return;
                }

                if (intent.getAction().equals(IntentIntegrator.ACTION_UPDATE_COMPLETED))
                {
                    Node node = null;
                    if (b.containsKey(IntentIntegrator.EXTRA_DOCUMENT))
                    {
                        node = (Node) b.getParcelable(IntentIntegrator.EXTRA_DOCUMENT);
                    }
                    else
                    {
                        node = (Node) b.getParcelable(IntentIntegrator.EXTRA_NODE);
                    }

                    Cursor favoriteCursor = mAppContext.getContentResolver().query(
                            SynchroProvider.CONTENT_URI,
                            SynchroSchema.COLUMN_ALL,
                            SynchroSchema.COLUMN_NODE_ID + " LIKE '"
                                    + NodeRefUtils.getCleanIdentifier(node.getIdentifier()) + "%'", null, null);
                    if (favoriteCursor.getCount() == 1 && favoriteCursor.moveToNext())
                    {
                        Account acc = AccountManager.retrieveAccount(mAppContext,
                                favoriteCursor.getLong(SynchroSchema.COLUMN_ACCOUNT_ID_ID));
                        if (canSync(acc))
                        {
                            SynchroManager.getInstance(mAppContext).sync(acc, node);
                        }
                    }
                    CursorUtils.closeCursor(favoriteCursor);
                    return;
                }
            }

            if (IntentIntegrator.ACTION_SYNCHROS_CANCEL.equals(intent.getAction()))
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
                                    ((AbstractSyncOperationRequestImpl) requestEntry.getValue())
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

            if (IntentIntegrator.ACTION_SYNCHROS_STOP.equals(intent.getAction()))
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
                                    ((AbstractSyncOperationRequestImpl) requestEntry.getValue()).getNotificationUri(),
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
                        context.getContentResolver().delete(
                                ((AbstractSyncOperationRequestImpl) operationRequest).getNotificationUri(), null, null);
                    }

                }
                return;
            }

            if (intent.getExtras() == null) { return; }

            String operationId = (String) intent.getExtras().get(EXTRA_SYNCHRO_ID);

            OperationsGroupRecord currentGroup = getOperationGroup(operationId);

            // ADD
            if (operationId != null && IntentIntegrator.ACTION_SYNCHRO_COMPLETED.equals(intent.getAction()))
            {
                int operationStatus = (int) intent.getExtras().getInt(EXTRA_SYNCHRO_RESULT);
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
            if (operationId != null && IntentIntegrator.ACTION_SYNCHRO_STOP.equals(intent.getAction()))
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
                        ((AbstractSyncOperationRequestImpl) request).createContentValues(Operation.STATUS_CANCEL),
                        null, null);
                currentGroup.failedRequest.add(request);
            }

            // PAUSE
            if (operationId != null && IntentIntegrator.ACTION_SYNCHRO_PAUSE.equals(intent.getAction()))
            {
                if (currentGroup.runningRequest.containsKey(operationId))
                {
                    request = currentGroup.runningRequest.remove(operationId);
                }
                else if (currentGroup.index.containsKey(operationId))
                {
                    request = currentGroup.index.remove(operationId);
                }

                Log.d(TAG, "PAUSED" + getNotificationUri(request));
                context.getContentResolver().update(getNotificationUri(request),
                        ((AbstractSyncOperationRequestImpl) request).createContentValues(Operation.STATUS_PAUSED),
                        null, null);
                currentGroup.index.put(operationId, request);
                if (!currentGroup.hasRunningRequest())
                {
                    operationsGroups.remove(currentGroup);
                }
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
                    // SYNC OPERATIONS
                    String[] projection = { SynchroSchema.COLUMN_ID };
                    String selection = SynchroSchema.COLUMN_STATUS + "=" + Operation.STATUS_PAUSED;
                    cursor = context.getContentResolver().query(SynchroProvider.CONTENT_URI, projection, selection,
                            null, null);

                    if (cursor.getCount() == 0) { return; }
                    long[] ids = new long[cursor.getCount()];
                    int i = 0;
                    while (cursor.moveToNext())
                    {
                        ids[i] = cursor.getLong(SynchroSchema.COLUMN_ID_ID);
                        i++;
                    }
                    retry(ids);
                }
            }
            catch (Exception e)
            {
                // Nothing special
            }
            finally
            {
                CursorUtils.closeCursor(cursor);
            }

        }
    }

    // ////////////////////////////////////////////////////
    // PUBLIC UTILS METHODS
    // ////////////////////////////////////////////////////
    public static ContentValues createContentValues(Context context, Account account, int requestType, Node node,
            long time)
    {
        return createContentValues(context, account, requestType, "", node, time, 0);
    }

    public static ContentValues createFavoriteContentValues(Context context, Account account, int requestType,
            Node node, long time)
    {
        ContentValues cValues = createContentValues(context, account, requestType, "", node, time, 0);
        cValues.put(SynchroSchema.COLUMN_IS_FAVORITE, SynchroProvider.FLAG_FAVORITE);
        return cValues;
    }

    public static ContentValues createContentValues(Context context, Account account, int requestType, String parent,
            Node node, long time, long folderSize)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(SynchroSchema.COLUMN_ACCOUNT_ID, account.getId());
        cValues.put(SynchroSchema.COLUMN_TENANT_ID, account.getRepositoryId());
        cValues.put(SynchroSchema.COLUMN_STATUS, Operation.STATUS_PENDING);
        cValues.put(SynchroSchema.COLUMN_REASON, -1);
        cValues.put(SynchroSchema.COLUMN_REQUEST_TYPE, requestType);
        cValues.put(SynchroSchema.COLUMN_TITLE, node.getName());
        cValues.put(SynchroSchema.COLUMN_NOTIFICATION_VISIBILITY, OperationRequest.VISIBILITY_HIDDEN);
        cValues.put(SynchroSchema.COLUMN_NODE_ID, node.getIdentifier());
        cValues.put(SynchroSchema.COLUMN_PARENT_ID, parent);
        if (node instanceof Document)
        {
            cValues.put(SynchroSchema.COLUMN_MIMETYPE, ((Document) node).getContentStreamMimeType());
            cValues.put(SynchroSchema.COLUMN_TOTAL_SIZE_BYTES, ((Document) node).getContentStreamLength());
            cValues.put(SynchroSchema.COLUMN_DOC_SIZE_BYTES, ((Document) node).getContentStreamLength());
            if (node.getProperty(PropertyIds.CONTENT_STREAM_ID) != null)
            {
                cValues.put(SynchroSchema.COLUMN_CONTENT_URI, (String) node.getProperty(PropertyIds.CONTENT_STREAM_ID)
                        .getValue());
            }
        }
        else
        {
            cValues.put(SynchroSchema.COLUMN_MIMETYPE, ContentModel.TYPE_FOLDER);
            cValues.put(SynchroSchema.COLUMN_TOTAL_SIZE_BYTES, folderSize);
            cValues.put(SynchroSchema.COLUMN_DOC_SIZE_BYTES, 0);
            if (folderSize == 0)
            {
                cValues.put(SynchroSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
            }
        }
        cValues.put(SynchroSchema.COLUMN_PROPERTIES, "");
        cValues.put(SynchroSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, 0);
        cValues.put(SynchroSchema.COLUMN_LOCAL_URI, "");
        cValues.put(SynchroSchema.COLUMN_ANALYZE_TIMESTAMP, time);
        cValues.put(SynchroSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, node.getModifiedAt().getTimeInMillis());
        cValues.put(SynchroSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, time);
        return cValues;
    }

    public static ContentValues createFavoriteContentValues(Context context, Account account, int requestType,
            String parent, Node node, long time, long folderSize)
    {
        ContentValues cValues = createContentValues(context, account, requestType, parent, node, time, folderSize);
        cValues.put(SynchroSchema.COLUMN_IS_FAVORITE, SynchroProvider.FLAG_FAVORITE);
        return cValues;
    }

    public static Uri getUri(long id)
    {
        return Uri.parse(SynchroProvider.CONTENT_URI + "/" + id);
    }

    public void sync(Account account)
    {
        if (account == null) { return; }
        OperationsRequestGroup group = new OperationsRequestGroup(mAppContext, account);
        group.enqueue(new SyncPrepareRequest().setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
        BatchOperationManager.getInstance(mAppContext).enqueue(group);
    }

    public void sync(Account account, Node n)
    {
        if (account == null) { return; }
        OperationsRequestGroup group = new OperationsRequestGroup(mAppContext, account);
        group.enqueue(new SyncPrepareRequest(SyncPrepareRequest.MODE_NODE, n)
                .setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
        BatchOperationManager.getInstance(mAppContext).enqueue(group);
    }

    public void unsync(Account account)
    {
        OperationsRequestGroup group = new OperationsRequestGroup(mAppContext, account);
        group.enqueue(new CleanSyncFavoriteRequest().setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
        BatchOperationManager.getInstance(mAppContext).enqueue(group);
    }

    public boolean isSynced(Account account, String nodeIdentifier)
    {
        if (account == null) { return false; }

        Cursor favoriteCursor = mAppContext.getContentResolver().query(
                SynchroProvider.CONTENT_URI,
                SynchroSchema.COLUMN_ALL,
                SynchroProvider.getAccountFilter(account) + " AND " + SynchroSchema.COLUMN_NODE_ID + " LIKE '"
                        + NodeRefUtils.getCleanIdentifier(nodeIdentifier) + "%'", null, null);
        boolean b = (favoriteCursor.getCount() == 1) && GeneralPreferences.hasActivateSync(mAppContext, account);
        CursorUtils.closeCursor(favoriteCursor);
        return b;
    }

    public boolean isSynced(Account account, Node node)
    {
        if (node.isFolder()) { return false; }
        return isSynced(account, node.getIdentifier());
    }

    public File getSyncFile(Account account, Node node)
    {
        if (node.isFolder()) { return null; }
        if (node instanceof NodeSyncPlaceHolder) { return StorageManager.getSynchroFile(mAppContext, account,
                node.getName(), node.getIdentifier()); }
        return StorageManager.getSynchroFile(mAppContext, account, (Document) node);
    }

    public static Cursor getCursorForId(Context context, Account acc, String identifier)
    {
        if (acc == null) { return null; }

        return context.getContentResolver().query(
                SynchroProvider.CONTENT_URI,
                SynchroSchema.COLUMN_ALL,
                SynchroProvider.getAccountFilter(acc) + " AND " + SynchroSchema.COLUMN_NODE_ID + " LIKE '"
                        + NodeRefUtils.getCleanIdentifier(identifier) + "%'", null, null);
    }

    public Uri getUri(Account account, String nodeIdentifier)
    {
        if (account == null) { return null; }

        Uri b = null;
        Cursor favoriteCursor = getCursorForId(mAppContext, account, nodeIdentifier);
        if (favoriteCursor.getCount() == 1 && favoriteCursor.moveToFirst())
        {
            b = Uri.parse(SynchroProvider.CONTENT_URI + "/" + favoriteCursor.getLong(SynchroSchema.COLUMN_ID_ID));
        }
        CursorUtils.closeCursor(favoriteCursor);
        return b;
    }

    public boolean canSync(Account account)
    {
        return GeneralPreferences.hasActivateSync(mAppContext, account)
                && ((GeneralPreferences.hasWifiOnlySync(mAppContext, account) && ConnectivityUtils
                        .isWifiAvailable(mAppContext)) || !GeneralPreferences.hasWifiOnlySync(mAppContext, account));
    }

    public boolean hasActivateSync(Account account)
    {
        return GeneralPreferences.hasActivateSync(mAppContext, account);
    }

    public boolean canSyncEverything(Account account)
    {
        return GeneralPreferences.canSyncEverything(mAppContext, account);
    }

    public boolean hasConnectivityToSync(Account account)
    {
        return ((GeneralPreferences.hasWifiOnlySync(mAppContext, account) && ConnectivityUtils
                .isWifiAvailable(mAppContext)) || !GeneralPreferences.hasWifiOnlySync(mAppContext, account));
    }

    /**
     * Flag the activity time.
     * 
     * @param context
     */
    public static void saveStartSyncPrepareTimestamp(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPref.edit();
        Account account = SessionUtils.getAccount(context);
        if (account != null)
        {
            editor.putLong(LAST_START_SYNC_PREPARE + account.getId(), new Date().getTime());
            editor.commit();
        }
    }

    public static void saveSyncPrepareTimestamp(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPref.edit();
        Account account = SessionUtils.getAccount(context);
        if (account != null)
        {
            editor.putLong(LAST_SYNC_ACTIVATED_AT + account.getId(), new Date().getTime());
            editor.commit();
        }
    }

    public static long getSyncPrepareTimestamp(Context context, Account account)
    {
        if (account == null){return -1;}
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getLong(LAST_SYNC_ACTIVATED_AT + account.getId(), new Date().getTime());
    }

    public static long getStartSyncPrepareTimestamp(Context context, Account account)
    {
        if (account == null){return -1;}
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getLong(LAST_START_SYNC_PREPARE + account.getId(), new Date().getTime());
    }

    /**
     * Start a sync if the last activity time is greater than 1 hour.
     */
    public void cronSync(Account account)
    {
        if (account == null) { return; }
        long now = new Date().getTime();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        long lastTime = sharedPref.getLong(LAST_SYNC_ACTIVATED_AT + account.getId(), now);
        if ((lastTime + 3600000) < now && canSync(account))
        {
            sync(account);
        }
    }

    public static boolean isFolder(Cursor cursor)
    {
        if (cursor == null) { return false; }
        return ContentModel.TYPE_FOLDER.equals(cursor.getString(SynchroSchema.COLUMN_MIMETYPE_ID));
    }

    // ////////////////////////////////////////////////////
    // STORAGE MANAGEMENT
    // ////////////////////////////////////////////////////
    private static final String QUERY_SUM = "SELECT SUM(" + SynchroSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR + ") FROM "
            + SynchroSchema.TABLENAME + " WHERE " + SynchroSchema.COLUMN_PARENT_ID + " = '%s';";

    private static final String QUERY_SUM_IN_PENDING = "SELECT SUM(" + SynchroSchema.COLUMN_DOC_SIZE_BYTES + ") FROM "
            + SynchroSchema.TABLENAME + " WHERE " + SynchroSchema.COLUMN_STATUS + " IN ( "
            + SyncOperation.STATUS_PENDING + "," + SyncOperation.STATUS_HIDDEN + ");";

    private static final String QUERY_SUM_TOTAL_IN_PENDING = "SELECT SUM(" + SynchroSchema.COLUMN_TOTAL_SIZE_BYTES
            + ") FROM " + SynchroSchema.TABLENAME + " WHERE " + SynchroSchema.COLUMN_STATUS + " = "
            + SyncOperation.STATUS_PENDING + " AND " + SynchroSchema.COLUMN_MIMETYPE + " NOT IN ('"
            + ContentModel.TYPE_FOLDER + "');";

    private static final String QUERY_TOTAL_STORED = "SELECT SUM(" + SynchroSchema.COLUMN_DOC_SIZE_BYTES + ") FROM "
            + SynchroSchema.TABLENAME + " WHERE " + SynchroSchema.COLUMN_STATUS + " IN ("
            + SyncOperation.STATUS_PENDING + ", " + SyncOperation.STATUS_SUCCESSFUL + ");";

    private static final String QUERY_SUM_TOTAL = "SELECT SUM(" + SynchroSchema.COLUMN_TOTAL_SIZE_BYTES + ") FROM "
            + SynchroSchema.TABLENAME + " WHERE " + SynchroSchema.COLUMN_PARENT_ID + " = '%s';";

    public synchronized void updateParentFolder(Account account, String identifier)
    {
        Long currentValue = null;
        Long totalSize = null;
        String parentFolderId = null;
        Cursor favoriteCursor = null, parentCursor = null, cursorTotal = null, cursor = null;

        try
        {
            // Retrieve Uri & ParentFolder
            Uri uri = null;
            favoriteCursor = mAppContext.getContentResolver().query(
                    SynchroProvider.CONTENT_URI,
                    SynchroSchema.COLUMN_ALL,
                    SynchroProvider.getAccountFilter(account) + " AND " + SynchroSchema.COLUMN_NODE_ID + " == '"
                            + NodeRefUtils.getCleanIdentifier(identifier) + "'", null, null);
            if (favoriteCursor.getCount() == 1 && favoriteCursor.moveToFirst())
            {
                parentFolderId = favoriteCursor.getString(SynchroSchema.COLUMN_PARENT_ID_ID);
                uri = Uri.parse(SynchroProvider.CONTENT_URI + "/" + favoriteCursor.getLong(SynchroSchema.COLUMN_ID_ID));

                parentCursor = mAppContext.getContentResolver().query(
                        SynchroProvider.CONTENT_URI,
                        SynchroSchema.COLUMN_ALL,
                        SynchroProvider.getAccountFilter(account) + " AND " + SynchroSchema.COLUMN_NODE_ID + " == '"
                                + NodeRefUtils.getCleanIdentifier(parentFolderId) + "'", null, null);
            }
            else
            {
                return;
            }

            if (parentCursor != null && parentCursor.getCount() == 1 && parentCursor.moveToFirst()
                    && SyncOperation.STATUS_HIDDEN == parentCursor.getInt(SynchroSchema.COLUMN_STATUS_ID))
            {
                // Node has been flag to deletion
                // We don't update
                return;
            }

            // Retrieve the TOTAL sum of children
            totalSize = retrieveSize(String.format(QUERY_SUM_TOTAL, identifier));
            if (totalSize == null) { return; }

            // REtrieve the sum of children
            currentValue = retrieveSize(String.format(QUERY_SUM, identifier));
            if (currentValue == null) { return; }

            // Update the parent
            ContentValues cValues = new ContentValues();
            cValues.put(SynchroSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, currentValue);
            cValues.put(SynchroSchema.COLUMN_TOTAL_SIZE_BYTES, totalSize);
            cValues.put(BatchOperationSchema.COLUMN_STATUS, Operation.STATUS_RUNNING);
            if (totalSize.longValue() == currentValue.longValue())
            {
                cValues.put(BatchOperationSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
            }
            mAppContext.getContentResolver().update(uri, cValues, null, null);
        }
        catch (Exception e)
        {

        }
        finally
        {
            CursorUtils.closeCursor(parentCursor);
            CursorUtils.closeCursor(cursorTotal);
            CursorUtils.closeCursor(favoriteCursor);
            CursorUtils.closeCursor(cursor);
        }

        // Recursive on grand parent
        if (parentFolderId != null)
        {
            updateParentFolder(account, parentFolderId);
        }
    }

    private Long retrieveSize(String query)
    {
        Long totalSize = null;

        // Retrieve the TOTAL sum of children
        Cursor cursorTotal = ApplicationManager.getInstance(mAppContext).getDatabaseManager().getWriteDb()
                .rawQuery(query, null);
        if (cursorTotal.moveToFirst())
        {
            totalSize = cursorTotal.getLong(0);
        }
        return totalSize;
    }

    public Long getAmountDataToTransfert()
    {
        return retrieveSize(QUERY_SUM_TOTAL_IN_PENDING);
    }

    public Long getAmountDataStored()
    {
        return retrieveSize(QUERY_TOTAL_STORED);
    }

    public Long getPreviousAmountDataStored()
    {
        return retrieveSize(QUERY_SUM_IN_PENDING);
    }

    // ////////////////////////////////////////////////////
    // SYNC POLICIES
    // ////////////////////////////////////////////////////
    public SyncScanInfo getScanInfo(Account acc)
    {
        long dataFinalStored = getAmountDataStored();
        long deltaStorage = getPreviousAmountDataStored();
        float totalBytes = StorageManager.getTotalBytes(mAppContext);
        float availableBytes = StorageManager.getAvailableBytes(mAppContext);
        long dataToTransfer = getAmountDataToTransfert();

        Log.d(TAG, "Data Transfer : " + dataToTransfer);
        Log.d(TAG, "Data Final : " + dataFinalStored);
        Log.d(TAG, "Data Delta  : " + deltaStorage);
        Log.d(TAG, "Data AvailableBytes : " + availableBytes);
        Log.d(TAG, "Data TotalBytes : " + totalBytes);

        boolean respectMobileTransferPolicy = respectMobileTransferPolicy(acc, dataToTransfer);
        boolean respectEnoughStorageSpace = respectEnoughStorageSpace(availableBytes, deltaStorage);
        boolean respectLimitStorageSpace = respectLimitStorageSpace(acc, availableBytes, deltaStorage, totalBytes);

        Log.d(TAG, "Transfert Policy : " + respectMobileTransferPolicy);
        Log.d(TAG, "Enough Space : " + respectEnoughStorageSpace);
        Log.d(TAG, "Limit Space : " + respectLimitStorageSpace);

        int scanResult = SyncScanInfo.RESULT_SUCCESS;
        if (!respectEnoughStorageSpace)
        {
            scanResult = SyncScanInfo.RESULT_ERROR_NOT_ENOUGH_STORAGE;
        }
        else if (!respectLimitStorageSpace)
        {
            scanResult = SyncScanInfo.RESULT_WARNING_LOW_STORAGE;
        }
        else if (!respectMobileTransferPolicy)
        {
            scanResult = SyncScanInfo.RESULT_WARNING_MOBILE_DATA;
        }

        return new SyncScanInfo(deltaStorage, dataToTransfer, scanResult);
    }

    private boolean respectMobileTransferPolicy(Account acc, long dataToTransfer)
    {
        if (!ConnectivityUtils.hasMobileConnectivity(mAppContext)) { return true; }

        // Check Data transfert only if on Mobile Network
        if (ConnectivityUtils.isMobileNetworkAvailable(mAppContext) && !ConnectivityUtils.isWifiAvailable(mAppContext))
        {
            long maxDataTransfer = GeneralPreferences.getDataSyncTransferAlert(mAppContext, acc);
            if (maxDataTransfer < dataToTransfer)
            {
                // Warning : Data transfer !
                // Request user Info
                return false;
            }
        }
        return true;
    }

    private boolean respectEnoughStorageSpace(float availableBytes, long deltaStorage)
    {
        // POLICY 1 : Enough Storage
        if ((availableBytes - deltaStorage) <= 0)
        {
            // ERROR : Not Enough Storage space
            // Sync canceled !
            return false;
        }
        return true;
    }

    private boolean respectLimitStorageSpace(Account acc, float availableBytes, long deltaStorage, float totalBytes)
    {
        float percentTotalSpace = GeneralPreferences.getDataSyncPercentFreeSpace(mAppContext, acc);

        // Check Delta data storage after sync
        if ((availableBytes - deltaStorage) < (percentTotalSpace * totalBytes))
        {
            // Warning : Storage space low
            // Request user Info
            return false;
        }
        return true;
    }

    // ////////////////////////////////////////////////////
    // PENDING OPERATIONS
    // ////////////////////////////////////////////////////
    private OperationsRequestGroup pendingOperationGroup;

    public void saveOperationGroup(OperationsRequestGroup group)
    {
        pendingOperationGroup = group;
    }

    public void runPendingOperationGroup()
    {
        if (pendingOperationGroup != null)
        {
            enqueue(pendingOperationGroup);
        }
        SyncScanInfo.getLastSyncScanData(mAppContext, SessionUtils.getAccount(mAppContext)).forceScan(mAppContext,
                SessionUtils.getAccount(mAppContext));
        pendingOperationGroup = null;
    }

}
