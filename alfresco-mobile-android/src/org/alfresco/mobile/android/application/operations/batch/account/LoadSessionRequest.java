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
package org.alfresco.mobile.android.application.operations.batch.account;

import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;

import android.content.ContentValues;
import android.database.Cursor;

public class LoadSessionRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 100;
    
    public static final String SESSION_MIME = "AlfrescoSession";
    
    private OAuthData data;
    
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public LoadSessionRequest()
    {
        super();
        requestTypeId = TYPE_ID;
        
        setMimeType(SESSION_MIME);
    }
    
    public LoadSessionRequest(OAuthData data)
    {
        this();
        this.data = data;
    }
    
    public LoadSessionRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public OAuthData getData()
    {
        return data;
    }
    
    @Override
    public String getRequestIdentifier()
    {
        return getAccountId() + "";
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        return cValues;
    }
}
