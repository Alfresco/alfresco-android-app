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
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.operations.SyncContent;
import org.alfresco.mobile.android.sync.operations.SyncContentDelete;
import org.alfresco.mobile.android.sync.operations.SyncContentDownload;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;
import org.alfresco.mobile.android.sync.operations.SyncContentUpdate;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;

public class PrepareSyncHelper extends PrepareBaseHelper
{
    private static final String TAG = PrepareSyncHelper.class.getName();

    protected List<SyncContent> group;

    public PrepareSyncHelper(Context context, AlfrescoAccount acc, AlfrescoSession session, int mode,
            long syncScanningTimeStamp, SyncResult syncResult, Node node)
    {
        super(context, acc, session, mode, syncScanningTimeStamp, syncResult, node);

        // Create the group
        group = new ArrayList<SyncContent>();
    }

    public PrepareSyncHelper(Context context, AlfrescoAccount acc, AlfrescoSession session, int mode,
            long syncScanningTimeStamp, SyncResult syncResult, String nodeIdentifier)
    {
        super(context, acc, session, mode, syncScanningTimeStamp, syncResult, nodeIdentifier);

        // Create the group
        group = new ArrayList<SyncContent>();
    }

    @Override
    public List<SyncContent> prepare()
    {
        scan();
        return group;
    }

    protected void prepareCreation(Uri syncUri, Document doc)
    {
        // Execution
        try
        {
            SyncContent.saveStatus(context, syncUri, SyncContentStatus.STATUS_PENDING);
            group.add(new SyncContentDownload(context, acc, session, syncResult, doc, syncUri));
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
        // Execution
        try
        {
            SyncContent.saveStatus(context, syncUri, SyncContentStatus.STATUS_PENDING);
            group.add(new SyncContentUpdate(context, acc, session, syncResult, cursorId
                    .getString(SyncContentSchema.COLUMN_PARENT_ID_ID), doc, new ContentFileImpl(localFile), syncUri,
                    false));
        }
        catch (Exception e)
        {
            // DO Nothing
        }
    }

    protected void prepareDelete(String id, Cursor cursorId)
    {
        if (SyncContentStatus.STATUS_REQUEST_USER == cursorId.getInt(SyncContentSchema.COLUMN_STATUS_ID)) { return; }

        // If Synced document
        // Flag the item inside the referential
        if (SyncContentStatus.STATUS_MODIFIED != cursorId.getInt(SyncContentSchema.COLUMN_STATUS_ID))
        {
            ContentValues cValues = new ContentValues();
            cValues.put(SyncContentSchema.COLUMN_STATUS, SyncContentStatus.STATUS_HIDDEN);
            cValues.put(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES, 0);
            cValues.put(SyncContentSchema.COLUMN_DOC_SIZE_BYTES, 0);
            if (!ContentModel.TYPE_FOLDER.equals(cursorId.getString(SyncContentSchema.COLUMN_STATUS_ID)))
            {
                cValues.put(SyncContentSchema.COLUMN_DOC_SIZE_BYTES,
                        -cursorId.getLong(SyncContentSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID));
            }
            context.getContentResolver().update(
                    SyncContentManager.getUri(cursorId.getLong(SyncContentSchema.COLUMN_ID_ID)), cValues, null,
                    null);
        }

        // Execute Delete
        try
        {
            SyncContent.saveStatus(context,
                    SyncContentManager.getUri(cursorId.getLong(SyncContentSchema.COLUMN_ID_ID)),
                    SyncContentStatus.STATUS_PENDING);
            group.add(new SyncContentDelete(context, acc, session, syncResult, id, SyncContentManager.getUri(cursorId
                    .getLong(SyncContentSchema.COLUMN_ID_ID))));
        }
        catch (Exception e)
        {
            // DO Nothing
        }
    }
}
