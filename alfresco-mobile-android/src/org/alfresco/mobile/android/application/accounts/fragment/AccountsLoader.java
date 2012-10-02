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
package org.alfresco.mobile.android.application.accounts.fragment;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.database.DatabaseManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.content.AsyncTaskLoader;
import android.content.Context;

public class AccountsLoader extends AsyncTaskLoader<List<Account>>
{

    public static final int ID = 46893278;

    List<Account> mApps;

    private Context context;

    private DatabaseManager db;

    public AccountsLoader(Context context)
    {
        super(context);
        this.context = context;
    }

    @Override
    public List<Account> loadInBackground()
    {
        AccountDAO AccountDao = new AccountDAO(context, SessionUtils.getDataBaseManager(context).getWriteDb());
        ArrayList<Account> l = new ArrayList<Account>(AccountDao.findAll());
        return l;
    }

    @Override
    public void deliverResult(List<Account> data)
    {
        if (isReset())
        {
            if (data != null)
            {
                onReleaseResources(data);
            }
        }
        List<Account> oldApps = data;
        mApps = data;

        if (isStarted())
        {
            super.deliverResult(data);
        }

        if (oldApps != null)
        {
            onReleaseResources(oldApps);
        }
    }

    @Override
    protected void onStartLoading()
    {
        if (mApps != null)
        {
            deliverResult(mApps);
        }
        else
        {
            forceLoad();
        }
    }

    @Override
    public void onCanceled(List<Account> data)
    {
        if (db != null)
        {
            db.close();
        }
        super.onCanceled(data);
    }

    @Override
    protected void onAbandon()
    {
        if (db != null)
        {
            db.close();
        }
        super.onAbandon();
    }

    protected void onReleaseResources(List<Account> apps)
    {
        if (db != null)
        {
            db.close();
        }
    }
}
