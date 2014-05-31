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

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class NodeRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    protected String nodeIdentifier;

    protected String parentFolderIdentifier;

    protected Node node;

    protected Folder parentFolder;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected NodeRequest(Context context, long accountId, String networkId, int notificationVisibility, String title,
            String mimeType, int requestTypeId, Folder parentFolder, Node node, String parentFolderIdentifier,
            String nodeIdentifier)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.nodeIdentifier = nodeIdentifier;
        this.parentFolderIdentifier = parentFolderIdentifier;
        this.node = node;
        this.parentFolder = parentFolder;
        context.getContentResolver().update(notificationUri, createContentValues(Operation.STATUS_PENDING), null, null);
    }

    public NodeRequest(Cursor cursor)
    {
        super(cursor);
        this.parentFolderIdentifier = cursor.getString(OperationSchema.COLUMN_PARENT_ID_ID);
        this.nodeIdentifier = cursor.getString(OperationSchema.COLUMN_NODE_ID_ID);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getNodeIdentifier()
    {
        return nodeIdentifier;
    }

    public String getParentFolderIdentifier()
    {
        return parentFolderIdentifier;
    }

    public Node getNode()
    {
        return node;
    }

    public Folder getParentFolder()
    {
        return parentFolder;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return nodeIdentifier;
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_NODE_ID, getNodeIdentifier());
        if (getParentFolderIdentifier() != null)
        {
            cValues.put(OperationSchema.COLUMN_PARENT_ID, getParentFolderIdentifier());
        }
        return cValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        protected String nodeIdentifier;

        protected String parentFolderIdentifier;

        protected Node node;

        protected Folder parentFolder;

        protected Builder()
        {
            setNotificationTitle((node != null) ? node.getName() : null);
            setMimeType((node != null) ? node.getName() : null);
        }

        public Builder(String parentFolderIdentifier, String nodeIdentifier)
        {
            this();
            this.nodeIdentifier = nodeIdentifier;
            this.parentFolderIdentifier = parentFolderIdentifier;
        }

        public Builder(Folder parentFolder, Node node)
        {
            this((parentFolder != null) ? parentFolder.getIdentifier() : null, (node != null) ? node.getIdentifier()
                    : null);
            this.node = node;
            this.parentFolder = parentFolder;
        }

        public NodeRequest build(Context context)
        {
            return new NodeRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, parentFolder, node, parentFolderIdentifier, nodeIdentifier);
        }
    }
}
