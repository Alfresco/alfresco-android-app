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
package org.alfresco.mobile.android.application.operations.batch.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;

import android.content.ContentValues;
import android.database.Cursor;

public class NodeOperationRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    protected String nodeIdentifier;

    protected String parentFolderIdentifier;

    protected Map<String, Serializable> persistentProperties;

    protected Map<String, Serializable> properties;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public NodeOperationRequest(String parentFolderIdentifier, String documentIdentifier)
    {
        this.nodeIdentifier = documentIdentifier;
        this.parentFolderIdentifier = parentFolderIdentifier;
    }

    public NodeOperationRequest(Cursor cursor)
    {
        super(cursor);
        // Parent
        this.parentFolderIdentifier = cursor.getString(BatchOperationSchema.COLUMN_PARENT_ID_ID);
        this.nodeIdentifier = cursor.getString(BatchOperationSchema.COLUMN_NODE_ID_ID);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getNodeIdentifier()
    {
        return nodeIdentifier;
    }

    public String getParentFolderIdentifier()
    {
        return parentFolderIdentifier;
    }

    @Override
    public String getRequestIdentifier()
    {
        return nodeIdentifier;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(BatchOperationSchema.COLUMN_NODE_ID, getNodeIdentifier());
        if (getParentFolderIdentifier() != null)
        {
            cValues.put(BatchOperationSchema.COLUMN_PARENT_ID, getParentFolderIdentifier());
        }
        if (persistentProperties != null && !persistentProperties.isEmpty())
        {
            cValues.put(BatchOperationSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        }
        return cValues;
    }

    protected Map<String, String> retrievePropertiesMap(Cursor cursor)
    {
        // PROPERTIES
        String rawProperties = cursor.getString(BatchOperationSchema.COLUMN_PROPERTIES_ID);
        if (rawProperties != null)
        {
            return MapUtil.stringToMap(rawProperties);
        }
        else
        {
            return new HashMap<String, String>();
        }
    }
}
