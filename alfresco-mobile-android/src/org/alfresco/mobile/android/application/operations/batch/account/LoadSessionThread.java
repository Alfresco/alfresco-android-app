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
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;

import android.content.Context;
import android.content.Intent;

public class LoadSessionThread extends AbstractBatchOperationThread<AlfrescoSession>
{
    private Account account;

    private OAuthData oauthData;

    public LoadSessionThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof LoadSessionRequest)
        {
            oauthData = ((LoadSessionRequest) request).getData();
        }
    }

    @Override
    protected LoaderResult<AlfrescoSession> doInBackground()
    {
        LoaderResult<AlfrescoSession> result = new LoaderResult<AlfrescoSession>();
        AlfrescoSession repoSession = null;
        LoadSessionHelper sHelper = null;
        try
        {
            // Retrieve informations about the account.
            account = AccountManager.retrieveAccount(context, accountId);
            if (listener != null)
            {
                listener.onPreExecute(this);
            }
            
            sHelper = new LoadSessionHelper(context, account, oauthData);
            repoSession = sHelper.requestSession();
            oauthData = sHelper.getOAuthData();
        }
        catch (Exception e)
        {
            if (sHelper != null){
                oauthData = sHelper.getOAuthData();
            }
            result.setException(e);
        }

        result.setData(repoSession);

        return result;
    }

    public OAuthData getOAuthData()
    {
        return oauthData;
    }

    public Account getAccount()
    {
        return account;
    }
    
    
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, accountId);
        return broadcastIntent;
    }
    
    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_STARTED);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, accountId);
        return broadcastIntent;
    }

}
