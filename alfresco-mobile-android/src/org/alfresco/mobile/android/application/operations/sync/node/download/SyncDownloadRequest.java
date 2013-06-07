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
package org.alfresco.mobile.android.application.operations.sync.node.download;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.operations.sync.node.SyncNodeOperationRequest;

import android.content.ContentValues;
import android.database.Cursor;

public class SyncDownloadRequest extends SyncNodeOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 10;

    private long contentStreamLength;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncDownloadRequest(String parentIdentifier, String documentIdentifier)
    {
        super(parentIdentifier, documentIdentifier);
        requestTypeId = TYPE_ID;
    }

    public SyncDownloadRequest(Document document)
    {
        this(null, document.getIdentifier());
        this.contentStreamLength = document.getContentStreamLength();

        setNotificationTitle(document.getName());
        setMimeType(document.getName());
    }

    public SyncDownloadRequest(Folder folder, Document document)
    {
        this(folder.getIdentifier(), document.getIdentifier());

        this.contentStreamLength = document.getContentStreamLength();

        setNotificationTitle(document.getName());
        setMimeType(document.getName());
    }
    
    public SyncDownloadRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
        contentStreamLength = cursor.getLong(BatchOperationSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public long getContentStreamLength()
    {
        return contentStreamLength;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status, long timestamp)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(BatchOperationSchema.COLUMN_TOTAL_SIZE_BYTES, getContentStreamLength());
        if (status != Operation.STATUS_RUNNING)
        {
            cValues.put(SynchroSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, -1);
        }
        if (timestamp != -1)
        {
            cValues.put(SynchroSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, timestamp);
        }
        return cValues;
    }

    public ContentValues createContentValues(long downloaded)
    {
        ContentValues cValues = super.createContentValues(Operation.STATUS_RUNNING);
        cValues.put(BatchOperationSchema.COLUMN_TOTAL_SIZE_BYTES, getContentStreamLength());
        cValues.put(BatchOperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, downloaded);
        return cValues;
    }
}
