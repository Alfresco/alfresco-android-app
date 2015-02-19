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
package org.alfresco.mobile.android.async.definition;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.ContentValues;
import android.content.Context;

public class TypeDefinitionRequest extends BaseOperationRequest
{
    public static final int DOCUMENT = 0;

    public static final int FOLDER = 1;

    public static final int ASPECT = 2;

    public static final int TASK = 3;

    private static final long serialVersionUID = 1L;

    // ///////////////////////////////////////////////////////////////////////////
    // Members
    // ///////////////////////////////////////////////////////////////////////////
    public static final int TYPE_ID = OperationRequestIds.ID_TYPE_DEFINITION_READ;

    final String type;

    final int typeDefinitionId;

    final Node node;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected TypeDefinitionRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, int definitionId, String type, Node node)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.type = type;
        this.node = node;
        this.typeDefinitionId = definitionId;
        save();
    }

    private void save()
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        return super.createContentValues(status);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        protected String type;

        protected Node node;

        protected int typeDefinitionId;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Node node)
        {
            super();
            this.node = node;
            this.typeDefinitionId = (node.isDocument()) ? DOCUMENT : FOLDER;
            this.requestTypeId = TYPE_ID;
        }

        public Builder(int typeDefinitionId, String type)
        {
            super();
            this.type = type;
            this.typeDefinitionId = typeDefinitionId;
            this.requestTypeId = TYPE_ID;
        }

        public TypeDefinitionRequest build(Context context)
        {
            return new TypeDefinitionRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, typeDefinitionId, type, node);
        }
    }
}
