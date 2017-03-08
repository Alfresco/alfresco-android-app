/*******************************************************************************
 * Copyright (C) 2005-2017 Alfresco Software Limited.
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

import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.SamlData;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.Context;
import android.database.Cursor;

public class CreateAccountRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_ACCOUNT_CREATE;

    public static final String SESSION_MIME = "AlfrescoSession";

    final String baseUrl;

    final String username;

    final String password;

    final String description;

    final OAuthData data;

    final SamlData samlData;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected CreateAccountRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, String url, String username, String password,
            String accountDescription, OAuthData data, SamlData samlData)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.data = data;
        this.baseUrl = url;
        this.username = username;
        this.password = password;
        this.description = accountDescription;
        this.samlData = samlData;
    }

    public CreateAccountRequest(Cursor cursor)
    {
        super(cursor);
        throw new UnsupportedOperationException("This constructor is not supported for CreateAccountRequest");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return baseUrl + "@" + username;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        protected OAuthData data;

        protected SamlData samlData;

        protected String baseUrl;

        protected String username;

        protected String password;

        protected String description;

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        protected Builder()
        {
        }

        public Builder(String url, String username, String password, String description)
        {
            super();
            this.requestTypeId = TYPE_ID;
            this.mimeType = SESSION_MIME;
            this.baseUrl = url;
            this.username = username;
            this.password = password;
            this.description = description;
        }

        public Builder(OAuthData data)
        {
            super();
            this.requestTypeId = TYPE_ID;
            this.mimeType = SESSION_MIME;
            this.data = data;
        }

        public Builder(String url, SamlData data)
        {
            super();
            this.baseUrl = url;
            this.requestTypeId = TYPE_ID;
            this.mimeType = SESSION_MIME;
            this.samlData = data;
            this.username = data.getUserId();
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder setOAuthData(OAuthData data)
        {
            this.data = data;
            return this;
        }

        public Builder setSamlData(SamlData data)
        {
            this.samlData = data;
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE REQUEST
        // ///////////////////////////////////////////////////////////////////////////
        public CreateAccountRequest build(Context context)
        {
            return new CreateAccountRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, baseUrl, username, password, description, data, samlData);
        }
    }
}
