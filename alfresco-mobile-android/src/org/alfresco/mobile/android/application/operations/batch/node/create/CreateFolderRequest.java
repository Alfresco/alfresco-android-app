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
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.node.NodeOperationRequest;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.ContentValues;
import android.database.Cursor;

public class CreateFolderRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;

    protected String folderName;

    public static final int TYPE_ID = 70;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateFolderRequest(Folder parentFolder, String folderName)
    {
        super(parentFolder.getIdentifier(), null);
        this.folderName = folderName;
        requestTypeId = TYPE_ID;

        properties = new HashMap<String, Serializable>(2);
        properties.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.FOLDER_BASETYPE_ID);
        
        persistentProperties = new HashMap<String, Serializable>();
        persistentProperties.put(ContentModel.PROP_NAME, folderName);
        persistentProperties.putAll(properties);

        setNotificationTitle(folderName);
        setMimeType(ContentModel.TYPE_FOLDER);
    }

    public CreateFolderRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;

        // PROPERTIES
        Map<String, String> tmpProperties = retrievePropertiesMap(cursor);

        this.folderName = "";
        if (tmpProperties.containsKey(ContentModel.PROP_NAME))
        {
            this.folderName = tmpProperties.remove(ContentModel.PROP_NAME);
        }
        Map<String, Serializable> finalProperties = new HashMap<String, Serializable>(tmpProperties);
        this.properties = finalProperties;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getFolderName()
    {
        return folderName;
    }

    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(BatchOperationSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        return cValues;
    }
}
