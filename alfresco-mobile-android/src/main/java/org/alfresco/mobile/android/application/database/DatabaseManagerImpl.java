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
package org.alfresco.mobile.android.application.database;

import org.alfresco.mobile.android.accounts.AccountSchema;
import org.alfresco.mobile.android.application.providers.search.HistorySearchSchema;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.platform.database.DatabaseManager;
import org.alfresco.mobile.android.platform.favorite.FavoritesSchema;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeSchema;
import org.alfresco.mobile.android.sync.SyncContentSchema;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author Jean Marie Pascal
 */
public class DatabaseManagerImpl extends DatabaseManager
{
    public static DatabaseManagerImpl getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new DatabaseManagerImpl(context.getApplicationContext());
            }

            return (DatabaseManagerImpl) mInstance;
        }
    }

    protected DatabaseManagerImpl(Context context)
    {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        OperationSchema.onCreate(ctx, db);
        SyncContentSchema.onCreate(ctx, db);
        HistorySearchSchema.onCreate(ctx, db);
        MimeTypeSchema.onCreate(ctx, db);
        FavoritesSchema.onCreate(ctx, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        AccountSchema.onUpgrade(ctx, db, oldVersion, newVersion);
        SyncContentSchema.onUpgrade(ctx, db, oldVersion, newVersion);
        HistorySearchSchema.onUpgrade(ctx, db, oldVersion, newVersion);
        MimeTypeSchema.onUpgrade(ctx, db, oldVersion, newVersion);
        FavoritesSchema.onUpgrade(ctx, db, oldVersion, newVersion);
    }

}
