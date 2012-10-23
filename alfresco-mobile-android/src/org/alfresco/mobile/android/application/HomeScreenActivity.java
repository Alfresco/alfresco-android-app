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

import java.util.List;

import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.fragment.AccountsLoader;
import org.alfresco.mobile.android.application.accounts.fragment.WizardConfirmationFragment;
import org.alfresco.mobile.android.application.accounts.fragment.WizardSelectAccountFragment;
import org.alfresco.mobile.android.application.accounts.signup.SignupCloudDialogFragment;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;

public class HomeScreenActivity extends Activity implements LoaderCallbacks<List<Account>>
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_single);
        getLoaderManager().restartLoader(AccountsLoader.ID, null, this);
        getLoaderManager().getLoader(AccountsLoader.ID).forceLoad();
        findViewById(R.id.left_pane).setVisibility(View.GONE);
        getActionBar().hide();

        if (getFragmentManager().findFragmentByTag(HomeScreenFragment.TAG) == null)
        {
            HomeScreenFragment newFragment = new HomeScreenFragment();
            FragmentDisplayer.replaceFragment(this, newFragment, DisplayUtils.getLeftFragmentId(this),
                    HomeScreenFragment.TAG, false);
        }
    }

    public void cloud(View v)
    {
        SignupCloudDialogFragment newFragment = new SignupCloudDialogFragment();
        FragmentDisplayer.replaceFragment(this, newFragment, DisplayUtils.getLeftFragmentId(this),
                SignupCloudDialogFragment.TAG, true);
    }

    public void launch(View v)
    {
        WizardSelectAccountFragment newFragment = new WizardSelectAccountFragment();
        FragmentDisplayer.replaceFragment(this, newFragment, DisplayUtils.getLeftFragmentId(this),
                WizardSelectAccountFragment.TAG, true);
    }

    public void validateServer()
    {
        WizardConfirmationFragment newFragment = new WizardConfirmationFragment();
        FragmentDisplayer.replaceFragment(this, newFragment, DisplayUtils.getLeftFragmentId(this),
                WizardConfirmationFragment.TAG, false);
    }

    @Override
    public Loader<List<Account>> onCreateLoader(int id, Bundle args)
    {
        return new AccountsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Account>> arg0, List<Account> results)
    {
        if (results != null && results.size() > 0)
        {
            Intent i = new Intent(this, MainActivity.class);
            this.startActivity(i);
            finish();
        }
        else
        {
            getActionBar().show();
            findViewById(R.id.left_pane).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if (IntentIntegrator.ACTION_REFRESH.equals(intent.getAction())
                && intent.getCategories().contains(IntentIntegrator.CATEGORY_REFRESH)
                && IntentIntegrator.ACCOUNT_TYPE.equals(intent.getType()))
        {
            validateServer();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Account>> arg0)
    {
        // TODO Auto-generated method stub

    }
}
