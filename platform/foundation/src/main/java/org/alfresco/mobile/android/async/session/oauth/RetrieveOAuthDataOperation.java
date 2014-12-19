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
package org.alfresco.mobile.android.async.session.oauth;

import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuthHelper;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.os.Bundle;

public class RetrieveOAuthDataOperation extends BaseOperation<OAuthData>
{
    private OAuthData oauthData;

    private Bundle b;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public RetrieveOAuthDataOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof RetrieveOAuthDataRequest)
        {
            b = ((RetrieveOAuthDataRequest) request).getOAuthBundle();
            session = ((RetrieveOAuthDataRequest) request).getSession();
        }
    }

    // ////////////////////////////////////////////////////
    // LIFE CYCLE
    // ////////////////////////////////////////////////////
    @Override
    protected LoaderResult<OAuthData> doInBackground()
    {
        LoaderResult<OAuthData> result = new LoaderResult<OAuthData>();
        OAuthData data = null;
        try
        {
            if (session != null && session instanceof CloudSession)
            {
                oauthData = ((CloudSession) session).getOAuthData();
                OAuthHelper helper = new OAuthHelper(session.getBaseUrl());
                data = helper.refreshToken(((CloudSession) session).getOAuthData());
                ((CloudSession) session).setOAuthData(data);
            }
            else
            {
                OAuthHelper helper = new OAuthHelper((String) b.get(RetrieveOAuthDataRequest.ARGUMENT_BASEURL));
                switch (b.getInt(RetrieveOAuthDataRequest.ARGUMENT_OPERATION))
                {
                    case RetrieveOAuthDataRequest.OPERATION_ACCESS_TOKEN:
                        data = helper.getAccessToken(b.getString(RetrieveOAuthDataRequest.ARGUMENT_APIKEY),
                                b.getString(RetrieveOAuthDataRequest.ARGUMENT_APISECRET),
                                b.getString(RetrieveOAuthDataRequest.ARGUMENT_CALLBACK_URL),
                                b.getString(RetrieveOAuthDataRequest.ARGUMENT_CODE));
                        break;
                    case RetrieveOAuthDataRequest.OPERATION_REFRESH_TOKEN:
                        data = helper.refreshToken(oauthData);
                        break;
                    default:
                        break;
                }
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(data);

        return result;
    }

    // ////////////////////////////////////////////////////
    // INTERNALS
    // ////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<OAuthData> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new RetrieveOAuthDataEvent(getRequestId(), result));
    }
}
