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
package org.alfresco.mobile.android.platform.favorite;

import org.alfresco.mobile.android.async.OperationsSchema;
import org.alfresco.mobile.android.platform.database.DatabaseVersionNumber;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public final class FavoritesSchema extends OperationsSchema
{

    private FavoritesSchema()
    {
    }

    // ////////////////////////////////////////////////////
    // TABLENAME
    // ////////////////////////////////////////////////////
    public static final String TABLENAME = "favorites";

    // ////////////////////////////////////////////////////
    // EXTRA COLUMNS
    // ////////////////////////////////////////////////////
    public static final String COLUMN_CONTENT_URI = "content_uri";

    public static final int COLUMN_CONTENT_URI_ID = COLUMN_LOCAL_URI_ID + 1;

    // SYNC DATE
    public static final String COLUMN_ANALYZE_TIMESTAMP = "analyze_timestamp";

    public static final int COLUMN_ANALYZE_TIMESTAMP_ID = COLUMN_CONTENT_URI_ID + 1;

    public static final String COLUMN_SERVER_MODIFICATION_TIMESTAMP = "server_modification_timestamp";

    public static final int COLUMN_SERVER_MODIFICATION_TIMESTAMP_ID = COLUMN_ANALYZE_TIMESTAMP_ID + 1;

    public static final String COLUMN_LOCAL_MODIFICATION_TIMESTAMP = "local_modification_timestamp";

    public static final int COLUMN_LOCAL_MODIFICATION_TIMESTAMP_ID = COLUMN_SERVER_MODIFICATION_TIMESTAMP_ID + 1;

    // ////////////////////////////////////////////////////
    // SYNC FOLDER
    // ////////////////////////////////////////////////////
    public static final String COLUMN_IS_FAVORITE = "favorited";

    public static final int COLUMN_IS_FAVORITE_ID = COLUMN_LOCAL_MODIFICATION_TIMESTAMP_ID + 1;

    public static final String COLUMN_IS_ROOT = "root";

    public static final int COLUMN_IS_ROOT_ID = COLUMN_IS_FAVORITE_ID + 1;

    public static final String COLUMN_DOC_SIZE_BYTES = "document_size";

    public static final int COLUMN_DOC_SIZE_BYTES_ID = COLUMN_IS_ROOT_ID + 1;

    private static final String[] COLUMNS_SYNC = { COLUMN_CONTENT_URI, COLUMN_ANALYZE_TIMESTAMP,
            COLUMN_SERVER_MODIFICATION_TIMESTAMP, COLUMN_LOCAL_MODIFICATION_TIMESTAMP, COLUMN_IS_FAVORITE,
            COLUMN_IS_ROOT, COLUMN_DOC_SIZE_BYTES };

    public static final String[] COLUMN_ALL = join(COLUMNS, COLUMNS_SYNC);

    // ////////////////////////////////////////////////////
    // QUERIES
    // ////////////////////////////////////////////////////
    private static final String QUERY_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLENAME + " ("
            + QUERY_CREATE_COLUMNS + "," + COLUMN_CONTENT_URI + " TEXT," + COLUMN_ANALYZE_TIMESTAMP + " LONG,"
            + COLUMN_SERVER_MODIFICATION_TIMESTAMP + " LONG," + COLUMN_LOCAL_MODIFICATION_TIMESTAMP + " LONG,"
            + COLUMN_IS_FAVORITE + " INT," + COLUMN_IS_ROOT + " INT," + COLUMN_DOC_SIZE_BYTES + " LONG" + ");";

    // ////////////////////////////////////////////////////
    // LIFECYCLE
    // ////////////////////////////////////////////////////
    public static void onCreate(Context context, SQLiteDatabase db)
    {
        db.execSQL(QUERY_TABLE_CREATE);
    }

    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion < DatabaseVersionNumber.VERSION_1_6_0)
        {
            db.execSQL(QUERY_TABLE_CREATE);
        }
    }

    // ////////////////////////////////////////////////////
    // DEBUG
    // ////////////////////////////////////////////////////
    private static final String QUERY_TABLE_DROP = "DROP TABLE IF EXISTS " + TABLENAME;

    /** Use with caution ! */
    public static void reset(SQLiteDatabase db)
    {
        db.execSQL(QUERY_TABLE_DROP);
        db.execSQL(QUERY_TABLE_CREATE);
    }
}
