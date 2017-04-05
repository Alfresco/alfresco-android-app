/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.security;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.features.PasscodeConfigFeature;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class PassCodeActivity extends FragmentActivity
{
    public static final int REQUEST_CODE_PASSCODE = 48976;

    public static final String REQUEST_DEFINE_PASSCODE = "definePasscode";

    // ///////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_passcode_panel);

        if (getSupportFragmentManager().findFragmentByTag(PassCodeDialogFragment.TAG) == null)
        {
            PassCodeDialogFragment f;
            if (getIntent() != null && getIntent().getAction() != null)
            {
                f = PassCodeDialogFragment.define();
            }
            else
            {
                f = PassCodeDialogFragment.requestPasscode();
            }
            FragmentDisplayer.with(this).load(f).back(false).animate(null).into(FragmentDisplayer.PANEL_LEFT);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (AlfrescoAccountManager.getInstance(this).isEmpty() && AlfrescoAccountManager.getInstance(this).hasData())
        {
            finish();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!PasscodePreferences.hasPasscodeEnable(this))
        {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    public static void requestUserPasscode(FragmentActivity activity)
    {
        PasscodeConfigFeature feature = new PasscodeConfigFeature(activity);
        if (feature.isProtected() && !PasscodePreferences.hasPasscode(activity))
        {
            Intent i = new Intent(activity, PassCodeActivity.class);
            i.setAction(REQUEST_DEFINE_PASSCODE);
            activity.startActivityForResult(i, REQUEST_CODE_PASSCODE);
        }
        else if (PasscodePreferences.hasPasscodeEnable(activity))
        {
            activity.startActivityForResult(new Intent(activity, PassCodeActivity.class), REQUEST_CODE_PASSCODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PassCodeActivity.REQUEST_CODE_PASSCODE && resultCode == RESULT_CANCELED)
        {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onBackPressed()
    {
        // We go back to the device home.
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

}
