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
package org.alfresco.mobile.android.sync.operations;

import java.io.File;
import java.io.IOException;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.ContentFileImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.io.IOUtils;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.sync.FavoritesSyncProvider;
import org.alfresco.mobile.android.sync.FavoritesSyncSchema;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class FavoriteSyncDelete extends FavoriteSync
{
    private static final String TAG = FavoriteSyncDelete.class.getName();

    public static final int TYPE_ID = 240;

    private final String nodeIdentifier;

    private long favoriteId;

    private String nodeName;

    protected Cursor cursor;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public FavoriteSyncDelete(Context context, AlfrescoAccount acc, AlfrescoSession session, SyncResult syncResult,
            String nodeIdenfitier, Uri localUri)
    {
        super(context, acc, session, syncResult, localUri);
        this.nodeIdentifier = nodeIdenfitier;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void execute()
    {
        super.execute();
        try
        {
            Log.d("Alfresco", "Delete for doc[" + nodeIdentifier + "]");

            cursor = context.getContentResolver().query(
                    FavoritesSyncProvider.CONTENT_URI,
                    FavoritesSyncSchema.COLUMN_ALL,
                    FavoritesSyncProvider.getAccountFilter(acc) + " AND " + FavoritesSyncSchema.COLUMN_NODE_ID
                            + " LIKE '" + nodeIdentifier + "%'", null, null);

            // Node has been deleted in server side.
            if (cursor != null && cursor.moveToFirst())
            {
                // Check modification date
                long localTimeStamp = cursor.getLong(FavoritesSyncSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP_ID);
                String nodeIdentifier = cursor.getString(FavoritesSyncSchema.COLUMN_NODE_ID_ID);
                nodeName = cursor.getString(FavoritesSyncSchema.COLUMN_TITLE_ID);
                favoriteId = cursor.getLong(FavoritesSyncSchema.COLUMN_ID_ID);

                File dlFile = FavoritesSyncManager.getInstance(context).getSynchroFile(acc,
                        cursor.getString(FavoritesSyncSchema.COLUMN_TITLE_ID),
                        cursor.getString(FavoritesSyncSchema.COLUMN_NODE_ID_ID));
                if (dlFile.lastModified() > localTimeStamp && hasLocalModification())
                {
                    try
                    {
                        // Check if it's a delete or unfavorite
                        // Remove cache to be sure we check directly from
                        // server.
                        ((AbstractAlfrescoSessionImpl) session).getCmisSession().removeObjectFromCache(nodeIdentifier);
                        Document docServer = (Document) session.getServiceRegistry().getDocumentFolderService()
                                .getNodeByIdentifier(nodeIdentifier);

                        if (docServer != null)
                        {
                            // Unfavorite operation
                            // Request to update
                            if (FavoriteSyncStatus.STATUS_TO_UPDATE == cursor
                                    .getInt(FavoritesSyncSchema.COLUMN_REASON_ID))
                            {
                                Node n = session.getServiceRegistry().getDocumentFolderService()
                                        .getNodeByIdentifier(nodeIdentifier);
                                session.getServiceRegistry().getDocumentFolderService()
                                        .updateContent((Document) n, new ContentFileImpl(dlFile));
                                delete(dlFile);
                            }
                            else
                            {
                                Log.d(TAG, "Unfavorited");
                                requestUserInteraction(syncUri, FavoriteSyncStatus.REASON_NODE_UNFAVORITED);
                            }
                        }
                        else
                        {
                            // Delete operation
                            move(cursor, syncUri);
                        }
                    }
                    catch (Exception e)
                    {
                        move(cursor, syncUri);
                    }
                }
                else
                {
                    // No local modification
                    // Delete IT!
                    delete(dlFile);
                }
                syncResult.stats.numDeletes++;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            syncResult.stats.numIoExceptions++;
            saveStatus(FavoriteSyncStatus.STATUS_FAILED);
        }
        finally
        {
            CursorUtils.closeCursor(cursor);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private void delete(File dlFile) throws IOException
    {
        IOUtils.deleteContents(dlFile.getParentFile());
        dlFile.getParentFile().delete();
        context.getContentResolver().delete(
                FavoritesSyncManager.getUri(cursor.getLong(cursor.getColumnIndex(FavoritesSyncSchema.COLUMN_ID))),
                null, null);
    }

    private void move(Cursor c, Uri notificationUri)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_STATUS, Operation.STATUS_RUNNING);
        context.getContentResolver().update(FavoritesSyncManager.getUri(favoriteId), cValues, null, null);

        // Current File
        Uri localFileUri = Uri.parse(c.getString(FavoritesSyncSchema.COLUMN_LOCAL_URI_ID));
        File localFile = new File(localFileUri.getPath());
        File parentFolder = localFile.getParentFile();

        // New File
        File downloadFolder = AlfrescoStorageManager.getInstance(context).getDownloadFolder(
                SessionUtils.getAccount(context));
        File newLocalFile = new File(downloadFolder, c.getString(FavoritesSyncSchema.COLUMN_TITLE_ID));
        newLocalFile = IOUtils.createFile(newLocalFile);

        // Move to "Download" and delete parent folder
        cValues.clear();
        if (localFile.renameTo(newLocalFile) && parentFolder.delete())
        {
            requestUserInteraction(notificationUri, FavoriteSyncStatus.REASON_NODE_DELETED);
        }
        else
        {
            cValues.put(OperationSchema.COLUMN_STATUS, FavoriteSyncStatus.STATUS_FAILED);
            context.getContentResolver().update(FavoritesSyncManager.getUri(favoriteId), cValues, null, null);
        }

        // Data Protection if necessary
        AlfrescoStorageManager.getInstance(context).manageFile(newLocalFile);

        c.close();
    }

    private boolean hasLocalModification()
    {
        if (DataProtectionManager.getInstance(context).isEncryptionEnable())
        {
            if (FavoriteSyncStatus.STATUS_MODIFIED == cursor.getInt(FavoritesSyncSchema.COLUMN_STATUS_ID)) { return true; }
            return false;
        }
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getDocumentName()
    {
        return nodeName;
    }
}
