/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
package org.alfresco.mobile.android.platform.accounts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.async.account.CreateAccountEvent;
import org.alfresco.mobile.android.async.account.DeleteAccountEvent;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.utils.BundleUtils;

import com.squareup.otto.Subscribe;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 * Responsible to manage accounts.
 * 
 * @author Jean Marie Pascal
 */
public class AlfrescoAccountManager extends Manager
{
    private static final String TAG = AlfrescoAccountManager.class.getName();

    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    protected Integer accountsSize;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public static AlfrescoAccountManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = Manager.getInstance(context, AlfrescoAccountManager.class.getSimpleName());
            }

            return (AlfrescoAccountManager) mInstance;
        }
    }

    protected AlfrescoAccountManager(Context context)
    {
        super(context);
        EventBusManager.getInstance().register(this);
        getCount();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void shutdown()
    {
        EventBusManager.getInstance().unregister(this);
        accountsSize = null;
        mInstance = null;
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
        return accountsSize != null && (accountsSize > 0);
    }

    public boolean hasMultipleAccount()
    {
        return accountsSize != null && (accountsSize > 1);
    }

    public boolean isEmpty()
    {
        getCount();
        return accountsSize == null || (accountsSize == 0);
    }

    public AlfrescoAccount getDefaultAccount()
    {
        // Default account to load
        SharedPreferences settings = appContext.getSharedPreferences(AccountsPreferences.ACCOUNT_PREFS, 0);
        long id = settings.getLong(AccountsPreferences.ACCOUNT_DEFAULT, -1);
        // Log.d(TAG, "Default AccountId " + id);
        if (id == -1)
        {
            return retrieveFirstAccount();
        }
        else
        {
            return retrieveAccount(id);
        }
    }

    public static List<AlfrescoAccount> retrieveAccounts(Context context)
    {
        List<AlfrescoAccount> accounts = new ArrayList<>();
        try
        {
            AccountManager mAccountManager = AccountManager.get(context);
            Account[] accountMs = mAccountManager.getAccountsByType(AlfrescoAccount.ACCOUNT_TYPE);
            accounts = new ArrayList<AlfrescoAccount>(accountMs.length);
            for (Account account : accountMs)
            {
                if (mAccountManager.getUserData(account, AlfrescoAccount.ACCOUNT_ID) != null)
                {
                    accounts.add(AlfrescoAccount.parse(mAccountManager, account));
                }
            }
            // Log.d(TAG, "accounts " + accounts.size());
        }
        catch (Exception e)
        {
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return accounts;
    }

    public AlfrescoAccount retrieveAccount(long id)
    {
        AccountManager mAccountManager = AccountManager.get(appContext);
        Account[] accounts = mAccountManager.getAccountsByType(AlfrescoAccount.ACCOUNT_TYPE);
        if (accounts.length == 0) { return null; }
        for (Account account : accounts)
        {
            String accountId = mAccountManager.getUserData(account, AlfrescoAccount.ACCOUNT_ID);
            if (accountId != null
                    && id == Long.parseLong(accountId)) { return AlfrescoAccount.parse(mAccountManager, account); }
        }
        return null;
    }

    public List<Account> getAndroidAccounts()
    {
        List<Account> accounts = null;
        try
        {
            AccountManager mAccountManager = AccountManager.get(appContext);
            Account[] accountMs = mAccountManager.getAccountsByType(AlfrescoAccount.ACCOUNT_TYPE);
            accounts = new ArrayList<Account>(accountMs.length);
            for (Account account : accountMs)
            {
                accounts.add(account);
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return accounts;
    }

    public Account getAndroidAccount(long id)
    {
        AccountManager mAccountManager = AccountManager.get(appContext);
        Account[] accounts = mAccountManager.getAccountsByType(AlfrescoAccount.ACCOUNT_TYPE);
        if (accounts.length == 0) { return null; }
        for (Account account : accounts)
        {
            String accountId = mAccountManager.getUserData(account, AlfrescoAccount.ACCOUNT_ID);
            if (accountId != null && id == Long.parseLong(accountId)) { return account; }
        }
        return null;
    }

    public AlfrescoAccount retrieveFirstAccount()
    {
        AccountManager mAccountManager = AccountManager.get(appContext);
        Account[] accounts = mAccountManager.getAccountsByType(AlfrescoAccount.ACCOUNT_TYPE);
        if (accounts.length == 0) { return null; }
        try
        {
            return AlfrescoAccount.parse(mAccountManager, accounts[0]);
        }
        catch (Exception e)
        {
            return AlfrescoAccount.parse(mAccountManager, accounts[1]);
        }
    }

    public String createUniqueAccountName(String defaultName)
    {
        // Check Account Name
        String accountName = defaultName;
        AccountManager mAccountManager = AccountManager.get(appContext);
        Map<String, Account> accountIndex;
        Account[] accounts = mAccountManager.getAccountsByType(AlfrescoAccount.ACCOUNT_TYPE);
        if (accounts.length > 0)
        {
            accountIndex = new HashMap<String, Account>(accounts.length);
            for (Account accountAvailable : accounts)
            {
                accountIndex.put(accountAvailable.name, accountAvailable);
            }

            if (accountIndex != null && accountIndex.containsKey(accountName))
            {
                int index = 0;
                // We need to change the name of the account
                while (accountIndex.containsKey(accountName))
                {
                    accountName = defaultName.concat("-").concat(Integer.toString(index));
                    index++;
                }
            }
        }

        return accountName;
    }

    protected long getAccountId()
    {
        long accountIndex = 0;
        Account[] accounts = AccountManager.get(appContext).getAccountsByType(AlfrescoAccount.ACCOUNT_TYPE);
        for (Account accountAvailable : accounts)
        {
            String value = AccountManager.get(appContext).getUserData(accountAvailable, AlfrescoAccount.ACCOUNT_ID);
            if (value == null)
            {
                continue;
            }
            long currentIndew = Long.parseLong(value);
            if (accountIndex <= currentIndew)
            {
                accountIndex = currentIndew + 1;
            }
        }
        return accountIndex;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public AlfrescoAccount create(String accountLabel, String url, String username, String password,
            String repositoryId, String typeId, String activation, String accessToken, String refreshToken,
            String isPaidAccount)
    {
        // Generate some properties
        String accountName = createUniqueAccountName(username);
        long accountId = getAccountId();

        // Prepare account
        Account newAccount = new Account(accountName, AlfrescoAccount.ACCOUNT_TYPE);
        Bundle b = new Bundle();
        b.putString(AlfrescoAccount.ACCOUNT_ID, Long.toString(accountId));
        BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_NAME, accountLabel);
        BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_URL, url);
        BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_USERNAME, username);
        BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_REPOSITORY_ID, repositoryId);
        BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_REPOSITORY_TYPE_ID, typeId);
        BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_IS_PAID_ACCOUNT, isPaidAccount);
        BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_ACTIVATION, activation);
        BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_ACCESS_TOKEN, accessToken);
        BundleUtils.addIfNotEmpty(b, AlfrescoAccount.ACCOUNT_REFRESH_TOKEN, refreshToken);

        // Time to create.
        if (AccountManager.get(appContext).addAccountExplicitly(newAccount, password, b))
        {
            // Create the Account data object
            return new AlfrescoAccount(accountId, accountLabel, url, username, password, repositoryId, typeId,
                    activation, accessToken, refreshToken, isPaidAccount);
        }
        else
        {
            // TODO Error ?
            return null;
        }
    }

    public AlfrescoAccount update(long accountId, String name, String url, String username, String pass,
            String workspace, Integer type, String activation, String accessToken, String refreshToken,
            int isPaidAccount)
    {
        return update(appContext, accountId, name, url, username, pass, workspace, type, activation, accessToken,
                refreshToken, isPaidAccount);
    }

    public AlfrescoAccount update(Context context, long accountId, String name, String url, String username,
            String pass, String workspace, Integer type, String activation, String accessToken, String refreshToken,
            int isPaidAccount)
    {
        Account acc = getAndroidAccount(accountId);
        AccountManager manager = AccountManager.get(context);
        manager.setUserData(acc, AlfrescoAccount.ACCOUNT_ID, Long.toString(accountId));
        manager.setUserData(acc, AlfrescoAccount.ACCOUNT_NAME, name);
        manager.setUserData(acc, AlfrescoAccount.ACCOUNT_URL, url);
        manager.setUserData(acc, AlfrescoAccount.ACCOUNT_USERNAME, username);
        manager.setUserData(acc, AlfrescoAccount.ACCOUNT_REPOSITORY_ID, workspace);
        manager.setUserData(acc, AlfrescoAccount.ACCOUNT_REPOSITORY_TYPE_ID, String.valueOf(type));
        manager.setUserData(acc, AlfrescoAccount.ACCOUNT_IS_PAID_ACCOUNT, (isPaidAccount == 1) ? "true" : "false");
        manager.setUserData(acc, AlfrescoAccount.ACCOUNT_ACTIVATION, activation);
        manager.setUserData(acc, AlfrescoAccount.ACCOUNT_ACCESS_TOKEN, accessToken);
        manager.setUserData(acc, AlfrescoAccount.ACCOUNT_REFRESH_TOKEN, refreshToken);
        manager.setPassword(acc, pass);
        return retrieveAccount(accountId);
    }

    public AlfrescoAccount resetPassword(long accountId, String key, String value)
    {
        Account acc = getAndroidAccount(accountId);
        AccountManager manager = AccountManager.get(appContext);
        manager.setUserData(acc, key, value);
        manager.setPassword(acc, "");
        return retrieveAccount(accountId);
    }

    public AlfrescoAccount setSamlToken(long accountId, String value)
    {
        Account acc = getAndroidAccount(accountId);
        AccountManager manager = AccountManager.get(appContext);
        manager.setPassword(acc, value);
        return retrieveAccount(accountId);
    }

    public void update(long accountId, String key, String value)
    {
        Account acc = getAndroidAccount(accountId);
        AccountManager manager = AccountManager.get(appContext);
        manager.setUserData(acc, key, value);
    }

    protected void getCount()
    {
        AccountManager mAccountManager = AccountManager.get(appContext);
        Account[] accountMs = mAccountManager.getAccountsByType(AlfrescoAccount.ACCOUNT_TYPE);
        if (accountMs != null)
        {
            accountsSize = accountMs.length;
        }
        else
        {
            accountsSize = 0;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onAccountCreated(CreateAccountEvent event)
    {
        getCount();
    }

    @Subscribe
    public void onAccountDeleted(DeleteAccountEvent event)
    {
        getCount();
    }

}
