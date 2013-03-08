/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.accounts.fragment;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.content.AsyncTaskLoader;
import android.content.Context;

public class AccountsLoader extends AsyncTaskLoader<List<Account>>
{
    public static final int ID = 46893278;

    private List<Account> mApps;

    private Context context;

    public AccountsLoader(Context context)
    {
        super(context);
        this.context = context;
    }

    @Override
    public List<Account> loadInBackground()
    {
        AccountDAO accountDao = new AccountDAO(context, SessionUtils.getDataBaseManager(context).getWriteDb());
        return new ArrayList<Account>(accountDao.findAll());
    }

    @Override
    public void deliverResult(List<Account> data)
    {
        mApps = data;

        if (isStarted())
        {
            super.deliverResult(data);
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
}
