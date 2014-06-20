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
package org.alfresco.mobile.android.application.operations.batch.file.delete;

import java.io.File;

import org.alfresco.mobile.android.application.operations.batch.file.FileOperationRequest;

import android.database.Cursor;

public class DeleteFileRequest extends FileOperationRequest
{
    private static final long serialVersionUID = 1L;
    
    public static final int TYPE_ID = 240;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public DeleteFileRequest(File file)
    {
        super(file.getPath());
        requestTypeId = TYPE_ID;
        
        setNotificationTitle(file.getName());
        setMimeType(file.getName());
    }

    public DeleteFileRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
    }
}
