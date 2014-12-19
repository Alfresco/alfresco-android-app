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
package org.alfresco.mobile.android.async.node;

import java.io.File;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.OperationsSchema;
import org.alfresco.mobile.android.async.utils.ContentFileProgressImpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class UpNodeRequest extends NodeRequest
{
    private static final long serialVersionUID = 1L;

    protected final ContentFile contentFile;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected UpNodeRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Folder parentFolder, Node node,
            String parentFolderIdentifier, String nodeIdentifier, ContentFile contentFile)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, parentFolder,
                node, parentFolderIdentifier, nodeIdentifier);
        this.contentFile = contentFile;
    }

    public UpNodeRequest(Cursor cursor)
    {
        super(cursor);
        String path = cursor.getString(OperationsSchema.COLUMN_LOCAL_URI_ID);
        File f = new File(path);
        this.contentFile = new ContentFileProgressImpl(f, f.getName(), mimeType);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        if (contentFile == null) { return cValues; }
        cValues.put(OperationSchema.COLUMN_TOTAL_SIZE_BYTES, contentFile.getLength());
        if (status != Operation.STATUS_RUNNING)
        {
            cValues.put(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, -1);
        }
        cValues.put(OperationSchema.COLUMN_LOCAL_URI, contentFile.getFile().getPath());
        return cValues;
    }

    public ContentValues createContentValues(long downloaded)
    {
        ContentValues cValues = super.createContentValues(Operation.STATUS_RUNNING);
        cValues.put(OperationSchema.COLUMN_TOTAL_SIZE_BYTES, contentFile.getLength());
        cValues.put(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, downloaded);
        return cValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends NodeRequest.Builder
    {
        protected ContentFile contentFile;

        protected Builder()
        {
        }

        public Builder(Folder parentFolder, Node node, ContentFile contentFile)
        {
            super(parentFolder, node);
            if (contentFile != null)
            {
                this.contentFile = new ContentFileProgressImpl(contentFile.getFile(), contentFile.getFileName(),
                        contentFile.getMimeType());
            }
            setMimeType(contentFile != null ? contentFile.getMimeType() : null);
        }

        public Builder(String parentFolderId, String nodeId, ContentFile contentFile)
        {
            super(parentFolderId, nodeId);
            if (contentFile != null)
            {
                this.contentFile = new ContentFileProgressImpl(contentFile.getFile(), contentFile.getFileName(),
                        contentFile.getMimeType());
            }
            setMimeType(contentFile != null ? contentFile.getMimeType() : null);
        }

        public UpNodeRequest build(Context context)
        {
            return new UpNodeRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, parentFolder, node, parentFolderIdentifier, nodeIdentifier, contentFile);
        }
    }
}
