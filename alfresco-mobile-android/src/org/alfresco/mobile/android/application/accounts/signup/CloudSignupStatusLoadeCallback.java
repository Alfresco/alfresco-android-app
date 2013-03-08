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
package org.alfresco.mobile.android.application.accounts.signup;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class CloudSignupStatusLoadeCallback implements LoaderCallbacks<LoaderResult<Boolean>>
{

    private static final String TAG = "CloudSignupStatusLoadeCallback";

    private Activity activity;

    private ProgressDialog mProgressDialog;

    private Fragment fr;

    private CloudSignupRequest request;

    public CloudSignupStatusLoadeCallback(Activity activity, Fragment fr, Account acc)
    {
        this.activity = activity;
        this.fr = fr;
        if (acc != null)
        {
            this.request = new CloudSignupRequest(acc);
        }
    }

    @Override
    public Loader<LoaderResult<Boolean>> onCreateLoader(final int id, Bundle args)
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
        return new CloudSignupStatusLoader(activity, request, activity.getString(R.string.signup_key));
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Boolean>> loader, LoaderResult<Boolean> results)
    {
        mProgressDialog.dismiss();
        Boolean hasData = results.getData();
        if (results.hasException())
        {
            Log.e(TAG, Log.getStackTraceString(results.getException()));
            MessengerManager.showLongToast(activity, activity.getString(R.string.error_general));
        }
        else if (hasData)
        {
            fr.getLoaderManager().destroyLoader(CloudSignupStatusLoader.ID);
            validateAccount();
        }
        else
        {
            MessengerManager.showLongToast(activity, activity.getString(R.string.account_not_activated_description));
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<Boolean>> arg0)
    {
        mProgressDialog.dismiss();
    }

    private void validateAccount()
    {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(IntentIntegrator.ALFRESCO_SCHEME_SHORT
                + "://activate-cloud-account/" + request.getIdentifier()));
        fr.startActivity(i);
    }
}
