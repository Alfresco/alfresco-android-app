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
package org.alfresco.mobile.android.application.operations.batch.node;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class AbstractUpRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;

    protected String documentName;

    protected long contentStreamLength;

    protected String localFilePath;
    
    protected Map<String, Serializable> persistentProperties;

    protected Map<String, Serializable> properties;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AbstractUpRequest(String parentFolderIdentifier, String documentName, String localFilePath, String mimetype,
            long contentStreamLength)
    {
        this(parentFolderIdentifier, null, documentName, localFilePath, mimetype, contentStreamLength);
    }

    public AbstractUpRequest(String parentFolderIdentifier, String documentIdentifier, String documentName,
            String localFilePath, String mimetype, long contentStreamLength)
    {
        super(parentFolderIdentifier, documentIdentifier);
        this.contentStreamLength = contentStreamLength;
        this.localFilePath = localFilePath;
        this.documentName = documentName;

        setNotificationTitle(documentName);
        setMimeType(mimetype);
    }

    public AbstractUpRequest(Cursor cursor)
    {
        super(cursor);
        ContentFile contentFile = new ContentFileProgressImpl(new File(
                cursor.getString(BatchOperationSchema.COLUMN_LOCAL_URI_ID)));
        this.contentStreamLength = contentFile.getLength();
        this.localFilePath = contentFile.getFile().getPath();
        this.documentName = cursor.getString(BatchOperationSchema.COLUMN_TITLE_ID);

        setNotificationTitle(documentName);
        setMimeType(contentFile.getMimeType());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public long getContentStreamLength()
    {
        return contentStreamLength;
    }

    public String getLocalFilePath()
    {
        return localFilePath;
    }

    public String getDocumentName()
    {
        return documentName;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(BatchOperationSchema.COLUMN_TOTAL_SIZE_BYTES, contentStreamLength);
        if (status != Operation.STATUS_RUNNING)
        {
            cValues.put(BatchOperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, -1);
        }
        if (persistentProperties != null && !persistentProperties.isEmpty())
        {
            cValues.put(BatchOperationSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        }
        cValues.put(BatchOperationSchema.COLUMN_LOCAL_URI, localFilePath);
        return cValues;
    }

    public ContentValues createContentValues(long downloaded)
    {
        ContentValues cValues = super.createContentValues(Operation.STATUS_RUNNING);
        cValues.put(BatchOperationSchema.COLUMN_TOTAL_SIZE_BYTES, contentStreamLength);
        cValues.put(BatchOperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, downloaded);
        return cValues;
    }
}
