/*******************************************************************************
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.intent.AlfrescoIntentAPI;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

/**
 * Base class for all activities.
 * 
 * @author Jean Marie Pascal
 */
public abstract class BaseActivity extends AlfrescoActivity
{
    protected boolean activateCheckPasscode = false;

    public final static int REQUEST_PERMISSION_SD = 70;

    public final static int REQUEST_PERMISSION_DL = 80;

    public final static int REQUEST_PERMISSION_IMPORT_SD = 90;

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
            SessionManager.getInstance(this)
                    .saveAccount(AlfrescoAccountManager.getInstance(this).retrieveAccount(accountId));
        }

        if (getIntent().hasExtra(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID))
        {
            long accountId = getIntent().getExtras().getLong(AlfrescoIntentAPI.EXTRA_ACCOUNT_ID);
            SessionManager.getInstance(this)
                    .saveAccount(AlfrescoAccountManager.getInstance(this).retrieveAccount(accountId));
        }
    }

    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean indeterminate)
    {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_spinner);

        if (progressBar == null) { return; }

        if (indeterminate)
        {
            progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            progressBar.setVisibility(View.GONE);
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
        super.onActivityResult(requestCode, resultCode, data);
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

    @Override
    public ActionMode startActionMode(final ActionMode.Callback callback)
    {
        // Fix for bug https://code.google.com/p/android/issues/detail?id=159527
        final ActionMode mode = super.startActionMode(callback);
        if (mode != null)
        {
            mode.invalidate();
        }
        return mode;
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
    protected void displayAsDialogActivity(double defaultCoefficient, double heightCoefficient)
    {
        // supportRequestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        WindowManager.LayoutParams params = getWindow().getAttributes();

        int[] values = UIUtils.getScreenDimension(this);
        int height = values[1];
        int width = values[0];

        double coefficient = defaultCoefficient;
        try
        {
            TypedValue outValue = new TypedValue();
            getResources().getValue(R.dimen.dialog_min_width_minor, outValue, true);
            coefficient = outValue.getFloat();
        }
        catch (Exception e)
        {
            coefficient = defaultCoefficient;
        }

        params.height = (int) Math.round(height * heightCoefficient);
        params.width = (int) Math.round(width * coefficient);

        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);
    }

    protected void displayAsDialogActivity()
    {
        displayAsDialogActivity(0.90f, 0.9);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_PERMISSION_SD:
            case REQUEST_PERMISSION_DL:
            case REQUEST_PERMISSION_IMPORT_SD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (requestCode == REQUEST_PERMISSION_IMPORT_SD)
                    {

                    }
                    else
                    {
                        FileExplorerFragment.with(this)
                                .file(requestCode == REQUEST_PERMISSION_DL ? Environment.getExternalStorageDirectory()
                                        : Environment
                                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                                .display();
                    }

                    // Permission Granted
                    AnalyticsHelper.reportOperationEvent(this, AnalyticsManager.CATEGORY_SETTINGS,
                            AnalyticsManager.ACTION_GRANT_PERMISSION, AnalyticsManager.LABEL_STORAGE, 1, false);
                }
                else
                {
                    if (requestCode == REQUEST_PERMISSION_IMPORT_SD)
                    {
                        finish();
                        AlfrescoNotificationManager.getInstance(this).showLongToast(R.string.permission_not_enough);
                    }

                    // Permission Denied
                    AnalyticsHelper.reportOperationEvent(this, AnalyticsManager.CATEGORY_SETTINGS,
                            AnalyticsManager.ACTION_DENY_PERMISSION, AnalyticsManager.LABEL_STORAGE, 1, false);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
