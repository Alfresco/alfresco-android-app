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
import org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences;
import org.alfresco.mobile.android.application.intent.AlfrescoIntentAPI;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

/**
 * Base class for all activities.
 * 
 * @author Jean Marie Pascal
 */
public abstract class BaseActivity extends AlfrescoActivity
{
    protected boolean activateCheckPasscode = false;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public BaseActivity()
    {
        telescopeId = R.id.telescope;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activateCheckPasscode = false;

        // Check intent
        if (getIntent().hasExtra(PrivateIntent.EXTRA_ACCOUNT_ID))
        {
            long accountId = getIntent().getExtras().getLong(PrivateIntent.EXTRA_ACCOUNT_ID);
            currentAccount = AlfrescoAccountManager.getInstance(this).retrieveAccount(accountId);
        }

        if (getIntent().hasExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID))
        {
            long accountId = getIntent().getExtras().getLong(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID);
            currentAccount = AlfrescoAccountManager.getInstance(this).retrieveAccount(accountId);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        PassCodeActivity.requestUserPasscode(this);
        activateCheckPasscode = PasscodePreferences.hasPasscodeEnable(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (!activateCheckPasscode)
        {
            PasscodePreferences.updateLastActivity(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PassCodeActivity.REQUEST_CODE_PASSCODE)
        {
            if (resultCode == RESULT_CANCELED)
            {
                finish();
            }
            else
            {
                activateCheckPasscode = true;
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PASSCODE PROTECTION
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasActivateCheckPasscode()
    {
        return activateCheckPasscode;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected void displayAsDialogActivity()
    {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        WindowManager.LayoutParams params = getWindow().getAttributes();

        int[] values = UIUtils.getScreenDimension(this);
        int height = values[1];
        int width = values[0];

        double coefficient = 0.90f;
        try
        {
            TypedValue outValue = new TypedValue();
            getResources().getValue(R.dimen.dialog_min_width_minor, outValue, true);
            coefficient = outValue.getFloat();
        }
        catch (Exception e)
        {
            coefficient = 0.90f;
        }

        params.height = (int) Math.round(height * 0.9);
        params.width = (int) Math.round(width * coefficient);

        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);
    }
}
