/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.accounts.oauth;

import org.alfresco.mobile.android.api.asynchronous.AbstractBaseLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuthHelper;

import android.content.Context;

/**
 * Provides an asynchronous loader to refresh Access token without recreating a
 * session object.
 * 
 * @author Jean Marie Pascal
 */
public class OAuthRefreshTokenLoader extends AbstractBaseLoader<LoaderResult<OAuthData>>
{
    public static final int ID = OAuthRefreshTokenLoader.class.hashCode();

    private CloudSession session;

    public OAuthRefreshTokenLoader(Context context, CloudSession session)
    {
        super(context);
        this.session = session;
    }

    @Override
    public LoaderResult<OAuthData> loadInBackground()
    {
        LoaderResult<OAuthData> result = new LoaderResult<OAuthData>();

        OAuthData oauthData = null;
        try
        {
            oauthData  = session.getOAuthData();
            OAuthHelper helper = new OAuthHelper(session.getBaseUrl());
            oauthData = helper.refreshToken(session.getOAuthData());
            session.setOAuthData(oauthData);
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(oauthData);

        return result;
    }
}
