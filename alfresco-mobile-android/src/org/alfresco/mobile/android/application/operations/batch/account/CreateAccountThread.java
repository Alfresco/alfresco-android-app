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

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.constants.OAuthConstant;
import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.accounts.fragment.AccountSettingsHelper;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;

import android.content.Context;
import android.content.Intent;

public class CreateAccountThread extends AbstractBatchOperationThread<Account>
{
    protected String baseUrl;

    protected String username;

    protected String password;

    protected String description;

    private OAuthData oauthData;

    private Person userPerson;

    public CreateAccountThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof CreateAccountRequest)
        {
            this.baseUrl = ((CreateAccountRequest) request).getBaseUrl();
            this.username = ((CreateAccountRequest) request).getUsername();
            this.password = ((CreateAccountRequest) request).getPassword();
            this.description = ((CreateAccountRequest) request).getDescription();
            this.oauthData = ((CreateAccountRequest) request).getData();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Account> doInBackground()
    {
        LoaderResult<Account> result = new LoaderResult<Account>();
        Account account = null;

        try
        {
            if (listener != null)
            {
                listener.onPreExecute(this);
            }

            AccountSettingsHelper settingsHelper = new AccountSettingsHelper(context, baseUrl, username, password,
                    oauthData);

            LoadSessionHelper sHelper = new LoadSessionHelper(context, settingsHelper);
            session = sHelper.requestSession();
            oauthData = sHelper.getOAuthData();
            userPerson = sHelper.getUser();
            account = createAccount();
            accountId = account.getId();
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
    private Account createAccount()
    {
        int type;
        boolean isPaidAccount = false;
        Account acc = null;

        if (oauthData == null)
        {
            // Non OAuth login
            type = Integer.valueOf(Account.TYPE_ALFRESCO_CMIS);
            if (session instanceof CloudSession && !session.getBaseUrl().startsWith(OAuthConstant.PUBLIC_API_HOSTNAME))
            {
                type = Integer.valueOf(Account.TYPE_ALFRESCO_TEST_BASIC);
            }
            else
            {
                type = (session instanceof CloudSession) ? Integer.valueOf(Account.TYPE_ALFRESCO_CLOUD) : Integer
                        .valueOf(Account.TYPE_ALFRESCO_CMIS);
            }

            String tmpDescription = (description != null && !description.isEmpty()) ? description : context
                    .getString(R.string.account_default_onpremise);

            isPaidAccount = isPaid(type, session);

            // Save Account
            acc = AccountManager.createAccount(context, tmpDescription, baseUrl, username, password, session
                    .getRepositoryInfo().getIdentifier(), type, null, null, null, isPaidAccount ? 1 : 0);
        }
        else
        {
            // OAuth login
            type = Integer.valueOf(Account.TYPE_ALFRESCO_CLOUD);
            if (session instanceof CloudSession && !session.getBaseUrl().startsWith(OAuthConstant.PUBLIC_API_HOSTNAME))
            {
                type = Integer.valueOf(Account.TYPE_ALFRESCO_TEST_OAUTH);
            }

            isPaidAccount = isPaid(type, session);

            // Save Account
            acc = AccountManager.createAccount(context, context.getString(R.string.account_default_cloud), session
                    .getBaseUrl(), userPerson.getIdentifier(), null, session.getRepositoryInfo().getIdentifier(), type,
                    null, ((CloudSession) session).getOAuthData().getAccessToken(), oauthData.getRefreshToken(),
                    isPaidAccount ? 1 : 0);
        }

        return acc;
    }

    private boolean isPaid(int type, AlfrescoSession session)
    {
        if (type == Account.TYPE_ALFRESCO_CLOUD)
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
    // BROADCAST EVENT
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, accountId);
        return broadcastIntent;
    }

    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_CREATE_ACCOUNT_STARTED);
        return broadcastIntent;
    }

}
