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
package org.alfresco.mobile.android.application.integration.node;


import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;

import android.content.ContentValues;
import android.database.Cursor;

public class NodeOperationRequest extends AbstractOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    protected String nodeIdentifier;
    protected String parentFolderIdentifier;

    public NodeOperationRequest(String parentFolderIdentifier, String documentIdentifier)
    {
        this.nodeIdentifier = documentIdentifier;
        this.parentFolderIdentifier = parentFolderIdentifier;
    }

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

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_NODE_ID, getNodeIdentifier());
        cValues.put(OperationSchema.COLUMN_PARENT_ID, getParentFolderIdentifier());
        return cValues;
    }
    
    public NodeOperationRequest(Cursor cursor){
        super(cursor);
        // Parent
        this.parentFolderIdentifier = cursor.getString(OperationSchema.COLUMN_PARENT_ID_ID);
        this.nodeIdentifier = cursor.getString(OperationSchema.COLUMN_NODE_ID_ID);
    }

}
