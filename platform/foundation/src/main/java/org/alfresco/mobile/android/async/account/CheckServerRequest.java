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
package org.alfresco.mobile.android.async.account;

import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.Context;
import android.database.Cursor;

public class CheckServerRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_ACCOUNT_CREATE;

    public static final String SESSION_MIME = "AlfrescoServer";

    final String baseUrl;

    final boolean https;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected CheckServerRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, String url, boolean https)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.baseUrl = url;
        this.https = https;
    }

    public CheckServerRequest(Cursor cursor)
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
        return baseUrl;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        protected String baseUrl;

        protected boolean https = false;

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        protected Builder()
        {
        }

        public Builder(boolean https, String url)
        {
            super();
            this.requestTypeId = TYPE_ID;
            this.mimeType = SESSION_MIME;
            this.baseUrl = url;
            this.https = https;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE REQUEST
        // ///////////////////////////////////////////////////////////////////////////
        public CheckServerRequest build(Context context)
        {
            return new CheckServerRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, baseUrl, https);
        }
    }
}
