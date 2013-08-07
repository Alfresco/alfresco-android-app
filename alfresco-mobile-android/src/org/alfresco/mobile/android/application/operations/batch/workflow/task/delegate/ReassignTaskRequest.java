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
package org.alfresco.mobile.android.application.operations.batch.workflow.task.delegate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.TaskOperationRequest;

import android.database.Cursor;

public class ReassignTaskRequest extends TaskOperationRequest
{
    private static final long serialVersionUID = 1L;

    private static final String PROP_PERSONID = "personId";

    private static final String PROP_ISCLAIMED = "isClaimed";

    public static final int TYPE_ID = 8010;

    private Person assignee;

    private String assigneeId;

    private Boolean isClaimed;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public ReassignTaskRequest(Task task, Person p)
    {
        super(task.getIdentifier());
        requestTypeId = TYPE_ID;

        this.assignee = p;
        this.assigneeId = p.getIdentifier();

        persistentProperties = new HashMap<String, Serializable>();
        persistentProperties.put(PROP_PERSONID, assigneeId);
    }

    public ReassignTaskRequest(Task task, String personIdentifier, boolean isClaimed)
    {
        super(task.getIdentifier());
        requestTypeId = TYPE_ID;

        this.assigneeId = personIdentifier;
        this.isClaimed = isClaimed;
        
        persistentProperties = new HashMap<String, Serializable>();
        persistentProperties.put(PROP_PERSONID, assigneeId);
        persistentProperties.put(PROP_ISCLAIMED, isClaimed);
    }

    public ReassignTaskRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;

        // PROPERTIES
        Map<String, String> tmpProperties = retrievePropertiesMap(cursor);
        this.assigneeId = tmpProperties.get(PROP_PERSONID);
        if (tmpProperties.containsKey(PROP_ISCLAIMED))
        {
            this.isClaimed = Boolean.parseBoolean(tmpProperties.get(PROP_ISCLAIMED));
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Person getAssignee()
    {
        return assignee;
    }

    public String getAssigneeId()
    {
        return assigneeId;
    }
    
    public Boolean getIsClaimed()
    {
        return isClaimed;
    }

}
