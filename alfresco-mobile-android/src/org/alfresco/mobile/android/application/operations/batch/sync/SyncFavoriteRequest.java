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
import org.alfresco.mobile.android.application.operations.batch.node.NodeOperationRequest;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;

import android.database.Cursor;

public class SyncFavoriteRequest extends NodeOperationRequest
{
    public static final int MODE_DOCUMENT = 0;
    
    public static final int MODE_DOCUMENTS = 1;

    public static final int MODE_FOLDERS = 2;

    public static final int MODE_BOTH = 4;
    
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 1000;

    public static final String MIME_SYNC = "SyncAnalyser";

    private static final String PROP_MODE = "Mode";

    private int mode;
    
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncFavoriteRequest()
    {
      this(MODE_DOCUMENTS);
    }
    
    public SyncFavoriteRequest(int mode)
    {
        super(null, null);
        requestTypeId = TYPE_ID;
        setMimeType(MIME_SYNC);
        
        this.mode = mode;
        
        persistentProperties = new HashMap<String, Serializable>(1);
        persistentProperties.put(PROP_MODE, mode);
    }
    
    public SyncFavoriteRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
        
        // PROPERTIES
        String rawProperties = cursor.getString(BatchOperationSchema.COLUMN_PROPERTIES_ID);
        Map<String, String> tmpProperties = MapUtil.stringToMap(rawProperties);

        if (tmpProperties.containsKey(PROP_MODE))
        {
            this.mode = Integer.parseInt(tmpProperties.remove(PROP_MODE));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getRequestIdentifier()
    {
        return MIME_SYNC + "_" + getAccountId();
    }
    
    public int getMode()
    {
        return mode;
    }
}
