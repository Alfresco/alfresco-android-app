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
package org.alfresco.mobile.android.application.integration.node.create;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.integration.node.AbstractUpRequest;
import org.alfresco.mobile.android.application.integration.utils.MapUtil;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class CreateDocumentRequest extends AbstractUpRequest
{
    private static final long serialVersionUID = 1L;

    private boolean isCreation;

    private Map<String, Serializable> properties;

    private List<String> tags;

    public static final int TYPE_ID = 20;

    private Map<String, Serializable> persistentProperties;

    private static final String PROP_ISCREATION = "isCreation";

    private static final String PROP_TAG = "tag";

    public CreateDocumentRequest(String parentFolderIdentifier, String documentName, ContentFile contentFile)
    {
        this(parentFolderIdentifier, documentName, null, null, contentFile, false);
    }

    public CreateDocumentRequest(String parentFolderIdentifier, String documentName,
            Map<String, Serializable> properties, List<String> tags, ContentFile contentFile, boolean isCreation)
    {
        super(parentFolderIdentifier, documentName, contentFile.getFile().getPath(), contentFile.getMimeType(),
                contentFile.getLength());
        requestTypeId = TYPE_ID;

        this.isCreation = isCreation;
        this.properties = properties;
        this.tags = tags;

        persistentProperties = new HashMap<String, Serializable>();
        if (properties != null)
        {
            persistentProperties.putAll(properties);
        }
        persistentProperties.put(ContentModel.PROP_NAME, documentName);
        persistentProperties.put(PROP_ISCREATION, isCreation);
        if (tags == null) { return; }
        int i = 0;
        for (String tagValue : tags)
        {
            persistentProperties.put(PROP_TAG + i, tagValue);
            i++;
        }
    }

    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

    @Override
    public String getRequestIdentifier()
    {
        return parentFolderIdentifier + documentName;
    }

    public boolean isCreation()
    {
        return isCreation;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        return cValues;
    }

    public CreateDocumentRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
        
        // PROPERTIES
        String rawProperties = cursor.getString(OperationSchema.COLUMN_PROPERTIES_ID);
        Map<String, String> tmpProperties = MapUtil.stringToMap(rawProperties);

        documentName = "";
        if (tmpProperties.containsKey(ContentModel.PROP_NAME))
        {
            documentName = tmpProperties.remove(ContentModel.PROP_NAME);
        }

        this.isCreation = false;
        if (tmpProperties.containsKey(PROP_ISCREATION))
        {
            this.isCreation = Boolean.parseBoolean(tmpProperties.remove(PROP_ISCREATION));
        }

        List<String> tags = new ArrayList<String>();
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
        this.tags = tags;
    }

}
