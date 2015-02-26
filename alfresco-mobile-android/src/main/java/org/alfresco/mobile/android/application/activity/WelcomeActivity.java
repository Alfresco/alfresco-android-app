/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.application.activity;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.help.HelpDialogFragment;
import org.alfresco.mobile.android.application.fragments.welcome.WelcomeFragment;
import org.alfresco.mobile.android.async.account.CreateAccountEvent;
import org.alfresco.mobile.android.async.session.oauth.RetrieveOAuthDataEvent;
import org.alfresco.mobile.android.platform.extensions.MobileIronManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;

import com.squareup.otto.Subscribe;

/**
 * Displays a wizard for the first AlfrescoAccount creation.
 * 
 * @author Jean Marie Pascal
 */
@TargetApi(21)
public class WelcomeActivity extends BaseActivity
{
    private static final String TAG = WelcomeActivity.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_single);

        if (getFragment(WelcomeFragment.TAG) == null)
        {
            FragmentDisplayer.load(WelcomeFragment.with(this).back(false)).animate(null)
                    .into(FragmentDisplayer.PANEL_LEFT);
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        if (intent.getAction() == null || intent.getData() == null || !Intent.ACTION_VIEW.equals(intent.getAction())) { return; }

        if (PrivateIntent.ALFRESCO_SCHEME_SHORT.equals(intent.getData().getScheme())
                && PrivateIntent.HELP_GUIDE.equals(intent.getData().getHost()))
        {
            HelpDialogFragment.with(this).display();
        }
    }

    @Override
    public void onBackPressed()
    {
        if (MobileIronManager.getInstance(this) != null && getFragment(WelcomeFragment.TAG) != null)
        {
            finish();
        }
        else
        {
            super.onBackPressed();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onRetrieveOAuthDataEvent(RetrieveOAuthDataEvent event)
    {
        displayWaitingDialog();
    }

    @Subscribe
    public void onAccountCreated(CreateAccountEvent event)
    {
        if (event.hasException) { return; }
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, event.data.getId());
        startActivity(i);
        finish();
    }
}
