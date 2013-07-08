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
package org.alfresco.mobile.android.application.accounts.oauth;

import java.util.Date;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class OAuthRefreshTokenCallback implements LoaderCallbacks<LoaderResult<OAuthData>>
{

    private static final String TAG = OAuthRefreshTokenCallback.class.getName();

    private static final String KEY_CLOUD_LOADING_TIME = "cloudLoadingTime";

    /**
     * flag to indicate it's not necessary to keep cloud session creation
     * datetime.
     */
    private static final long DEFAULT_LOADING_TIME = -1;

    /**
     * We want to refresh token after 50 minutes even if the official timeout is
     * 60 min.
     */
    private static final long DEFAULT_LOADING_TIMEOUT = 3000000;

    private Account acc;

    private Activity activity;

    private Context appContext;

    private CloudSession session;

    public OAuthRefreshTokenCallback(Activity activity, Account acc, CloudSession session)
    {
        this.activity = activity;
        this.appContext = activity.getApplicationContext();
        this.acc = acc;
        this.session = session;
    }

    @Override
    public Loader<LoaderResult<OAuthData>> onCreateLoader(final int id, Bundle args)
    {
        if (activity != null)
        {
            activity.setProgressBarIndeterminateVisibility(true);
            WaitingDialogFragment.newInstance(R.string.app_name, R.string.error_session_refresh, true).show(
                    activity.getFragmentManager(), WaitingDialogFragment.TAG);
        }
        return new OAuthRefreshTokenLoader(activity, session);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<OAuthData>> loader, LoaderResult<OAuthData> results)
    {
        if (activity != null)
        {
            OAuthRefreshTokenCallback.saveLastCloudLoadingTime(activity);
            activity.setProgressBarIndeterminateVisibility(false);
        }
        if (!results.hasException())
        {
            saveNewOauthData(results);
        }
        else
        {
            switch ((int) acc.getTypeId())
            {
                case Account.TYPE_ALFRESCO_TEST_OAUTH:
                case Account.TYPE_ALFRESCO_CLOUD:
                    CloudExceptionUtils.handleCloudException(activity, results.getException(), true);
                    break;
                default:
                    break;
            }
            Log.e(TAG, Log.getStackTraceString(results.getException()));
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<OAuthData>> loader)
    {

    }

    private void saveNewOauthData(LoaderResult<OAuthData> loader)
    {
        switch ((int) acc.getTypeId())
        {
            case Account.TYPE_ALFRESCO_TEST_OAUTH:
            case Account.TYPE_ALFRESCO_CLOUD:
                acc = AccountManager.update(appContext, acc.getId(), acc.getDescription(), acc.getUrl(), acc
                        .getUsername(), acc.getPassword(), acc.getRepositoryId(),
                        Integer.valueOf((int) acc.getTypeId()), null, loader.getData().getAccessToken(), loader
                                .getData().getRefreshToken(), acc.getIsPaidAccount() ? 1 : 0);
                break;
            default:
                break;
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, acc.getId());
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(
                new Intent(IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED).putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID,
                        acc.getId()));
    }

    /**
     * Utility method to flag when the cloud session has been created for the
     * last time. This time is used to check Oauth timeout.
     * 
     * @param context
     */
    public static void saveLastCloudLoadingTime(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPref.edit();
        editor.putLong(KEY_CLOUD_LOADING_TIME, new Date().getTime());
        editor.commit();
    }

    /**
     * Utility method to remove the flag when the cloud session has been
     * created.
     * 
     * @param context
     */
    public static void removeLastCloudLoadingTime(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPref.edit();
        editor.remove(KEY_CLOUD_LOADING_TIME);
        editor.commit();
    }

    /**
     * Indicates if the application must requires a new refresh token.
     * 
     * @param context
     */
    public static boolean doesRequireRefreshToken(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        long activationTime = sharedPref.getLong(KEY_CLOUD_LOADING_TIME, DEFAULT_LOADING_TIME);
        if (activationTime == DEFAULT_LOADING_TIME) { return false; }

        long now = new Date().getTime();
        return (now - activationTime) > DEFAULT_LOADING_TIMEOUT;
    }

    /**
     * Request if necessary a new refresh token for cloudSession.
     * 
     * @param alfrescoSession
     * @param activity
     */
    public static void requestRefreshToken(AlfrescoSession alfrescoSession, Activity activity)
    {
        if (alfrescoSession instanceof CloudSession && doesRequireRefreshToken(activity))
        {
            ActionManager.actionRequestAuthentication(activity, SessionUtils.getAccount(activity));
        }
    }

}
