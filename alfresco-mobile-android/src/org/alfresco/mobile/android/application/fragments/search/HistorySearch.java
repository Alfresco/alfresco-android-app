/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.search;

import java.io.Serializable;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public class HistorySearch implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_DOCUMENT = 1;

    public static final int TYPE_FOLDER = 2;

    public static final int TYPE_PERSON = 4;

    private long id;

    private long accountId;

    private int type;
    
    private int advanced;

    private String description;

    private String query;

    private long timestamp;

    public HistorySearch(long id, long accountId, int type, int advanced, String description, String query, long timestamp)
    {
        super();
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.advanced = advanced;
        this.description = description;
        this.query = query;
        this.timestamp = timestamp;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(long accountId)
    {
        this.accountId = accountId;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public int getAdvanced()
    {
        return advanced;
    }

    public void setAdvanced(int advanced)
    {
        this.advanced = advanced;
    }
    
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    
}
