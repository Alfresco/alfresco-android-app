/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.search;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public final class HistorySearchManager
{
    private static final String TAG = HistorySearchManager.class.getName();

    private static HistorySearchManager mInstance;

    private final Context appContext;

    private static final Object LOCK = new Object();

    private Integer historySize;

    public static final Uri CONTENT_URI = HistorySearchProvider.CONTENT_URI;

    public static final String[] COLUMN_ALL = HistorySearchSchema.COLUMN_ALL;

    public static HistorySearchManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new HistorySearchManager(context.getApplicationContext());
            }

            return mInstance;
        }
    }

    private HistorySearchManager(Context context)
    {
        // Init/retrieve manager
        this.appContext = context;
        getCount();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasData()
    {
        getCount();
        return (historySize != null);
    }

    public boolean hasAccount()
    {
        if (historySize == null) { return false; }
        return (historySize > 0);
    }

    public boolean hasMultipleAccount()
    {
        if (historySize == null) { return false; }
        return (historySize > 1);
    }

    public boolean isEmpty()
    {
        getCount();
        if (historySize == null) { return true; }
        return (historySize == 0);
    }

    public static HistorySearch retrieveHistorySearch(Context context, long id)
    {
        Cursor cursor = context.getContentResolver().query(getUri(id), COLUMN_ALL, null, null, null);
        Log.d(TAG, cursor.getCount() + " ");
        if (cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            return createHistorySearch(cursor);
        }
        cursor.close();
        return null;
    }

    public static HistorySearch createHistorySearch(Cursor c)
    {
        HistorySearch account = new HistorySearch(c.getLong(HistorySearchSchema.COLUMN_ID_ID),
                c.getLong(HistorySearchSchema.COLUMN_ACCOUNT_ID_ID), c.getInt(HistorySearchSchema.COLUMN_TYPE_ID),
                c.getInt(HistorySearchSchema.COLUMN_ADVANCED_ID),
                c.getString(HistorySearchSchema.COLUMN_DESCRIPTION_ID),
                c.getString(HistorySearchSchema.COLUMN_QUERY_ID),
                c.getLong(HistorySearchSchema.COLUMN_LAST_REQUEST_TIMESTAMP_ID));
        c.close();
        return account;
    }

    public static HistorySearch createHistorySearch(Context context, long accountId, int type, int advanced,
            String description, String query, long timestamp)
    {
        Uri accountUri = context.getContentResolver().insert(HistorySearchProvider.CONTENT_URI,
                createContentValues(accountId, type, advanced, description, query, timestamp));

        if (accountUri == null) { return null; }

        return HistorySearchManager.retrieveHistorySearch(context, Long.parseLong(accountUri.getLastPathSegment()));
    }

    public static HistorySearch retrieveFirstAccount(Context context)
    {
        Cursor cursor = context.getContentResolver().query(HistorySearchProvider.CONTENT_URI, COLUMN_ALL, null, null,
                null);
        if (cursor == null) { return null; }
        if (cursor.getCount() == 0)
        {
            Log.d(TAG, cursor.getCount() + " ");
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        return createHistorySearch(cursor);
    }

    public HistorySearch update(long id, long accountId, int type, int advanced, String description, String query,
            long timestamp)
    {
        return update(appContext, id, accountId, type, advanced, description, query, timestamp);
    }

    public static HistorySearch update(Context context, long id, long accountId, int type, int advanced,
            String description, String query, long timestamp)
    {
        context.getContentResolver().update(getUri(id),
                createContentValues(accountId, type, advanced, description, query, timestamp), null, null);

        return HistorySearchManager.retrieveHistorySearch(context, accountId);
    }

    public static Uri getUri(long id)
    {
        return Uri.parse(HistorySearchProvider.CONTENT_URI + "/" + id);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private static ContentValues createContentValues(long accountId, int type, int advanced, String description,
            String query, long timestamp)
    {
        ContentValues updateValues = new ContentValues();

        updateValues.put(HistorySearchSchema.COLUMN_ACCOUNT_ID, accountId);
        updateValues.put(HistorySearchSchema.COLUMN_TYPE, type);
        updateValues.put(HistorySearchSchema.COLUMN_ADVANCED, advanced);
        updateValues.put(HistorySearchSchema.COLUMN_DESCRIPTION, description);
        updateValues.put(HistorySearchSchema.COLUMN_QUERY, query);
        updateValues.put(HistorySearchSchema.COLUMN_LAST_REQUEST_TIMESTAMP, timestamp);
        return updateValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Account Content Provider
    // ///////////////////////////////////////////////////////////////////////////
    private void getCount()
    {
        Cursor cursor = appContext.getContentResolver().query(HistorySearchProvider.CONTENT_URI, COLUMN_ALL, null,
                null, null);
        if (cursor != null)
        {
            historySize = cursor.getCount();
            cursor.close();
        }
        else
        {
            historySize = 0;
        }
    }
}
