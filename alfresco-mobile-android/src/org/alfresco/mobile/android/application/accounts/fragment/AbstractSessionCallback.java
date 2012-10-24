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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.CloudSessionLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;

public abstract class AbstractSessionCallback implements LoaderCallbacks<LoaderResult<AlfrescoSession>>
{
    protected Activity activity;

    protected String baseUrl;

    protected String username;

    protected String password;

    protected OAuthData data;

    public Loader<LoaderResult<AlfrescoSession>> getSessionLoader(SessionSettingsHelper settingsHelper){
        Map<String, Serializable> settings = settingsHelper.prepareCommonSettings();
        if (settingsHelper.isCloud())
        {
            settings.putAll(settingsHelper.prepareCloudSettings(true, false));
            return new CloudSessionLoader(settingsHelper.getContext(), settingsHelper.getData(), settings, settingsHelper.getNewToken());
        }
        else
        {
            return new SessionLoader(settingsHelper.getContext(), settingsHelper.getBaseUrl(), settingsHelper.getUsername(), settingsHelper.getPassword(), settings);
        }
    }
    
    public Loader<LoaderResult<AlfrescoSession>> getSessionLoader(Activity activity, String baseUrl, String username,
            String password, OAuthData data, boolean requestNewToken, boolean createAccount)
    {
        // Default settings for Alfresco Application
        SessionSettingsHelper settingsHelper = new SessionSettingsHelper(activity, baseUrl, username, password, data);
        Map<String, Serializable> settings = settingsHelper.prepareCommonSettings();

        // Specific for Test Instance server
        if (settingsHelper.isCloud())
        {
            settings.putAll(settingsHelper.prepareCloudSettings(requestNewToken, createAccount));
            return new CloudSessionLoader(activity, data, settings, settingsHelper.getNewToken());
        }
        else
        {
            return new SessionLoader(activity, baseUrl, username, password, settings);
        }
    }

}
