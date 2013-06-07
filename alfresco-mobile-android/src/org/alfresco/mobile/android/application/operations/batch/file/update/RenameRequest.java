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
package org.alfresco.mobile.android.application.operations.batch.file.update;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.file.FileOperationRequest;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;

import android.content.ContentValues;
import android.database.Cursor;

public class RenameRequest extends FileOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 235;

    private Map<String, Serializable> persistentProperties;

    protected String fileName;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public RenameRequest(File file, String newName)
    {
        super(file.getPath());
        requestTypeId = TYPE_ID;

        this.fileName = newName;

        persistentProperties = new HashMap<String, Serializable>();
        persistentProperties.put(ContentModel.PROP_NAME, newName);
    }

    public RenameRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;

        // PROPERTIES
        String rawProperties = cursor.getString(BatchOperationSchema.COLUMN_PROPERTIES_ID);
        Map<String, String> tmpProperties = MapUtil.stringToMap(rawProperties);

        this.fileName = "";
        if (tmpProperties.containsKey(ContentModel.PROP_NAME))
        {
            this.fileName = tmpProperties.remove(ContentModel.PROP_NAME);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getFileNewName()
    {
        return fileName;
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
