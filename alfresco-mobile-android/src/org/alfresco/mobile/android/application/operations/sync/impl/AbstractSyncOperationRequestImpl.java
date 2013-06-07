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
package org.alfresco.mobile.android.application.operations.sync.impl;

import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.impl.AbstractOperationRequestImpl;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class AbstractSyncOperationRequestImpl extends AbstractOperationRequestImpl
{

    private static final long serialVersionUID = 1L;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AbstractSyncOperationRequestImpl()
    {
        super();
    }
    
    public AbstractSyncOperationRequestImpl(Cursor cursor){
        super(cursor);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = new ContentValues();

        cValues.put(BatchOperationSchema.COLUMN_NOTIFICATION_VISIBILITY, getNotificationVisibility());
        cValues.put(BatchOperationSchema.COLUMN_ACCOUNT_ID, getAccountId());
        cValues.put(BatchOperationSchema.COLUMN_TITLE, getNotificationTitle());
        cValues.put(BatchOperationSchema.COLUMN_MIMETYPE, getMimeType());
        cValues.put(BatchOperationSchema.COLUMN_REQUEST_TYPE, getTypeId());
        cValues.put(BatchOperationSchema.COLUMN_REASON, -1);
        cValues.put(BatchOperationSchema.COLUMN_STATUS, status);
        return cValues;
    }
}
