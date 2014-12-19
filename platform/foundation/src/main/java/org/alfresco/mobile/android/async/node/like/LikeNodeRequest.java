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
package org.alfresco.mobile.android.async.node.like;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.node.NodeRequest;

import android.content.ContentValues;
import android.content.Context;

public class LikeNodeRequest extends NodeRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_LIKE_LIST;

    final Boolean isReadOnly;

    final Boolean isLike;

    private static final String PROP_READ_ONLY = "readOnly";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected LikeNodeRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Folder parentFolder, Node node,
            String parentFolderIdentifier, String nodeIdentifier, boolean isReadOnly, Boolean isLike)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, parentFolder,
                node, parentFolderIdentifier, nodeIdentifier);
        this.isReadOnly = isReadOnly;
        this.isLike = isLike;

        save();
    }

    private void save()
    {
        persistentProperties = new HashMap<String, Serializable>();
        persistentProperties.put(PROP_READ_ONLY, isReadOnly);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Boolean isReadOnly()
    {
        return isReadOnly;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        return cValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends NodeRequest.Builder
    {
        private Boolean isReadOnly = true;

        private Boolean isLike = null;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Node node, Boolean isReadOnly)
        {
            super(null, node);
            this.isReadOnly = isReadOnly;
            this.requestTypeId = TYPE_ID;
        }

        public Builder(Node node, Boolean isReadOnly, Boolean isLike)
        {
            super(null, node);
            this.isReadOnly = isReadOnly;
            this.isLike = isLike;
            this.requestTypeId = TYPE_ID;
        }

        public Builder(String nodeIdentifier, Boolean isReadOnly)
        {
            super(null, nodeIdentifier);
            this.isReadOnly = isReadOnly;
            this.requestTypeId = TYPE_ID;
        }

        public LikeNodeRequest build(Context context)
        {
            return new LikeNodeRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, parentFolder, node, parentFolderIdentifier, nodeIdentifier, isReadOnly, isLike);
        }
    }
}
