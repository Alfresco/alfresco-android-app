/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.sync;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;

import android.content.ContentValues;
import android.database.Cursor;

public class SyncFavoriteRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 1000;

    public static final String MIME_SYNC = "SyncAnalyser";

    protected Map<String, Serializable> persistentProperties;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncFavoriteRequest()
    {
        super();
        requestTypeId = TYPE_ID;
        
        setMimeType(MIME_SYNC);
    }
    
    public SyncFavoriteRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getRequestIdentifier()
    {
        return MIME_SYNC + "_" + getAccountId();
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        if (persistentProperties != null && !persistentProperties.isEmpty())
        {
            cValues.put(BatchOperationSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        }
        return cValues;
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
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
