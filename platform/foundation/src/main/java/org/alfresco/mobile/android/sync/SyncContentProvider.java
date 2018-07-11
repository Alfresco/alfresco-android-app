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
package org.alfresco.mobile.android.sync;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

public abstract class SyncContentProvider extends ContentProvider implements AlfrescoContentProvider
{
    private static final String TAG = SyncContentProvider.class.getName();

    protected DatabaseManager databaseManager;

    private static final int SYNC = 0;

    private static final int SYNC_ID = 1;

    public static final String AUTHORITY = AUTHORITY_ALFRESCO_BASE + ".sync";

    private static final String BASE_PATH = "id";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/syncs";

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/sync";

    public static final int FLAG_SYNC_SET = 1;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH, SYNC);
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#", SYNC_ID);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase db = databaseManager.getWriteDb();
        int rowsDeleted = 0;
        switch (uriType)
        {
            case SYNC:
                rowsDeleted = db.delete(SyncContentSchema.TABLENAME, selection, selectionArgs);
                break;
            case SYNC_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {
                    rowsDeleted = db.delete(SyncContentSchema.TABLENAME, SyncContentSchema.COLUMN_ID + "=" + id,
                            null);
                }
                else
                {
                    rowsDeleted = db.delete(SyncContentSchema.TABLENAME, SyncContentSchema.COLUMN_ID + "=" + id
                            + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // getContext().getContentResolver().notifyChange(uri, null);
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
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase db = databaseManager.getWriteDb();
        long id = 0;

        switch (uriType)
        {
            case SYNC:
                id = db.insert(SyncContentSchema.TABLENAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (id == -1)
        {
            Log.e(TAG, uri + " " + values);
        }
        // getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(CONTENT_URI + "/" + id);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        queryBuilder.setStrict(true);
        queryBuilder.setTables(SyncContentSchema.TABLENAME);
        queryBuilder.setProjectionMap(createProjectionMap(projection));

        int uriType = URI_MATCHER.match(uri);
        switch (uriType)
        {
            case SYNC:
                break;
            case SYNC_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(SyncContentSchema.COLUMN_ID + "=" + uri.getLastPathSegment());
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
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase sqlDB = databaseManager.getWriteDb();
        int rowsUpdated = 0;
        switch (uriType)
        {
            case SYNC:
                rowsUpdated = sqlDB.update(SyncContentSchema.TABLENAME, values, selection, selectionArgs);
                break;
            case SYNC_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {
                    rowsUpdated = sqlDB.update(SyncContentSchema.TABLENAME, values, SyncContentSchema.COLUMN_ID
                            + "=" + id, null);
                }
                else
                {
                    rowsUpdated = sqlDB.update(SyncContentSchema.TABLENAME, values, SyncContentSchema.COLUMN_ID
                            + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection)
    {
        if (projection != null)
        {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(SyncContentSchema.COLUMN_ALL));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) { throw new IllegalArgumentException(
                    "Unknown columns in projection"); }
        }
    }

    private Map<String, String> createProjectionMap(String[] projection) {
        Map<String, String> projectionMap = new HashMap<>();
        for (String column : projection) {
            projectionMap.put(column, column);
        }
        return projectionMap;
    }

    public static String getAccountFilter(AlfrescoAccount acc)
    {
        return SyncContentSchema.COLUMN_ACCOUNT_ID + " == " + acc.getId();
    }

    public static String getAccountFilter(long accId)
    {
        return SyncContentSchema.COLUMN_ACCOUNT_ID + " == " + accId;
    }

}
