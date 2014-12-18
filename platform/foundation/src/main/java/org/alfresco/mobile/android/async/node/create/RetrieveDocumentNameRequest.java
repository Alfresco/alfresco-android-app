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
package org.alfresco.mobile.android.async.node.create;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.node.NodeRequest;

import android.content.ContentValues;
import android.content.Context;

public class RetrieveDocumentNameRequest extends NodeRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 21;

    final String documentName;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected RetrieveDocumentNameRequest(Context context, long accountId, String networkId,
            int notificationVisibility, String title, String mimeType, int requestTypeId, Folder parentFolder,
            Node node, String parentFolderIdentifier, String nodeIdentifier, String documentName)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, parentFolder,
                node, parentFolderIdentifier, nodeIdentifier);
        this.documentName = documentName;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return "RetrieveDocumentNameRequest" + documentName;
    }

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
        protected String documentName;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(String parentFolderIdentifier, String folderName)
        {
            this();
            this.documentName = folderName;
            this.parentFolderIdentifier = parentFolderIdentifier;
        }

        public Builder(Folder parentFolder, String folderName)
        {
            this();
            this.documentName = folderName;
            this.parentFolder = parentFolder;
        }

        public RetrieveDocumentNameRequest build(Context context)
        {
            return new RetrieveDocumentNameRequest(context, accountId, networkId, notificationVisibility, title,
                    mimeType, requestTypeId, parentFolder, node, parentFolderIdentifier, nodeIdentifier, documentName);
        }
    }
}
