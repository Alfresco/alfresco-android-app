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
package org.alfresco.mobile.android.async.file.create;

import java.io.File;

import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.file.FileOperationRequest;

import android.content.Context;
import android.database.Cursor;

public class CreateDirectoryRequest extends FileOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_FILE_CREATE_DIRECTORY;

    private static final String PROP_NAME = "folderName";

    final String folderName;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected CreateDirectoryRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, File file, String folderName)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, file);
        this.folderName = folderName;

        // Save extra info
        persistentProperties.put(PROP_NAME, folderName);
    }

    public CreateDirectoryRequest(Cursor cursor)
    {
        super(cursor);
        this.folderName = (String) persistentProperties.remove(PROP_NAME);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends FileOperationRequest.Builder
    {
        protected String folderName;

        protected Builder()
        {
        }

        public Builder(File parentFolder, String folderName)
        {
            super(parentFolder);
            this.folderName = folderName;
            requestTypeId = TYPE_ID;
        }

        public CreateDirectoryRequest build(Context context)
        {
            return new CreateDirectoryRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, file, folderName);
        }
    }
}
