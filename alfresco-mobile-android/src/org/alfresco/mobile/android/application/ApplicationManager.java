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
package org.alfresco.mobile.android.application;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.database.DatabaseManager;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.manager.UpgradeManager;

import android.app.Activity;
import android.content.Context;

/**
 * Provides high level service and responsible to manage sessions across
 * activities.
 * 
 * @author Jean Marie Pascal
 */
public final class ApplicationManager
{
    // private static final String TAG = ApplicationManager.class.getName();

    private static ApplicationManager mInstance;

    private final Context appContext;

    private static final Object LOCK = new Object();

    private DatabaseManager databaseManager;

    private Map<Long, AlfrescoSession> sessionIndex = new HashMap<Long, AlfrescoSession>();

    private Account currentAccount;

    private RenditionManager renditionManager;

    private AccountManager accountManager;

    private UpgradeManager upgradeManager;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    private ApplicationManager(Context applicationContext)
    {
        appContext = applicationContext;
        databaseManager = new DatabaseManager(appContext);
        upgradeManager = new UpgradeManager(appContext);
    }

    public static ApplicationManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new ApplicationManager(context.getApplicationContext());
            }

            return mInstance;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MANAGERS
    // ///////////////////////////////////////////////////////////////////////////
    public DatabaseManager getDatabaseManager()
    {
        return databaseManager;
    }

    public RenditionManager getRenditionManager(Activity activity)
    {
        Account acc = currentAccount;
        if (activity instanceof BaseActivity)
        {
            acc = ((BaseActivity) activity).getCurrentAccount();
            if (((BaseActivity) activity).getRenditionManager() == null)
            {
                renditionManager = new RenditionManager(activity, sessionIndex.get(acc.getId()));
                ((BaseActivity) activity).setRenditionManager(renditionManager);
            }
        }

        if (renditionManager == null && acc != null)
        {
            renditionManager = new RenditionManager(activity, sessionIndex.get(acc.getId()));
        }
        return renditionManager;
    }

    public void setAccountManager(AccountManager manager)
    {
        this.accountManager = manager;
    }

    public AccountManager getAccountManager()
    {
        if (accountManager == null)
        {
            accountManager = AccountManager.getInstance(appContext);
        }
        return accountManager;
    }

    public UpgradeManager getUpgradeManager()
    {
        return upgradeManager;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACCOUNT / SESSION MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public Account getCurrentAccount()
    {
        return currentAccount;
    }

    public void saveAccount(Account currentAccount)
    {
        this.currentAccount = currentAccount;
    }

    public AlfrescoSession getCurrentSession()
    {
        if (currentAccount == null) { return null; }
        return sessionIndex.get(currentAccount.getId());
    }

    public void saveSession(Account account, AlfrescoSession session)
    {
        sessionIndex.put(account.getId(), session);
    }

    public void saveSession(AlfrescoSession session)
    {
        saveSession(currentAccount, session);
    }

    public void removeAccount(long accountId)
    {
        if (currentAccount != null && currentAccount.getId() == accountId)
        {
            currentAccount = null;
        }
        sessionIndex.remove(accountId);
    }

    public boolean hasSession(Long accountId)
    {
        return sessionIndex.containsKey(accountId);
    }

    public AlfrescoSession getSession(Long accountId)
    {
        return sessionIndex.get(accountId);
    }

    public void clear()
    {
        currentAccount = null;
        renditionManager = null;
        sessionIndex.clear();
        accountManager.clear();
        mInstance = null;
    }
}
