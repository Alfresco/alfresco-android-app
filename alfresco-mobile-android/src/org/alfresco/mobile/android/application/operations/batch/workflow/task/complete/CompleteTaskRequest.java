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
package org.alfresco.mobile.android.application.operations.batch.workflow.task.complete;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.TaskOperationRequest;

import android.database.Cursor;

public class CompleteTaskRequest extends TaskOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 8000;

    private Map<String, Serializable> properties;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CompleteTaskRequest(Task task, Map<String, Serializable> properties)
    {
        super(task.getIdentifier());
        requestTypeId = TYPE_ID;

        this.properties = properties;

        persistentProperties = new HashMap<String, Serializable>();
        if (properties != null)
        {
            persistentProperties.putAll(properties);
        }
    }

    public CompleteTaskRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;

        // PROPERTIES
        Map<String, String> tmpProperties = retrievePropertiesMap(cursor);
        Map<String, Serializable> finalProperties = new HashMap<String, Serializable>(tmpProperties);
        this.properties = finalProperties;

    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }
}
