/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.sync;

import java.io.File;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class PrepareFavoriteHelper extends PrepareBaseHelper
{
    private static final String TAG = PrepareFavoriteHelper.class.getName();

    public PrepareFavoriteHelper(Context context, SyncPrepareThread syncScanThread, long syncScanningTimeStamp)
    {
        super(context, syncScanThread, syncScanningTimeStamp);
    }

    @Override
    public OperationsRequestGroup prepare()
    {
        scan();
        return null;
    }
    
    protected void prepareCreation(Uri localUri, Document doc)
    {
        // If listing mode, update Metadata associated
        ContentValues cValues = new ContentValues();
        cValues.put(SynchroSchema.COLUMN_NODE_ID, doc.getIdentifier());
        cValues.put(SynchroSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, doc.getModifiedAt().getTimeInMillis());
        context.getContentResolver().update(localUri, cValues, null, null);
        return;
    }

    protected void rename(Document doc, File localFile, Uri localUri)
    {
        // If Favorite listing simply rename the entry.
        ContentValues cValues = new ContentValues();
        cValues.put(BatchOperationSchema.COLUMN_TITLE, doc.getName());
        context.getContentResolver().update(localUri, cValues, null, null);
    }

    protected void prepareUpdate(Document doc, Cursor cursorId, File localFile, Uri localUri)
    {
        return;
    }

    protected void prepareDelete(String id, Cursor cursorId)
    {
        // If Favorite listing simply delete the entry.
        context.getContentResolver().delete(SynchroManager.getUri(cursorId.getLong(SynchroSchema.COLUMN_ID_ID)), null,
                null);
        return;
    }
}
