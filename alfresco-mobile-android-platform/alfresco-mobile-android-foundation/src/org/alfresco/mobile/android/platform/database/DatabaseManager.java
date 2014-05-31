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
package org.alfresco.mobile.android.platform.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Jean Marie Pascal
 */
public abstract class DatabaseManager extends SQLiteOpenHelper implements DatabaseVersionNumber
{
    protected static final String DATABASE_NAME = "AlfrescoMobileDataBase";

    public static final int DATABASE_VERSION = LATEST_VERSION;

    protected static final Object LOCK = new Object();

    protected static DatabaseManager mInstance;

    protected Context ctx;

    protected SQLiteDatabase sqliteDb;

    protected DatabaseManager(Context context)
    {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        this.ctx = context;
    }

    public SQLiteDatabase getWriteDb()
    {
        if (sqliteDb == null || !sqliteDb.isOpen())
        {
            sqliteDb = getWritableDatabase();
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
}
