package org.alfresco.mobile.android.application;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.database.DatabaseManager;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class ApplicationManager
{
    private static final String TAG = ApplicationManager.class.getName();

    private static ApplicationManager mInstance;

    private final Context appContext;

    private static final Object mLock = new Object();

    private DatabaseManager databaseManager;

    private Map<Long, AlfrescoSession> sessionIndex = new HashMap<Long, AlfrescoSession>();

    private Account currentAccount;

    private RenditionManager renditionManager;

    private AccountManager accountManager;

    private ApplicationManager(Context applicationContext)
    {
        appContext = applicationContext;
        databaseManager = new DatabaseManager(appContext);
    }

    public static ApplicationManager getInstance(Context context)
    {
        synchronized (mLock)
        {
            if (mInstance == null)
            {
                mInstance = new ApplicationManager(context.getApplicationContext());
            }

            return mInstance;
        }
    }

    public DatabaseManager getDatabaseManager()
    {
        return databaseManager;
    }

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

    public RenditionManager getRenditionManager(Activity activity)
    {
        if (renditionManager == null && currentAccount != null)
        {
            renditionManager = new RenditionManager(activity, sessionIndex.get(currentAccount.getId()));
        }
        return renditionManager;
    }

    public void setRenditionManager(RenditionManager renditionManager)
    {
        this.renditionManager = renditionManager;
    }

    public void setAccountManager(AccountManager manager)
    {
       this.accountManager = manager;
    }

    public AccountManager getAccountManager()
    {
        return accountManager;
    }
}
