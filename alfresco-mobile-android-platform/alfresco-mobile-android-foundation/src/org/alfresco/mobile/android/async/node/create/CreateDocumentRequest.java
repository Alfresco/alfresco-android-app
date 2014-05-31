/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.async.node.create;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.node.UpNodeRequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class CreateDocumentRequest extends UpNodeRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_NODE_CREATE;

    private static final String PROP_ISCREATION = "isCreation";

    private static final String PROP_TAG = "tag";

    private static final String PROP_TYPE = "type";

    final boolean isCreation;

    final String documentName;

    final List<String> tags;

    final String type;

    final Map<String, Serializable> properties;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    protected CreateDocumentRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Folder parentFolder, Node node,
            String parentFolderIdentifier, String nodeIdentifier, ContentFile contentFile, String documentName,
            String type, Map<String, Serializable> properties, List<String> tags, boolean isCreation)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, parentFolder,
                node, parentFolderIdentifier, nodeIdentifier, contentFile);

        this.isCreation = isCreation;
        this.properties = properties;
        this.tags = tags;
        this.type = type;
        this.documentName = documentName;

        // Save extra data
        if (properties != null)
        {
            persistentProperties.putAll(properties);
        }
        persistentProperties.put(PROP_TYPE, type);
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

    public CreateDocumentRequest(Cursor cursor)
    {
        super(cursor);

        this.type = (String) persistentProperties.remove(PROP_TYPE);

        if (persistentProperties.containsKey(ContentModel.PROP_NAME))
        {
            this.documentName = (String) persistentProperties.remove(ContentModel.PROP_NAME);
            this.title = documentName;
        }
        else
        {
            this.documentName = "";
        }

        if (persistentProperties.containsKey(PROP_ISCREATION))
        {
            this.isCreation = Boolean.parseBoolean((String) persistentProperties.remove(PROP_ISCREATION));
        }
        else
        {
            this.isCreation = false;
        }

        List<String> tags = new ArrayList<String>();
        List<String> keys = new ArrayList<String>();
        for (Entry<String, Serializable> entry : persistentProperties.entrySet())
        {
            if (entry.getKey().startsWith(PROP_TAG))
            {
                tags.add((String) entry.getValue());
                keys.add(entry.getKey());
            }
        }

        for (String key : keys)
        {
            persistentProperties.remove(key);
        }

        Map<String, Serializable> finalProperties = new HashMap<String, Serializable>(persistentProperties);
        this.properties = finalProperties;
        this.tags = tags;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(long downloaded)
    {
        ContentValues cValues = super.createContentValues(downloaded);
        return cValues;
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        return cValues;
    }

    @Override
    public boolean isLongRunning()
    {
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends UpNodeRequest.Builder
    {
        private boolean isCreation;

        private List<String> tags;

        private Map<String, Serializable> properties;

        private String type;

        private String documentName;

        protected Builder()
        {
        }

        public Builder(Folder parentFolder, String documentName, ContentFile contentFile)
        {
            super(parentFolder, null, contentFile);
            this.documentName = documentName;
            this.isCreation = false;
            requestTypeId = TYPE_ID;
            setNotificationTitle(documentName);
        }

        public Builder(Folder parentFolder, String documentName, String type, ContentFile contentFile,
                Map<String, Serializable> properties, List<String> tags, boolean isCreation)
        {
            super(parentFolder, null, contentFile);
            this.documentName = documentName;
            this.isCreation = isCreation;
            this.tags = tags;
            this.properties = properties;
            this.type = type;
            requestTypeId = TYPE_ID;
            setNotificationTitle(documentName);
        }

        public CreateDocumentRequest build(Context context)
        {
            return new CreateDocumentRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, parentFolder, node, parentFolderIdentifier, nodeIdentifier, contentFile,
                    documentName, type, properties, tags, isCreation);
        }
    }
}
