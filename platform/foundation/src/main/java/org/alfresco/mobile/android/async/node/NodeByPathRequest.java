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
package org.alfresco.mobile.android.async.node;

import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class NodeByPathRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    protected String path;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected NodeByPathRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, String path)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.path = path;
        context.getContentResolver().update(notificationUri, createContentValues(Operation.STATUS_PENDING), null, null);
    }

    public NodeByPathRequest(Cursor cursor)
    {
        super(cursor);
        this.path = cursor.getString(OperationSchema.COLUMN_NODE_ID_ID);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getPath()
    {
        return path;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return path;
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_NODE_ID, getPath());
        return cValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        protected String path;

        protected Builder()
        {
            setNotificationTitle(path);
            setMimeType(null);
        }

        public Builder(String path)
        {
            this();
            this.path = path;
        }

        public NodeByPathRequest build(Context context)
        {
            return new NodeByPathRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, path);
        }
    }
}
