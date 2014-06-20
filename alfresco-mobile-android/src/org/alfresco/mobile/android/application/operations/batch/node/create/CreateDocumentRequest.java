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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.application.operations.batch.node.AbstractUpRequest;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;

import android.database.Cursor;

public class CreateDocumentRequest extends AbstractUpRequest
{
    private static final long serialVersionUID = 1L;

    private boolean isCreation;

    private List<String> tags;

    public static final int TYPE_ID = 20;

    private static final String PROP_ISCREATION = "isCreation";

    private static final String PROP_TAG = "tag";

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
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

        save();
    }

    public CreateDocumentRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;

        // PROPERTIES
        Map<String, String> tmpProperties = retrievePropertiesMap(cursor);

        documentName = "";
        if (tmpProperties.containsKey(ContentModel.PROP_NAME))
        {
            documentName = tmpProperties.remove(ContentModel.PROP_NAME);
            setNotificationTitle(documentName);
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
        save();
    }
    
    private void save(){
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

    // ////////////////////////////////////////////////////
    // GETTERS
    // ////////////////////////////////////////////////////
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
    
    // ////////////////////////////////////////////////////
    // SETTERS
    // ////////////////////////////////////////////////////
    public void setDocumentName(String name)
    {
        documentName = name;
        setNotificationTitle(name);
        if (persistentProperties != null)
        {
            persistentProperties.put(ContentModel.PROP_NAME, name);
        }
    }
    
    public void setContentFile(File f)
    {
        ContentFile contentFile = new ContentFileProgressImpl(f);
        this.contentStreamLength = contentFile.getLength();
        this.localFilePath = contentFile.getFile().getPath();
        this.documentName = f.getName();

        setNotificationTitle(documentName);
        setMimeType(contentFile.getMimeType());
    }
}
