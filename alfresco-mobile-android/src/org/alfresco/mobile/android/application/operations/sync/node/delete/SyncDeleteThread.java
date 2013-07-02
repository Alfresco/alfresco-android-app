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
package org.alfresco.mobile.android.application.operations.sync.node.delete;

import java.io.File;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.operations.sync.impl.AbstractSyncOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.sync.node.SyncNodeOperationThread;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SyncDeleteThread extends SyncNodeOperationThread<Void>
{
    private static final String TAG = SyncDeleteThread.class.getName();

    private long favoriteId;

    private String nodeName;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncDeleteThread(Context ctx, AbstractSyncOperationRequestImpl request)
    {
        super(ctx, request);
        saveStatus = false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Void> doInBackground()
    {
        LoaderResult<Void> result = new LoaderResult<Void>();
        try
        {
            result = super.doInBackground();

            // Node has been deleted in server side.
            if (cursor != null && cursor.moveToFirst())
            {
                // Check modification date
                long localTimeStamp = cursor.getLong(SynchroSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP_ID);
                String nodeIdentifier = cursor.getString(SynchroSchema.COLUMN_NODE_ID_ID);
                nodeName = cursor.getString(SynchroSchema.COLUMN_TITLE_ID);
                favoriteId = cursor.getLong(SynchroSchema.COLUMN_ID_ID);

                File dlFile = StorageManager.getSynchroFile(context, acc,
                        cursor.getString(SynchroSchema.COLUMN_TITLE_ID),
                        cursor.getString(SynchroSchema.COLUMN_NODE_ID_ID));
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
                            Log.d(TAG, "Unfavorited");
                            requestUserInteraction(request.getNotificationUri(), SyncOperation.REASON_NODE_UNFAVORITED);
                        }
                        else
                        {
                            // Delete operation
                            move(cursor);
                        }
                    }
                    catch (Exception e)
                    {
                        move(cursor);
                    }
                }
                else
                {
                    // No local modification
                    // Delete them
                    IOUtils.deleteContents(dlFile.getParentFile());
                    dlFile.getParentFile().delete();
                    context.getContentResolver().delete(
                            SynchroManager.getUri(cursor.getLong(cursor.getColumnIndex(SynchroSchema.COLUMN_ID))),
                            null, null);
                }
            }

        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            if (result == null)
            {
                result = new LoaderResult<Void>();
            }
            result.setException(e);
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private void move(Cursor c)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(BatchOperationSchema.COLUMN_STATUS, Operation.STATUS_RUNNING);
        context.getContentResolver().update(SynchroManager.getUri(favoriteId), cValues, null, null);

        // Current File
        Uri localFileUri = Uri.parse(c.getString(SynchroSchema.COLUMN_LOCAL_URI_ID));
        File localFile = new File(localFileUri.getPath());
        File parentFolder = localFile.getParentFile();

        // New File
        File downloadFolder = StorageManager.getDownloadFolder(context, SessionUtils.getAccount(context));
        File newLocalFile = new File(downloadFolder, c.getString(SynchroSchema.COLUMN_TITLE_ID));
        newLocalFile = IOUtils.createFile(newLocalFile);

        // Move to "Download" and delete parent folder
        cValues.clear();
        if (localFile.renameTo(newLocalFile) && parentFolder.delete())
        {
            requestUserInteraction(request.getNotificationUri(), SyncOperation.REASON_NODE_DELETED);
        }
        else
        {
            cValues.put(BatchOperationSchema.COLUMN_STATUS, SyncOperation.STATUS_FAILED);
            context.getContentResolver().update(SynchroManager.getUri(favoriteId), cValues, null, null);
        }

        c.close();
    }

    private boolean hasLocalModification()
    {
        if (DataProtectionManager.getInstance(context).isEncryptionEnable())
        {
            if (SyncOperation.STATUS_MODIFIED == cursor.getInt(SynchroSchema.COLUMN_STATUS_ID)) { return true; }
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
