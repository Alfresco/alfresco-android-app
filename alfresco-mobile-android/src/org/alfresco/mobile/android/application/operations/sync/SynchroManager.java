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

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationManager;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsGroupRecord;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.sync.CleanSyncFavoriteRequest;
import org.alfresco.mobile.android.application.operations.batch.sync.SyncFavoriteRequest;
import org.alfresco.mobile.android.application.operations.sync.impl.AbstractSyncOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.sync.node.delete.SyncDeleteRequest;
import org.alfresco.mobile.android.application.operations.sync.node.download.SyncDownloadRequest;
import org.alfresco.mobile.android.application.operations.sync.node.update.SyncUpdateRequest;
import org.alfresco.mobile.android.application.operations.sync.utils.NodeSyncPlaceHolder;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
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
        return ((AbstractSyncOperationRequestImpl) operationRequest).getNotificationUri();
    }

    // ////////////////////////////////////////////////////
    // EVENTS
    // ////////////////////////////////////////////////////
    public static final String EXTRA_SYNCHRO_ID = "synchroId";

    public static final String EXTRA_SYNCHRO_RESULT = "synchroResult";

    public static final String ACTION_START_SYNC = "startSyncService";

    private static final String LAST_SYNC_ACTIVATED_AT = "LastSyncDateTime";

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

                    if (acc != null && node != null && canSync(acc))
                    {
                        sync(acc);
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
                    favoriteCursor.close();
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
                            SynchroManager.getInstance(mAppContext).sync(acc);
                        }
                    }
                    favoriteCursor.close();
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
    // PUBLIC UTILS METHODS
    // ////////////////////////////////////////////////////
    public static ContentValues createContentValues(Context context, Account account, int requestType, Document doc,
            long time)
    {
        return createContentValues(context, account, requestType, "", doc, time);
    }

    public static ContentValues createContentValues(Context context, Account account, int requestType, String parent,
            Document doc, long time)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(SynchroSchema.COLUMN_ACCOUNT_ID, account.getId());
        cValues.put(SynchroSchema.COLUMN_TENANT_ID, account.getRepositoryId());
        cValues.put(SynchroSchema.COLUMN_STATUS, Operation.STATUS_PENDING);
        cValues.put(SynchroSchema.COLUMN_REASON, -1);
        cValues.put(SynchroSchema.COLUMN_REQUEST_TYPE, requestType);
        cValues.put(SynchroSchema.COLUMN_TITLE, doc.getName());
        cValues.put(SynchroSchema.COLUMN_NOTIFICATION_VISIBILITY, OperationRequest.VISIBILITY_HIDDEN);
        cValues.put(SynchroSchema.COLUMN_NODE_ID, doc.getIdentifier());
        cValues.put(SynchroSchema.COLUMN_PARENT_ID, parent);
        cValues.put(SynchroSchema.COLUMN_MIMETYPE, doc.getContentStreamMimeType());
        cValues.put(SynchroSchema.COLUMN_PROPERTIES, "");
        cValues.put(SynchroSchema.COLUMN_TOTAL_SIZE_BYTES, doc.getContentStreamLength());
        cValues.put(SynchroSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, -1);
        cValues.put(SynchroSchema.COLUMN_LOCAL_URI, "");
        if (doc.getProperty(PropertyIds.CONTENT_STREAM_ID) != null)
        {
            cValues.put(SynchroSchema.COLUMN_CONTENT_URI, (String) doc.getProperty(PropertyIds.CONTENT_STREAM_ID)
                    .getValue());
        }
        cValues.put(SynchroSchema.COLUMN_ANALYZE_TIMESTAMP, time);
        cValues.put(SynchroSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, doc.getModifiedAt().getTimeInMillis());
        cValues.put(SynchroSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, time);
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
        group.enqueue(new SyncFavoriteRequest().setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
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
        boolean b = (favoriteCursor.getCount() == 1)
                && GeneralPreferences.hasActivateSync(mAppContext, account);
        favoriteCursor.close();
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
        if (node instanceof NodeSyncPlaceHolder) { return StorageManager.getSynchroFile(mAppContext,
                account, node.getName(), node.getIdentifier()); }
        return StorageManager.getSynchroFile(mAppContext, account, (Document) node);
    }

    public Uri getUri(Account account, String nodeIdentifier)
    {
        if (account == null) { return null; }
        
        Uri b = null;
        Cursor favoriteCursor = mAppContext.getContentResolver().query(
                SynchroProvider.CONTENT_URI,
                SynchroSchema.COLUMN_ALL,
                SynchroProvider.getAccountFilter(account) + " AND " + SynchroSchema.COLUMN_NODE_ID + " LIKE '"
                        + NodeRefUtils.getCleanIdentifier(nodeIdentifier) + "%'", null, null);
        if (favoriteCursor.getCount() == 1 && favoriteCursor.moveToFirst())
        {
            b = Uri.parse(SynchroProvider.CONTENT_URI + "/" + favoriteCursor.getLong(SynchroSchema.COLUMN_ID_ID));
        }
        favoriteCursor.close();
        return b;
    }

    public boolean canSync(Account account)
    {
        return GeneralPreferences.hasActivateSync(mAppContext, account)
                && ((GeneralPreferences.hasWifiOnlySync(mAppContext, account) && ConnectivityUtils
                        .isWifiAvailable(mAppContext)) || !GeneralPreferences.hasWifiOnlySync(mAppContext, account));
    }

    /**
     * Flag the activity time.
     * 
     * @param context
     */
    public static void updateLastActivity(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPref.edit();
        editor.putLong(LAST_SYNC_ACTIVATED_AT, new Date().getTime());
        editor.commit();
    }

    /**
     * Start a sync if the last activity time is greater than 1 hour.
     */
    public void cronSync(Account account)
    {
        long now = new Date().getTime();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        long lastTime = sharedPref.getLong(LAST_SYNC_ACTIVATED_AT, now);
        if ((lastTime + 3600000) < now && canSync(account))
        {
            sync(account);
        }
    }
}
