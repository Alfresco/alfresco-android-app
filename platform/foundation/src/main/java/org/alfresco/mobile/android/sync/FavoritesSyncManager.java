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
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.provider.MapUtil;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.operations.FavoriteSyncStatus;
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
import android.util.Log;

public class FavoritesSyncManager extends Manager
{
    private static final String TAG = FavoritesSyncManager.class.getName();

    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    // ////////////////////////////////////////////////////
    // EVENTS
    // ////////////////////////////////////////////////////
    private static final String LAST_SYNC_ACTIVATED_AT = "LastSyncDateTime";

    private static final String LAST_START_SYNC_PREPARE = "LastSyncPrepareDateTime";

    // ////////////////////////////////////////////////////
    // SYNC MODE
    // ////////////////////////////////////////////////////
    public static final String ARGUMENT_MODE = "mode";

    public static final int MODE_NODE = 0;

    public static final int MODE_DOCUMENTS = 1;

    public static final int MODE_FOLDERS = 2;

    public static final int MODE_BOTH = 4;

    public static final String ARGUMENT_IGNORE_WARNING = "ignoreWarning";

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
    protected FavoritesSyncManager(Context applicationContext)
    {
        super(applicationContext);
    }

    public static FavoritesSyncManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            synchronized (LOCK)
            {
                if (mInstance == null)
                {
                    mInstance = Manager.getInstance(context, FavoritesSyncManager.class.getSimpleName());
                }

                return (FavoritesSyncManager) mInstance;
            }
        }
    }

    // ////////////////////////////////////////////////////
    // PUBLIC UTILS METHODS
    // ////////////////////////////////////////////////////
    public static ContentValues createContentValues(Context context, AlfrescoAccount account, int requestType,
            Node node, long time)
    {
        return createContentValues(context, account, requestType, "", node, time, 0);
    }

    public static ContentValues createFavoriteContentValues(Context context, AlfrescoAccount account, int requestType,
            Node node, long time)
    {
        ContentValues cValues = createContentValues(context, account, requestType, "", node, time, 0);
        cValues.put(FavoritesSyncSchema.COLUMN_IS_FAVORITE, FavoritesSyncProvider.FLAG_FAVORITE);
        return cValues;
    }

    public static ContentValues createContentValues(Context context, AlfrescoAccount account, int requestType,
            String parent, Node node, long time, long folderSize)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(FavoritesSyncSchema.COLUMN_ACCOUNT_ID, account.getId());
        cValues.put(FavoritesSyncSchema.COLUMN_TENANT_ID, account.getRepositoryId());
        cValues.put(FavoritesSyncSchema.COLUMN_STATUS, Operation.STATUS_PENDING);
        cValues.put(FavoritesSyncSchema.COLUMN_REASON, -1);
        cValues.put(FavoritesSyncSchema.COLUMN_REQUEST_TYPE, requestType);
        cValues.put(FavoritesSyncSchema.COLUMN_TITLE, node.getName());
        cValues.put(FavoritesSyncSchema.COLUMN_NOTIFICATION_VISIBILITY, OperationRequest.VISIBILITY_HIDDEN);
        cValues.put(FavoritesSyncSchema.COLUMN_NODE_ID, node.getIdentifier());
        cValues.put(FavoritesSyncSchema.COLUMN_PARENT_ID, parent);
        if (node instanceof Document)
        {
            cValues.put(FavoritesSyncSchema.COLUMN_MIMETYPE, ((Document) node).getContentStreamMimeType());
            cValues.put(FavoritesSyncSchema.COLUMN_TOTAL_SIZE_BYTES, ((Document) node).getContentStreamLength());
            cValues.put(FavoritesSyncSchema.COLUMN_DOC_SIZE_BYTES, ((Document) node).getContentStreamLength());
            if (node.getProperty(PropertyIds.CONTENT_STREAM_ID) != null)
            {
                cValues.put(FavoritesSyncSchema.COLUMN_CONTENT_URI,
                        (String) node.getProperty(PropertyIds.CONTENT_STREAM_ID).getValue());
            }
        }
        else
        {
            cValues.put(FavoritesSyncSchema.COLUMN_MIMETYPE, ContentModel.TYPE_FOLDER);
            cValues.put(FavoritesSyncSchema.COLUMN_TOTAL_SIZE_BYTES, folderSize);
            cValues.put(FavoritesSyncSchema.COLUMN_DOC_SIZE_BYTES, 0);
            if (folderSize == 0)
            {
                cValues.put(FavoritesSyncSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
            }
        }
        cValues.put(FavoritesSyncSchema.COLUMN_PROPERTIES, serializeProperties(node));
        cValues.put(FavoritesSyncSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, 0);
        cValues.put(FavoritesSyncSchema.COLUMN_LOCAL_URI, "");
        cValues.put(FavoritesSyncSchema.COLUMN_ANALYZE_TIMESTAMP, time);
        cValues.put(FavoritesSyncSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, node.getModifiedAt().getTimeInMillis());
        cValues.put(FavoritesSyncSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, time);
        cValues.put(FavoritesSyncSchema.COLUMN_IS_FAVORITE, 0);
        return cValues;
    }

    public static ContentValues createFavoriteContentValues(Context context, AlfrescoAccount account, int requestType,
            String parent, Node node, long time, long folderSize)
    {
        ContentValues cValues = createContentValues(context, account, requestType, parent, node, time, folderSize);
        cValues.put(FavoritesSyncSchema.COLUMN_IS_FAVORITE, FavoritesSyncProvider.FLAG_FAVORITE);
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
        return Uri.parse(FavoritesSyncProvider.CONTENT_URI + "/" + id);
    }

    public void sync(AlfrescoAccount account)
    {
        if (account == null) { return; }
        Bundle settingsBundle = new Bundle();
        settingsBundle.putInt(ARGUMENT_MODE, FavoritesSyncManager.MODE_BOTH);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(AlfrescoAccountManager.getInstance(appContext).getAndroidAccount(account.getId()),
                FavoritesSyncProvider.AUTHORITY, settingsBundle);
    }

    public void sync(AlfrescoAccount account, String nodeIdentifier)
    {
        if (account == null) { return; }
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putInt(ARGUMENT_MODE, FavoritesSyncManager.MODE_NODE);
        settingsBundle.putSerializable(ARGUMENT_NODE_ID, nodeIdentifier);
        ContentResolver.requestSync(AlfrescoAccountManager.getInstance(appContext).getAndroidAccount(account.getId()),
                FavoritesSyncProvider.AUTHORITY, settingsBundle);
    }

    public void runPendingOperationGroup(AlfrescoAccount account)
    {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putInt(ARGUMENT_MODE, FavoritesSyncManager.MODE_NODE);
        ContentResolver.requestSync(AlfrescoAccountManager.getInstance(appContext).getAndroidAccount(account.getId()),
                FavoritesSyncProvider.AUTHORITY, settingsBundle);
    }

    public void unsync(AlfrescoAccount account)
    {
        if (account == null) { return; }
        Operator.with(appContext).load(new CleanSyncFavoriteRequest.Builder(account, false));
    }

    public boolean isSynced(AlfrescoAccount account, String nodeIdentifier)
    {
        if (account == null) { return false; }

        Cursor favoriteCursor = appContext.getContentResolver().query(
                FavoritesSyncProvider.CONTENT_URI,
                FavoritesSyncSchema.COLUMN_ALL,
                FavoritesSyncProvider.getAccountFilter(account) + " AND " + FavoritesSyncSchema.COLUMN_NODE_ID
                        + " LIKE '" + NodeRefUtils.getCleanIdentifier(nodeIdentifier) + "%'", null, null);
        boolean b = (favoriteCursor.getCount() == 1) && hasActivateSync(account);
        CursorUtils.closeCursor(favoriteCursor);
        return b;
    }

    public boolean isSynced(AlfrescoAccount account, Node node)
    {
        return !(account == null || node == null) && !node.isFolder() && isSynced(account, node.getIdentifier());
    }

    public File getSyncFile(AlfrescoAccount account, Node node)
    {
        if (account == null || node == null) { return null; }
        if (node.isFolder()) { return null; }
        if (node instanceof NodeSyncPlaceHolder) { return getSynchroFile(account, node.getName(), node.getIdentifier()); }
        return getSynchroFile(account, (Document) node);
    }

    public static Cursor getCursorForId(Context context, AlfrescoAccount acc, String identifier)
    {
        if (acc == null) { return null; }

        return context.getContentResolver().query(
                FavoritesSyncProvider.CONTENT_URI,
                FavoritesSyncSchema.COLUMN_ALL,
                FavoritesSyncProvider.getAccountFilter(acc) + " AND " + FavoritesSyncSchema.COLUMN_NODE_ID + " LIKE '"
                        + NodeRefUtils.getCleanIdentifier(identifier) + "%'", null, null);
    }

    public Uri getUri(AlfrescoAccount account, String nodeIdentifier)
    {
        if (account == null) { return null; }

        Uri b = null;
        Cursor favoriteCursor = getCursorForId(appContext, account, nodeIdentifier);
        if (favoriteCursor.getCount() == 1 && favoriteCursor.moveToFirst())
        {
            b = Uri.parse(FavoritesSyncProvider.CONTENT_URI + "/"
                    + favoriteCursor.getLong(FavoritesSyncSchema.COLUMN_ID_ID));
        }
        CursorUtils.closeCursor(favoriteCursor);
        return b;
    }

    public boolean canSync(AlfrescoAccount account)
    {
        return hasActivateSync(account)
                && ((hasWifiOnlySync(account) && ConnectivityUtils.isWifiAvailable(appContext)) || !hasWifiOnlySync(account));
    }

    public boolean hasConnectivityToSync(AlfrescoAccount account)
    {
        return ((hasWifiOnlySync(account) && ConnectivityUtils.isWifiAvailable(appContext)) || !hasWifiOnlySync(account));
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
            editor.commit();
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
            editor.commit();
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
            sync(account);
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
                && ContentModel.TYPE_FOLDER.equals(cursor.getString(FavoritesSyncSchema.COLUMN_MIMETYPE_ID));
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
        return (tempFolder != null && file.getPath().startsWith(tempFolder.getPath()) && pathS[pathS.length - 3]
                .contains(SYNCHRO_DIRECTORY));
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
            // TODO Check if clean or not ?
            File uuidFolder = new File(synchroFolder, NodeRefUtils.getCleanIdentifier(NodeRefUtils
                    .getNodeIdentifier(nodeIdentifier)));
            uuidFolder.mkdirs();
            return new File(uuidFolder, documentName);
        }
        return null;
    }

    // ////////////////////////////////////////////////////
    // STORAGE MANAGEMENT
    // ////////////////////////////////////////////////////
    private static final String QUERY_SUM = "SELECT SUM(" + FavoritesSyncSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR
            + ") FROM " + FavoritesSyncSchema.TABLENAME + " WHERE " + FavoritesSyncSchema.COLUMN_PARENT_ID + " = '%s';";

    private static final String QUERY_SUM_IN_PENDING = "SELECT SUM(" + FavoritesSyncSchema.COLUMN_DOC_SIZE_BYTES
            + ") FROM " + FavoritesSyncSchema.TABLENAME + " WHERE " + FavoritesSyncSchema.COLUMN_ACCOUNT_ID
            + " == %s AND " + FavoritesSyncSchema.COLUMN_STATUS + " IN ( " + FavoriteSyncStatus.STATUS_PENDING + ","
            + FavoriteSyncStatus.STATUS_HIDDEN + ");";

    private static final String QUERY_SUM_TOTAL_IN_PENDING = "SELECT SUM("
            + FavoritesSyncSchema.COLUMN_TOTAL_SIZE_BYTES + ") FROM " + FavoritesSyncSchema.TABLENAME + " WHERE "
            + FavoritesSyncSchema.COLUMN_ACCOUNT_ID + " == %s AND " + FavoritesSyncSchema.COLUMN_STATUS + " = "
            + FavoriteSyncStatus.STATUS_PENDING + " AND " + FavoritesSyncSchema.COLUMN_MIMETYPE + " NOT IN ('"
            + ContentModel.TYPE_FOLDER + "');";

    private static final String QUERY_TOTAL_STORED = "SELECT SUM(" + FavoritesSyncSchema.COLUMN_DOC_SIZE_BYTES
            + ") FROM " + FavoritesSyncSchema.TABLENAME + " WHERE " + FavoritesSyncSchema.COLUMN_ACCOUNT_ID
            + " == %s AND " + FavoritesSyncSchema.COLUMN_STATUS + " IN (" + FavoriteSyncStatus.STATUS_PENDING + ", "
            + FavoriteSyncStatus.STATUS_SUCCESSFUL + ");";

    private static final String QUERY_SUM_TOTAL = "SELECT SUM(" + FavoritesSyncSchema.COLUMN_TOTAL_SIZE_BYTES
            + ") FROM " + FavoritesSyncSchema.TABLENAME + " WHERE " + FavoritesSyncSchema.COLUMN_PARENT_ID + " = '%s';";

    public synchronized void updateParentFolder(AlfrescoAccount account, String identifier)
    {
        Long currentValue = null;
        Long totalSize = null;
        String parentFolderId = null;
        Cursor favoriteCursor = null, parentCursor = null, cursorTotal = null, cursor = null;

        try
        {
            // Retrieve Uri & ParentFolder
            Uri uri = null;
            favoriteCursor = appContext.getContentResolver().query(
                    FavoritesSyncProvider.CONTENT_URI,
                    FavoritesSyncSchema.COLUMN_ALL,
                    FavoritesSyncProvider.getAccountFilter(account) + " AND " + FavoritesSyncSchema.COLUMN_NODE_ID
                            + " == '" + NodeRefUtils.getCleanIdentifier(identifier) + "'", null, null);
            if (favoriteCursor.getCount() == 1 && favoriteCursor.moveToFirst())
            {
                parentFolderId = favoriteCursor.getString(FavoritesSyncSchema.COLUMN_PARENT_ID_ID);
                uri = Uri.parse(FavoritesSyncProvider.CONTENT_URI + "/"
                        + favoriteCursor.getLong(FavoritesSyncSchema.COLUMN_ID_ID));

                parentCursor = appContext.getContentResolver().query(
                        FavoritesSyncProvider.CONTENT_URI,
                        FavoritesSyncSchema.COLUMN_ALL,
                        FavoritesSyncProvider.getAccountFilter(account) + " AND " + FavoritesSyncSchema.COLUMN_NODE_ID
                                + " == '" + NodeRefUtils.getCleanIdentifier(parentFolderId) + "'", null, null);
            }
            else
            {
                return;
            }

            if (parentCursor != null && parentCursor.getCount() == 1 && parentCursor.moveToFirst()
                    && FavoriteSyncStatus.STATUS_HIDDEN == parentCursor.getInt(FavoritesSyncSchema.COLUMN_STATUS_ID))
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
            cValues.put(FavoritesSyncSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, currentValue);
            cValues.put(FavoritesSyncSchema.COLUMN_TOTAL_SIZE_BYTES, totalSize);
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
            sharedPref.edit().putBoolean(SYNCHRO_WIFI_PREFIX + account.getId(), isWifiOnly).commit();
        }
    }

    public boolean hasWifiOnlySync(AlfrescoAccount account)
    {
        if (account != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
            return sharedPref.getBoolean(SYNCHRO_WIFI_PREFIX + account.getId(), false);
        }
        return false;
    }

    public boolean hasActivateSync(AlfrescoAccount account)
    {
        if (account != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
            return sharedPref.getBoolean(SYNCHRO_PREFIX + account.getId(), false);
        }
        return false;
    }

    public void setActivateSync(boolean isActive)
    {
        setActivateSync(SessionUtils.getAccount(appContext), isActive);
    }

    public void setActivateSync(AlfrescoAccount account, boolean isActive)
    {
        if (account != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
            sharedPref.edit().putBoolean(SYNCHRO_PREFIX + account.getId(), isActive).commit();
            ContentResolver.setSyncAutomatically(
                    AlfrescoAccountManager.getInstance(appContext).getAndroidAccount(account.getId()),
                    FavoritesSyncProvider.AUTHORITY, isActive);
        }
    }

    public void setDisplayActivateSync(boolean isActive)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        if (SessionUtils.getAccount(appContext) != null)
        {
            final AlfrescoAccount account = SessionUtils.getAccount(appContext);
            sharedPref.edit().putBoolean(SYNCHRO_DISPLAY_PREFIX + account.getId(), isActive).commit();
        }
    }

    public boolean hasDisplayedActivateSync()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        if (SessionUtils.getAccount(appContext) != null)
        {
            final AlfrescoAccount account = SessionUtils.getAccount(appContext);
            return sharedPref.getBoolean(SYNCHRO_DISPLAY_PREFIX + account.getId(), false);
        }
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
            sharedPref.edit().putBoolean(SYNCHRO_EVEYTHING_PREFIX + account.getId(), isActive).commit();
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
            sharedPref.edit().putLong(SYNCHRO_DATA_ALERT_PREFIX + account.getId(), length).commit();
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
            sharedPref.edit().putFloat(SYNCHRO_FREE_SPACE_ALERT_PREFIX + account.getId(), percent).commit();
        }
    }

}
