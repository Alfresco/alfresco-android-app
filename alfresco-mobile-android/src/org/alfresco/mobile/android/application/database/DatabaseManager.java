/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.database;

import org.alfresco.mobile.android.application.accounts.AccountSchema;
import org.alfresco.mobile.android.application.integration.OperationSchema;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager
{
    private static final String DATABASE_NAME = "AlfrescoMobileDataBase";

    public static final int DATABASE_VERSION = 4;

    private final GenericDbHelper dbHelper;

    private SQLiteDatabase sqliteDb;

    public DatabaseManager(Context context)
    {
        dbHelper = new GenericDbHelper(context);
    }

    public static DatabaseManager newInstance(Context context)
    {
        return new DatabaseManager(context);
    }

    public SQLiteDatabase getWriteDb()
    {
        if (sqliteDb == null || !sqliteDb.isOpen())
        {
            sqliteDb = dbHelper.getWritableDatabase();
        }
        while (sqliteDb.isDbLockedByCurrentThread() || sqliteDb.isDbLockedByOtherThreads())
        {
            // db is locked, keep looping
        }
        return sqliteDb;
    }

    public void close()
    {
        if (sqliteDb != null)
        {
            sqliteDb.close();
        }
    }

    private static class GenericDbHelper extends SQLiteOpenHelper
    {
        private Context ctx;

        GenericDbHelper(Context ctx)
        {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
            this.ctx = ctx;
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            AccountSchema.onCreate(ctx, db);
            OperationSchema.onCreate(ctx, db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            AccountSchema.onUpgrade(ctx, db, oldVersion, newVersion);
            OperationSchema.onUpgrade(ctx, db, oldVersion, newVersion);
        }
    }

}
