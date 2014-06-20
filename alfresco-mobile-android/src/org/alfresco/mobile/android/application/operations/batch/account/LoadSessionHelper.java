/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.account;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuthHelper;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.accounts.fragment.AccountSettingsHelper;

import android.content.Context;

public class LoadSessionHelper
{
    private static final String BASE_URL = "org.alfresco.mobile.binding.internal.baseurl";

    private static final String USER = "org.alfresco.mobile.internal.credential.user";

    private Context context;

    private long accountId;

    private Account account;

    private OAuthData oauthData;

    private AccountSettingsHelper settingsHelper;

    private Person userPerson;

    public LoadSessionHelper(Context context, long accountId)
    {
        this(context, AccountManager.retrieveAccount(context, accountId), null);
    }
    
    public LoadSessionHelper(Context context, Account account, OAuthData data)
    {
        this(context, new AccountSettingsHelper(context, account, data));
        this.account = account;
    }

    public LoadSessionHelper(Context context, AccountSettingsHelper settingsHelper)
    {
        this.context = context;
        this.settingsHelper = settingsHelper;
    }

    public AlfrescoSession requestSession()
    {
        // Prepare Settings
        Map<String, Serializable> settings = settingsHelper.prepareCommonSettings();

        if (settingsHelper.isCloud())
        {
            // CLOUD
            settings.putAll(settingsHelper.prepareCloudSettings());
            oauthData = settingsHelper.getData();

            if (settingsHelper.getNewToken())
            {
                OAuthHelper helper = null;
                if (settings.containsKey(BASE_URL))
                {
                    helper = new OAuthHelper((String) settings.get(BASE_URL));
                }
                else
                {
                    helper = new OAuthHelper();
                }
                oauthData = helper.refreshToken(oauthData);
            }

            CloudSession cloudSession = CloudSession.connect(oauthData, settings);

            // We don't know the name of the user during cloud session creation (OAuth principle)
            // To retrieve the user name, we request the person object associated to the session.
            if (cloudSession.getParameter(USER) != null && cloudSession.getParameter(USER) == CloudSession.USER_ME)
            {
                userPerson = cloudSession.getServiceRegistry().getPersonService().getPerson(CloudSession.USER_ME);
            }
            return cloudSession;
        }
        else
        {
            // ON PREMISE
            settings.putAll(settingsHelper.prepareSSLSettings());
            return RepositorySession.connect(settingsHelper.getBaseUrl(), settingsHelper.getUsername(),
                    settingsHelper.getPassword(), settings);
        }
    }

    public OAuthData getOAuthData()
    {
        return oauthData;
    }

    public Account getAccount()
    {
        if (account == null)
        {
            account = AccountManager.retrieveAccount(context, accountId);
        }
        return account;
    }

    public Person getUser()
    {
        return userPerson;
    }
}
