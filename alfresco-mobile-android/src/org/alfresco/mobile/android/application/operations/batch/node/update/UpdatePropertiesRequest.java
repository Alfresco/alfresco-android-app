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
package org.alfresco.mobile.android.application.operations.batch.node.update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.node.NodeOperationRequest;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;

import android.content.ContentValues;
import android.database.Cursor;

public class UpdatePropertiesRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 35;

    private Map<String, Serializable> properties;

    private List<String> tags;
    
    private Map<String, Serializable> persistentProperties;
    
    private static final String PROP_TAG = "tag";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public UpdatePropertiesRequest(Folder parent, Node node,  Map<String, Serializable> properties)
    {
        this(parent.getIdentifier(), node.getIdentifier(), properties, null);
    }
    
    public UpdatePropertiesRequest(String parentFolderIdentifier, String documentIdentifier,  Map<String, Serializable> properties, List<String> tags)
    {
        super(parentFolderIdentifier, documentIdentifier);
        requestTypeId = TYPE_ID;
        
        this.properties = properties;
        this.tags = tags;

        persistentProperties = new HashMap<String, Serializable>();
        if (properties != null)
        {
            persistentProperties.putAll(properties);
        }
        if (tags == null) { return; }
        int i = 0;
        for (String tagValue : tags)
        {
            persistentProperties.put(PROP_TAG + i, tagValue);
            i++;
        }
    }
    
    public UpdatePropertiesRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
        
        // PROPERTIES
        Map<String, String> tmpProperties = retrievePropertiesMap(cursor);

        tags = new ArrayList<String>();
        List<String> keys = new ArrayList<String>();
        for (Entry<String, String> entry : tmpProperties.entrySet())
        {
            if (entry.getKey().startsWith(PROP_TAG))
            {
                tags.add(entry.getValue());
                keys.add(entry.getKey());
            }
        }

        for (String key : keys)
        {
            tmpProperties.remove(key);
        }

        Map<String, Serializable> finalProperties = new HashMap<String, Serializable>(tmpProperties);
        this.properties = finalProperties;
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public List<String> getTags()
    {
        return tags;
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
