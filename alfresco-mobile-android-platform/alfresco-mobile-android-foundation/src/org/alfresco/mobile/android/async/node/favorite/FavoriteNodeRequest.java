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
package org.alfresco.mobile.android.async.node.favorite;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.node.NodeRequest;

import android.content.Context;

public class FavoriteNodeRequest extends NodeRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_FAVORITE_CREATE;

    final Boolean markFavorite;

    final Boolean batchFavorite;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected FavoriteNodeRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Folder parentFolder, Node node,
            String parentFolderIdentifier, String nodeIdentifier, Boolean markFavorite, Boolean batchFavorite)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, parentFolder,
                node, parentFolderIdentifier, nodeIdentifier);
        this.markFavorite = markFavorite;
        this.batchFavorite = batchFavorite;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends NodeRequest.Builder
    {
        protected Boolean markFavorite;

        protected Boolean batchFavorite;

        protected Builder()
        {
        }

        public Builder(String parentFolderIdentifier, String documentIdentifier, boolean markFavorite)
        {
            super(parentFolderIdentifier, documentIdentifier);
            this.requestTypeId = TYPE_ID;
        }

        public Builder(Folder folder, Node node)
        {
            super(folder, node);
            this.requestTypeId = TYPE_ID;
        }

        public Builder(Folder folder, Node node, boolean markFavorite, boolean batchFavorite)
        {
            super(folder, node);
            this.markFavorite = markFavorite;
            this.batchFavorite = batchFavorite;
            this.requestTypeId = TYPE_ID;
        }

        public Builder(String nodeIdentifier, boolean markFavorite, boolean batchFavorite)
        {
            super(null, nodeIdentifier);
            this.markFavorite = markFavorite;
            this.batchFavorite = batchFavorite;
            this.requestTypeId = TYPE_ID;
        }

        public FavoriteNodeRequest build(Context context)
        {
            return new FavoriteNodeRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, parentFolder, node, parentFolderIdentifier, nodeIdentifier, markFavorite,
                    batchFavorite);
        }
    }

}
