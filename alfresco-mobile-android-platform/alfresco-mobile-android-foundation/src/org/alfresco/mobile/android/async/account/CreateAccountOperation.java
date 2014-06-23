/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.async.account;

import org.alfresco.mobile.android.api.constants.OAuthConstant;
import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
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

import android.accounts.AccountManager;

public class CreateAccountOperation extends BaseOperation<AlfrescoAccount>
{
    protected String baseUrl;

    protected String username;

    protected String password;

    protected String description;

    private OAuthData oauthData;

    private Person userPerson;

    private AccountManager mAccountManager;

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

            AlfrescoSessionSettings settingsHelper = null;
            if (oauthData != null)
            {
                settingsHelper = SessionManager.getInstance(context).prepareSettings(oauthData);
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
            result.setException(e);
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
        boolean isPaidAccount = false;
        AlfrescoAccount acc = null;
        mAccountManager = AccountManager.get(context);

        if (oauthData == null)
        {
            // ON PREMISE

            // Retrieve Type
            type = AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_CMIS;
            if (session instanceof CloudSession && !session.getBaseUrl().startsWith(OAuthConstant.PUBLIC_API_HOSTNAME))
            {
                type = AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_TEST_BASIC;
            }
            else
            {
                type = (session instanceof CloudSession) ? AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_CLOUD
                        : AlfrescoAccount.REPOSITORY_TYPE_ALFRESCO_CMIS;
            }

            // Retrieve Name/Description
            String accountLabel = (description != null && !description.isEmpty()) ? description : context
                    .getString(R.string.account_default_onpremise);

            // Retrieve Paid Info
            isPaidAccount = isPaid(type, session);

            // Create Account
            acc = AlfrescoAccountManager.getInstance(context).create(accountLabel, session.getBaseUrl(), username, password,
                    session.getRepositoryInfo().getIdentifier(), type, null, null, null,
                    Boolean.toString(isPaidAccount));
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
                    session.getBaseUrl(), userPerson.getIdentifier(), null, session.getRepositoryInfo().getIdentifier(), type, null,
                    ((CloudSession) session).getOAuthData().getAccessToken(),
                    ((CloudSession) session).getOAuthData().getRefreshToken(), Boolean.toString(isPaidAccount));
        }

        return acc;
    }

    private boolean isPaid(String type, AlfrescoSession session)
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
        SessionManager.getInstance(context).saveSession(result.getData(), session);
        SessionManager.getInstance(context).saveAccount(result.getData());
        EventBusManager.getInstance().post(new CreateAccountEvent(getRequestId(), result, session));
    }
}
