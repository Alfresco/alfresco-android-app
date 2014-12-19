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
package org.alfresco.mobile.android.async.file.browse;

import java.io.File;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.impl.ListingOperationRequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class FilesRequest extends ListingOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_FILE_LIST;

    final String filePath;

    final File file;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected FilesRequest(Context context, long accountId, String networkId, int notificationVisibility, String title,
            String mimeType, int requestTypeId, ListingContext listingContext, File file, String filePath)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, listingContext);
        this.file = file;
        this.filePath = filePath;
    }

    public FilesRequest(Cursor cursor)
    {
        super(cursor);
        this.filePath = cursor.getString(OperationSchema.COLUMN_NODE_ID_ID);
        this.file = new File(filePath);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        if (file != null)
        {
            cValues.put(OperationSchema.COLUMN_NODE_ID, file.getPath());
        }
        return cValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends ListingOperationRequest.Builder
    {
        private String filePath;

        private File file;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(File file)
        {
            this();
            this.file = file;
            setNotificationTitle(file.getName());
            setMimeType(file.getName());
        }

        public Builder(String filePath)
        {
            this();
            this.filePath = filePath;
        }

        public FilesRequest build(Context context)
        {
            return new FilesRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, listingContext, file, filePath);
        }
    }

}
