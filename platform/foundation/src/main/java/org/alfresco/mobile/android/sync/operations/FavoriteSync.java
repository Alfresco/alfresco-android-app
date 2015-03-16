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
package org.alfresco.mobile.android.sync.operations;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;

public class FavoriteSync
{
    protected final Context context;

    protected final AlfrescoSession session;

    protected final AlfrescoAccount acc;

    protected final SyncResult syncResult;

    protected final Uri syncUri;

    public FavoriteSync(Context context, AlfrescoAccount acc, AlfrescoSession session, SyncResult syncResult,
            Uri syncUri)
    {
        this.context = context;
        this.acc = acc;
        this.session = session;
        this.syncResult = syncResult;
        this.syncUri = syncUri;
    }

    protected void requestUserInteraction(Uri localUri, int reasonId)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_STATUS, FavoriteSyncStatus.STATUS_REQUEST_USER);
        cValues.put(OperationSchema.COLUMN_REASON, reasonId);
        context.getContentResolver().update(localUri, cValues, null, null);
    }

    public void saveStatus(int status)
    {
        if (syncUri != null)
        {
            ContentValues cValues = new ContentValues();
            cValues.put(OperationSchema.COLUMN_STATUS, status);
            context.getContentResolver().update(syncUri, cValues, null, null);
        }
    }

    public static void saveStatus(Context context, Uri syncUri, int status)
    {
        if (syncUri != null)
        {
            ContentValues cValues = new ContentValues();
            cValues.put(OperationSchema.COLUMN_STATUS, status);
            context.getContentResolver().update(syncUri, cValues, null, null);
        }
    }

    public void execute()
    {
        saveStatus(FavoriteSyncStatus.STATUS_RUNNING);
    }
}
