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
package org.alfresco.mobile.android.application.operations.batch.node.like;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.operations.batch.node.NodeOperationRequest;

import android.database.Cursor;

public class LikeNodeRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 50;

    private Boolean like;
    
    private static final String PROP_LIKE = "like";
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public LikeNodeRequest(String parentIdentifier, String documentIdentifier)
    {
        super(parentIdentifier, documentIdentifier);
        requestTypeId = TYPE_ID;
    }

    public LikeNodeRequest(Folder parent, Node node)
    {
        this(parent.getIdentifier(), node.getIdentifier());
        setNotificationTitle(node.getName());
        setMimeType(node.getName());
    }

    public LikeNodeRequest(String parentIdentifier, String documentIdentifier, boolean doLike)
    {
        this(parentIdentifier, documentIdentifier);
        this.like = doLike;
        
        persistentProperties = new HashMap<String, Serializable>();
        persistentProperties.put(PROP_LIKE, like);
    }

    public LikeNodeRequest(Folder parent, Node node, boolean doLike)
    {
        this(parent.getIdentifier(), node.getIdentifier(), doLike);
        setNotificationTitle(node.getName());
        setMimeType(node.getName());
    }
    
    public LikeNodeRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;

        Map<String, String> tmpProperties = retrievePropertiesMap(cursor);
        if (tmpProperties.containsKey(PROP_LIKE))
        {
            this.like = Boolean.parseBoolean(tmpProperties.remove(PROP_LIKE));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Boolean getLikeOperation()
    {
        return like;
    }
}
