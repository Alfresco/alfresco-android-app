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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.alfresco.mobile.android.api.asynchronous.CloudSessionLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.api.constants.OAuthConstant;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.LoginLoaderCallback;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.manager.StorageManager;
import org.apache.chemistry.opencmis.commons.SessionParameter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Loader;
import android.os.Bundle;

@TargetApi(11)
public class AccountLoaderCallback implements LoaderCallbacks<LoaderResult<AlfrescoSession>>
{

    private Activity activity;

    private String baseUrl;

    private URL url;

    private String username;

    private String password;

    private ProgressDialog mProgressDialog;

    private Fragment fr;

    private static final String ARGUMENT_URL = "agumentUrl";

    private int attempt = 0;

    private String description;

    private OAuthData data;

    public AccountLoaderCallback(Activity activity, Fragment fr, String url, String username, String password,
            String description)
    {
        this.activity = activity;
        this.baseUrl = url;
        this.username = username;
        this.password = password;
        this.description = description;
        this.fr = fr;
    }

    public AccountLoaderCallback(Activity activity, Fragment fr, OAuthData data)
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
                MessengerManager.showToast(activity, "Check Your Informations");
                return null;
            }
            return new SessionLoader(activity, args.getString(ARGUMENT_URL), username, password);
        }

        mProgressDialog = ProgressDialog.show(activity, "Please wait", "Contacting your server...", true, true,
                new OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        activity.getLoaderManager().destroyLoader(id);
                    }
                });

        // Default settings for Alfresco Application
        HashMap<String, Serializable> settings = new HashMap<String, Serializable>(2);
        settings.put(SessionParameter.CONNECT_TIMEOUT, "10000");
        settings.put(SessionParameter.READ_TIMEOUT, "60000");
        settings.put(AlfrescoSession.EXTRACT_METADATA, true);
        settings.put(AlfrescoSession.CREATE_THUMBNAIL, true);
        settings.put(AlfrescoSession.CACHE_FOLDER, StorageManager.getCacheDir(activity, "AlfrescoMobile"));

        if (data != null)
        {
            return new CloudSessionLoader(activity, data, settings);
        }
        // TODO REMOVE ALL BEFORE RELEASE!!
        // Specific for Test Instance server
        else if (baseUrl != null && baseUrl.toString().startsWith(LoginLoaderCallback.ALFRESCO_CLOUD_URL))
        {
            // TODO Remove it when public
            String tmpurl = "http://devapis.alfresco.com/alfresco/a";

            // Check Properties available inside the device
            if (LoginLoaderCallback.ENABLE_CONFIG_FILE)
            {
                File f = new File(LoginLoaderCallback.CLOUD_CONFIG_PATH);
                if (f.exists() && LoginLoaderCallback.ENABLE_CONFIG_FILE)
                {
                    Properties prop = new Properties();
                    try
                    {
                        // load a properties file
                        prop.load(new FileInputStream(f));
                        tmpurl = prop.getProperty("url");
                    }
                    catch (IOException ex)
                    {
                        throw new AlfrescoServiceException(ErrorCodeRegistry.PARSING_GENERIC, "Error with config files");
                    }
                }
            }

            settings.put(LoginLoaderCallback.CLOUD_BASIC_AUTH, true);
            settings.put(LoginLoaderCallback.BASE_URL, tmpurl);
            settings.put(LoginLoaderCallback.USER, username);
            settings.put(LoginLoaderCallback.PASSWORD, password);

            return new CloudSessionLoader(activity, null, settings);
        }
        else
        {
            return new SessionLoader(activity, baseUrl, username, password, settings);
        }
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
                id = serverDao.insert(
                        description,
                        baseUrl,
                        username,
                        password,
                        session.getRepositoryInfo().getIdentifier(),
                        (session instanceof CloudSession) ? Integer.valueOf(Account.TYPE_ALFRESCO_CLOUD) : Integer
                                .valueOf(Account.TYPE_ALFRESCO_CMIS), null, null);
            }
            else
            {
                Person user = null;
                if (loader instanceof CloudSessionLoader)
                {
                    user = ((CloudSessionLoader) loader).getUser();
                }
                id = serverDao.insert("Alfresco Cloud", OAuthConstant.CLOUD_URL, user.getIdentifier(), null, session
                        .getRepositoryInfo().getIdentifier(), Integer.valueOf(Account.TYPE_ALFRESCO_CLOUD), data
                        .getAccessToken(), data.getRefreshToken());
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
                MessengerManager.showToast(activity, "Check Your Informations");
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
