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
package org.alfresco.mobile.android.application.integration.node;

import java.io.File;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class AbstractUpRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;

    protected String documentName;
    
    protected long contentStreamLength;
    
    protected String localFilePath;
    
    public AbstractUpRequest(String parentFolderIdentifier, String documentName, String localFilePath, String mimetype, long contentStreamLength)
    {
       this(parentFolderIdentifier, null, documentName, localFilePath, mimetype, contentStreamLength);
    }
    
    public AbstractUpRequest(String parentFolderIdentifier, String documentIdentifier, String documentName, String localFilePath, String mimetype, long contentStreamLength)
    {
        super(parentFolderIdentifier, documentIdentifier);
        this.contentStreamLength = contentStreamLength;
        this.localFilePath = localFilePath;
        this.documentName = documentName;
        
        setNotificationTitle(documentName);
        setMimeType(mimetype);
    }

    
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_TOTAL_SIZE_BYTES, contentStreamLength);
        if (status != Operation.STATUS_RUNNING)
        {
            cValues.put(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, -1);
        }
        cValues.put(OperationSchema.COLUMN_LOCAL_URI, localFilePath);
        return cValues;
    }

    public ContentValues createContentValues(long downloaded)
    {
        ContentValues cValues = super.createContentValues(Operation.STATUS_RUNNING);
        cValues.put(OperationSchema.COLUMN_TOTAL_SIZE_BYTES, contentStreamLength);
        cValues.put(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, downloaded);
        return cValues;
    }


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
    
    
    public AbstractUpRequest(Cursor cursor){
        super(cursor);
        ContentFile contentFile = new ContentFileProgressImpl(new File(cursor.getString(OperationSchema.COLUMN_LOCAL_URI_ID)));
        this.contentStreamLength = contentFile.getLength();
        this.localFilePath = contentFile.getFile().getPath();
        this.documentName = contentFile.getFileName();
        
        setNotificationTitle(documentName);
        setMimeType(contentFile.getMimeType());
    }
}
