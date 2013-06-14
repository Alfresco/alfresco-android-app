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
package org.alfresco.mobile.android.application.operations.batch;

import org.alfresco.mobile.android.application.operations.OperationSchema;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public final class BatchOperationSchema extends OperationSchema
{

    private BatchOperationSchema()
    {
    }

    // ////////////////////////////////////////////////////
    // TABLENAME
    // ////////////////////////////////////////////////////
    public static final String TABLENAME = "Operation";
    
    // ////////////////////////////////////////////////////
    // EXTRA COLUMNS
    // ////////////////////////////////////////////////////
    public static final String[] COLUMN_ALL = COLUMNS;

    // ////////////////////////////////////////////////////
    // QUERIES
    // ////////////////////////////////////////////////////
    private static final String QUERY_TABLE_CREATE = "CREATE TABLE " + TABLENAME + " (" + QUERY_CREATE_COLUMNS + ");";

    private static final String QUERY_TABLE_DROP = "DROP TABLE IF EXISTS " + TABLENAME;

    // ////////////////////////////////////////////////////
    // LIFECYCLE
    // ////////////////////////////////////////////////////
    public static void onCreate(Context context, SQLiteDatabase db)
    {
        db.execSQL(QUERY_TABLE_CREATE);
    }

    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion <= 3)
        {
            db.execSQL(QUERY_TABLE_CREATE);
        }
    }

    // TODO REMOVE BEFORE RELEASE
    public static void reset(SQLiteDatabase db)
    {
        db.execSQL(QUERY_TABLE_DROP);
        db.execSQL(QUERY_TABLE_CREATE);
    }
}
