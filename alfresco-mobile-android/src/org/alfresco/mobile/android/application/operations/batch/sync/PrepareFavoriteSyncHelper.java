/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.impl.ContentFileImpl;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.operations.sync.node.delete.SyncDeleteRequest;
import org.alfresco.mobile.android.application.operations.sync.node.download.SyncDownloadRequest;
import org.alfresco.mobile.android.application.operations.sync.node.update.SyncUpdateRequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class PrepareFavoriteSyncHelper extends PrepareFavoriteHelper
{
    private static final String TAG = PrepareFavoriteSyncHelper.class.getName();

    protected OperationsRequestGroup group;

    public PrepareFavoriteSyncHelper(Context context, SyncPrepareThread syncScanThread, long syncScanningTimeStamp)
    {
        super(context, syncScanThread, syncScanningTimeStamp);

        // Create the group
        group = new OperationsRequestGroup(context, acc);
    }

    @Override
    public OperationsRequestGroup prepare()
    {
        scan();
        return group;
    }
    
    protected void prepareCreation(Uri localUri, Document doc)
    {
        // Execution
        SyncDownloadRequest dl = new SyncDownloadRequest(doc);
        dl.setNotificationUri(localUri);
        dl.setNotificationTitle(doc.getName());
        group.enqueue(dl.setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
    }

    protected void rename(Document doc, File localFile, Uri localUri)
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
        cValues.put(BatchOperationSchema.COLUMN_LOCAL_URI, newLocalFile.getPath());
        cValues.put(BatchOperationSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
        cValues.put(BatchOperationSchema.COLUMN_TITLE, doc.getName());
        context.getContentResolver().update(localUri, cValues, null, null);
    }
    
    protected void prepareUpdate(Document doc, Cursor cursorId, File localFile, Uri localUri)
    {
        SyncUpdateRequest updateRequest = new SyncUpdateRequest(cursorId.getString(SynchroSchema.COLUMN_PARENT_ID_ID),
                doc, new ContentFileImpl(localFile));
        updateRequest.setNotificationTitle(doc.getName());
        updateRequest.setNotificationUri(localUri);
        group.enqueue(updateRequest.setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
    }
    
    protected void prepareDelete(String id, Cursor cursorId)
    {
        // If Synced document
        // Flag the item inside the referential
        if (SyncOperation.STATUS_MODIFIED != cursorId.getInt(SynchroSchema.COLUMN_STATUS_ID))
        {
            ContentValues cValues = new ContentValues();
            cValues.put(SynchroSchema.COLUMN_STATUS, SyncOperation.STATUS_HIDDEN);
            cValues.put(SynchroSchema.COLUMN_TOTAL_SIZE_BYTES, 0);
            cValues.put(SynchroSchema.COLUMN_DOC_SIZE_BYTES, 0);
            if (!ContentModel.TYPE_FOLDER.equals(cursorId.getString(SynchroSchema.COLUMN_STATUS_ID)))
            {
                cValues.put(SynchroSchema.COLUMN_DOC_SIZE_BYTES,
                        -cursorId.getLong(SynchroSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID));
            }
            context.getContentResolver().update(SynchroManager.getUri(cursorId.getLong(SynchroSchema.COLUMN_ID_ID)),
                    cValues, null, null);
        }

        // Execution
        SyncDeleteRequest deleteRequest = new SyncDeleteRequest(id, cursorId.getString(SynchroSchema.COLUMN_TITLE_ID),
                SynchroManager.getUri(cursorId.getLong(SynchroSchema.COLUMN_ID_ID)));
        deleteRequest.setNotificationTitle(cursorId.getString(SynchroSchema.COLUMN_TITLE_ID));
        group.enqueue(deleteRequest.setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
    }
    

}
