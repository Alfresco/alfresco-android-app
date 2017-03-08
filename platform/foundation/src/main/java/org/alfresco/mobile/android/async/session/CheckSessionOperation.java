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

import org.alfresco.mobile.android.api.exceptions.AlfrescoSessionException;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.CreateAccountOperation;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;

public class CheckSessionOperation extends BaseOperation<AlfrescoSession>
{
    protected String baseUrl, username, password, auth;

    protected AlfrescoAccount updatedAccount;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public CheckSessionOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof CheckSessionRequest)
        {
            this.baseUrl = ((CheckSessionRequest) request).baseUrl;
            this.username = ((CheckSessionRequest) request).username;
            this.password = ((CheckSessionRequest) request).password;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<AlfrescoSession> doInBackground()
    {
        LoaderResult<AlfrescoSession> result = new LoaderResult<AlfrescoSession>();
        AlfrescoSession repoSession = null;
        LoadSessionHelper sHelper = null;
        try
        {
            // Retrieve informations about the account.
            acc = AlfrescoAccountManager.getInstance(context).retrieveAccount(accountId);

            AlfrescoAccount tmpAccount = new AlfrescoAccount(acc.getId(), acc.getTitle(), baseUrl, username, password,
                    acc.getRepositoryId(), Integer.toString(acc.getTypeId()), acc.getActivation(),
                    acc.getAccessToken(), acc.getRefreshToken(), Boolean.toString(acc.getIsPaidAccount()));
            if (listener != null)
            {
                listener.onPreExecute(this);
            }
            sHelper = new LoadSessionHelper(context, tmpAccount);
            repoSession = sHelper.requestSession();

            String type = CreateAccountOperation.getType(repoSession);

            updatedAccount = new AlfrescoAccount(acc.getId(), acc.getTitle(), repoSession.getBaseUrl(), username,
                    password, repoSession.getRepositoryInfo().getIdentifier(), type, acc.getActivation(),
                    acc.getAccessToken(), acc.getRefreshToken(), Boolean.toString(CreateAccountOperation.isPaid(type,
                            repoSession)));
        }
        catch (Exception e)
        {
            // We retrieve account in case the type has changed
            acc = AlfrescoAccountManager.getInstance(context).retrieveAccount(accountId);

            if (e instanceof AlfrescoSessionException)
            {
                result.setException((Exception) e.getCause());
            }
            else
            {
                result.setException(e);
            }
        }

        result.setData(repoSession);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<AlfrescoSession> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new CheckSessionEvent(getRequestId(), result, updatedAccount));
    }
}
