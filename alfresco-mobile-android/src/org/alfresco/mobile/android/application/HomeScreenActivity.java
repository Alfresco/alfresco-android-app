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
package org.alfresco.mobile.android.application;

import org.alfresco.mobile.android.application.accounts.fragment.AccountTypesFragment;
import org.alfresco.mobile.android.application.accounts.signup.CloudSignupDialogFragment;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class HomeScreenActivity extends BaseActivity
{
    private static final String TAG = HomeScreenActivity.class.getName();

    
    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_single);

        if (getFragmentManager().findFragmentByTag(HomeScreenFragment.TAG) == null)
        {
            HomeScreenFragment newFragment = new HomeScreenFragment();
            FragmentDisplayer.replaceFragment(this, newFragment, DisplayUtils.getLeftFragmentId(this),
                    HomeScreenFragment.TAG, false);
        }
    }

    @Override
    protected void onStart()
    {
        IntentFilter filters = new IntentFilter();
        filters.addAction(IntentIntegrator.ACTION_CREATE_ACCOUNT_STARTED);
        filters.addAction(IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED);
        registerPrivateReceiver(new HomeScreenReceiver(), filters);
        super.onStart();
    }
    
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        // Use if OAuth process goes wrong
        if (IntentIntegrator.ACTION_DISPLAY_ERROR_HOMESCREEN.equals(intent.getAction()))
        {
            if (getFragment(WaitingDialogFragment.TAG) != null)
            {
                ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
            }

            if (intent.getExtras() != null)
            {
                Exception e = (Exception) intent.getExtras().getSerializable(IntentIntegrator.DISPLAY_ERROR_DATA);
                CloudExceptionUtils.handleCloudException(this, e, false);
            }
            MessengerManager.showLongToast(this, getString(R.string.error_general));
            getFragmentManager().popBackStack();

            return;
        }

        if (intent.getAction() != null && intent.getData() != null && Intent.ACTION_VIEW.equals(intent.getAction())
                && IntentIntegrator.ALFRESCO_SCHEME_SHORT.equals(intent.getData().getScheme())
                && IntentIntegrator.CLOUD_SIGNUP.equals(intent.getData().getHost()))
        {
            getFragmentManager().popBackStack(AccountTypesFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            cloud(null);
        }

        // Intent for Display Dialog
        if (IntentIntegrator.ACTION_DISPLAY_DIALOG_HOMESCREEN.equals(intent.getAction()))
        {
            SimpleAlertDialogFragment.newInstance(intent.getExtras()).show(getFragmentManager(),
                    SimpleAlertDialogFragment.TAG);
            return;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void cloud(View v)
    {
        CloudSignupDialogFragment newFragment = new CloudSignupDialogFragment();
        FragmentDisplayer.replaceFragment(this, newFragment, DisplayUtils.getLeftFragmentId(this),
                CloudSignupDialogFragment.TAG, true);
    }

    public void launch(View v)
    {
        AccountTypesFragment newFragment = new AccountTypesFragment();
        FragmentDisplayer.replaceFragment(this, newFragment, DisplayUtils.getLeftFragmentId(this),
                AccountTypesFragment.TAG, true);
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    private class HomeScreenReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            
            Log.d(TAG, intent.getAction());

            if (IntentIntegrator.ACTION_CREATE_ACCOUNT_STARTED.equals(intent.getAction()))
            {
                displayWaitingDialog();
                return;
            }
            
            if (IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED.equals(intent.getAction()))
            {
                removeWaitingDialog();
                Intent i = new Intent(activity, MainActivity.class);
                i.putExtras(intent.getExtras());
                activity.startActivity(i);
                activity.finish();
                return;
            }
            return;
        }
    }
}
