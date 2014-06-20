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
package org.alfresco.mobile.android.application.operations.sync.node;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.operations.sync.impl.AbstractSyncOperationRequestImpl;

import android.content.ContentValues;
import android.database.Cursor;

public class SyncNodeOperationRequest extends AbstractSyncOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    protected String nodeIdentifier;

    protected String parentFolderIdentifier;

    protected Map<String, Serializable> persistentProperties;

    protected Map<String, Serializable> properties;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncNodeOperationRequest(String parentFolderIdentifier, String documentIdentifier)
    {
        this.nodeIdentifier = documentIdentifier;
        this.parentFolderIdentifier = parentFolderIdentifier;
    }

    public SyncNodeOperationRequest(Cursor cursor)
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
        cValues.put(SynchroSchema.COLUMN_NODE_ID, getNodeIdentifier());
        if (getParentFolderIdentifier() != null)
        {
            cValues.put(SynchroSchema.COLUMN_PARENT_ID, getParentFolderIdentifier());
        }
        if (persistentProperties != null && !persistentProperties.isEmpty())
        {
            cValues.put(SynchroSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        }
        return cValues;
    }
}
