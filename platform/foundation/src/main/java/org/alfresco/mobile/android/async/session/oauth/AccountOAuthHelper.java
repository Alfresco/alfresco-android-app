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
package org.alfresco.mobile.android.async.session.oauth;

import java.util.Date;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;

public class AccountOAuthHelper
{

    private static final String TAG = AccountOAuthHelper.class.getName();

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

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    private AccountOAuthHelper()
    {
    }

    // //////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////
    public static void onNewOauthData(Context context, RetrieveOAuthDataEvent event)
    {
        AlfrescoAccount acc = SessionUtils.getAccount(context);
        AccountOAuthHelper.saveLastCloudLoadingTime(context);
        if (!event.hasException)
        {
            saveNewOauthData(context, acc, event.data);
        }
        else
        {
            switch (acc.getTypeId())
            {
                case AlfrescoAccount.TYPE_ALFRESCO_TEST_OAUTH:
                case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                    CloudExceptionUtils.handleCloudException(context, event.exception, true);
                    break;
                default:
                    break;
            }
            Log.e(TAG, Log.getStackTraceString(event.exception));
        }
    }

    public static AlfrescoAccount saveNewOauthData(Context context, AlfrescoAccount acc, OAuthData data)
    {
        AccountOAuthHelper.saveLastCloudLoadingTime(context);
        if (acc == null) { return null; }
        switch (acc.getTypeId())
        {
            case AlfrescoAccount.TYPE_ALFRESCO_TEST_OAUTH:
            case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                acc = AlfrescoAccountManager.getInstance(context).update(acc.getId(), acc.getTitle(), acc.getUrl(),
                        acc.getUsername(), acc.getPassword(), acc.getRepositoryId(), acc.getTypeId(), null,
                        data.getAccessToken(), data.getRefreshToken(), acc.getIsPaidAccount() ? 1 : 0);
                break;
            default:
                break;
        }
        return acc;
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
        editor.apply();
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
        editor.apply();
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
    public static void requestRefreshToken(AlfrescoSession alfrescoSession, FragmentActivity activity)
    {
        if (alfrescoSession != null && alfrescoSession instanceof CloudSession && doesRequireRefreshToken(activity))
        {
            Operator.with(activity).load(new RetrieveOAuthDataRequest.Builder((CloudSession) alfrescoSession));
        }
    }

}
