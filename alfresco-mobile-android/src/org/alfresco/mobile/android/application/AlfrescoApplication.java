/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.database.DatabaseManager;

import android.app.Application;

public class AlfrescoApplication extends Application
{

    //-------------------------------------------------------------------------------------
    //FOR ALFRESCO APP
    //-------------------------------------------------------------------------------------

    private Account account;
    private AlfrescoSession alfSession;
    private DatabaseManager databaseManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        databaseManager = new DatabaseManager(this);
    }
    
    
    public AlfrescoSession getRepositorySession()
    {
        return alfSession;
    }

    public void setRepositorySession(AlfrescoSession alfSession)
    {
        this.alfSession = alfSession;
    }

    public DatabaseManager getDatabaseManager()
    {
        return databaseManager;
    }

    public void setDatabaseManager(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }


    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        this.account = account;
    }
}
