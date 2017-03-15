/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
package org.alfresco.mobile.android.async.session;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.SamlData;
import org.alfresco.mobile.android.api.session.authentication.SamlInfo;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuthHelper;
import org.alfresco.mobile.android.async.account.CheckServerOperation;
import org.alfresco.mobile.android.async.session.oauth.AccountOAuthHelper;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoSessionSettings;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;

import android.content.Context;

public class LoadSessionHelper
{
    private static final String BASE_URL = "org.alfresco.mobile.binding.internal.baseurl";

    private static final String USER = "org.alfresco.mobile.internal.credential.user";

    private Context context;

    private long accountId;

    private AlfrescoAccount account;

    private OAuthData oauthData;

    private OAuthData originalOauthData;

    private AlfrescoSessionSettings sessionSettings;

    private Person userPerson;

    public LoadSessionHelper(Context context, long accountId)
    {
        this(context, AlfrescoAccountManager.getInstance(context).retrieveAccount(accountId));
    }

    public LoadSessionHelper(Context context, AlfrescoAccount account)
    {
        this(context, SessionManager.getInstance(context).prepareSettings(account));
        this.account = account;
    }

    public LoadSessionHelper(Context context, AlfrescoAccount account, OAuthData data)
    {
        this(context, SessionManager.getInstance(context).prepareSettings(account, data));
        this.account = account;
    }

    public LoadSessionHelper(Context context, AlfrescoAccount account, SamlData data)
    {
        this(context, SessionManager.getInstance(context).prepareSettings(account, data));
        this.account = account;
    }

    public LoadSessionHelper(Context context, AlfrescoSessionSettings settings)
    {
        this.context = context;
        this.sessionSettings = settings;
    }

    public AlfrescoSession requestSession()
    {
        // Prepare Settings
        Map<String, Serializable> settings = sessionSettings.extraSettings;

        if (sessionSettings.isCloud)
        {
            // CLOUD
            oauthData = sessionSettings.oAuthData;
            originalOauthData = oauthData;
            if (sessionSettings.requestNewOAuthToken && AccountOAuthHelper.doesRequireRefreshToken(context))
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
                account = AccountOAuthHelper.saveNewOauthData(context, getAccount(), oauthData);
            }

            CloudSession cloudSession = CloudSession.connect(oauthData, settings);

            // We don't know the name of the user during cloud session creation
            // (OAuth principle)
            // To retrieve the user name, we request the person object
            // associated to the session.
            if (cloudSession.getParameter(USER) != null && cloudSession.getParameter(USER) == CloudSession.USER_ME)
            {
                userPerson = cloudSession.getServiceRegistry().getPersonService().getPerson(CloudSession.USER_ME);
            }
            return cloudSession;
        }
        else
        {
            SamlInfo info = CheckServerOperation.getSamlInfo(sessionSettings.baseUrl);

            if (sessionSettings.samlData == null)
            {
                if (info != null && info.isSamlEnabled())
                {
                    // Permission Granted
                    AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_ACCOUNT,
                            AnalyticsManager.ACTION_CHANGE_AUTHENTICATION, AnalyticsManager.LABEL_SAML_AUTH, 1, false);

                    account = AlfrescoAccountManager.getInstance(context).resetPassword(account.getId(),
                            AlfrescoAccount.ACCOUNT_REPOSITORY_TYPE_ID,
                            String.valueOf(AlfrescoAccount.TYPE_ALFRESCO_CMIS_SAML));

                    SessionManager.getInstance(context)
                            .saveAccount(AlfrescoAccountManager.getInstance(context).retrieveAccount(account.getId()));

                    // Error ?
                    return RepositorySession.connect(sessionSettings.baseUrl, sessionSettings.samlData,
                            sessionSettings.extraSettings);
                }
                else
                {
                    // ON PREMISE
                    return RepositorySession.connect(sessionSettings.baseUrl, sessionSettings.username,
                            sessionSettings.password, sessionSettings.extraSettings);
                }
            }
            else if (info != null)
            {
                // Check SAML Status ?
                if (info.isSamlEnabled())
                {
                    // ON PREMISE + SAML
                    return RepositorySession.connect(sessionSettings.baseUrl, sessionSettings.samlData,
                            sessionSettings.extraSettings);
                }
                else
                {
                    AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_ACCOUNT,
                            AnalyticsManager.ACTION_CHANGE_AUTHENTICATION, AnalyticsManager.LABEL_BASIC_AUTH, 1, false);

                    // SAML has been deactivated...
                    // We revert to normal account
                    account = AlfrescoAccountManager.getInstance(context).resetPassword(account.getId(),
                            AlfrescoAccount.ACCOUNT_REPOSITORY_TYPE_ID,
                            String.valueOf(AlfrescoAccount.TYPE_ALFRESCO_CMIS));

                    SessionManager.getInstance(context)
                            .saveAccount(AlfrescoAccountManager.getInstance(context).retrieveAccount(account.getId()));

                    // Error ?
                    return RepositorySession.connect(sessionSettings.baseUrl, sessionSettings.username,
                            sessionSettings.password, sessionSettings.extraSettings);
                }
            }
            else
            {
                return null;
            }
        }
    }

    public OAuthData getOAuthData()
    {
        return oauthData != null ? oauthData : originalOauthData;
    }

    public AlfrescoAccount getAccount()
    {
        if (account == null)
        {
            account = AlfrescoAccountManager.getInstance(context).retrieveAccount(accountId);
        }
        return account;
    }

    public Person getUser()
    {
        return userPerson;
    }
}
