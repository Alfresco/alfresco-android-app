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
package org.alfresco.mobile.android.async.node.version;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.ListingOperationRequest;

import android.content.Context;

public class DocumentVersionsRequest extends ListingOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_VERSION_LIST;

    final String nodeIdentifier;

    final Document document;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected DocumentVersionsRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, ListingContext listingContext, Document document,
            String nodeIdentifier)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, listingContext);
        this.nodeIdentifier = nodeIdentifier;
        this.document = document;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Document getDocument()
    {
        return document;
    }

    public String getNodeIdentifier()
    {
        return nodeIdentifier;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return DocumentVersionsRequest.class.getName();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends ListingOperationRequest.Builder
    {
        protected String nodeIdentifier;

        protected Document document;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Document document)
        {
            this();
            this.document = document;
            setNotificationTitle(document.getName());
            setMimeType(document.getName());
        }

        public Builder(String nodeIdentifier)
        {
            this();
            this.nodeIdentifier = nodeIdentifier;
        }

        public DocumentVersionsRequest build(Context context)
        {
            return new DocumentVersionsRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, listingContext, document, nodeIdentifier);
        }
    }

}
