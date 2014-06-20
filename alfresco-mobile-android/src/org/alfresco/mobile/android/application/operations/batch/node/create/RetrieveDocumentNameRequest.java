/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.node.create;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.application.operations.batch.node.NodeOperationRequest;

import android.content.ContentValues;
import android.database.Cursor;

public class RetrieveDocumentNameRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 21;

    protected String documentName;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public RetrieveDocumentNameRequest(String parentFolderIdentifier, String documentName)
    {
        super(parentFolderIdentifier, null);
        requestTypeId = TYPE_ID;

        this.documentName = documentName;

        setNotificationTitle(documentName);
        setMimeType(documentName);

        persistentProperties = new HashMap<String, Serializable>();
        persistentProperties.put(ContentModel.PROP_NAME, documentName);
    }

    public RetrieveDocumentNameRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;

        Map<String, String> tmpProperties = retrievePropertiesMap(cursor);
        if (tmpProperties.containsKey(ContentModel.PROP_NAME))
        {
            documentName = tmpProperties.remove(ContentModel.PROP_NAME);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getDocumentName()
    {
        return documentName;
    }

    @Override
    public String getRequestIdentifier()
    {
        return "RetrieveDocumentNameRequest" + documentName;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        return cValues;
    }
}
