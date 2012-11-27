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
package org.alfresco.mobile.android.application.accounts.oauth;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

@TargetApi(11)
public class OAuthRefreshTokenCallback implements LoaderCallbacks<LoaderResult<OAuthData>>
{

    private static final String TAG = "AccountLoginLoaderCallback";

    private Account acc;

    private Activity activity;

    private CloudSession session;

    public OAuthRefreshTokenCallback(Activity activity, Account acc, CloudSession session)
    {
        this.activity = activity;
        this.acc = acc;
        this.session = session;
    }

    @Override
    public Loader<LoaderResult<OAuthData>> onCreateLoader(final int id, Bundle args)
    {

        Loader<LoaderResult<OAuthData>> loader = new OAuthRefreshTokenLoader(activity, session);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<OAuthData>> loader, LoaderResult<OAuthData> results)
    {
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
            }
            Log.e(TAG, Log.getStackTraceString(results.getException()));
        }
        activity.setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<OAuthData>> loader)
    {

    }

    private void saveNewOauthData(LoaderResult<OAuthData> loader)
    {
        Log.d(TAG, loader.toString());
        switch ((int) acc.getTypeId())
        {
            case Account.TYPE_ALFRESCO_TEST_OAUTH:
            case Account.TYPE_ALFRESCO_CLOUD:
                AccountDAO accountDao = new AccountDAO(activity, SessionUtils.getDataBaseManager(activity).getWriteDb());
                if (accountDao.update(acc.getId(), acc.getDescription(), acc.getUrl(), acc.getUsername(), acc
                        .getPassword(), acc.getRepositoryId(), Integer.valueOf((int) acc.getTypeId()), null, loader
                        .getData().getAccessToken(), loader.getData().getRefreshToken()))
                {
                    SessionUtils.setAccount(activity, accountDao.findById(acc.getId()));
                }
                break;
        }
        Intent i = new Intent(activity, MainActivity.class);
        i.setAction(IntentIntegrator.ACTION_LOAD_SESSION_FINISH);
        activity.startActivity(i);
    }

}
