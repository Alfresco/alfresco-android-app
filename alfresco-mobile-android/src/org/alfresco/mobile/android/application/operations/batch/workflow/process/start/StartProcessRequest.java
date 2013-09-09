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
package org.alfresco.mobile.android.application.operations.batch.workflow.process.start;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;

import android.content.ContentValues;
import android.database.Cursor;

public class StartProcessRequest extends AbstractBatchOperationRequestImpl
{
    public static final int TYPE_ID = 8100;

    private Map<String, Serializable> properties;

    private static final long serialVersionUID = 1L;

    protected Map<String, Serializable> persistentProperties;
    protected ProcessDefinition processDefinition;
    protected List<Person> assignees;
    protected List<Document> items;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public StartProcessRequest(ProcessDefinition processDefinition, List<Person> assignees, Map<String, Serializable> variables, List<Document> items)
    {
        this.processDefinition = processDefinition;
        this.assignees = assignees;
        this.properties = variables;
        this.items = items;
        requestTypeId = TYPE_ID;
    }

    public StartProcessRequest(Cursor cursor)
    {
        super(cursor);
        //TODO IMPLEMENT !
    }
    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

    public ProcessDefinition getProcessDefinition()
    {
        return processDefinition;
    }

    public List<Person> getAssignees()
    {
        return assignees;
    }

    public List<Document> getItems()
    {
        return items;
    }
    
    @Override
    public String getRequestIdentifier()
    {
        return processDefinition.getIdentifier();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        //TODO IMPLEMENT !
        ContentValues cValues = super.createContentValues(status);
        return cValues;
    }
    
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
