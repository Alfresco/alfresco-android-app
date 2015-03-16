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
package org.alfresco.mobile.android.async.node.update;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.node.NodeRequest;

import android.content.Context;

public class UpdateNodeRequest extends NodeRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_NODE_UPDATE;

    /** list of property values that must be applied. */
    final Map<String, Serializable> properties;

    /** Binary Content of the future document. */
    final ContentFile contentFile;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected UpdateNodeRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Folder parentFolder, Node node,
            String parentFolderIdentifier, String nodeIdentifier, Map<String, Serializable> properties,
            ContentFile contentFile)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, parentFolder,
                node, parentFolderIdentifier, nodeIdentifier);
        this.contentFile = contentFile;
        this.properties = properties;

        save();
    }

    private void save()
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

    public ContentFile getContentFile()
    {
        return contentFile;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends NodeRequest.Builder
    {
        private Map<String, Serializable> properties;

        private ContentFile contentFile;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Node node, Map<String, Serializable> properties)
        {
            this(null, node, properties);
        }

        public Builder(Folder parentFolder, Node node, Map<String, Serializable> properties)
        {
            super(parentFolder, node);
            this.properties = properties;
        }

        public Builder(Node node, Map<String, Serializable> properties, ContentFile contentFile)
        {
            this(null, node, properties, contentFile);
        }

        public Builder(Folder parentFolder, Node node, Map<String, Serializable> properties, ContentFile contentFile)
        {
            super(parentFolder, node);
            this.properties = properties;
            this.contentFile = contentFile;
        }

        public UpdateNodeRequest build(Context context)
        {
            return new UpdateNodeRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, parentFolder, node, parentFolderIdentifier, nodeIdentifier, properties, contentFile);
        }
    }
}
