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
package org.alfresco.mobile.android.async.node.delete;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.node.NodeRequest;

import android.content.Context;
import android.database.Cursor;

public class DeleteNodeRequest extends NodeRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_NODE_DELETE;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected DeleteNodeRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Folder parentFolder, Node node,
            String parentFolderIdentifier, String nodeIdentifier)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, parentFolder,
                node, parentFolderIdentifier, nodeIdentifier);
        requestTypeId = TYPE_ID;
        title = node.getName();
        mimeType = node.getName();
    }

    public DeleteNodeRequest(Cursor cursor)
    {
        super(cursor);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends NodeRequest.Builder
    {
        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Node node)
        {
            super(null, node);
            this.requestTypeId = TYPE_ID;
        }

        public Builder(Folder parentFolder, Node node)
        {
            super(parentFolder, node);
            this.requestTypeId = TYPE_ID;
        }

        public DeleteNodeRequest build(Context context)
        {
            return new DeleteNodeRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, parentFolder, node, parentFolderIdentifier, nodeIdentifier);
        }
    }
}
