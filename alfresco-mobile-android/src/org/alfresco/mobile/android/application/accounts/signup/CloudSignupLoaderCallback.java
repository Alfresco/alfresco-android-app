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
package org.alfresco.mobile.android.application.accounts.signup;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.accounts.fragment.AccountDetailsFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountSettingsHelper;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Loader;
import android.os.Bundle;

public class CloudSignupLoaderCallback implements LoaderCallbacks<LoaderResult<CloudSignupRequest>>
{

    private Activity activity;

    private String firstName;

    private String lastName;

    private String emailAddress;

    private String password;

    private String description;

    private ProgressDialog mProgressDialog;

    private Fragment fr;

    public CloudSignupLoaderCallback(Activity activity, Fragment fr, String firstName, String lastName,
            String emailAddress, String password, String description)
    {
        this.activity = activity;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.description = description;
        this.fr = fr;
    }

    @Override
    public Loader<LoaderResult<CloudSignupRequest>> onCreateLoader(final int id, Bundle args)
    {

        mProgressDialog = ProgressDialog.show(activity, "Please wait", "Contacting your server...", true, true,
                new OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        activity.getLoaderManager().destroyLoader(id);
                    }
                });
        return new CloudSignupLoader(activity, firstName, lastName, emailAddress, password, activity.getText(
                R.string.signup_key).toString());
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<CloudSignupRequest>> arg0, LoaderResult<CloudSignupRequest> results)
    {
        CloudSignupRequest request = results.getData();
        if (request != null && fr instanceof CloudSignupDialogFragment)
        {
            AccountDAO serverDao = new AccountDAO(activity, SessionUtils.getDataBaseManager(activity).getWriteDb());
            // TODO replace

            if (serverDao.insert(description, AccountSettingsHelper.getSignUpHostname(), emailAddress, password, "",
                    Integer.valueOf(Account.TYPE_ALFRESCO_CLOUD),
                    request.getIdentifier() + "?key=" + request.getRegistrationKey(), null, null) != -1)
            {
                mProgressDialog.dismiss();
                ((CloudSignupDialogFragment) fr).displayAccounts();
            }
            else
            {
                MessengerManager.showLongToast(activity, "Error during insert.");
            }

        }
        else if (request != null && fr instanceof AccountDetailsFragment)
        {
            mProgressDialog.dismiss();
            activity.showDialog(MainActivity.CLOUD_RESEND_EMAIL);
        }
        else if (results.hasException())
        {
            mProgressDialog.dismiss();
            MessengerManager.showLongToast(activity, results.getException().getMessage());
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<CloudSignupRequest>> arg0)
    {
        mProgressDialog.dismiss();
    }
}
