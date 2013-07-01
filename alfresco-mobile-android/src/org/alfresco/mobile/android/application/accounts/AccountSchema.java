/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.accounts;

import org.alfresco.mobile.android.application.database.DatabaseVersionNumber;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Represents the implementation & management side of Account object inside the
 * database.
 * 
 * @author Jean Marie Pascal
 */
public final class AccountSchema
{
    
    private AccountSchema(){
    }

    public static final String TABLENAME = "accounts";
    
    public static final String COLUMN_ID = "_id";

    public static final int COLUMN_ID_ID = 0;

    public static final String COLUMN_NAME = "name";

    public static final int COLUMN_NAME_ID = 1;

    public static final String COLUMN_URL = "url";

    public static final int COLUMN_URL_ID = COLUMN_NAME_ID + 1;

    public static final String COLUMN_USERNAME = "username";

    public static final int COLUMN_USERNAME_ID =  COLUMN_URL_ID + 1;

    public static final String COLUMN_PASSWORD = "password";

    public static final int COLUMN_PASSWORD_ID =  COLUMN_USERNAME_ID + 1;

    public static final String COLUMN_REPOSITORY_ID = "repositoryId";

    public static final int COLUMN_REPOSITORY_ID_ID =  COLUMN_PASSWORD_ID + 1;

    public static final String COLUMN_REPOSITORY_TYPE = "repositoryTypeId";

    public static final int COLUMN_REPOSITORY_TYPE_ID =  COLUMN_REPOSITORY_ID_ID + 1;

    public static final String COLUMN_ACTIVATION = "activation";

    public static final int COLUMN_ACTIVATION_ID =  COLUMN_REPOSITORY_TYPE_ID + 1;

    public static final String COLUMN_ACCESS_TOKEN = "accessToken";

    public static final int COLUMN_ACCESS_TOKEN_ID =  COLUMN_ACTIVATION_ID + 1;

    public static final String COLUMN_REFRESH_TOKEN = "refreshToken";

    public static final int COLUMN_REFRESH_TOKEN_ID = COLUMN_ACCESS_TOKEN_ID + 1;

    public static final String COLUMN_IS_PAID_ACCOUNT = "isPaidAccount";

    public static final int COLUMN_IS_PAID_ACCOUNT_ID =  COLUMN_REFRESH_TOKEN_ID + 1;
    
    public static final String[] COLUMN_ALL = { 
        COLUMN_ID, 
        COLUMN_NAME,
        COLUMN_URL,
        COLUMN_USERNAME,
        COLUMN_PASSWORD,
        COLUMN_REPOSITORY_ID,
        COLUMN_REPOSITORY_TYPE, 
        COLUMN_ACTIVATION,
        COLUMN_ACCESS_TOKEN,
        COLUMN_REFRESH_TOKEN,
        COLUMN_IS_PAID_ACCOUNT, 
        };

    private static final String QUERY_TABLE_CREATE = "create table " + TABLENAME + " (" 
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
            + COLUMN_NAME + " TEXT NOT NULL," 
            + COLUMN_URL + " TEXT NOT NULL,"
            + COLUMN_USERNAME + " TEXT NOT NULL," 
            + COLUMN_PASSWORD + " TEXT," 
            + COLUMN_REPOSITORY_ID + " TEXT NOT NULL," 
            + COLUMN_REPOSITORY_TYPE + " INTEGER," 
            + COLUMN_ACTIVATION + " TEXT,"
            + COLUMN_ACCESS_TOKEN + " TEXT," 
            + COLUMN_REFRESH_TOKEN + " TEXT," 
            + COLUMN_IS_PAID_ACCOUNT + " INTEGER);";
    
    
    private static final String TABLENAME_OLD = "Account";
    
    // Update database to add the Paid Account flag. This was introduced in
    // DB version 3.
    private static final String QUERY_ADD_PAID_ACCOUNT_COLUM = "ALTER TABLE " + TABLENAME_OLD + " ADD COLUMN " + COLUMN_IS_PAID_ACCOUNT
            + " integer default 0;";
    
    // Update database to create account content provider
    // Only purpose rename id column to _id
    // DB version 4.
    private static final String TMP_TABLENAME = "TMP_" + TABLENAME_OLD;
    private static final String QUERY_RENAME_TABLE_OLD = "ALTER TABLE " + TABLENAME_OLD + " RENAME TO " + TMP_TABLENAME +" ;";
    private static final String REPLICATE_ACCOUNT = 
            "INSERT INTO " + TABLENAME  + " (" 
            + COLUMN_NAME               + ", "
            + COLUMN_URL                + ", "
            + COLUMN_USERNAME           + ", "
            + COLUMN_PASSWORD           + ", "
            + COLUMN_REPOSITORY_ID      + ", "
            + COLUMN_REPOSITORY_TYPE    + ", "
            + COLUMN_ACTIVATION         + ", "
            + COLUMN_ACCESS_TOKEN       + ", "
            + COLUMN_REFRESH_TOKEN      + ", "
            + COLUMN_IS_PAID_ACCOUNT    + ") " 
            + " SELECT "
            + COLUMN_NAME               + ", "
            + COLUMN_URL                + ", "
            + COLUMN_USERNAME           + ", "
            + COLUMN_PASSWORD           + ", "
            + COLUMN_REPOSITORY_ID      + ", "
            + COLUMN_REPOSITORY_TYPE    + ", "
            + COLUMN_ACTIVATION         + ", "
            + COLUMN_ACCESS_TOKEN       + ", "
            + COLUMN_REFRESH_TOKEN      + ", "
            + COLUMN_IS_PAID_ACCOUNT
            + " FROM " + TMP_TABLENAME +" ;";
    private static final String QUERY_DROP_TABLE_OLD = "DROP TABLE IF EXISTS " + TMP_TABLENAME;

    public static void onCreate(Context context, SQLiteDatabase db)
    {
        db.execSQL(QUERY_TABLE_CREATE);
    }

    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Update database to add the Paid Account flag. This was introduced in
        // DB version 3.
        if (oldVersion <= DatabaseVersionNumber.VERSION_1_0_0)
        {
            db.execSQL(QUERY_ADD_PAID_ACCOUNT_COLUM);
        }
        
        // Update database to create account content provider
        if (oldVersion <= DatabaseVersionNumber.VERSION_1_1_0)
        {
            //Rename old table
            db.execSQL(QUERY_RENAME_TABLE_OLD);
            db.execSQL(QUERY_TABLE_CREATE);
            db.execSQL(REPLICATE_ACCOUNT);
            db.execSQL(QUERY_DROP_TABLE_OLD);
        }
    }
}
