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
package org.alfresco.mobile.android.async.node.download;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.node.NodeRequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class DownloadRequest extends NodeRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_NODE_DOWNLOAD;

    private static final String PROP_OVERWRITE = "overwrite";

    private static final String PROP_LENGTH = "contentStreamLength";

    final boolean overwrite;

    final long contentStreamLength;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected DownloadRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Folder parentFolder, Node node,
            String parentFolderIdentifier, String nodeIdentifier, boolean overwrite)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, parentFolder,
                node, parentFolderIdentifier, nodeIdentifier);
        this.overwrite = overwrite;
        this.contentStreamLength = ((Document) node).getContentStreamLength();

        // Save extra info
        persistentProperties.put(PROP_OVERWRITE, overwrite);
        persistentProperties.put(PROP_LENGTH, contentStreamLength);
    }

    public DownloadRequest(Cursor cursor)
    {
        super(cursor);
        if (persistentProperties.containsKey(PROP_OVERWRITE))
        {
            this.overwrite = Boolean.parseBoolean((String) persistentProperties.remove(PROP_OVERWRITE));
        }
        else
        {
            this.overwrite = false;
        }

        if (persistentProperties.containsKey(PROP_LENGTH))
        {
            this.contentStreamLength = Long.parseLong((String) persistentProperties.remove(PROP_LENGTH));
        }
        else
        {
            this.contentStreamLength = 0;
        }
    }

    @Override
    public boolean isLongRunning()
    {
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_TOTAL_SIZE_BYTES, contentStreamLength);
        if (status != Operation.STATUS_RUNNING)
        {
            cValues.put(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, -1);
        }
        return cValues;
    }

    public ContentValues createContentValues(long downloaded)
    {
        ContentValues cValues = super.createContentValues(Operation.STATUS_RUNNING);
        cValues.put(OperationSchema.COLUMN_TOTAL_SIZE_BYTES, contentStreamLength);
        cValues.put(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, downloaded);
        return cValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends NodeRequest.Builder
    {
        private boolean overwrite = false;

        protected Builder()
        {
        }

        public Builder(Folder folder, Document document, boolean overwrite)
        {
            super(folder, document);
            this.overwrite = overwrite;
            this.requestTypeId = TYPE_ID;
            setMimeType(document.getContentStreamMimeType());
            setNotificationTitle(document.getName());
        }

        public DownloadRequest build(Context context)
        {
            return new DownloadRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, parentFolder, node, parentFolderIdentifier, nodeIdentifier, overwrite);
        }
    }

}
