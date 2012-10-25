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
package org.alfresco.mobile.android.application.accounts.fragment;

import java.net.URL;

import org.alfresco.mobile.android.api.asynchronous.CloudSessionLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.api.constants.OAuthConstant;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

@TargetApi(11)
public class AccountCreationLoaderCallback extends AbstractSessionCallback
{
    private static final String TAG = "AccountCreationLoaderCallback";

    private URL url;

    private ProgressDialog mProgressDialog;

    private Fragment fr;

    private static final String ARGUMENT_URL = "agumentUrl";

    private int attempt = 0;

    private String description;

    /**
     * Case Basic Auth
     */
    public AccountCreationLoaderCallback(Activity activity, Fragment fr, String url, String username, String password,
            String description)
    {
        this.activity = activity;
        this.baseUrl = url;
        this.username = username;
        this.password = password;
        this.description = description;
        this.fr = fr;
    }

    /**
     * Case OAuth
     */
    public AccountCreationLoaderCallback(Activity activity, Fragment fr, OAuthData data)
    {
        this.activity = activity;
        this.fr = fr;
        this.data = data;
    }

    @Override
    public Loader<LoaderResult<AlfrescoSession>> onCreateLoader(final int id, Bundle args)
    {

        // TODO Check this portion
        if (args != null)
        {
            if (args.getString(ARGUMENT_URL) == null)
            {
                mProgressDialog.dismiss();
                MessengerManager.showToast(activity, R.string.error_signin_form_mandatory);
                return null;
            }
            return new SessionLoader(activity, args.getString(ARGUMENT_URL), username, password);
        }

        mProgressDialog = ProgressDialog.show(activity, getText(R.string.wait_title), getText(R.string.wait_message),
                true, true, new OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        activity.getLoaderManager().destroyLoader(id);
                    }
                });

        return getSessionLoader(activity, baseUrl, username, password, data, false, true);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<AlfrescoSession>> loader, LoaderResult<AlfrescoSession> results)
    {
        AlfrescoSession session = results.getData();
        if (session != null)
        {
            SessionUtils.setsession(activity, session);
            AccountDAO serverDao = new AccountDAO(activity, SessionUtils.getDataBaseManager(activity).getWriteDb());
            long id = -1L;
            if (data == null)
            {
                int type = Integer.valueOf(Account.TYPE_ALFRESCO_CMIS);
                if (session instanceof CloudSession
                        && !session.getBaseUrl().startsWith(OAuthConstant.PUBLIC_API_HOSTNAME))
                {
                    type = (data != null) ? Integer.valueOf(Account.TYPE_ALFRESCO_TEST_OAUTH) : Integer
                            .valueOf(Account.TYPE_ALFRESCO_TEST_BASIC);
                }
                else
                {
                    type = (session instanceof CloudSession) ? Integer.valueOf(Account.TYPE_ALFRESCO_CLOUD) : Integer
                            .valueOf(Account.TYPE_ALFRESCO_CMIS);
                }

                id = serverDao.insert(description, baseUrl, username, password, session.getRepositoryInfo()
                        .getIdentifier(), type, null, null);
            }
            else
            {
                Person user = null;
                if (loader instanceof CloudSessionLoader)
                {
                    user = ((CloudSessionLoader) loader).getUser();
                }

                int type = Integer.valueOf(Account.TYPE_ALFRESCO_CLOUD);
                if (session instanceof CloudSession
                        && !session.getBaseUrl().startsWith(OAuthConstant.PUBLIC_API_HOSTNAME))
                {
                    type = (data != null) ? Integer.valueOf(Account.TYPE_ALFRESCO_TEST_OAUTH) : Integer
                            .valueOf(Account.TYPE_ALFRESCO_TEST_BASIC);
                }

                // TODO Enable refreshtoken instead of fake value.
                id = serverDao.insert("Alfresco Cloud", session.getBaseUrl(), user.getIdentifier(), null, session
                        .getRepositoryInfo().getIdentifier(), type, ((CloudSession) session).getOAuthData()
                        .getAccessToken(), ((CloudSessionLoader) loader).getOAuthData().getRefreshToken()
                        );
            }

            SessionUtils.setAccount(activity, serverDao.findById(id));

            if (fr != null)
            {
                ActionManager.actionRefresh(fr, IntentIntegrator.CATEGORY_REFRESH_ALL, IntentIntegrator.ACCOUNT_TYPE);
            }
            mProgressDialog.dismiss();
        }
        else
        {
            LoaderManager lm = activity.getLoaderManager();
            Bundle b = new Bundle();
            String s = getNextURl();
            if (s != null)
            {
                b.putString(ARGUMENT_URL, s);
                lm.restartLoader(SessionLoader.ID, b, this);
                lm.getLoader(SessionLoader.ID).forceLoad();
            }
            else
            {
                mProgressDialog.dismiss();
                MessengerManager
                        .showToast(activity, getText(R.string.error_session_creation) + results.getException().getMessage());
                Log.e(TAG, Log.getStackTraceString(results.getException()));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<AlfrescoSession>> arg0)
    {
        mProgressDialog.dismiss();
    }

    private String getNextURl()
    {
        String newUrl = null;
        try
        {
            switch (attempt)
            {
                case 0:
                    url = new URL(baseUrl);
                    newUrl = new URL("https", url.getHost(), 443, url.getPath()).toString();
                    attempt++;
                    break;

                case 1:
                    url = new URL(baseUrl);
                    newUrl = new URL("http", url.getHost(), 8080, url.getPath()).toString();
                    attempt++;
                    break;

                case 2:
                    url = new URL(baseUrl);
                    newUrl = new URL(url.getProtocol(), url.getHost(), url.getPath()).toString();
                    attempt++;
                    break;

                default:
                    break;
            }
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
        return newUrl;
    }

}
