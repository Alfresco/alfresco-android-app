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
package org.alfresco.mobile.android.application.accounts;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.integration.OperationManager;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.OperationRequestGroup;
import org.alfresco.mobile.android.application.integration.account.LoadSessionRequest;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

/**
 * Responsible to manage accounts.
 * 
 * @author Jean Marie Pascal
 */
public class AccountManager
{
    private static final String TAG = AccountManager.class.getName();

    private static AccountManager mInstance;

    private final Context appContext;

    private static final Object mLock = new Object();

    private Integer accountCursor;

    private AccountManagerReceiver receiver;

    private ApplicationManager appManager;

    private LocalBroadcastManager broadManager;

    public static AccountManager getInstance(Context context)
    {
        synchronized (mLock)
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
    private void unregister()
    {
        if (receiver != null)
        {
            Log.d(TAG, "unregister()");
            broadManager.unregisterReceiver(receiver);
        }
    }

    private void register()
    {
        Log.d(TAG, "register()");

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

    public boolean hasData()
    {
        return (accountCursor != null);
    }

    public boolean hasAccount()
    {
        if (accountCursor == null) { return false; }
        return (accountCursor > 0);
    }

    public boolean isEmpty()
    {
        if (accountCursor == null) { return true; }
        return (accountCursor == 0);
    }

    public Account getDefaultAccount()
    {
        // Default account to load
        SharedPreferences settings = appContext.getSharedPreferences(AccountsPreferences.ACCOUNT_PREFS, 0);
        long id = settings.getLong(AccountsPreferences.ACCOUNT_DEFAULT, -1);
        Log.d(TAG, "Default AccountId " + id);
        if (id == -1)
        {
            return AccountProvider.retrieveFirstAccount(appContext);
        }
        else
        {
            return AccountProvider.retrieveAccount(appContext, id);
        }
    }

    private void loadSession(Account acc, OAuthData data)
    {
        if (appManager.hasSession(acc.getId()))
        {
            appManager.removeAccount(acc.getId());
        }

        OperationRequestGroup group = new OperationRequestGroup(appContext, acc);
        group.enqueue(new LoadSessionRequest(data).setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN)
                .setNotificationTitle(acc.getDescription()));
        OperationManager.getInstance(appContext).enqueue(group);
    }

    private AlfrescoSession loadSession(Account account)
    {
        AlfrescoSession session = null;
        Account currentAccount = appManager.getCurrentAccount();

        Log.d(TAG, account + " " + currentAccount);

        // First Session Loading
        if (account == null && currentAccount == null)
        {
            currentAccount = getDefaultAccount();
            if (currentAccount == null)
            {
                // TODO Change broadcast
                broadManager.sendBroadcast(new Intent(IntentIntegrator.ACTION_ACCOUNT_INACTIVE));
            }
        }
        else if (account != null)
        {
            // User has choose a specific account to load
            currentAccount = account;
        }

        Log.d(TAG, "Accounts " + account + " " + currentAccount);
        if (currentAccount.getActivation() != null)
        {
            // SEND broadcast : account is not active !
            broadManager.sendBroadcast(new Intent(IntentIntegrator.ACTION_ACCOUNT_INACTIVE));
        }

        // Check if Session available for this specific account
        if (appManager.hasSession(currentAccount.getId()))
        {
            session = appManager.getSession(currentAccount.getId());

            broadManager.sendBroadcast(new Intent(IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED).putExtra(
                    IntentIntegrator.EXTRA_ACCOUNT_ID, currentAccount.getId()));
        }
        else
        {
            // Create the session for the specific account
            createSession(currentAccount);
        }

        // Mark accountId for the specific activity.
        // Help to retrieve session associated to a specific activity
        appManager.saveAccount(currentAccount);

        return session;
    }

    private void createSession(Account currentAccount)
    {
        if (appManager.hasSession(currentAccount.getId()))
        {
            appManager.removeAccount(currentAccount.getId());
        }

        OperationRequestGroup group = new OperationRequestGroup(appContext, currentAccount);
        group.enqueue(new LoadSessionRequest().setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN)
                .setNotificationTitle(currentAccount.getDescription()));
        OperationManager.getInstance(appContext).enqueue(group);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Account Content Provider
    // ///////////////////////////////////////////////////////////////////////////
    private void getCount()
    {
        Cursor cursor = appContext.getContentResolver().query(AccountProvider.CONTENT_URI, AccountSchema.COLUMN_ALL,
                null, null, null);
        accountCursor = cursor.getCount();
        cursor.close();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    private class AccountManagerReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, "RECEIVE : " + intent.getAction());

            if (IntentIntegrator.ACTION_CREATE_ACCOUNT.equals(intent.getAction()))
            {
                if (intent.hasExtra(IntentIntegrator.EXTRA_CREATE_REQUEST))
                {
                    OperationRequestGroup group = new OperationRequestGroup(appContext);
                    group.enqueue((OperationRequest) intent.getExtras().getSerializable(
                            IntentIntegrator.EXTRA_CREATE_REQUEST));
                    OperationManager.getInstance(appContext).enqueue(group);
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
                acc = AccountProvider.retrieveAccount(appContext,
                        intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
                Log.d(TAG, "AccountId : " + acc);
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
                    ContentValues values = AccountProvider.createContentValues(acc);
                    values.put(AccountSchema.COLUMN_REPOSITORY_ID, intent.getExtras().getString(IntentIntegrator.EXTRA_NETWORK_ID));
                    appContext.getContentResolver().update(AccountProvider.getUri(acc.getId()), values, null, null);
                }
                createSession(acc);
                return;
            }
        }
    }

}
