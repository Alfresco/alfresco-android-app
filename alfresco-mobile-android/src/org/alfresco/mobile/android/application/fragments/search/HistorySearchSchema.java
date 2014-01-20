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
package org.alfresco.mobile.android.application.fragments.search;

import org.alfresco.mobile.android.application.database.DatabaseVersionNumber;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public final class HistorySearchSchema
{
    
    private HistorySearchSchema(){
    }

    public static final String TABLENAME = "search_history";
    
    public static final String COLUMN_ID = "_id";

    public static final int COLUMN_ID_ID = 0;

    public static final String COLUMN_ACCOUNT_ID = "accountId";

    public static final int COLUMN_ACCOUNT_ID_ID = 1;

    public static final String COLUMN_TYPE = "type";

    public static final int COLUMN_TYPE_ID = COLUMN_ACCOUNT_ID_ID + 1;
    
    public static final String COLUMN_ADVANCED = "advanced";

    public static final int COLUMN_ADVANCED_ID =  COLUMN_TYPE_ID + 1;

    public static final String COLUMN_DESCRIPTION = "description";

    public static final int COLUMN_DESCRIPTION_ID =  COLUMN_ADVANCED_ID + 1;

    public static final String COLUMN_QUERY = "query";

    public static final int COLUMN_QUERY_ID =  COLUMN_DESCRIPTION_ID + 1;

    public static final String COLUMN_LAST_REQUEST_TIMESTAMP = "lastRequestTimestamp";

    public static final int COLUMN_LAST_REQUEST_TIMESTAMP_ID =  COLUMN_QUERY_ID + 1;

    public static final String[] COLUMN_ALL = { 
        COLUMN_ID, 
        COLUMN_ACCOUNT_ID,
        COLUMN_TYPE,
        COLUMN_ADVANCED,
        COLUMN_DESCRIPTION,
        COLUMN_QUERY,
        COLUMN_LAST_REQUEST_TIMESTAMP
        };

    private static final String QUERY_TABLE_CREATE = "create table " + TABLENAME + " (" 
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
            + COLUMN_ACCOUNT_ID + " INTEGER NOT NULL," 
            + COLUMN_ADVANCED + " INTEGER NOT NULL,"
            + COLUMN_TYPE + " INTEGER NOT NULL,"
            + COLUMN_DESCRIPTION + " TEXT NOT NULL," 
            + COLUMN_QUERY + " TEXT," 
            + COLUMN_LAST_REQUEST_TIMESTAMP + " LONG NOT NULL" 
            + ");";
    
    public static void onCreate(Context context, SQLiteDatabase db)
    {
        db.execSQL(QUERY_TABLE_CREATE);
    }

    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion <= DatabaseVersionNumber.VERSION_1_4_0)
        {
            db.execSQL(QUERY_TABLE_CREATE);
        }
    }
}
