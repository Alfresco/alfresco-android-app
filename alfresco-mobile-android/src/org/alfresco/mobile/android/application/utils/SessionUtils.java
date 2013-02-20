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
package org.alfresco.mobile.android.application.utils;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.AlfrescoApplication;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.database.DatabaseManager;
import org.alfresco.mobile.android.application.integration.PublicDispatcherActivity;

import android.app.Activity;
import android.content.Context;

public class SessionUtils
{

    public static AlfrescoSession getSession(Context c)
    {
        if (c instanceof PublicDispatcherActivity)
        {
            return ((AlfrescoApplication) c.getApplicationContext()).getImportSession();
        }
        else
        {
            return ((AlfrescoApplication) c.getApplicationContext()).getRepositorySession();
        }
    }

    public static void setsession(Context c, AlfrescoSession s)
    {
        if (c instanceof PublicDispatcherActivity)
        {
            ((AlfrescoApplication) c.getApplicationContext()).setImportSession(s);
        }
        else
        {
            ((AlfrescoApplication) c.getApplicationContext()).setRepositorySession(s);
        }
    }

    public static boolean isCloudSession(Context c)
    {
        return getSession(c) instanceof CloudSession;
    }

    public static Account getAccount(Context c)
    {
        return ((AlfrescoApplication) c.getApplicationContext()).getAccount();
    }

    public static void setAccount(Context c, Account account)
    {
        ((AlfrescoApplication) c.getApplicationContext()).setAccount(account);
    }

    public static DatabaseManager getDataBaseManager(Context c)
    {
        DatabaseManager cdl = ((AlfrescoApplication) c.getApplicationContext()).getDatabaseManager();
        if (cdl == null)
        {
            cdl = initDataBaseManager((Activity) c);
        }
        return cdl;
    }

    private static DatabaseManager initDataBaseManager(Activity c)
    {
        if (getSession(c) != null)
        {
            ((AlfrescoApplication) c.getApplicationContext()).setDatabaseManager(new DatabaseManager(c
                    .getApplicationContext()));
        }
        else
        {
            return null;
        }
        return getDataBaseManager(c);
    }

}
