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

import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.application.HomeScreenActivity;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.menu.MainMenuFragment;
import org.alfresco.mobile.android.application.loaders.NodeLoader;
import org.alfresco.mobile.android.application.loaders.NodeLoaderCallback;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.nfc.NfcAdapter;
import android.os.Bundle;

public class AccountsLoaderCallback implements LoaderCallbacks<List<Account>>
{

    private MainActivity activity;

    public AccountsLoaderCallback(MainActivity activity)
    {
        this.activity = activity;
    }

    @Override
    public Loader<List<Account>> onCreateLoader(int id, Bundle args)
    {
        return new AccountsLoader(activity);
    }

    @Override
    public void onLoadFinished(Loader<List<Account>> arg0, List<Account> results)
    {
        if (results == null || results.isEmpty())
        {
            activity.startActivityForResult(new Intent(activity, HomeScreenActivity.class), 1);
            return;
        }

        // INTENT for CLOUD VALIDATION
        boolean signup = false;
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())
                && getIntent().getCategories().contains(Intent.CATEGORY_BROWSABLE)
                && getIntent().getData().getHost().equals("activate-cloud-account"))
        {
            for (Account account : results)
            {
                if (account.getActivation() != null
                        && account.getActivation().contains(getIntent().getData().getEncodedPath().substring(1).replace("%24", "$")))
                {
                    SessionUtils.setAccount(activity, account);
                    signup = true;
                    break;
                }
            }
            if (!signup){
                MessengerManager.showLongToast(activity, "Unable to find an account to activate.");
            }
        }
        else

        // VIEW INTENT
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())
                || Intent.ACTION_SEND.equals(getIntent().getAction()))
        {

            String url = getIntent().getDataString();
            if (Intent.ACTION_SEND.equals(getIntent().getAction()) && getIntent().getExtras() != null
                    && getIntent().getExtras().getString(Intent.EXTRA_TEXT) != null)
            {
                url = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
            }

            MessengerManager.showLongToast(activity, url);
            // Load First Account by default
            NodeLoaderCallback call = new NodeLoaderCallback(activity, results, url);
            LoaderManager lm = activity.getLoaderManager();
            lm.restartLoader(NodeLoader.ID, null, call);
            lm.getLoader(NodeLoader.ID).forceLoad();
            // return;
        }

        activity.setAccounts(results);
        if (activity.getFragmentManager().findFragmentByTag(MainMenuFragment.TAG) != null)
        {
            ((MainMenuFragment) activity.getFragmentManager().findFragmentByTag(MainMenuFragment.TAG))
                    .setAccounts(results);
        }

        // First creation of session.
        Account currentAccount = SessionUtils.getAccount(activity);
        if (currentAccount == null)
        {

            currentAccount = AccountsPreferences.getDefaultAccount(activity, results);
            if (SessionUtils.getSession(activity) == null && currentAccount.getActivation() == null
                    && ConnectivityUtils.hasInternetAvailable(activity))
            {
                activity.loadAccount(currentAccount);
            }
            SessionUtils.setAccount(activity, currentAccount);
        } else if(signup){
            activity.loadAccount(currentAccount);
        }
        else
        {
            // Case of config changes to retrieve the sessionLoader and continue
            // the work.
            if (SessionUtils.getSession(activity) == null
                    && (activity.getLoaderManager().getLoader(SessionLoader.ID) != null || signup))
            {
                activity.setProgressBarIndeterminateVisibility(true);
                AccountLoginLoaderCallback call = new AccountLoginLoaderCallback(activity, currentAccount);
                activity.getLoaderManager().initLoader(SessionLoader.ID, null, call);
            }

        }
        activity.invalidateOptionsMenu();
    }

    private Intent getIntent()
    {
        return activity.getIntent();
    }

    @Override
    public void onLoaderReset(Loader<List<Account>> arg0)
    {
        // TODO Auto-generated method stub

    }

}
