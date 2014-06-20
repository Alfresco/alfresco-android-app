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
package org.alfresco.mobile.android.application.operations.sync.node.update;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.operations.sync.node.AbstractSyncUpRequest;

import android.database.Cursor;

public class SyncUpdateRequest extends AbstractSyncUpRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 30;

    private boolean doRemove = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncUpdateRequest(Folder parentFolder, Document document, ContentFile contentFile)
    {
        super(parentFolder.getIdentifier(), document.getIdentifier(), document.getName(), contentFile.getFile()
                .getPath(), contentFile.getMimeType(), contentFile.getLength());
        requestTypeId = TYPE_ID;
    }

    public SyncUpdateRequest(String parentIdentifier, Document document, ContentFile contentFile)
    {
        super(parentIdentifier, document.getIdentifier(), document.getName(), contentFile.getFile().getPath(),
                contentFile.getMimeType(), contentFile.getLength());
        requestTypeId = TYPE_ID;
    }

    public SyncUpdateRequest(String parentIdentifier, String nodeIdentifier, String documentName,
            ContentFile contentFile, boolean remove)
    {
        super(parentIdentifier, nodeIdentifier, documentName, contentFile.getFile().getPath(), contentFile
                .getMimeType(), contentFile.getLength());
        requestTypeId = TYPE_ID;
        doRemove = remove;
    }

    public SyncUpdateRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getRequestIdentifier()
    {
        return nodeIdentifier;
    }

    public boolean doRemove()
    {
        return doRemove;
    }
}
