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
package org.alfresco.mobile.android.async.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.OperationsContentProvider;
import org.alfresco.mobile.android.platform.provider.MapUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public abstract class BaseOperationRequest extends OperationRequest
{
    private static final long serialVersionUID = 1L;

    protected Map<String, Serializable> persistentProperties;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected BaseOperationRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        persistentProperties = new HashMap<String, Serializable>();
    }

    public BaseOperationRequest(Cursor cursor)
    {
        super(cursor);
        persistentProperties = retrievePropertiesMap(cursor);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected Uri generateNotificationUri(Context context)
    {
        return context.getContentResolver().insert(OperationsContentProvider.CONTENT_URI,
                createContentValues(Operation.STATUS_PENDING));
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = createDefaultContentValues();
        cValues.put(OperationSchema.COLUMN_NOTIFICATION_VISIBILITY, notificationVisibility);
        cValues.put(OperationSchema.COLUMN_ACCOUNT_ID, accountId);
        cValues.put(OperationSchema.COLUMN_TITLE, title);
        cValues.put(OperationSchema.COLUMN_MIMETYPE, mimeType);
        cValues.put(OperationSchema.COLUMN_REQUEST_TYPE, requestTypeId);
        cValues.put(OperationSchema.COLUMN_REASON, -1);
        cValues.put(OperationSchema.COLUMN_STATUS, status);
        if (persistentProperties != null && !persistentProperties.isEmpty())
        {
            cValues.put(OperationSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        }
        return cValues;
    }

    protected Map<String, Serializable> retrievePropertiesMap(Cursor cursor)
    {
        // PROPERTIES
        String rawProperties = cursor.getString(OperationSchema.COLUMN_PROPERTIES_ID);
        if (rawProperties != null)
        {
            return MapUtil.stringToMap(rawProperties);
        }
        else
        {
            return new HashMap<String, Serializable>();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILITY
    // ///////////////////////////////////////////////////////////////////////////
    public static ContentValues createDefaultContentValues()
    {
        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_ACCOUNT_ID, -1);
        cValues.put(OperationSchema.COLUMN_TENANT_ID, "");
        cValues.put(OperationSchema.COLUMN_STATUS, -1);
        cValues.put(OperationSchema.COLUMN_REASON, -1);
        cValues.put(OperationSchema.COLUMN_REQUEST_TYPE, -1);
        cValues.put(OperationSchema.COLUMN_TITLE, "");
        cValues.put(OperationSchema.COLUMN_NOTIFICATION_VISIBILITY, OperationRequest.VISIBILITY_HIDDEN);
        cValues.put(OperationSchema.COLUMN_NODE_ID, "");
        cValues.put(OperationSchema.COLUMN_PARENT_ID, "");
        cValues.put(OperationSchema.COLUMN_MIMETYPE, "");
        cValues.put(OperationSchema.COLUMN_PROPERTIES, "");
        cValues.put(OperationSchema.COLUMN_TOTAL_SIZE_BYTES, -1);
        cValues.put(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, -1);
        cValues.put(OperationSchema.COLUMN_LOCAL_URI, "");
        return cValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static abstract class Builder extends OperationRequest.OperationBuilder
    {
        protected Builder()
        {

        }
    }

}
