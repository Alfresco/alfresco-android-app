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
package org.alfresco.mobile.android.application.integration.sync;

import org.alfresco.mobile.android.application.integration.OperationSchema;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public final class SyncSchema
{

    private SyncSchema()
    {
    }

    //ID
    public static final String TABLENAME = "synchro";

    public static final String COLUMN_ID = "_id";

    public static final int COLUMN_ID_ID = 0;

    public static final String COLUMN_ACCOUNT_ID = "account_identifier";

    public static final int COLUMN_ACCOUNT_ID_ID = 1;
    
    public static final String COLUMN_TENANT_ID = "tenant_identifier";

    public static final int COLUMN_TENANT_ID_ID = COLUMN_ACCOUNT_ID_ID + 1;
    
    //STATUS
    public static final String COLUMN_STATUS = "status";

    public static final int COLUMN_STATUS_ID = COLUMN_TENANT_ID_ID + 1;
    
    public static final String COLUMN_REASON = "reason";

    public static final int COLUMN_REASON_ID = COLUMN_STATUS_ID + 1;
    
    //REQUEST TYPE
    public static final String COLUMN_REQUEST_TYPE = "request_type";

    public static final int COLUMN_REQUEST_TYPE_ID = COLUMN_REASON_ID + 1;

    //NODE REQUEST
    public static final String COLUMN_NODE_ID = "node_identifier";

    public static final int COLUMN_NODE_ID_ID = COLUMN_REQUEST_TYPE_ID + 1;
    
    public static final String COLUMN_PARENT_ID = "parent_identifier";

    public static final int COLUMN_PARENT_ID_ID = COLUMN_NODE_ID_ID + 1;

    public static final String COLUMN_MIMETYPE = "mime_type";

    public static final int COLUMN_MIMETYPE_ID = COLUMN_PARENT_ID_ID + 1;
    
    public static final String COLUMN_PROPERTIES = "properties";

    public static final int COLUMN_PROPERTIES_ID = COLUMN_MIMETYPE_ID + 1;

    //UPx REQUEST
    public static final String COLUMN_TOTAL_SIZE_BYTES = "total_size";

    public static final int COLUMN_TOTAL_SIZE_BYTES_ID = COLUMN_PROPERTIES_ID + 1;

    public static final String COLUMN_BYTES_DOWNLOADED_SO_FAR = "bytes_so_far";

    public static final int COLUMN_BYTES_DOWNLOADED_SO_FAR_ID = COLUMN_TOTAL_SIZE_BYTES_ID + 1;
    
    public static final String COLUMN_LOCAL_URI = "local_uri";

    public static final int COLUMN_LOCAL_URI_ID = COLUMN_BYTES_DOWNLOADED_SO_FAR_ID + 1;
  

    public static String[] COLUMN_ALL = { 
            COLUMN_ID, 
            COLUMN_ACCOUNT_ID,
            COLUMN_TENANT_ID,
            COLUMN_STATUS,
            COLUMN_REASON,
            COLUMN_REQUEST_TYPE,
            COLUMN_NODE_ID, 
            COLUMN_PARENT_ID,
            COLUMN_MIMETYPE,
            COLUMN_PROPERTIES,
            COLUMN_TOTAL_SIZE_BYTES, 
            COLUMN_BYTES_DOWNLOADED_SO_FAR, 
            COLUMN_LOCAL_URI 
            };

    private static final String QUERY_TABLE_CREATE = "create table " + TABLENAME + " (" 
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
            + COLUMN_ACCOUNT_ID + " LONG," 
            + COLUMN_TENANT_ID + " TEXT,"
            + COLUMN_STATUS + " INTEGER," 
            + COLUMN_REASON + " INTEGER,"
            + COLUMN_REQUEST_TYPE + " INTEGER,"
            + COLUMN_NODE_ID + " TEXT," 
            + COLUMN_PARENT_ID + " TEXT," 
            + COLUMN_MIMETYPE + " TEXT NOT NULL,"
            + COLUMN_PROPERTIES + " TEXT,"
            + COLUMN_TOTAL_SIZE_BYTES + " LONG,"
            + COLUMN_BYTES_DOWNLOADED_SO_FAR + " LONG," 
            + COLUMN_LOCAL_URI + " TEXT);";

    private static final String QUERY_TABLE_DROP = "DROP TABLE IF EXISTS " + TABLENAME;

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

    public static ContentValues createDefaultContentValues()
    {
        ContentValues cValues = new ContentValues();
        cValues.put(SyncSchema.COLUMN_ACCOUNT_ID, -1);
        cValues.put(OperationSchema.COLUMN_TENANT_ID, "");
        cValues.put(SyncSchema.COLUMN_STATUS, -1);
        cValues.put(SyncSchema.COLUMN_REASON, -1);
        cValues.put(SyncSchema.COLUMN_REQUEST_TYPE, -1);
        cValues.put(SyncSchema.COLUMN_NODE_ID, "");
        cValues.put(SyncSchema.COLUMN_PARENT_ID, "");
        cValues.put(SyncSchema.COLUMN_MIMETYPE, "");
        cValues.put(SyncSchema.COLUMN_PROPERTIES, "");
        cValues.put(SyncSchema.COLUMN_TOTAL_SIZE_BYTES, -1);
        cValues.put(SyncSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, -1);
        cValues.put(SyncSchema.COLUMN_LOCAL_URI, "");
        return cValues;
    }

}
