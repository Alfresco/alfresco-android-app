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
package org.alfresco.mobile.android.application;

import org.alfresco.mobile.android.api.asynchronous.CloudSessionLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.accounts.fragment.AbstractSessionCallback;
import org.alfresco.mobile.android.application.accounts.fragment.SessionSettingsHelper;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

@TargetApi(11)
public class LoginLoaderCallback extends AbstractSessionCallback
{
    private Account acc;
    private SessionSettingsHelper settingsHelper;

    public LoginLoaderCallback(Activity activity, Account acc)
    {
        this.activity = activity;
        this.acc = acc;
        settingsHelper = new SessionSettingsHelper(activity, acc);
    }

    @Override
    public Loader<LoaderResult<AlfrescoSession>> onCreateLoader(final int id, Bundle args)
    {
        return getSessionLoader(settingsHelper);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<AlfrescoSession>> loader, LoaderResult<AlfrescoSession> results)
    {

        if (!results.hasException())
        {

            switch ((int) acc.getTypeId())
            {
                case Account.TYPE_ALFRESCO_TEST_OAUTH:
                case Account.TYPE_ALFRESCO_CLOUD:
                    AccountDAO accountDao = new AccountDAO(activity, SessionUtils.getDataBaseManager(activity)
                            .getWriteDb());
                    accountDao.update(acc.getId(), acc.getDescription(), acc.getUrl(), acc.getUsername(),
                            acc.getPassword(), acc.getRepositoryId(), Integer.valueOf((int) acc.getTypeId()), null,
                            ((CloudSessionLoader) loader).getOAuthData().getAccessToken(),
                            ((CloudSessionLoader) loader).getOAuthData().getRefreshToken());
                    break;
            }

            SessionUtils.setsession(activity, results.getData());
            Intent i = new Intent(activity, MainActivity.class);
            i.setAction(IntentIntegrator.ACTION_LOAD_SESSION_FINISH);
            activity.startActivity(i);
        }
        else
        {
            MessengerManager.showLongToast(activity, "ERROR : Session not loaded : "
                    + results.getException().getMessage());
            Log.e("Session", Log.getStackTraceString(results.getException()));
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<AlfrescoSession>> arg0)
    {
        // TODO Auto-generated method stub

    }

}
