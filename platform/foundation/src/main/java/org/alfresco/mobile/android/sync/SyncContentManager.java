/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.sync;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.clean.CleanSyncFavoriteRequest;
import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.provider.MapUtil;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;
import org.alfresco.mobile.android.sync.operations.SyncContentUpdate;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class SyncContentManager extends Manager
{
    // Default intervall every 12 hours
    public static final long DEFAULT_INTERVAL = 12 * 60 * 60;

    // minimum intervall every 1 hour
    public static final long MIN_INTERVAL = 60 * 60;

    private static final String TAG = SyncContentManager.class.getName();

    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    // ////////////////////////////////////////////////////
    // EVENTS
    // ////////////////////////////////////////////////////
    private static final String LAST_SYNC_ACTIVATED_AT = "LastSyncDateTime";

    private static final String LAST_START_SYNC_PREPARE = "LastSyncPrepareDateTime";

    private static final String HAS_MIGRATE_SYNC = "HasMigrateSync";

    // ////////////////////////////////////////////////////
    // SYNC MODE
    // ////////////////////////////////////////////////////
    public static final String ARGUMENT_MODE = "syncMode";

    public static final int MODE_NODE = 0;

    public static final int MODE_DOCUMENTS = 1;

    public static final int MODE_FOLDERS = 2;

    public static final int MODE_BOTH = 4;

    public static final String ARGUMENT_IGNORE_WARNING = "ignoreWarning";

    public static final String ARGUMENT_ANALYTIC = "analytic";

    public static final String ARGUMENT_NODE = "node";

    public static final String ARGUMENT_NODE_ID = "nodeId";

    private static final String SYNCHRO_DIRECTORY = "Synchro";

    // ////////////////////////////////////////////////////
    // SETTINGS
    // ////////////////////////////////////////////////////
    private static final String SYNCHRO_PREFIX = "SynchroEnable-";

    private static final String SYNCHRO_EVEYTHING_PREFIX = "SynchroEverythingEnable-";

    private static final String SYNCHRO_WIFI_PREFIX = "SynchroWifiEnable-";

    private static final String SYNCHRO_DISPLAY_PREFIX = "SynchroDisplayEnable-";

    private static final String SYNCHRO_DATA_ALERT_PREFIX = "SynchroDataAlert-";

    private static final long SYNCHRO_DATA_ALERT_LENGTH = 20971520; // 20Mb

    private static final String SYNCHRO_FREE_SPACE_ALERT_PREFIX = "SynchroDataAlert-";

    // In Percent of total space
    private static final float SYNCHRO_FREE_SPACE_ALERT_LENGTH = 0.1f;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    protected SyncContentManager(Context applicationContext)
    {
        super(applicationContext);
    }

    public static SyncContentManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            synchronized (LOCK)
            {
                if (mInstance == null)
                {
                    mInstance = Manager.getInstance(context, SyncContentManager.class.getSimpleName());
                }

                return (SyncContentManager) mInstance;
            }
        }
    }

    // ////////////////////////////////////////////////////
    // PUBLIC UTILS METHODS
    // ////////////////////////////////////////////////////
    public Uri createTmpSyncFile(AlfrescoAccount account, String name, String parentId, String mimetype)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(SyncContentSchema.COLUMN_ACCOUNT_ID, account.getId());
        cValues.put(SyncContentSchema.COLUMN_TENANT_ID, account.getRepositoryId());
        cValues.put(SyncContentSchema.COLUMN_STATUS, Operation.STATUS_PENDING);
        cValues.put(SyncContentSchema.COLUMN_REASON, -1);
        cValues.put(SyncContentSchema.COLUMN_REQUEST_TYPE, SyncContentUpdate.TYPE_ID);
        cValues.put(SyncContentSchema.COLUMN_TITLE, name);
        cValues.put(SyncContentSchema.COLUMN_NOTIFICATION_VISIBILITY, OperationRequest.VISIBILITY_HIDDEN);
        cValues.put(SyncContentSchema.COLUMN_NODE_ID, "");
        cValues.put(SyncContentSchema.COLUMN_PARENT_ID, parentId);
        cValues.put(SyncContentSchema.COLUMN_MIMETYPE, mimetype);
        cValues.put(SyncContentSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, 0);
        cValues.put(SyncContentSchema.COLUMN_LOCAL_URI, "");
        cValues.put(SyncContentSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, -1);
        cValues.put(SyncContentSchema.COLUMN_IS_SYNC_ROOT, 0);

        return appContext.getContentResolver().insert(SyncContentProvider.CONTENT_URI, cValues);
    }

    public void update(Uri uri, ContentValues cValues)
    {
        appContext.getContentResolver().update(uri, cValues, null, null);
    }

    public static ContentValues createContentValues(Context context, AlfrescoAccount account, int requestType,
            Node node, long time)
    {
        return createContentValues(context, account, requestType, "", node, time, 0);
    }

    public static ContentValues createRemoteRootContentValues(Context context, AlfrescoAccount account, int requestType,
            Node node, long time)
    {
        ContentValues cValues = createContentValues(context, account, requestType, "", node, time, 0);
        cValues.put(SyncContentSchema.COLUMN_IS_SYNC_ROOT, SyncContentProvider.FLAG_SYNC_SET);
        return cValues;
    }

    public static ContentValues createContentValues(Context context, AlfrescoAccount account, int requestType,
            String parentId, Node node, long time, long folderSize)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(SyncContentSchema.COLUMN_ACCOUNT_ID, account.getId());
        cValues.put(SyncContentSchema.COLUMN_TENANT_ID, account.getRepositoryId());
        cValues.put(SyncContentSchema.COLUMN_STATUS, Operation.STATUS_PENDING);
        cValues.put(SyncContentSchema.COLUMN_REASON, -1);
        cValues.put(SyncContentSchema.COLUMN_REQUEST_TYPE, requestType);
        cValues.put(SyncContentSchema.COLUMN_TITLE, node.getName());
        cValues.put(SyncContentSchema.COLUMN_NOTIFICATION_VISIBILITY, OperationRequest.VISIBILITY_HIDDEN);
        cValues.put(SyncContentSchema.COLUMN_NODE_ID, node.getIdentifier());
        cValues.put(SyncContentSchema.COLUMN_PARENT_ID, parentId);
        if (node instanceof Document)
        {
            cValues.put(SyncContentSchema.COLUMN_MIMETYPE, ((Document) node).getContentStreamMimeType());
            cValues.put(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES, ((Document) node).getContentStreamLength());
            cValues.put(SyncContentSchema.COLUMN_DOC_SIZE_BYTES, ((Document) node).getContentStreamLength());
            if (node.getProperty(PropertyIds.CONTENT_STREAM_ID) != null)
            {
                cValues.put(SyncContentSchema.COLUMN_CONTENT_URI,
                        (String) node.getProperty(PropertyIds.CONTENT_STREAM_ID).getValue());
            }
        }
        else
        {
            cValues.put(SyncContentSchema.COLUMN_MIMETYPE, ContentModel.TYPE_FOLDER);
            cValues.put(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES, folderSize);
            cValues.put(SyncContentSchema.COLUMN_DOC_SIZE_BYTES, 0);
            if (folderSize == 0)
            {
                cValues.put(SyncContentSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
            }
        }
        cValues.put(SyncContentSchema.COLUMN_PROPERTIES, serializeProperties(node));
        cValues.put(SyncContentSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, 0);
        cValues.put(SyncContentSchema.COLUMN_LOCAL_URI, "");
        cValues.put(SyncContentSchema.COLUMN_ANALYZE_TIMESTAMP, time);
        cValues.put(SyncContentSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, node.getModifiedAt().getTimeInMillis());
        cValues.put(SyncContentSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, time);
        cValues.put(SyncContentSchema.COLUMN_IS_SYNC_ROOT, 0);
        return cValues;
    }

    public static ContentValues createSyncRootContentValues(Context context, AlfrescoAccount account, int requestType,
            String parent, Node node, long time, long folderSize)
    {
        ContentValues cValues = createContentValues(context, account, requestType, parent, node, time, folderSize);
        cValues.put(SyncContentSchema.COLUMN_IS_SYNC_ROOT, SyncContentProvider.FLAG_SYNC_SET);
        return cValues;
    }

    public static String serializeProperties(Node node)
    {
        HashMap<String, Serializable> persistentProperties = new HashMap<String, Serializable>();
        Map<String, Property> props = node.getProperties();
        for (Entry<String, Property> entry : props.entrySet())
        {
            if (entry.getValue().getValue() instanceof GregorianCalendar)
            {
                persistentProperties.put(entry.getKey(),
                        ((GregorianCalendar) entry.getValue().getValue()).getTimeInMillis());
            }
            else
            {
                persistentProperties.put(entry.getKey(), (Serializable) entry.getValue().getValue());
            }
        }
        if (!persistentProperties.isEmpty())
        {
            return MapUtil.mapToString(persistentProperties);
        }
        else
        {
            return "";
        }

    }

    public static Uri getUri(long id)
    {
        return Uri.parse(SyncContentProvider.CONTENT_URI + "/" + id);
    }

    public void sync(String analyticInfo, AlfrescoAccount account)
    {
        if (account == null) { return; }
        Bundle settingsBundle = new Bundle();
        settingsBundle.putInt(ARGUMENT_MODE, SyncContentManager.MODE_BOTH);
        settingsBundle.putString(ARGUMENT_ANALYTIC, analyticInfo);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putBoolean(ARGUMENT_IGNORE_WARNING, false);
        ContentResolver.requestSync(AlfrescoAccountManager.getInstance(appContext).getAndroidAccount(account.getId()),
                SyncContentProvider.AUTHORITY, settingsBundle);
    }

    public void sync(AlfrescoAccount account)
    {
        sync(AnalyticsManager.LABEL_SYNC_SYSTEM, account);
    }

    public void sync(AlfrescoAccount account, String nodeIdentifier)
    {
        if (account == null) { return; }
        Bundle settingsBundle = new Bundle();
        settingsBundle.putString(ARGUMENT_ANALYTIC, AnalyticsManager.LABEL_SYNC_ACTION);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putInt(ARGUMENT_MODE, SyncContentManager.MODE_NODE);
        settingsBundle.putSerializable(ARGUMENT_NODE_ID, nodeIdentifier);
        settingsBundle.putBoolean(ARGUMENT_IGNORE_WARNING, false);
        ContentResolver.requestSync(AlfrescoAccountManager.getInstance(appContext).getAndroidAccount(account.getId()),
                SyncContentProvider.AUTHORITY, settingsBundle);
    }

    public void runPendingOperationGroup(AlfrescoAccount account)
    {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putBoolean(ARGUMENT_IGNORE_WARNING, true);
        settingsBundle.putInt(SyncContentManager.ARGUMENT_MODE, SyncContentManager.MODE_BOTH);
        ContentResolver.requestSync(AlfrescoAccountManager.getInstance(appContext).getAndroidAccount(account.getId()),
                SyncContentProvider.AUTHORITY, settingsBundle);
    }

    /**
     * @param account
     * @param interval must be in seconds
     */
    public void syncPeriodically(AlfrescoAccount account, Long interval)
    {
        if (account == null) { return; }
        long period = DEFAULT_INTERVAL;
        if (interval != null)
        {
            period = interval;
        }
        Bundle settingsBundle = new Bundle();
        if (period != DEFAULT_INTERVAL)
        {
            // only track sync done via server config and not the default one
            settingsBundle.putString(ARGUMENT_ANALYTIC, AnalyticsManager.LABEL_SYNC_SCHEDULER_CHANGED);
        }
        settingsBundle.putInt(ARGUMENT_MODE, SyncContentManager.MODE_BOTH);
        settingsBundle.putBoolean(ARGUMENT_IGNORE_WARNING, false);
        ContentResolver.addPeriodicSync(
                AlfrescoAccountManager.getInstance(appContext).getAndroidAccount(account.getId()),
                SyncContentProvider.AUTHORITY, settingsBundle, period);
        Log.d("[SCHEDULER]", " syncPeriodically: " + interval);
    }

    public void unsync(AlfrescoAccount account)
    {
        if (account == null) { return; }
        Operator.with(appContext).load(new CleanSyncFavoriteRequest.Builder(account, true));
    }

    public boolean isSynced(AlfrescoAccount account, String nodeIdentifier)
    {
        if (account == null) { return false; }

        Cursor favoriteCursor = appContext.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                SyncContentSchema.COLUMN_ALL,
                SyncContentProvider.getAccountFilter(account) + " AND " + SyncContentSchema.COLUMN_NODE_ID + " LIKE '"
                        + NodeRefUtils.getCleanIdentifier(nodeIdentifier) + "%' AND " + SyncContentSchema.COLUMN_STATUS
                        + " NOT IN ( " + SyncContentStatus.STATUS_HIDDEN + ")",
                null, null);
        boolean b = (favoriteCursor.getCount() == 1) && hasActivateSync(account);
        CursorUtils.closeCursor(favoriteCursor);
        return b;
    }

    public boolean isRootSynced(AlfrescoAccount account, String nodeIdentifier)
    {
        if (account == null) { return false; }

        Cursor favoriteCursor = appContext.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                SyncContentSchema.COLUMN_ALL,
                SyncContentProvider.getAccountFilter(account) + " AND " + SyncContentSchema.COLUMN_NODE_ID + " LIKE '"
                        + NodeRefUtils.getCleanIdentifier(nodeIdentifier) + "%' AND " + SyncContentSchema.COLUMN_STATUS
                        + " NOT IN ( " + SyncContentStatus.STATUS_HIDDEN + ")" + " AND "
                        + SyncContentSchema.COLUMN_IS_SYNC_ROOT + " == 1",
                null, null);
        boolean b = (favoriteCursor.getCount() == 1) && hasActivateSync(account);
        CursorUtils.closeCursor(favoriteCursor);
        return b;
    }

    public boolean isSynced(AlfrescoAccount account, Node node)
    {
        return !(account == null || node == null) && !node.isFolder() && isSynced(account, node.getIdentifier());
    }

    public boolean isRootSynced(AlfrescoAccount account, Node node)
    {
        return !(account == null || node == null) && isRootSynced(account, node.getIdentifier());
    }

    public File getSyncFile(AlfrescoAccount account, Node node)
    {
        if (account == null || node == null) { return null; }
        if (node.isFolder()) { return null; }
        if (node instanceof NodeSyncPlaceHolder) { return getSynchroFile(account, node.getName(),
                node.getIdentifier()); }
        return getSynchroFile(account, (Document) node);
    }

    public File getSyncFile(AlfrescoAccount account, String name, String identifier)
    {
        if (account == null || TextUtils.isEmpty(name) || TextUtils.isEmpty(identifier)) { return null; }
        if (appContext != null && account != null)
        {
            File synchroFolder = getSynchroFolder(account);
            File uuidFolder = new File(synchroFolder, identifier);
            uuidFolder.mkdirs();
            if (!uuidFolder.exists()) { return null; }
            return new File(uuidFolder, name);
        }
        return null;
    }

    public static Cursor getCursorForId(Context context, AlfrescoAccount acc, String identifier)
    {
        if (acc == null) { return null; }

        return context.getContentResolver().query(SyncContentProvider.CONTENT_URI, SyncContentSchema.COLUMN_ALL,
                SyncContentProvider.getAccountFilter(acc) + " AND " + SyncContentSchema.COLUMN_NODE_ID + " LIKE '"
                        + NodeRefUtils.getCleanIdentifier(identifier) + "%'",
                null, null);
    }

    public static boolean hasPendingSync(Context context, AlfrescoAccount acc)
    {
        if (acc == null) { return false; }

        StringBuilder builder = new StringBuilder(SyncContentProvider.getAccountFilter(acc));
        builder.append(" AND ");
        builder.append(SyncContentSchema.COLUMN_STATUS + " IN (");
        builder.append(SyncContentStatus.STATUS_PENDING);
        builder.append(",");
        builder.append(SyncContentStatus.STATUS_HIDDEN);
        builder.append(",");
        builder.append(SyncContentStatus.STATUS_MODIFIED);
        builder.append(",");
        builder.append(SyncContentStatus.STATUS_TO_UPDATE);
        builder.append(")");

        Cursor pendingcursor = context.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                SyncContentSchema.COLUMN_ALL, builder.toString(), null, null);
        int count = pendingcursor.getCount();
        CursorUtils.closeCursor(pendingcursor);

        return count > 0;
    }

    public Uri getUri(AlfrescoAccount account, String nodeIdentifier)
    {
        if (account == null) { return null; }

        Uri b = null;
        Cursor favoriteCursor = getCursorForId(appContext, account, nodeIdentifier);
        if (favoriteCursor.getCount() == 1 && favoriteCursor.moveToFirst())
        {
            b = Uri.parse(
                    SyncContentProvider.CONTENT_URI + "/" + favoriteCursor.getLong(SyncContentSchema.COLUMN_ID_ID));
        }
        CursorUtils.closeCursor(favoriteCursor);
        return b;
    }

    public boolean canSync(AlfrescoAccount account)
    {
        return hasActivateSync(account) && hasConnectivityToSync(account);
    }

    public boolean hasConnectivityToSync(AlfrescoAccount account)
    {
        return ((hasWifiOnlySync(account.getId()) && ConnectivityUtils.isWifiAvailable(appContext))
                || (!hasWifiOnlySync(account.getId()) && ConnectivityUtils.hasInternetAvailable(appContext)));
    }

    public boolean hasConnectivityToSync(Long accountId)
    {
        return ((hasWifiOnlySync(accountId) && ConnectivityUtils.isWifiAvailable(appContext))
                || (!hasWifiOnlySync(accountId) && ConnectivityUtils.hasInternetAvailable(appContext)));
    }

    /**
     * Flag the activity time.
     */
    public void saveStartSyncPrepareTimestamp()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        Editor editor = sharedPref.edit();
        AlfrescoAccount account = SessionUtils.getAccount(appContext);
        if (account != null)
        {
            editor.putLong(LAST_START_SYNC_PREPARE + account.getId(), new Date().getTime());
            editor.apply();
        }
    }

    public void saveSyncPrepareTimestamp()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        Editor editor = sharedPref.edit();
        AlfrescoAccount account = SessionUtils.getAccount(appContext);
        if (account != null)
        {
            editor.putLong(LAST_SYNC_ACTIVATED_AT + account.getId(), new Date().getTime());
            editor.apply();
        }
    }

    public long getSyncPrepareTimestamp(AlfrescoAccount account)
    {
        if (account == null) { return -1; }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        return sharedPref.getLong(LAST_SYNC_ACTIVATED_AT + account.getId(), new Date().getTime());
    }

    public long getStartSyncPrepareTimestamp(AlfrescoAccount account)
    {
        if (account == null) { return -1; }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        return sharedPref.getLong(LAST_START_SYNC_PREPARE + account.getId(), new Date().getTime());
    }

    /**
     * Start a sync if the last activity time is greater than 1 hour.
     */
    public void cronSync(AlfrescoAccount account)
    {
        if (account == null) { return; }
        long now = new Date().getTime();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        long lastTime = sharedPref.getLong(LAST_SYNC_ACTIVATED_AT + account.getId(), now);
        if ((lastTime + 3600000) < now && canSync(account))
        {
            sync(AnalyticsManager.LABEL_SYNC_CRON, account);
        }
    }

    // ////////////////////////////////////////////////////
    // I/O MANAGEMENT
    // ////////////////////////////////////////////////////
    public File getSynchroFolder(AlfrescoAccount acc)
    {
        return AlfrescoStorageManager.getInstance(appContext).getPrivateFolder(SYNCHRO_DIRECTORY, acc);
    }

    public File getSynchroFolder(String username, String accountUrl)
    {
        return AlfrescoStorageManager.getInstance(appContext).getPrivateFolder(SYNCHRO_DIRECTORY, username, accountUrl);
    }

    public static boolean isFolder(Cursor cursor)
    {
        return cursor != null
                && ContentModel.TYPE_FOLDER.equals(cursor.getString(SyncContentSchema.COLUMN_MIMETYPE_ID));
    }

    public boolean isSyncFile(File file)
    {
        if (SessionUtils.getAccount(appContext) == null) { return true; }
        File tempFolder = getSynchroFolder(SessionUtils.getAccount(appContext));

        return (tempFolder != null && file.getParentFile().getParent().compareTo(tempFolder.getPath()) == 0);
    }

    public boolean isSynchroFile(File file)
    {
        File tempFolder = appContext.getExternalFilesDir(null);
        String path = file.getPath();
        String[] pathS = path.split("/");
        return (tempFolder != null && file.getPath().startsWith(tempFolder.getPath())
                && pathS[pathS.length - 3].contains(SYNCHRO_DIRECTORY));
    }

    public File getSynchroFile(AlfrescoAccount acc, Document doc)
    {
        if (appContext != null && doc != null) { return getSynchroFile(acc, doc.getName(), doc.getIdentifier()); }
        return null;
    }

    public File getSynchroFile(AlfrescoAccount acc, String documentName, String nodeIdentifier)
    {
        if (appContext != null && acc != null)
        {
            File synchroFolder = getSynchroFolder(acc);
            File uuidFolder;
            if (NodeRefUtils.isIdentifier(nodeIdentifier) || NodeRefUtils.isNodeRef(nodeIdentifier))
            {
                uuidFolder = new File(synchroFolder, NodeRefUtils.getNodeIdentifier(nodeIdentifier));
            }
            else
            {
                uuidFolder = new File(synchroFolder, nodeIdentifier);
            }
            uuidFolder.mkdirs();
            if (!uuidFolder.exists()) { return null; }
            return new File(uuidFolder, documentName);
        }
        return null;
    }

    // ////////////////////////////////////////////////////
    // STORAGE MANAGEMENT
    // ////////////////////////////////////////////////////
    private static final String QUERY_SUM = "SELECT SUM(" + SyncContentSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR + ") FROM "
            + SyncContentSchema.TABLENAME + " WHERE " + SyncContentSchema.COLUMN_PARENT_ID + " = '%s';";

    private static final String QUERY_SUM_IN_PENDING = "SELECT SUM(" + SyncContentSchema.COLUMN_DOC_SIZE_BYTES
            + ") FROM " + SyncContentSchema.TABLENAME + " WHERE " + SyncContentSchema.COLUMN_ACCOUNT_ID + " == %s AND "
            + SyncContentSchema.COLUMN_STATUS + " IN ( " + SyncContentStatus.STATUS_PENDING + ","
            + SyncContentStatus.STATUS_HIDDEN + ");";

    private static final String QUERY_SUM_TOTAL_IN_PENDING = "SELECT SUM(" + SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES
            + ") FROM " + SyncContentSchema.TABLENAME + " WHERE " + SyncContentSchema.COLUMN_ACCOUNT_ID + " == %s AND "
            + SyncContentSchema.COLUMN_STATUS + " = " + SyncContentStatus.STATUS_PENDING + " AND "
            + SyncContentSchema.COLUMN_MIMETYPE + " NOT IN ('" + ContentModel.TYPE_FOLDER + "');";

    private static final String QUERY_TOTAL_STORED = "SELECT SUM(" + SyncContentSchema.COLUMN_DOC_SIZE_BYTES + ") FROM "
            + SyncContentSchema.TABLENAME + " WHERE " + SyncContentSchema.COLUMN_ACCOUNT_ID + " == %s AND "
            + SyncContentSchema.COLUMN_STATUS + " IN (" + SyncContentStatus.STATUS_PENDING + ", "
            + SyncContentStatus.STATUS_SUCCESSFUL + ");";

    private static final String QUERY_SUM_TOTAL = "SELECT SUM(" + SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES + ") FROM "
            + SyncContentSchema.TABLENAME + " WHERE " + SyncContentSchema.COLUMN_PARENT_ID + " = '%s';";

    public synchronized void updateParentFolder(AlfrescoAccount account, String identifier)
    {
        Long currentValue = null;
        Long totalSize = null;
        String parentFolderId = null;
        Cursor syncCursor = null, parentCursor = null, cursorTotal = null, cursor = null;

        try
        {
            // Retrieve Uri & ParentFolder
            Uri uri = null;
            syncCursor = appContext.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                    SyncContentSchema.COLUMN_ALL,
                    SyncContentProvider.getAccountFilter(account) + " AND " + SyncContentSchema.COLUMN_NODE_ID + " == '"
                            + NodeRefUtils.getCleanIdentifier(identifier) + "'",
                    null, null);
            if (syncCursor.getCount() == 1 && syncCursor.moveToFirst())
            {
                parentFolderId = syncCursor.getString(SyncContentSchema.COLUMN_PARENT_ID_ID);
                uri = Uri.parse(
                        SyncContentProvider.CONTENT_URI + "/" + syncCursor.getLong(SyncContentSchema.COLUMN_ID_ID));

                parentCursor = appContext.getContentResolver()
                        .query(SyncContentProvider.CONTENT_URI, SyncContentSchema.COLUMN_ALL,
                                SyncContentProvider.getAccountFilter(account) + " AND "
                                        + SyncContentSchema.COLUMN_NODE_ID + " == '"
                                        + NodeRefUtils.getCleanIdentifier(parentFolderId) + "'",
                                null, null);
            }
            else
            {
                return;
            }

            if (parentCursor != null && parentCursor.getCount() == 1 && parentCursor.moveToFirst()
                    && SyncContentStatus.STATUS_HIDDEN == parentCursor.getInt(SyncContentSchema.COLUMN_STATUS_ID))
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
            cValues.put(SyncContentSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, currentValue);
            cValues.put(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES, totalSize);
            cValues.put(OperationSchema.COLUMN_STATUS, Operation.STATUS_RUNNING);
            if (totalSize.longValue() == currentValue.longValue())
            {
                cValues.put(OperationSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
            }
            appContext.getContentResolver().update(uri, cValues, null, null);
        }
        catch (Exception e)
        {

        }
        finally
        {
            CursorUtils.closeCursor(parentCursor);
            CursorUtils.closeCursor(cursorTotal);
            CursorUtils.closeCursor(syncCursor);
            CursorUtils.closeCursor(cursor);
        }

        // Recursive on grand parent
        if (parentFolderId != null)
        {
            updateParentFolder(account, parentFolderId);
        }
    }

    protected Long retrieveSize(String query)
    {
        return 0L;
    }

    public Long getAmountDataToTransfert(AlfrescoAccount acc)
    {
        return retrieveSize(String.format(QUERY_SUM_TOTAL_IN_PENDING, acc.getId()));
    }

    public Long getAmountDataStored(AlfrescoAccount acc)
    {
        return retrieveSize(String.format(QUERY_TOTAL_STORED, acc.getId()));
    }

    public Long getPreviousAmountDataStored(AlfrescoAccount acc)
    {
        return retrieveSize(String.format(QUERY_SUM_IN_PENDING, acc.getId()));
    }

    // ////////////////////////////////////////////////////
    // SYNC POLICIES
    // ////////////////////////////////////////////////////
    public SyncScanInfo getScanInfo(AlfrescoAccount acc)
    {
        // IF sync is disabled scanInfo is success by default.
        if (!hasActivateSync(acc)) { return new SyncScanInfo(0, 0, SyncScanInfo.RESULT_SUCCESS); }

        long dataFinalStored = getAmountDataStored(acc);
        long deltaStorage = getPreviousAmountDataStored(acc);
        float totalBytes = AlfrescoStorageManager.getInstance(appContext).getTotalBytes();
        float availableBytes = AlfrescoStorageManager.getInstance(appContext).getAvailableBytes();
        long dataToTransfer = getAmountDataToTransfert(acc);

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

    private boolean respectMobileTransferPolicy(AlfrescoAccount acc, long dataToTransfer)
    {
        if (!ConnectivityUtils.hasMobileConnectivity(appContext)) { return true; }

        // Check Data transfert only if on Mobile Network
        if (ConnectivityUtils.isMobileNetworkAvailable(appContext) && !ConnectivityUtils.isWifiAvailable(appContext))
        {
            long maxDataTransfer = getDataSyncTransferAlert(acc);
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
        // In case we remove data or no data
        if (deltaStorage <= 0) { return true; }

        // POLICY 1 : Enough Storage
        if ((availableBytes - deltaStorage) <= 0)
        {
            // ERROR : Not Enough Storage space
            // Sync canceled !
            return false;
        }
        return true;
    }

    private boolean respectLimitStorageSpace(AlfrescoAccount acc, float availableBytes, long deltaStorage,
            float totalBytes)
    {
        // In case we remove data or no data
        if (deltaStorage <= 0) { return true; }

        float percentTotalSpace = getDataSyncPercentFreeSpace(acc);

        // Check Delta data storage after sync
        if ((availableBytes - deltaStorage) < (percentTotalSpace * totalBytes))
        {
            // Warning : Storage space low
            // Request user Info
            return false;
        }
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SETTINGS SYNC
    // ///////////////////////////////////////////////////////////////////////////
    public void setWifiOnlySync(AlfrescoAccount account, boolean isWifiOnly)
    {
        if (account != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
            sharedPref.edit().putBoolean(SYNCHRO_WIFI_PREFIX + account.getId(), isWifiOnly).apply();
        }
    }

    public boolean hasWifiOnlySync(AlfrescoAccount account)
    {
        return account != null && hasWifiOnlySync(account.getId());
    }

    public boolean hasWifiOnlySync(Long accountId)
    {
        if (accountId != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
            return sharedPref.getBoolean(SYNCHRO_WIFI_PREFIX + accountId, false);
        }
        return false;
    }

    public boolean hasActivateSync(AlfrescoAccount account)
    {
        if (account != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
            if (sharedPref.contains(SYNCHRO_PREFIX
                    + account.getId())) { return sharedPref.getBoolean(SYNCHRO_PREFIX + account.getId(), true); }
        }
        return false;
    }

    public void setActivateSync(long accountId, boolean isActive)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        sharedPref.edit().putBoolean(SYNCHRO_PREFIX + accountId, isActive).apply();
        ContentResolver.setSyncAutomatically(
                AlfrescoAccountManager.getInstance(appContext).getAndroidAccount(accountId),
                SyncContentProvider.AUTHORITY, isActive);
    }

    public void setActivateSync(AlfrescoAccount account, boolean isActive)
    {
        if (account != null)
        {
            setActivateSync(account.getId(), isActive);
        }
    }

    public void setDisplayActivateSync(AlfrescoAccount account, boolean isActive)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        if (account != null)
        {
            sharedPref.edit().putBoolean(SYNCHRO_DISPLAY_PREFIX + account.getId(), isActive).apply();
        }
    }

    public boolean hasDisplayedActivateSync(AlfrescoAccount account)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        if (account != null) { return sharedPref.getBoolean(SYNCHRO_DISPLAY_PREFIX + account.getId(), false); }
        return false;
    }

    public boolean canSyncEverything(AlfrescoAccount account)
    {
        if (account != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
            return sharedPref.getBoolean(SYNCHRO_EVEYTHING_PREFIX + account.getId(), false);
        }
        return false;
    }

    public void setSyncEverything(boolean isActive)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        if (SessionUtils.getAccount(appContext) != null)
        {
            final AlfrescoAccount account = SessionUtils.getAccount(appContext);
            sharedPref.edit().putBoolean(SYNCHRO_EVEYTHING_PREFIX + account.getId(), isActive).apply();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SETTINGS SYNC FOLDER
    // ///////////////////////////////////////////////////////////////////////////
    public long getDataSyncTransferAlert(AlfrescoAccount account)
    {
        if (account != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
            return sharedPref.getLong(SYNCHRO_DATA_ALERT_PREFIX + account.getId(), SYNCHRO_DATA_ALERT_LENGTH);
        }
        return SYNCHRO_DATA_ALERT_LENGTH;
    }

    public void setDataSyncTransferAlert(long length)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        if (SessionUtils.getAccount(appContext) != null)
        {
            final AlfrescoAccount account = SessionUtils.getAccount(appContext);
            sharedPref.edit().putLong(SYNCHRO_DATA_ALERT_PREFIX + account.getId(), length).apply();
        }
    }

    public float getDataSyncPercentFreeSpace(AlfrescoAccount account)
    {
        if (account != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
            return sharedPref.getFloat(SYNCHRO_FREE_SPACE_ALERT_PREFIX + account.getId(),
                    SYNCHRO_FREE_SPACE_ALERT_LENGTH);
        }
        return SYNCHRO_FREE_SPACE_ALERT_LENGTH;
    }

    public void setDataSyncTransferAlert(float percent)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        if (SessionUtils.getAccount(appContext) != null)
        {
            final AlfrescoAccount account = SessionUtils.getAccount(appContext);
            sharedPref.edit().putFloat(SYNCHRO_FREE_SPACE_ALERT_PREFIX + account.getId(), percent).apply();
        }
    }

    public static boolean displaySyncInfo(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        boolean hasSync = false;
        for (AlfrescoAccount account : AlfrescoAccountManager.retrieveAccounts(context))
        {
            hasSync = hasSync || SyncContentManager.getInstance(context).hasActivateSync(account);
        }
        return (!prefs.contains(HAS_MIGRATE_SYNC) && hasSync);
    }

    public static void saveStateInfo(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        prefs.edit().putBoolean(HAS_MIGRATE_SYNC, true).apply();
    }

}
