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
package org.alfresco.mobile.android.application.accounts;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.commons.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.account.LoadSessionRequest;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Responsible to manage accounts.
 * 
 * @author Jean Marie Pascal
 */
public final class AccountManager
{
    private static final String TAG = AccountManager.class.getName();

    private static AccountManager mInstance;

    private final Context appContext;

    private static final Object LOCK = new Object();

    private Integer accountsSize;

    private AccountManagerReceiver receiver;

    private ApplicationManager appManager;

    private LocalBroadcastManager broadManager;

    public static final Uri CONTENT_URI = AccountProvider.CONTENT_URI;

    public static final String[] COLUMN_ALL = AccountSchema.COLUMN_ALL;

    public static AccountManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new AccountManager(context.getApplicationContext());
            }

            return mInstance;
        }
    }

    private AccountManager(Context context)
    {
        // Init/retrieve manager
        this.appContext = context;
        broadManager = LocalBroadcastManager.getInstance(context);
        receiver = new AccountManagerReceiver();
        register();
        appManager = ApplicationManager.getInstance(context);
        getCount();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasData()
    {
        getCount();
        return (accountsSize != null);
    }

    public boolean hasAccount()
    {
        if (accountsSize == null) { return false; }
        return (accountsSize > 0);
    }

    public boolean hasMultipleAccount()
    {
        if (accountsSize == null) { return false; }
        return (accountsSize > 1);
    }

    public boolean isEmpty()
    {
        getCount();
        if (accountsSize == null) { return true; }
        return (accountsSize == 0);
    }

    public Account getDefaultAccount()
    {
        // Default account to load
        SharedPreferences settings = appContext.getSharedPreferences(AccountsPreferences.ACCOUNT_PREFS, 0);
        long id = settings.getLong(AccountsPreferences.ACCOUNT_DEFAULT, -1);
        //Log.d(TAG, "Default AccountId " + id);
        if (id == -1)
        {
            return retrieveFirstAccount(appContext);
        }
        else
        {
            return retrieveAccount(appContext, id);
        }
    }
    
    public static List<Account> retrieveAccounts(Context context)
    {
        List<Account> accounts = new ArrayList<Account>();
        try
        {
            Cursor cursor = context.getContentResolver().query(CONTENT_URI, COLUMN_ALL, null, null, null);
            accounts = new ArrayList<Account>(cursor.getCount());
            while (cursor.moveToNext())
            {
                accounts.add(createAccountWithoutClose(cursor));
            }
            //Log.d(TAG, "accounts " + accounts.size());

            cursor.close();
        }
        catch (Exception e)
        {
            Log.d(TAG, Log.getStackTraceString(e));
        }
      
        return accounts;
    }

    public static Account retrieveAccount(Context context, long id)
    {
        Cursor cursor = context.getContentResolver().query(getUri(id), COLUMN_ALL, null, null, null);
        //Log.d(TAG, cursor.getCount() + " ");
        if (cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            return createAccount(cursor);
        }
        cursor.close();
        return null;
    }
    
    
    private static Account createAccountWithoutClose(Cursor c)
    {
        Account account = new Account(c.getInt(AccountSchema.COLUMN_ID_ID), c.getString(AccountSchema.COLUMN_NAME_ID),
                c.getString(AccountSchema.COLUMN_URL_ID), c.getString(AccountSchema.COLUMN_USERNAME_ID),
                c.getString(AccountSchema.COLUMN_PASSWORD_ID), c.getString(AccountSchema.COLUMN_REPOSITORY_ID_ID),
                c.getInt(AccountSchema.COLUMN_REPOSITORY_TYPE_ID), c.getString(AccountSchema.COLUMN_ACTIVATION_ID),
                c.getString(AccountSchema.COLUMN_ACCESS_TOKEN_ID), c.getString(AccountSchema.COLUMN_REFRESH_TOKEN_ID),
                c.getInt(AccountSchema.COLUMN_IS_PAID_ACCOUNT_ID));
        return account;
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

    public static Account createAccount(Context context, String name, String url, String username, String pass,
            String workspace, Integer type, String activation, String accessToken, String refreshToken,
            int isPaidAccount)
    {
        Uri accountUri = context.getContentResolver().insert(
                AccountProvider.CONTENT_URI,
                createContentValues(name, url, username, pass, workspace, type, activation, accessToken, refreshToken,
                        isPaidAccount));

        if (accountUri == null) { return null; }

        return AccountManager.retrieveAccount(context, Long.parseLong(accountUri.getLastPathSegment()));
    }

    public static Account retrieveFirstAccount(Context context)
    {
        Cursor cursor = context.getContentResolver().query(AccountProvider.CONTENT_URI, COLUMN_ALL, null, null, null);
       //Log.d(TAG, cursor.getCount() + " ");
        if (cursor.getCount() == 0)
        {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        return createAccount(cursor);
    }

    public Account update(long accountId, String name, String url, String username, String pass, String workspace,
            Integer type, String activation, String accessToken, String refreshToken, int isPaidAccount)
    {
        return update(appContext, accountId, name, url, username, pass, workspace, type, activation, accessToken,
                refreshToken, isPaidAccount);
    }

    public static Account update(Context context, long accountId, String name, String url, String username,
            String pass, String workspace, Integer type, String activation, String accessToken, String refreshToken,
            int isPaidAccount)
    {
        context.getContentResolver().update(
                getUri(accountId),
                createContentValues(name, url, username, pass, workspace, type, activation, accessToken, refreshToken,
                        isPaidAccount), null, null);

        return AccountManager.retrieveAccount(context, accountId);
    }

    public static Uri getUri(long id)
    {
        return Uri.parse(AccountProvider.CONTENT_URI + "/" + id);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private void register()
    {
        if (receiver != null)
        {
            receiver = new AccountManagerReceiver();
        }

        IntentFilter filters = new IntentFilter(IntentIntegrator.ACTION_LOAD_ACCOUNT);
        filters.addAction(IntentIntegrator.ACTION_RELOAD_ACCOUNT);
        filters.addAction(IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED);
        filters.addAction(IntentIntegrator.ACTION_DELETE_ACCOUNT_COMPLETED);
        filters.addAction(IntentIntegrator.ACTION_CREATE_ACCOUNT);
        broadManager.registerReceiver(receiver, filters);
    }

    private static ContentValues createContentValues(Account acc)
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

    private static ContentValues createContentValues(String name, String url, String username, String pass,
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

    private void loadSession(Account acc, OAuthData data)
    {
        if (appManager.hasSession(acc.getId()))
        {
            appManager.removeAccount(acc.getId());
        }

        OperationsRequestGroup group = new OperationsRequestGroup(appContext, acc);
        group.enqueue(new LoadSessionRequest(data).setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN)
                .setNotificationTitle(acc.getDescription()));
        BatchOperationManager.getInstance(appContext).enqueue(group);
    }

    private AlfrescoSession loadSession(Account account)
    {
        AlfrescoSession session = null;
        Account accountToLoad = appManager.getCurrentAccount();

        // First Session Loading
        if (account == null && accountToLoad == null)
        {
            accountToLoad = getDefaultAccount();
            if (accountToLoad == null)
            {
                broadManager.sendBroadcast(new Intent(IntentIntegrator.ACTION_ACCOUNT_INACTIVE));
            }
        }
        else if (account != null)
        {
            // User has choose a specific account to load
            accountToLoad = account;
        }

        if (accountToLoad == null) { return null; }

        if (accountToLoad.getActivation() != null)
        {
            // SEND broadcast : account is not active !
            broadManager.sendBroadcast(new Intent(IntentIntegrator.ACTION_ACCOUNT_INACTIVE));
        }

        // Check if Session available for this specific account
        if (appManager.hasSession(accountToLoad.getId()))
        {
            session = appManager.getSession(accountToLoad.getId());

            broadManager.sendBroadcast(new Intent(IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED).putExtra(
                    IntentIntegrator.EXTRA_ACCOUNT_ID, accountToLoad.getId()));
        }
        else if (appManager.getCurrentAccount() == null
                || accountToLoad.getId() != appManager.getCurrentAccount().getId())
        {
            // Create the session for the specific account
            createSession(accountToLoad);
        }

        // Mark accountId for the specific activity.
        // Help to retrieve session associated to a specific activity
        appManager.saveAccount(accountToLoad);

        return session;
    }

    private void createSession(Account currentAccount)
    {
        // Check Connectivity
        if (!ConnectivityUtils.hasInternetAvailable(appContext))
        {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_ERROR);
            broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_alfresco_logo);
            broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_session_creation_message);
            broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
            broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_MESSAGE, R.string.error_session_nodata);
            broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, currentAccount.getId());
            LocalBroadcastManager.getInstance(appContext).sendBroadcast(broadcastIntent);
            return;
        }

        if (appManager.hasSession(currentAccount.getId()))
        {
            appManager.removeAccount(currentAccount.getId());
        }

        OperationsRequestGroup group = new OperationsRequestGroup(appContext, currentAccount);
        group.enqueue(new LoadSessionRequest().setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN)
                .setNotificationTitle(currentAccount.getDescription()));
        BatchOperationManager.getInstance(appContext).enqueue(group);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Account Content Provider
    // ///////////////////////////////////////////////////////////////////////////
    private void getCount()
    {
        Cursor cursor = appContext.getContentResolver()
                .query(AccountProvider.CONTENT_URI, COLUMN_ALL, null, null, null);
        if (cursor != null)
        {
            accountsSize = cursor.getCount();
            cursor.close();
        }
        else
        {
            accountsSize = 0;
        }
    }

    public void clear()
    {
        accountsSize = null;
        mInstance = null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    private class AccountManagerReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            //Log.d(TAG, "RECEIVE : " + intent.getAction());

            if (IntentIntegrator.ACTION_CREATE_ACCOUNT.equals(intent.getAction()))
            {
                if (intent.hasExtra(IntentIntegrator.EXTRA_CREATE_REQUEST))
                {
                    OperationsRequestGroup group = new OperationsRequestGroup(appContext);
                    group.enqueue((OperationRequest) intent.getExtras().getSerializable(
                            IntentIntegrator.EXTRA_CREATE_REQUEST));
                    BatchOperationManager.getInstance(appContext).enqueue(group);
                }
                return;
            }

            if (IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED.equals(intent.getAction())
                    || IntentIntegrator.ACTION_DELETE_ACCOUNT_COMPLETED.equals(intent.getAction()))
            {
                getCount();
                return;
            }

            Account acc = null;
            if (intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
            {
                acc = retrieveAccount(appContext, intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
                //Log.d(TAG, "AccountId : " + acc);
            }

            if (IntentIntegrator.ACTION_LOAD_ACCOUNT.equals(intent.getAction()))
            {
                if (intent.hasExtra(IntentIntegrator.EXTRA_OAUTH_DATA))
                {
                    loadSession(acc, (OAuthData) intent.getExtras().getSerializable(IntentIntegrator.EXTRA_OAUTH_DATA));
                }
                else
                {
                    loadSession(acc);
                }
                return;
            }

            if (IntentIntegrator.ACTION_RELOAD_ACCOUNT.equals(intent.getAction()))
            {
                if (intent.hasExtra(IntentIntegrator.EXTRA_NETWORK_ID))
                {
                    ContentValues values = createContentValues(acc);
                    values.put(AccountSchema.COLUMN_REPOSITORY_ID,
                            intent.getExtras().getString(IntentIntegrator.EXTRA_NETWORK_ID));
                    appContext.getContentResolver().update(getUri(acc.getId()), values, null, null);
                }
                createSession(acc);
                return;
            }
        }
    }

}
