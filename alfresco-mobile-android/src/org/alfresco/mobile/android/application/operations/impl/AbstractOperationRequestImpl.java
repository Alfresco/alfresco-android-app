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
package org.alfresco.mobile.android.application.operations.impl;

import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public abstract class AbstractOperationRequestImpl implements OperationRequest
{
    private static final long serialVersionUID = 1L;

    private long accountId;
    
    private String networkId;

    private int notificationVisibility = VISIBILITY_NOTIFICATIONS;

    private String title;

    private String description;

    private String mimeType;

    protected int requestTypeId;

    private Uri notificationUri;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AbstractOperationRequestImpl()
    {
    }
    
    public AbstractOperationRequestImpl(Cursor cursor){
        this.accountId = cursor.getLong(BatchOperationSchema.COLUMN_ACCOUNT_ID_ID);
        this.notificationVisibility = cursor.getInt(BatchOperationSchema.COLUMN_NOTIFICATION_VISIBILITY_ID);
        this.title = cursor.getString(BatchOperationSchema.COLUMN_TITLE_ID);
        this.mimeType = cursor.getString(BatchOperationSchema.COLUMN_MIMETYPE_ID);
        this.requestTypeId = cursor.getInt(BatchOperationSchema.COLUMN_REQUEST_TYPE_ID);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTER / SETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public abstract String getRequestIdentifier();

    public int getNotificationVisibility()
    {
        return notificationVisibility;
    }

    public String getNotificationTitle()
    {
        return title;
    }

    public String getNotificationDescription()
    {
        return description;
    }

    public OperationRequest setNotificationVisibility(int visibility)
    {
        this.notificationVisibility = visibility;
        return this;
    }

    public OperationRequest setNotificationTitle(String title)
    {
        this.title = title;
        return this;
    }

    public OperationRequest setNotificationDescription(String description)
    {
        this.description = description;
        return this;
    }

    public long getAccountId()
    {
        return accountId;
    }

    public OperationRequest setAccountId(long accountId)
    {
        this.accountId = accountId;
        return this;
    }
    
    public String getNetworkId()
    {
        return networkId;
    }

    public OperationRequest setNetworkId(String networkId)
    {
        this.networkId = networkId;
        return this;
    }


    public int getTypeId()
    {
        return requestTypeId;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    //TODO MimetypeManager Content Resolver !
    //We user filename for the moment instead of real mimetype
    public OperationRequest setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
        return this;
    }

    public Uri getNotificationUri()
    {
        return notificationUri;
    }
    
    public OperationRequest setNotificationUri(Uri notificationUri)
    {
        this.notificationUri = notificationUri;
        return this;
    }

    public abstract ContentValues createContentValues(int status);
}
