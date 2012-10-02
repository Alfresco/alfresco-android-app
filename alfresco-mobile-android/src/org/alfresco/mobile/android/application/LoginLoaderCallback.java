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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Properties;

import org.alfresco.mobile.android.api.asynchronous.CloudSessionLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuth2DataImpl;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.manager.StorageManager;
import org.apache.chemistry.opencmis.commons.SessionParameter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

@TargetApi(11)
public class LoginLoaderCallback implements LoaderCallbacks<LoaderResult<AlfrescoSession>>
{
    // TODO REMOVE ALL BEFORE RELEASE!!
    public static final String ALFRESCO_CLOUD_URL = "http://my.alfresco.com";

    public static final String BASE_URL = "org.alfresco.mobile.binding.internal.baseurl";

    public static final String USER = "org.alfresco.mobile.internal.credential.user";

    public static final String PASSWORD = "org.alfresco.mobile.internal.credential.password";

    public static final String CLOUD_BASIC_AUTH = "org.alfresco.mobile.binding.internal.cloud.basic";

    public static final String CLOUD_CONFIG_PATH = Environment.getExternalStorageDirectory().getPath()
            + "/alfresco-mobile/cloud-config.properties";

    public static final boolean ENABLE_CONFIG_FILE = true;

    private Activity activity;

    private String url;

    private String username;

    private String password;

    private OAuthData data;

    private Account acc;

    private String getText(int resId)
    {
        return activity.getText(resId).toString();
    }

    public LoginLoaderCallback(Activity activity, Account acc)
    {
        this.activity = activity;
        this.acc = acc;
        if (acc.getTypeId() == Account.TYPE_ALFRESCO_CLOUD)
        {
            data = new OAuth2DataImpl(getText(R.string.oauth_api_key), getText(R.string.oauth_api_secret),
                    acc.getAccessToken(), acc.getRefreshToken());
        }
        else if (acc.getTypeId() == Account.TYPE_ALFRESCO_CMIS)
        {
            this.url = acc.getUrl();
            this.username = acc.getUsername();
            this.password = acc.getPassword();
        }
    }

    @Override
    public Loader<LoaderResult<AlfrescoSession>> onCreateLoader(final int id, Bundle args)
    {
        // Default settings for Alfresco Application
        HashMap<String, Serializable> settings = new HashMap<String, Serializable>(2);
        settings.put(SessionParameter.CONNECT_TIMEOUT, "10000");
        settings.put(SessionParameter.READ_TIMEOUT, "60000");
        settings.put(AlfrescoSession.EXTRACT_METADATA, true);
        settings.put(AlfrescoSession.CREATE_THUMBNAIL, true);
        settings.put(AlfrescoSession.CACHE_FOLDER, StorageManager.getCacheDir(activity, "AlfrescoMobile"));

        if (data != null)
        {
            settings.put(AlfrescoSession.EXTRACT_METADATA, false);
            settings.put(AlfrescoSession.CREATE_THUMBNAIL, false);
            return new CloudSessionLoader(activity, data, settings, true);
        }
        // TODO REMOVE ALL BEFORE RELEASE!!
        // Specific for Test Instance server
        if (url.startsWith(ALFRESCO_CLOUD_URL))
        {

            // TODO Remove it when public
            url = "http://devapis.alfresco.com/alfresco/a";

            // Check Properties available inside the device
            if (ENABLE_CONFIG_FILE)
            {
                File f = new File(CLOUD_CONFIG_PATH);
                if (f.exists() && ENABLE_CONFIG_FILE)
                {
                    Properties prop = new Properties();
                    try
                    {
                        // load a properties file
                        prop.load(new FileInputStream(f));
                        url = prop.getProperty("url");
                    }
                    catch (IOException ex)
                    {
                        throw new AlfrescoServiceException(ErrorCodeRegistry.PARSING_GENERIC, "Error with config files");
                    }
                }
            }

            settings.put(CLOUD_BASIC_AUTH, true);
            settings.put(BASE_URL, url);
            settings.put(USER, username);
            settings.put(PASSWORD, password);
            return new CloudSessionLoader(activity, null, settings);
        }
        else
        {
            return new SessionLoader(activity, url, username, password, settings);
        }
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<AlfrescoSession>> loader, LoaderResult<AlfrescoSession> results)
    {

        if (!results.hasException())
        {
            if (results.getData() instanceof CloudSession)
            {
                AccountDAO accountDao = new AccountDAO(activity, SessionUtils.getDataBaseManager(activity).getWriteDb());
                accountDao.update(acc.getId(), acc.getDescription(), acc.getUrl(), acc.getUsername(),
                        acc.getPassword(), acc.getRepositoryId(), Integer.valueOf(Account.TYPE_ALFRESCO_CLOUD), null,
                        ((CloudSessionLoader) loader).getOAuthData().getAccessToken(), ((CloudSessionLoader) loader)
                                .getOAuthData().getRefreshToken());
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
