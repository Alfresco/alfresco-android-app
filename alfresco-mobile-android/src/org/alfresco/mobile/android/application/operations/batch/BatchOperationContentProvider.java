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

import java.util.Arrays;
import java.util.HashSet;

import org.alfresco.mobile.android.application.AlfrescoContentProvider;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.database.DatabaseManager;
import org.alfresco.mobile.android.application.operations.OperationSchema;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class BatchOperationContentProvider extends ContentProvider implements AlfrescoContentProvider
{

    private static final String TAG = BatchOperationContentProvider.class.getName();

    private DatabaseManager databaseManager;

    private static final int OPERATIONS = 0;

    private static final int OPERATION_ID = 1;

    private static final String AUTHORITY = AUTHORITY_ALFRESCO_BASE + ".operations";

    private static final String BASE_PATH = "operations";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/operations";

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/operation";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, OPERATIONS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", OPERATION_ID);
    }

    @Override
    public boolean onCreate()
    {
        databaseManager = ApplicationManager.getInstance(getContext()).getDatabaseManager();
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase db = databaseManager.getWriteDb();
        int rowsDeleted = 0;
        switch (uriType)
        {
            case OPERATIONS:
                rowsDeleted = db.delete(BatchOperationSchema.TABLENAME, selection, selectionArgs);
                break;
            case OPERATION_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {
                    rowsDeleted = db.delete(BatchOperationSchema.TABLENAME, BatchOperationSchema.COLUMN_ID + "=" + id,
                            null);
                }
                else
                {
                    rowsDeleted = db.delete(BatchOperationSchema.TABLENAME, BatchOperationSchema.COLUMN_ID + "=" + id
                            + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase db = databaseManager.getWriteDb();
        long id = 0;

        switch (uriType)
        {
            case OPERATIONS:
                id = db.insert(BatchOperationSchema.TABLENAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (id == -1)
        {
            Log.e(TAG, uri + " " + values);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(CONTENT_URI + "/" + id);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        queryBuilder.setTables(BatchOperationSchema.TABLENAME);

        int uriType = uriMatcher.match(uri);
        switch (uriType)
        {
            case OPERATIONS:
                break;
            case OPERATION_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(BatchOperationSchema.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = databaseManager.getWriteDb();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseManager.getWriteDb();
        int rowsUpdated = 0;
        switch (uriType)
        {
            case OPERATIONS:
                rowsUpdated = sqlDB.update(BatchOperationSchema.TABLENAME, values, selection, selectionArgs);
                break;
            case OPERATION_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {
                    rowsUpdated = sqlDB.update(BatchOperationSchema.TABLENAME, values, BatchOperationSchema.COLUMN_ID
                            + "=" + id, null);
                }
                else
                {
                    rowsUpdated = sqlDB.update(BatchOperationSchema.TABLENAME, values, BatchOperationSchema.COLUMN_ID
                            + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection)
    {
        if (projection != null)
        {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(BatchOperationSchema.COLUMN_ALL));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) { throw new IllegalArgumentException(
                    "Unknown columns in projection"); }
        }
    }
    
    public static String getAccountFilter(Account acc)
    {
        return OperationSchema.COLUMN_ACCOUNT_ID + " == " + acc.getId();
    }

}
