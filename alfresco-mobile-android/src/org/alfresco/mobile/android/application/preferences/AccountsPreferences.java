package org.alfresco.mobile.android.application.preferences;

import java.util.List;

import org.alfresco.mobile.android.application.accounts.Account;

import android.app.Activity;
import android.content.SharedPreferences;

public class AccountsPreferences
{
    public static final String ACCOUNT_PREFS = "org.alfresco.mobile.android.account.preferences";

    public static final String ACCOUNT_DEFAULT = "org.alfresco.mobile.android.account.preferences.default";

    public static Account getDefaultAccount(Activity activity, List<Account> accounts)
    {
        Account currentAccount = null;
        // Default account to load
        SharedPreferences settings = activity.getSharedPreferences(ACCOUNT_PREFS, 0);
        Long defaultAccountId = settings.getLong(ACCOUNT_DEFAULT, -1);

        if (defaultAccountId == -1)
        {
            currentAccount = accounts.get(0);
        }

        for (Account account : accounts)
        {
            if (account.getId() == defaultAccountId)
            {
                currentAccount = account;
                break;
            }
        }
        return currentAccount;
    }

}
