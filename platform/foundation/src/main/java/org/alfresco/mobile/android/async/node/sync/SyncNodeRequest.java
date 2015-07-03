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
package org.alfresco.mobile.android.async.node.sync;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.node.NodeRequest;

import android.content.Context;

public class SyncNodeRequest extends NodeRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_SYNC_CREATE;

    final Boolean markSync;

    final Boolean batchSync;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected SyncNodeRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Folder parentFolder, Node node,
            String parentFolderIdentifier, String nodeIdentifier, Boolean markSync, Boolean batchSync)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, parentFolder,
                node, parentFolderIdentifier, nodeIdentifier);
        this.markSync = markSync;
        this.batchSync = batchSync;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends NodeRequest.Builder
    {
        protected Boolean markSync;

        protected Boolean batchSync;

        protected Builder()
        {
        }

        public Builder(String parentFolderIdentifier, String documentIdentifier, boolean markSync)
        {
            super(parentFolderIdentifier, documentIdentifier);
            this.requestTypeId = TYPE_ID;
            this.markSync = markSync;
        }

        public Builder(Folder folder, Node node, boolean markSync)
        {
            super(folder, node);
            this.requestTypeId = TYPE_ID;
            this.markSync = markSync;
        }

        public Builder(Folder folder, Node node)
        {
            super(folder, node);
            this.requestTypeId = TYPE_ID;
        }

        public Builder(Folder folder, Node node, boolean markSync, boolean batchSync)
        {
            super(folder, node);
            this.markSync = markSync;
            this.batchSync = batchSync;
            this.requestTypeId = TYPE_ID;
        }

        public Builder(String nodeIdentifier, boolean markSync, boolean batchSync)
        {
            super(null, nodeIdentifier);
            this.markSync = markSync;
            this.batchSync = batchSync;
            this.requestTypeId = TYPE_ID;
        }

        public SyncNodeRequest build(Context context)
        {
            return new SyncNodeRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, parentFolder, node, parentFolderIdentifier, nodeIdentifier, markSync, batchSync);
        }
    }

}
