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
package org.alfresco.mobile.android.async.file;

import java.io.File;

import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class FileOperationRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    public final File file;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected FileOperationRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, File file)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.file = file;
    }

    public FileOperationRequest(Cursor cursor)
    {
        super(cursor);
        String filePath = cursor.getString(OperationSchema.COLUMN_NODE_ID_ID);
        this.file = new File(filePath);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return file.getPath();
    }

    public String getPath()
    {
        if (file == null) { return null; }
        return file.getPath();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
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
        protected File file;

        protected Builder()
        {
        }

        protected Builder(File file)
        {
            this.file = file;
            setNotificationTitle(file.getName());
            setMimeType(file.getName());
        }

        public FileOperationRequest build(Context context)
        {
            return new FileOperationRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, file);
        }
    }
}
