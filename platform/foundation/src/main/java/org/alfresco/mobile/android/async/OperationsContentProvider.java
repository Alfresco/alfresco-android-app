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
package org.alfresco.mobile.android.async;

import java.util.Arrays;
import java.util.HashSet;

import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.database.DatabaseManager;
import org.alfresco.mobile.android.platform.provider.AlfrescoContentProvider;

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

public abstract class OperationsContentProvider extends ContentProvider implements AlfrescoContentProvider
{
    private static final String TAG = OperationsContentProvider.class.getName();

    protected DatabaseManager databaseManager;

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
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase db = databaseManager.getWriteDb();
        int rowsDeleted = 0;
        switch (uriType)
        {
            case OPERATIONS:
                rowsDeleted = db.delete(OperationSchema.TABLENAME, selection, selectionArgs);
                break;
            case OPERATION_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {
                    rowsDeleted = db.delete(OperationSchema.TABLENAME, OperationSchema.COLUMN_ID + "=" + id, null);
                }
                else
                {
                    rowsDeleted = db.delete(OperationSchema.TABLENAME, OperationSchema.COLUMN_ID + "=" + id + " and "
                            + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        // Log.d("Provider", "[REMOVE] " + uri);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri)
    {
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
                id = db.insert(OperationSchema.TABLENAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (id == -1)
        {
            Log.e(TAG, uri + " " + values);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        // Log.d("Provider", "[Insert] " + uri);

        return Uri.parse(CONTENT_URI + "/" + id);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        queryBuilder.setTables(OperationSchema.TABLENAME);

        int uriType = uriMatcher.match(uri);
        switch (uriType)
        {
            case OPERATIONS:
                break;
            case OPERATION_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(OperationSchema.COLUMN_ID + "=" + uri.getLastPathSegment());
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
                rowsUpdated = sqlDB.update(OperationSchema.TABLENAME, values, selection, selectionArgs);
                break;
            case OPERATION_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {
                    rowsUpdated = sqlDB.update(OperationSchema.TABLENAME, values, OperationSchema.COLUMN_ID + "=" + id,
                            null);
                }
                else
                {
                    rowsUpdated = sqlDB.update(OperationSchema.TABLENAME, values, OperationSchema.COLUMN_ID + "=" + id
                            + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        // Log.d("Provider", "[Update] " + uri);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection)
    {
        if (projection != null)
        {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(OperationSchema.COLUMN_ALL));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) { throw new IllegalArgumentException(
                    "Unknown columns in projection"); }
        }
    }

    public static String getAccountFilter(AlfrescoAccount acc)
    {
        return OperationsSchema.COLUMN_ACCOUNT_ID + " == " + acc.getId();
    }

    public static String getAccountFilter(long accountId)
    {
        return OperationsSchema.COLUMN_ACCOUNT_ID + " == " + accountId;
    }

}
