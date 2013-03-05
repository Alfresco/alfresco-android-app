/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.preferences;

import java.util.List;

import org.alfresco.mobile.android.application.accounts.Account;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Manage application preferences associated to accounts objects.
 * 
 * @author Jean Marie Pascal
 */
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
        if (currentAccount == null)
        {
            currentAccount = accounts.get(0);
        }

        return currentAccount;
    }

}
