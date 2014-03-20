/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroProvider;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.utils.IOUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class CleanSyncFavoriteThread extends AbstractBatchOperationThread<Void>
{
    private static final String TAG = CleanSyncFavoriteThread.class.getName();

    private String accountUsername;

    private String accountUrl;

    private Boolean isDeletion = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CleanSyncFavoriteThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof CleanSyncFavoriteRequest)
        {
            this.accountId = ((CleanSyncFavoriteRequest) request).getAccountId();
            this.accountUrl = ((CleanSyncFavoriteRequest) request).getAccountUrl();
            this.accountUsername = ((CleanSyncFavoriteRequest) request).getAccountUsername();
            this.isDeletion = ((CleanSyncFavoriteRequest) request).isDeletion();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Void> doInBackground()
    {
        LoaderResult<Void> result = new LoaderResult<Void>();
        try
        {
            // Delete All local files in sync folder
            File synchroFolder = StorageManager.getSynchroFolder(context, accountUsername, accountUrl);
            if (synchroFolder != null && synchroFolder.exists())
            {
                IOUtils.deleteContents(synchroFolder);
            }

            // For each sync row, reset status
            Cursor allFavoritesCursor = context.getContentResolver().query(SynchroProvider.CONTENT_URI,
                    SynchroSchema.COLUMN_ALL, SynchroProvider.getAccountFilter(accountId), null, null);
            while (allFavoritesCursor.moveToNext())
            {
                if (isDeletion)
                {
                    // Update Sync Info
                    context.getContentResolver().delete(SynchroManager.getUri(allFavoritesCursor.getLong(SynchroSchema.COLUMN_ID_ID)), null, null);
                }
                else
                {
                    // Update Sync Info
                    ContentValues cValues = new ContentValues();
                    cValues.put(BatchOperationSchema.COLUMN_STATUS, SyncOperation.STATUS_PENDING);
                    context.getContentResolver().update(SynchroManager.getUri(allFavoritesCursor.getLong(SynchroSchema.COLUMN_ID_ID)), cValues, null, null);
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        return result;
    }

}
