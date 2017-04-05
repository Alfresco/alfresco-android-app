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
package org.alfresco.mobile.android.async.account;

import org.alfresco.mobile.android.api.constants.OAuthConstant;
import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.exceptions.AlfrescoSessionException;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.SamlData;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.async.session.LoadSessionHelper;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoSessionSettings;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.favorite.FavoritesManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

import android.util.Log;

public class CreateAccountOperation extends BaseOperation<AlfrescoAccount>
{
    protected String baseUrl;

    protected String username;

    protected String password;

    protected String description;

    private OAuthData oauthData;

    private SamlData samlData;

    private Person userPerson;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public CreateAccountOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof CreateAccountRequest)
        {
            this.baseUrl = ((CreateAccountRequest) request).baseUrl;
            this.username = ((CreateAccountRequest) request).username;
            this.password = ((CreateAccountRequest) request).password;
            this.description = ((CreateAccountRequest) request).description;
            this.oauthData = ((CreateAccountRequest) request).data;
            this.samlData = ((CreateAccountRequest) request).samlData;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<AlfrescoAccount> doInBackground()
    {
        LoaderResult<AlfrescoAccount> result = new LoaderResult<AlfrescoAccount>();
        AlfrescoAccount account = null;

        try
        {
            if (listener != null)
            {
                listener.onPreExecute(this);
            }

            AlfrescoSessionSettings settingsHelper;
            if (oauthData != null)
            {
                settingsHelper = SessionManager.getInstance(context).prepareSettings(oauthData);
            }
            else if (samlData != null)
            {
                settingsHelper = SessionManager.getInstance(context).prepareSettings(baseUrl, samlData);
            }
            else
            {
                settingsHelper = SessionManager.getInstance(context).prepareSettings(baseUrl, username, password);
            }

            LoadSessionHelper sHelper = new LoadSessionHelper(context, settingsHelper);
            session = sHelper.requestSession();
            oauthData = sHelper.getOAuthData();
            userPerson = sHelper.getUser();
            account = createAccount();
        }
        catch (Exception e)
        {
            if (e instanceof AlfrescoSessionException)
            {
                result.setException((Exception) e.getCause());
                Log.e("TAG", Log.getStackTraceString(e));
            }
            else
            {
                result.setException(e);
            }
        }

        result.setData(account);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private AlfrescoAccount createAccount()
    {
        String type;
        boolean isPaidAccount;
        AlfrescoAccount acc;

        if (oauthData == null)
        {
            // ON PREMISE

            // Retrieve Type
            if (session instanceof CloudSession && !session.getBaseUrl().startsWith(OAuthConstant.PUBLIC_API_HOSTNAME))
            {
                type = AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_TEST_BASIC;
            }
            else if (samlData != null)
            {
                type = AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_CMIS_SAML;
            }
            else
            {
                type = (session instanceof CloudSession) ? AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_CLOUD
                        : AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_CMIS;
            }

            // Retrieve Name/Description
            String accountLabel = (description != null && !description.isEmpty()) ? description
                    : context.getString(R.string.account_default_onpremise);

            // Retrieve Paid Info
            isPaidAccount = isPaid(type, session);

            // Create Account
            acc = AlfrescoAccountManager.getInstance(context).create(accountLabel, session.getBaseUrl(), username,
                    samlData != null ? samlData.getTicket() : password, session.getRepositoryInfo().getIdentifier(),
                    type, null, null, null, Boolean.toString(isPaidAccount));

            AnalyticsHelper.checkServerConfiguration(context, session, acc);
        }
        else
        {
            // CLOUD

            // Retrieve Type
            type = AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_CLOUD;
            if (session instanceof CloudSession && !session.getBaseUrl().startsWith(OAuthConstant.PUBLIC_API_HOSTNAME))
            {
                type = AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_TEST_OAUTH;
            }

            // Retrieve Paid Info
            isPaidAccount = isPaid(type, session);

            // Create Account
            acc = AlfrescoAccountManager.getInstance(context).create(context.getString(R.string.account_default_cloud),
                    session.getBaseUrl(), userPerson.getIdentifier(), null, session.getRepositoryInfo().getIdentifier(),
                    type, null, ((CloudSession) session).getOAuthData().getAccessToken(),
                    ((CloudSession) session).getOAuthData().getRefreshToken(), Boolean.toString(isPaidAccount));
        }

        // Activate Automatic Sync for Sync Content & Favorite
        SyncContentManager.saveStateInfo(context);
        SyncContentManager.getInstance(context).setActivateSync(acc, true);
        FavoritesManager.getInstance(context).setActivateSync(acc, true);

        return acc;
    }

    public static boolean isPaid(String type, AlfrescoSession session)
    {
        if (type.equals(AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_CLOUD)
                || type.equals(AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_TEST_OAUTH))
        {
            return (((CloudSession) session).getNetwork().isPaidNetwork());
        }
        else
        {
            String edition = session.getRepositoryInfo().getEdition();
            return (edition.equals(OnPremiseConstant.ALFRESCO_EDITION_ENTERPRISE));
        }
    }

    public static String getType(AlfrescoSession session)
    {
        // Retrieve Type
        if (session instanceof CloudSession && !session.getBaseUrl().startsWith(OAuthConstant.PUBLIC_API_HOSTNAME))
        {
            return AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_TEST_BASIC;
        }
        else
        {
            return (session instanceof CloudSession) ? AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_CLOUD
                    : AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_CMIS;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public String getBaseUrl()
    {
        return baseUrl;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getDescription()
    {
        return description;
    }

    public OAuthData getOauthData()
    {
        return oauthData;
    }

    public Person getCloudUser()
    {
        return userPerson;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<AlfrescoAccount> result)
    {
        super.onPostExecute(result);

        // Analytics
        // try to identify why theres an error
        String label = AnalyticsHelper.getAccountType(session);
        boolean hasException = false;
        try
        {
            if (result.hasException())
            {
                if (result.getException() instanceof AlfrescoSessionException
                        && result.getException().getCause() != null)
                {
                    if (result.getException().getCause() instanceof CmisUnauthorizedException)
                    {
                        hasException = false;
                        label = AnalyticsManager.LABEL_UNAUTHORIZED;
                    }
                    else if (result.getException().getCause() instanceof CmisConnectionException)
                    {
                        if (ConnectivityUtils.hasInternetAvailable(context))
                        {
                            hasException = false;
                            label = AnalyticsManager.LABEL_UNKNOWN_SERVER;
                        }
                        else
                        {
                            hasException = false;
                            label = AnalyticsManager.LABEL_OFFLINE;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            hasException = true;
            label = AnalyticsManager.LABEL_FAILED;
        }

        AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_ACCOUNT, AnalyticsManager.ACTION_CREATE,
                label, 1, hasException);

        if (result.getData() != null)
        {
            SessionManager.getInstance(context).saveSession(result.getData(), session);
            SessionManager.getInstance(context).saveAccount(result.getData());
        }
        EventBusManager.getInstance().post(new CreateAccountEvent(getRequestId(), result, session));
    }
}
