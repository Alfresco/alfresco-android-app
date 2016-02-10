/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.accounts;

import java.util.List;

import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.database.DatabaseVersionNumber;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Represents the implementation & management side of Account object inside the
 * database.
 * 
 * @author Jean Marie Pascal
 */
@Deprecated
public final class AccountSchema
{

    private AccountSchema()
    {
    }

    public static final String TABLENAME = "accounts";

    public static final String COLUMN_ID = "_id";

    public static final int COLUMN_ID_ID = 0;

    public static final String COLUMN_NAME = "name";

    public static final int COLUMN_NAME_ID = 1;

    public static final String COLUMN_URL = "url";

    public static final int COLUMN_URL_ID = COLUMN_NAME_ID + 1;

    public static final String COLUMN_USERNAME = "username";

    public static final int COLUMN_USERNAME_ID = COLUMN_URL_ID + 1;

    public static final String COLUMN_PASSWORD = "password";

    public static final int COLUMN_PASSWORD_ID = COLUMN_USERNAME_ID + 1;

    public static final String COLUMN_REPOSITORY_ID = "repositoryId";

    public static final int COLUMN_REPOSITORY_ID_ID = COLUMN_PASSWORD_ID + 1;

    public static final String COLUMN_REPOSITORY_TYPE = "repositoryTypeId";

    public static final int COLUMN_REPOSITORY_TYPE_ID = COLUMN_REPOSITORY_ID_ID + 1;

    public static final String COLUMN_ACTIVATION = "activation";

    public static final int COLUMN_ACTIVATION_ID = COLUMN_REPOSITORY_TYPE_ID + 1;

    public static final String COLUMN_ACCESS_TOKEN = "accessToken";

    public static final int COLUMN_ACCESS_TOKEN_ID = COLUMN_ACTIVATION_ID + 1;

    public static final String COLUMN_REFRESH_TOKEN = "refreshToken";

    public static final int COLUMN_REFRESH_TOKEN_ID = COLUMN_ACCESS_TOKEN_ID + 1;

    public static final String COLUMN_IS_PAID_ACCOUNT = "isPaidAccount";

    public static final int COLUMN_IS_PAID_ACCOUNT_ID = COLUMN_REFRESH_TOKEN_ID + 1;

    public static final String[] COLUMN_ALL = { COLUMN_ID, COLUMN_NAME, COLUMN_URL, COLUMN_USERNAME, COLUMN_PASSWORD,
            COLUMN_REPOSITORY_ID, COLUMN_REPOSITORY_TYPE, COLUMN_ACTIVATION, COLUMN_ACCESS_TOKEN, COLUMN_REFRESH_TOKEN,
            COLUMN_IS_PAID_ACCOUNT, };

    private static final String QUERY_TABLE_CREATE = "create table " + TABLENAME + " (" + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT NOT NULL," + COLUMN_URL + " TEXT NOT NULL,"
            + COLUMN_USERNAME + " TEXT NOT NULL," + COLUMN_PASSWORD + " TEXT," + COLUMN_REPOSITORY_ID
            + " TEXT NOT NULL," + COLUMN_REPOSITORY_TYPE + " INTEGER," + COLUMN_ACTIVATION + " TEXT,"
            + COLUMN_ACCESS_TOKEN + " TEXT," + COLUMN_REFRESH_TOKEN + " TEXT," + COLUMN_IS_PAID_ACCOUNT + " INTEGER);";

    private static final String TABLENAME_OLD = "Account";

    // Update database to add the Paid Account flag. This was introduced in
    // DB version 3.
    private static final String QUERY_ADD_PAID_ACCOUNT_COLUM = "ALTER TABLE " + TABLENAME_OLD + " ADD COLUMN "
            + COLUMN_IS_PAID_ACCOUNT + " integer default 0;";

    // Update database to create account content provider
    // Only purpose rename id column to _id
    // DB version 4.
    private static final String TMP_TABLENAME = "TMP_" + TABLENAME_OLD;

