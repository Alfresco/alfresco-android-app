/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
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

import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.Context;
import android.database.Cursor;

public class CheckSessionRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_ACCOUNT_CHECK;

    public static final String SESSION_MIME = "AlfrescoServer";

    final String baseUrl, username, password;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected CheckSessionRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, String url, String username, String password)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.baseUrl = url;
        this.username = username;
        this.password = password;
    }

    public CheckSessionRequest(Cursor cursor)
    {
        super(cursor);
        throw new UnsupportedOperationException("This constructor is not supported for CheckSessionRequest");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return baseUrl;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        protected String baseUrl, username, password;

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        protected Builder()
        {
        }

        public Builder(String url, String username, String password)
        {
            super();
            this.requestTypeId = TYPE_ID;
            this.username = username;
            this.baseUrl = url;
            this.password = password;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE REQUEST
        // ///////////////////////////////////////////////////////////////////////////
        public CheckSessionRequest build(Context context)
        {
            return new CheckSessionRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, baseUrl, username, password);
        }
    }
}
