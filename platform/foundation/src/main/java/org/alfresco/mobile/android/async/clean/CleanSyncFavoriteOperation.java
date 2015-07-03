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
package org.alfresco.mobile.android.async.clean;

import java.io.File;

import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.io.IOUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentProvider;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class CleanSyncFavoriteOperation extends BaseOperation<Void>
{
    private static final String TAG = CleanSyncFavoriteOperation.class.getName();

    private String accountUsername;

    private String accountUrl;

    private Boolean isDeletion = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CleanSyncFavoriteOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof CleanSyncFavoriteRequest)
        {
            this.accountId = ((CleanSyncFavoriteRequest) request).account.getId();
            this.accountUrl = ((CleanSyncFavoriteRequest) request).account.getUrl();
            this.accountUsername = ((CleanSyncFavoriteRequest) request).account.getUsername();
            this.isDeletion = ((CleanSyncFavoriteRequest) request).deleteContent;
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
            File synchroFolder = SyncContentManager.getInstance(context)
                    .getSynchroFolder(accountUsername, accountUrl);
            if (synchroFolder != null && synchroFolder.exists())
            {
                IOUtils.deleteContents(synchroFolder);
            }

            // For each sync row, reset status
            Cursor allFavoritesCursor = context.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                    SyncContentSchema.COLUMN_ALL, SyncContentProvider.getAccountFilter(accountId), null, null);
            while (allFavoritesCursor.moveToNext())
            {
                if (isDeletion)
                {
                    // Update Sync Info
                    context.getContentResolver().delete(
                            SyncContentManager.getUri(allFavoritesCursor.getLong(SyncContentSchema.COLUMN_ID_ID)),
                            null, null);
                }
                else
                {
                    // Update Sync Info
                    ContentValues cValues = new ContentValues();
                    cValues.put(OperationSchema.COLUMN_STATUS, SyncContentStatus.STATUS_PENDING);
                    context.getContentResolver().update(
                            SyncContentManager.getUri(allFavoritesCursor.getLong(SyncContentSchema.COLUMN_ID_ID)),
                            cValues, null, null);
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

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Void> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new CleanSyncFavoriteEvent());
    }

}