    private static final String QUERY_RENAME_TABLE_OLD = "ALTER TABLE " + TABLENAME_OLD + " RENAME TO " + TMP_TABLENAME
            + " ;";

    private static final String REPLICATE_ACCOUNT = "INSERT INTO " + TABLENAME + " (" + COLUMN_NAME + ", " + COLUMN_URL
            + ", " + COLUMN_USERNAME + ", " + COLUMN_PASSWORD + ", " + COLUMN_REPOSITORY_ID + ", "
            + COLUMN_REPOSITORY_TYPE + ", " + COLUMN_ACTIVATION + ", " + COLUMN_ACCESS_TOKEN + ", "
            + COLUMN_REFRESH_TOKEN + ", " + COLUMN_IS_PAID_ACCOUNT + ") " + " SELECT " + COLUMN_NAME + ", "
            + COLUMN_URL + ", " + COLUMN_USERNAME + ", " + COLUMN_PASSWORD + ", " + COLUMN_REPOSITORY_ID + ", "
            + COLUMN_REPOSITORY_TYPE + ", " + COLUMN_ACTIVATION + ", " + COLUMN_ACCESS_TOKEN + ", "
            + COLUMN_REFRESH_TOKEN + ", " + COLUMN_IS_PAID_ACCOUNT + " FROM " + TMP_TABLENAME + " ;";

    private static final String QUERY_DROP_TABLE_OLD = "DROP TABLE IF EXISTS " + TMP_TABLENAME;

    private static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLENAME;

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
            // Rename old table
            db.execSQL(QUERY_RENAME_TABLE_OLD);
            db.execSQL(QUERY_TABLE_CREATE);
            db.execSQL(REPLICATE_ACCOUNT);
            db.execSQL(QUERY_DROP_TABLE_OLD);
        }

        // Migrate all accounts to Account Manager
        if (oldVersion < DatabaseVersionNumber.VERSION_1_5_0)
        {
            //
            List<Account> accounts = AccountProvider.retrieveAccounts(db);
            android.accounts.Account newAccount;
            String accountName;
            AccountManager mAccountManager = AccountManager.get(context);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            SyncContentManager syncManager = SyncContentManager.getInstance(context);
            for (Account account : accounts)
            {
                // Check Account Name
                accountName = AlfrescoAccountManager.getInstance(context)
                        .createUniqueAccountName(account.getUsername());
                newAccount = new android.accounts.Account(accountName, AlfrescoAccount.ACCOUNT_TYPE);
                Bundle b = new Bundle();
                // Very important !
                // We keep the same Account Id from previous version.
                // Used by the SyncService
                b.putString(AlfrescoAccount.ACCOUNT_ID, Long.toString(account.getId()));
                BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_NAME, account.getDescription());
                BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_URL, account.getUrl());
                BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_USERNAME, account.getUsername());
                BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_REPOSITORY_ID, account.getRepositoryId());
                BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_REPOSITORY_TYPE_ID,
                        String.valueOf(account.getTypeId()));
                BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_IS_PAID_ACCOUNT,
                        Boolean.toString(account.getIsPaidAccount()));
                BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_ACTIVATION, account.getActivation());
                BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_ACCESS_TOKEN, account.getAccessToken());
                BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_REFRESH_TOKEN, account.getRefreshToken());

                // Time to create.
                if (mAccountManager.addAccountExplicitly(newAccount, account.getPassword(), b))
                {
                    // Let's define if sync automatically regarding previous
                    // settings
                    syncManager.setActivateSync(account.getId(),
                            sharedPref.getBoolean(SYNCHRO_PREFIX + account.getId(), false));
                    sharedPref.edit().remove(SYNCHRO_PREFIX + account.getId()).apply();
                }

                Log.i("Migration", "Account " + account.getDescription() + "[" + account.getId()
                        + "] has been migrated");

            }
            // Delete old table
            db.execSQL(QUERY_DROP_TABLE);
        }
    }

    private static final String SYNCHRO_PREFIX = "SynchroEnable-";

}
