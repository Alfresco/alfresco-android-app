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
package org.alfresco.mobile.android.async.node.comment;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.node.NodeRequest;

import android.content.Context;

public class CreateCommentRequest extends NodeRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_COMMENT_CREATE;

    final String comment;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected CreateCommentRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Folder parentFolder, Node node,
            String parentFolderIdentifier, String nodeIdentifier, String comment)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, parentFolder,
                node, parentFolderIdentifier, nodeIdentifier);
        this.comment = comment;
        requestTypeId = TYPE_ID;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends NodeRequest.Builder
    {
        private String comment;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Node node, String comment)
        {
            super(null, node);
            this.requestTypeId = TYPE_ID;
            this.comment = comment;
        }

        public CreateCommentRequest build(Context context)
        {
            return new CreateCommentRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, parentFolder, node, parentFolderIdentifier, nodeIdentifier, comment);
        }
    }
}
