package org.alfresco.mobile.android.application.accounts;

import java.util.Arrays;
import java.util.HashSet;

import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.database.DatabaseManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AccountProvider extends ContentProvider
{
    private static final String TAG = AccountProvider.class.getName();

    private DatabaseManager databaseManager;

    private static final int ACCOUNTS = 0;

    private static final int ACCOUNT_ID = 1;

    private static final String AUTHORITY = "org.alfresco.mobile.android.provider.accounts";

    private static final String BASE_PATH = "account";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/accounts";

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/account";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, ACCOUNTS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ACCOUNT_ID);
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
            case ACCOUNTS:
                rowsDeleted = db.delete(AccountSchema.TABLENAME, selection, selectionArgs);
                break;
            case ACCOUNT_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {
                    rowsDeleted = db.delete(AccountSchema.TABLENAME, AccountSchema.COLUMN_ID + "=" + id, null);
                }
                else
                {
                    rowsDeleted = db.delete(AccountSchema.TABLENAME, AccountSchema.COLUMN_ID + "=" + id + " and "
                            + selection, selectionArgs);
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
            case ACCOUNTS:
                id = db.insert(AccountSchema.TABLENAME, null, values);
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

        queryBuilder.setTables(AccountSchema.TABLENAME);

        int uriType = uriMatcher.match(uri);
        switch (uriType)
        {
            case ACCOUNTS:
                break;
            case ACCOUNT_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(AccountSchema.COLUMN_ID + "=" + uri.getLastPathSegment());
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
            case ACCOUNTS:
                rowsUpdated = sqlDB.update(AccountSchema.TABLENAME, values, selection, selectionArgs);
                break;
            case ACCOUNT_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {
                    rowsUpdated = sqlDB.update(AccountSchema.TABLENAME, values, AccountSchema.COLUMN_ID + "=" + id,
                            null);
                }
                else
                {
                    rowsUpdated = sqlDB.update(AccountSchema.TABLENAME, values, AccountSchema.COLUMN_ID + "=" + id
                            + " and " + selection, selectionArgs);
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
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(AccountSchema.COLUMN_ALL));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) { throw new IllegalArgumentException(
                    "Unknown columns in projection"); }
        }
    }

    public static Uri getUri(long id)
    {
        return Uri.parse(AccountProvider.CONTENT_URI + "/" + id);
    }

    public static ContentValues createContentValues(Account acc)
    {
        ContentValues updateValues = new ContentValues();

        updateValues.put(AccountSchema.COLUMN_NAME, acc.getDescription());
        updateValues.put(AccountSchema.COLUMN_URL, acc.getUrl());
        updateValues.put(AccountSchema.COLUMN_USERNAME, acc.getUsername());
        updateValues.put(AccountSchema.COLUMN_PASSWORD, acc.getPassword());
        updateValues.put(AccountSchema.COLUMN_REPOSITORY_ID, acc.getRepositoryId());
        updateValues.put(AccountSchema.COLUMN_REPOSITORY_TYPE, acc.getTypeId());
        updateValues.put(AccountSchema.COLUMN_ACTIVATION, acc.getActivation());
        updateValues.put(AccountSchema.COLUMN_ACCESS_TOKEN, acc.getAccessToken());
        updateValues.put(AccountSchema.COLUMN_REFRESH_TOKEN, acc.getRefreshToken());
        updateValues.put(AccountSchema.COLUMN_IS_PAID_ACCOUNT, acc.getIsPaidAccount());
        return updateValues;
    }
    
    public static ContentValues createContentValues(String name, String url, String username, String pass,
            String workspace, Integer type, String activation, String accessToken, String refreshToken,
            int isPaidAccount)
    {
        ContentValues updateValues = new ContentValues();

        updateValues.put(AccountSchema.COLUMN_NAME, name);
        updateValues.put(AccountSchema.COLUMN_URL, url);
        updateValues.put(AccountSchema.COLUMN_USERNAME, username);
        updateValues.put(AccountSchema.COLUMN_PASSWORD, pass);
        updateValues.put(AccountSchema.COLUMN_REPOSITORY_ID, workspace);
        updateValues.put(AccountSchema.COLUMN_REPOSITORY_TYPE, type);
        updateValues.put(AccountSchema.COLUMN_ACTIVATION, activation);
        updateValues.put(AccountSchema.COLUMN_ACCESS_TOKEN, accessToken);
        updateValues.put(AccountSchema.COLUMN_REFRESH_TOKEN, refreshToken);
        updateValues.put(AccountSchema.COLUMN_IS_PAID_ACCOUNT, isPaidAccount);
        return updateValues;
    }

    public static Account createAccount(Cursor c)
    {
        Account account = new Account(c.getInt(AccountSchema.COLUMN_ID_ID), c.getString(AccountSchema.COLUMN_NAME_ID),
                c.getString(AccountSchema.COLUMN_URL_ID), c.getString(AccountSchema.COLUMN_USERNAME_ID),
                c.getString(AccountSchema.COLUMN_PASSWORD_ID), c.getString(AccountSchema.COLUMN_REPOSITORY_ID_ID),
                c.getInt(AccountSchema.COLUMN_REPOSITORY_TYPE_ID), c.getString(AccountSchema.COLUMN_ACTIVATION_ID),
                c.getString(AccountSchema.COLUMN_ACCESS_TOKEN_ID), c.getString(AccountSchema.COLUMN_REFRESH_TOKEN_ID),
                c.getInt(AccountSchema.COLUMN_IS_PAID_ACCOUNT_ID));
        c.close();
        return account;
    }

    public static Account retrieveAccount(Context context, long id)
    {
        Cursor cursor = context.getContentResolver().query(AccountProvider.getUri(id), AccountSchema.COLUMN_ALL, null,
                null, null);
        Log.d(TAG, cursor.getCount() + " ");
        if (cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            return AccountProvider.createAccount(cursor);
        }
        cursor.close();
        return null;
    }

    public static Account retrieveFirstAccount(Context context)
    {
        Cursor cursor = context.getContentResolver().query(CONTENT_URI, AccountSchema.COLUMN_ALL, null, null, null);
        Log.d(TAG, cursor.getCount() + " ");
        if (cursor.getCount() == 0){
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        return AccountProvider.createAccount(cursor);
    }
}
