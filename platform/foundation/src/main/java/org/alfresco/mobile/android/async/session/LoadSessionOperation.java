/*******************************************************************************
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
package org.alfresco.mobile.android.async.session;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;

public class LoadSessionOperation extends BaseOperation<AlfrescoSession>
{
    private OAuthData oauthData;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public LoadSessionOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof LoadSessionRequest)
        {
            oauthData = ((LoadSessionRequest) request).getData();
        }
    }

    // ////////////////////////////////////////////////////
    // LIFE CYCLE
    // ////////////////////////////////////////////////////
    @Override
    protected LoaderResult<AlfrescoSession> doInBackground()
    {
        LoaderResult<AlfrescoSession> result = new LoaderResult<AlfrescoSession>();
        AlfrescoSession repoSession = null;
        LoadSessionHelper sHelper = null;
        try
        {
            // Retrieve informations about the account.
            acc = AlfrescoAccountManager.getInstance(context).retrieveAccount(accountId);
            if (listener != null)
            {
                listener.onPreExecute(this);
            }

            sHelper = new LoadSessionHelper(context, acc, oauthData);
            repoSession = sHelper.requestSession();
            oauthData = sHelper.getOAuthData();
        }
        catch (Exception e)
        {
            // We retrieve account in case the type has changed
            acc = AlfrescoAccountManager.getInstance(context).retrieveAccount(accountId);

            if (sHelper != null)
            {
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
}
