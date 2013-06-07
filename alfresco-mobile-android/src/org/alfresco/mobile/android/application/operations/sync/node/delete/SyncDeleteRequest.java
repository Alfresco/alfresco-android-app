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
package org.alfresco.mobile.android.application.operations.sync.node.delete;

import org.alfresco.mobile.android.application.operations.sync.node.SyncNodeOperationRequest;

import android.database.Cursor;
import android.net.Uri;

public class SyncDeleteRequest extends SyncNodeOperationRequest
{
    private static final long serialVersionUID = 1L;
    
    public static final int TYPE_ID = 240;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncDeleteRequest(String nodeIdentifier, String nodeName, Uri uri)
    {
        super(null, nodeIdentifier);
        requestTypeId = TYPE_ID;
        
        setNotificationTitle(nodeName);
        setMimeType(nodeName);
        setNotificationUri(uri);
    }

    public SyncDeleteRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
    }
}
