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
package org.alfresco.mobile.android.sync.prepare;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.ContentFileImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.sync.FavoritesSyncSchema;
import org.alfresco.mobile.android.sync.operations.FavoriteSync;
import org.alfresco.mobile.android.sync.operations.FavoriteSyncDelete;
import org.alfresco.mobile.android.sync.operations.FavoriteSyncDownload;
import org.alfresco.mobile.android.sync.operations.FavoriteSyncStatus;
import org.alfresco.mobile.android.sync.operations.FavoriteSyncUpdate;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;

public class PrepareFavoriteSyncHelper extends PrepareFavoriteHelper
{
    private static final String TAG = PrepareFavoriteSyncHelper.class.getName();

    protected List<FavoriteSync> group;

    public PrepareFavoriteSyncHelper(Context context, AlfrescoAccount acc, AlfrescoSession session, int mode,
            long syncScanningTimeStamp, SyncResult result, Node node)
    {
        super(context, acc, session, mode, syncScanningTimeStamp, result, node);

        // Create the group
        group = new ArrayList<FavoriteSync>();
    }

    public PrepareFavoriteSyncHelper(Context context, AlfrescoAccount acc, AlfrescoSession session, int mode,
            long syncScanningTimeStamp, SyncResult result, String nodeIdentifier)
    {
        super(context, acc, session, mode, syncScanningTimeStamp, result, nodeIdentifier);

        // Create the group
        group = new ArrayList<FavoriteSync>();
    }

    @Override
    public List<FavoriteSync> prepare()
    {
        scan();
        return group;
    }

    protected void prepareCreation(Uri syncUri, Document doc)
    {
        // Execute Creation
        try
        {
            FavoriteSync.saveStatus(context, syncUri, FavoriteSyncStatus.STATUS_PENDING);
            group.add(new FavoriteSyncDownload(context, acc, session, syncResult, doc, syncUri));
        }
        catch (Exception e)
        {
            // DO Nothing
        }
    }

    protected void rename(Document doc, File localFile, Uri localUri)
    {
        // Doc has been renamed or metadata changes
        // ==> update properties only
        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_STATUS, Operation.STATUS_RUNNING);
        context.getContentResolver().update(localUri, cValues, null, null);

        // Rename file
        File newLocalFile = new File(localFile.getParentFile(), doc.getName());
        localFile.renameTo(newLocalFile);

        // Update Sync Info
        cValues.clear();
        cValues.put(OperationSchema.COLUMN_LOCAL_URI, newLocalFile.getPath());
        cValues.put(OperationSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
        cValues.put(OperationSchema.COLUMN_TITLE, doc.getName());
        context.getContentResolver().update(localUri, cValues, null, null);
    }

    protected void prepareUpdate(Document doc, Cursor cursorId, File localFile, Uri syncUri)
    {
        // Execute Update
        try
        {
            FavoriteSync.saveStatus(context, syncUri, FavoriteSyncStatus.STATUS_PENDING);
            group.add(new FavoriteSyncUpdate(context, acc, session, syncResult, cursorId
                    .getString(FavoritesSyncSchema.COLUMN_PARENT_ID_ID), doc, new ContentFileImpl(localFile), syncUri,
                    false));
        }
        catch (Exception e)
        {
            // DO Nothing
        }
    }

    protected void prepareDelete(String id, Cursor cursorId)
    {
        // If Synced document
        // Flag the item inside the referential
        if (FavoriteSyncStatus.STATUS_MODIFIED != cursorId.getInt(FavoritesSyncSchema.COLUMN_STATUS_ID))
        {
            ContentValues cValues = new ContentValues();
            cValues.put(FavoritesSyncSchema.COLUMN_STATUS, FavoriteSyncStatus.STATUS_HIDDEN);
            cValues.put(FavoritesSyncSchema.COLUMN_TOTAL_SIZE_BYTES, 0);
            cValues.put(FavoritesSyncSchema.COLUMN_DOC_SIZE_BYTES, 0);
            if (!ContentModel.TYPE_FOLDER.equals(cursorId.getString(FavoritesSyncSchema.COLUMN_STATUS_ID)))
            {
                cValues.put(FavoritesSyncSchema.COLUMN_DOC_SIZE_BYTES,
                        -cursorId.getLong(FavoritesSyncSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID));
            }
            context.getContentResolver().update(
                    FavoritesSyncManager.getUri(cursorId.getLong(FavoritesSyncSchema.COLUMN_ID_ID)), cValues, null,
                    null);
        }

        // Execute Delete
        try
        {
            FavoriteSync.saveStatus(context,
                    FavoritesSyncManager.getUri(cursorId.getLong(FavoritesSyncSchema.COLUMN_ID_ID)),
                    FavoriteSyncStatus.STATUS_PENDING);
            group.add(new FavoriteSyncDelete(context, acc, session, syncResult, id, FavoritesSyncManager
                    .getUri(cursorId.getLong(FavoritesSyncSchema.COLUMN_ID_ID))));
        }
        catch (Exception e)
        {
            // DO Nothing
        }
    }
}
