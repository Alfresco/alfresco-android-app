/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.model.impl.ContentFileImpl;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroProvider;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.operations.sync.node.delete.SyncDeleteRequest;
import org.alfresco.mobile.android.application.operations.sync.node.download.SyncDownloadRequest;
import org.alfresco.mobile.android.application.operations.sync.node.update.SyncUpdateRequest;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SyncFavoriteThread extends AbstractBatchOperationThread<Void>
{
    public static final int MODE_DOCUMENTS = 1;

    public static final int MODE_FOLDERS = 2;

    public static final int MODE_BOTH = 4;

    private static final String TAG = SyncFavoriteThread.class.getName();

    private int mode = MODE_DOCUMENTS;

    private ListingContext listingContext;

    private OperationsRequestGroup group;

    private boolean canSync;

    private long syncScanningTimeStamp;

    private PagingResult<Document> remoteFavorites;

    private Cursor localFavoritesCursor;

    private ArrayList<String> remoteFavoritesId;

    private boolean isForceSync;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncFavoriteThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof SyncFavoriteRequest)
        {
            isForceSync = ((SyncFavoriteRequest) request).isForceSync();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Void> doInBackground()
    {
        LoaderResult<Void> result = new LoaderResult<Void>();
        try
        {
            result = super.doInBackground();

            canSync = (isForceSync) ? true : SynchroManager.getInstance(context).canSync(acc);

            group = new OperationsRequestGroup(context, acc);

            switch (mode)
            {
                case MODE_DOCUMENTS:

                    // Timestamp the scan process
                    syncScanningTimeStamp = new GregorianCalendar(TimeZone.getTimeZone("GMT")).getTimeInMillis();

                    // Retrieve list of Favorites
                    remoteFavorites = session.getServiceRegistry().getDocumentFolderService()
                            .getFavoriteDocuments(listingContext);

                    // Retrieve list of local Favorites
                    localFavoritesCursor = context.getContentResolver().query(SynchroProvider.CONTENT_URI,
                            SynchroSchema.COLUMN_ALL, null, null, null);

                    // We have favorites
                    // Update the referential contentProvider
                    if (!isFirstSync())
                    {
                        // Check updated favorites
                        scanUpdateItem();

                        // Check deleted favorites
                        scanDeleteItem();
                    }

                    if (localFavoritesCursor != null)
                    {
                        localFavoritesCursor.close();
                    }
                    break;
                default:
                    break;
            }

            if (!group.getRequests().isEmpty())
            {
                SynchroManager.getInstance(context).enqueue(group);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(LoaderResult<Void> result)
    {
        super.onPostExecute(result);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_SYNC_SCAN_COMPLETED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SCANNING
    // ///////////////////////////////////////////////////////////////////////////
    private boolean isFirstSync()
    {
        if (localFavoritesCursor.getCount() == 0)
        {
            // USE CASE : FIRST
            // If 0 ==> Bulk Insert
            for (Document doc : remoteFavorites.getList())
            {
                addSyncDownloadRequest(doc, syncScanningTimeStamp);
            }
            return true;
        }
        return false;
    }

    private void scanUpdateItem()
    {
        // Favorites are present.
        // Check if new, update or delete action
        remoteFavoritesId = new ArrayList<String>(remoteFavorites.getTotalItems());
        Cursor cursorId = null;

        long localServerTimeStamp = -1;
        long remoteServerTimeStamp = -1;

        File localFile = null;
        boolean hasLocalModification = false;
        Uri localUri = null;
        Uri localFileUri = null;
        String localContentUri = "";
        String docContentUri = "";
        Permissions permissions = null;

        // Browse the results
        for (Document doc : remoteFavorites.getList())
        {
            remoteFavoritesId.add(NodeRefUtils.getCleanIdentifier(doc.getIdentifier()));

            // Try to retrieve local info
            cursorId = context.getContentResolver().query(
                    SynchroProvider.CONTENT_URI,
                    SynchroSchema.COLUMN_ALL,
                    SynchroSchema.COLUMN_NODE_ID + " LIKE '" + NodeRefUtils.getCleanIdentifier(doc.getIdentifier())
                            + "%'", null, null);

            if (cursorId.moveToFirst())
            {
                // Info available locally
                localUri = SynchroManager.getUri(cursorId.getLong(SynchroSchema.COLUMN_ID_ID));
                hasLocalModification = hasLocalModification(cursorId);
                permissions = session.getServiceRegistry().getDocumentFolderService().getPermissions(doc);

                // Check ContentStream modification
                localContentUri = cursorId.getString(SynchroSchema.COLUMN_CONTENT_URI_ID);
                docContentUri = doc.getProperty(PropertyIds.CONTENT_STREAM_ID).getValue();
                if (localContentUri != null && docContentUri != null && !localContentUri.equals(docContentUri))
                {
                    // Content has changed in server side
                    // Check local modification
                    if (hasLocalModification)
                    {
                        if (permissions.canEdit())
                        {
                            // Local Change present
                            // User has the right to upload
                            requestUserInteraction(localUri, SyncOperation.REASON_LOCAL_MODIFICATION);
                        }
                        else
                        {
                            // Local Change present
                            // User has read right
                            requestUserInteraction(localUri, SyncOperation.REASON_NO_PERMISSION);
                        }
                    }
                    else
                    {
                        // No local change
                        // We download the new content
                        addSyncDownloadRequest(localUri, doc, syncScanningTimeStamp);
                    }
                    continue;
                }

                // Check if Local file exist
                // Content might been deleted with a file explorer
                localFileUri = Uri.parse(cursorId.getString(SynchroSchema.COLUMN_LOCAL_URI_ID));
                localFile = new File(localFileUri.getPath());
                if (!localFile.exists())
                {
                    // Content is not present, we download content
                    addSyncDownloadRequest(localUri, doc, syncScanningTimeStamp);
                    continue;
                }

                // Check modification Date and local modification
                localServerTimeStamp = cursorId.getLong(SynchroSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP_ID);
                remoteServerTimeStamp = doc.getModifiedAt().getTimeInMillis();
                hasLocalModification = hasLocalModification(cursorId, localFile);

                // Check if it's a modification in server side
                if (remoteServerTimeStamp > localServerTimeStamp)
                {
                    // Server side modification
                    // Check if local modification
                    if (hasLocalModification)
                    {
                        // Request User Decision
                        if (permissions.canEdit())
                        {
                            // Local Change present
                            // User has the right to upload
                            requestUserInteraction(localUri, SyncOperation.REASON_LOCAL_MODIFICATION);
                        }
                        else
                        {
                            // Local Change present
                            // User has read right
                            requestUserInteraction(localUri, SyncOperation.REASON_NO_PERMISSION);
                        }
                    }
                    else
                    {
                        // Document metadata has changed
                        // Content is still the same
                        // We rename the document.
                        rename(doc, cursorId, localFile, localUri);
                    }
                    continue;
                }

                // Check if it's a local modification
                if (hasLocalModification && remoteServerTimeStamp == localServerTimeStamp)
                {
                    // Local change available
                    // Check permission
                    if (permissions.canEdit())
                    {
                        // Let's Update!
                        addSyncUpdateRequest(doc, cursorId, localFile, localUri);
                    }
                    else
                    {
                        // Local Change present
                        // User has read right
                        requestUserInteraction(localUri, SyncOperation.REASON_NO_PERMISSION);
                    }
                }
            }
            else
            {
                // Info unavailable
                // USE CASE : NEW
                addSyncDownloadRequest(doc, syncScanningTimeStamp);
            }
            cursorId.close();
        }
    }

    private void scanDeleteItem()
    {
        // USE CASE : DELETE
        // Compare referential and list of favorite Ids
        if (!localFavoritesCursor.isBeforeFirst())
        {
            localFavoritesCursor.moveToFirst();
            localFavoritesCursor.moveToPrevious();
        }

        List<String> favoriteLocalNode = new ArrayList<String>(remoteFavoritesId.size());
        while (localFavoritesCursor.moveToNext())
        {
            // Clean Id because of version number after update
            // (1.4, 1.5...)
            if (remoteFavoritesId.contains(NodeRefUtils.getCleanIdentifier(localFavoritesCursor
                    .getString(SynchroSchema.COLUMN_NODE_ID_ID))))
            {
                remoteFavoritesId.remove(NodeRefUtils.getCleanIdentifier(localFavoritesCursor
                        .getString(SynchroSchema.COLUMN_NODE_ID_ID)));
            }
            else
            {
                favoriteLocalNode.add(localFavoritesCursor.getString(SynchroSchema.COLUMN_NODE_ID_ID));
            }
        }

        Cursor localFavoriteCursor = null;
        // If nodeIds present, favorite are no longer in repo
        for (String id : favoriteLocalNode)
        {
            localFavoriteCursor = context.getContentResolver().query(SynchroProvider.CONTENT_URI,
                    SynchroSchema.COLUMN_ALL,
                    SynchroSchema.COLUMN_NODE_ID + " LIKE '" + NodeRefUtils.getCleanIdentifier(id) + "%'", null, null);

            if (localFavoriteCursor.getCount() > 1)
            {
                while (localFavoriteCursor.moveToNext())
                {
                    // Check status
                    switch (localFavoriteCursor.getInt(SynchroSchema.COLUMN_STATUS_ID))
                    {
                        case SyncOperation.STATUS_HIDDEN:
                            addSyncdeleteRequest(id, localFavoriteCursor);
                            break;
                        default:
                            break;
                    }
                }
            }
            else if (localFavoriteCursor.getCount() == 1 && localFavoriteCursor.moveToFirst())
            {
                addSyncdeleteRequest(id, localFavoriteCursor);
            }
        }

        if (localFavoriteCursor != null)
        {
            localFavoriteCursor.close();
        }
    }

    private boolean hasLocalModification(Cursor cursor, File localFile)
    {
        // Check modification Date and local modification
        long localSyncTimeStamp = cursor.getLong(SynchroSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP_ID);
        return (localSyncTimeStamp != -1 && localFile != null && localFile.lastModified() > localSyncTimeStamp);
    }

    private boolean hasLocalModification(Cursor cursor)
    {
        // Check modification Date and local modification
        Uri localFileUri = Uri.parse(cursor.getString(SynchroSchema.COLUMN_LOCAL_URI_ID));
        File localFile = new File(localFileUri.getPath());
        long localSyncTimeStamp = cursor.getLong(SynchroSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP_ID);
        return (localSyncTimeStamp != -1 && localFile != null && localFile.lastModified() > localSyncTimeStamp);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATE SYNC TASK
    // ///////////////////////////////////////////////////////////////////////////
    private void addSyncDownloadRequest(Document doc, long timeStamp)
    {
        // Flag the item inside the referential
        Uri uri = context.getContentResolver().insert(SynchroProvider.CONTENT_URI,
                SynchroManager.createContentValues(context, acc, SyncDownloadRequest.TYPE_ID, doc, timeStamp));

        // Execution
        addSyncDownloadRequest(uri, doc, timeStamp);
    }

    private void addSyncDownloadRequest(Uri localUri, Document doc, long timeStamp)
    {
        // Execution
        if (!canSync) { return; }
        SyncDownloadRequest dl = new SyncDownloadRequest(doc);
        dl.setNotificationUri(localUri);
        dl.setNotificationTitle(doc.getName());
        group.enqueue(dl.setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
    }

    private void addSyncUpdateRequest(Document doc, Cursor cursorId, File localFile, Uri localUri)
    {
        if (!canSync) { return; }
        SyncUpdateRequest updateRequest = new SyncUpdateRequest(cursorId.getString(SynchroSchema.COLUMN_PARENT_ID_ID),
                doc, new ContentFileImpl(localFile));
        updateRequest.setNotificationTitle(doc.getName());
        updateRequest.setNotificationUri(localUri);
        group.enqueue(updateRequest.setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
    }

    private void rename(Document doc, Cursor cursorId, File localFile, Uri localUri)
    {
        // Doc has been renamed or metadata changes
        // ==> update properties only
        ContentValues cValues = new ContentValues();
        cValues.put(BatchOperationSchema.COLUMN_STATUS, Operation.STATUS_RUNNING);
        context.getContentResolver().update(localUri, cValues, null, null);

        // Rename file
        File newLocalFile = new File(localFile.getParentFile(), doc.getName());
        localFile.renameTo(newLocalFile);

        // Update Sync Info
        cValues.clear();
        cValues.put(BatchOperationSchema.COLUMN_TITLE, doc.getName());
        cValues.put(BatchOperationSchema.COLUMN_LOCAL_URI, Uri.fromFile(newLocalFile).toString());
        cValues.put(BatchOperationSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
        context.getContentResolver().update(localUri, cValues, null, null);
    }

    private void addSyncdeleteRequest(String id, Cursor cursorId)
    {
        // Flag the item inside the referential
        ContentValues cValues = new ContentValues();
        cValues.put(SynchroSchema.COLUMN_STATUS, SyncOperation.STATUS_HIDDEN);
        context.getContentResolver().update(SynchroManager.getUri(cursorId.getLong(SynchroSchema.COLUMN_ID_ID)),
                cValues, null, null);

        // Execution
        if (!canSync) { return; }
        SyncDeleteRequest deleteRequest = new SyncDeleteRequest(id,
                cursorId.getString(SynchroSchema.COLUMN_TITLE_ID), SynchroManager.getUri(cursorId
                        .getLong(SynchroSchema.COLUMN_ID_ID)));
        deleteRequest.setNotificationTitle(cursorId.getString(SynchroSchema.COLUMN_TITLE_ID));
        group.enqueue(deleteRequest.setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Set a listing context for a specific paging loader.
     * 
     * @param listingContext
     */
    public void setListingContext(ListingContext listingContext)
    {
        this.listingContext = listingContext;
    }

    protected void requestUserInteraction(Uri localUri, int reasonId)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(BatchOperationSchema.COLUMN_STATUS, SyncOperation.STATUS_REQUEST_USER);
        cValues.put(BatchOperationSchema.COLUMN_REASON, reasonId);
        context.getContentResolver().update(localUri, cValues, null, null);
    }
}
