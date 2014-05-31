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
package org.alfresco.mobile.android.async.account.signup;

import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;
import org.alfresco.mobile.android.platform.data.CloudSignupRequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class SignUpStatusRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    private static final int TYPE_ID = OperationRequestIds.ID_ACCOUNT_SIGNUP_STATUS;

    // ////////////////////////////////////////////////////
    // MEMBERS
    // ////////////////////////////////////////////////////
    final CloudSignupRequest signUpRequest;

    final String apiKey;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected SignUpStatusRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, String apiKey, CloudSignupRequest request)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.signUpRequest = request;
        this.apiKey = apiKey;
    }

    public SignUpStatusRequest(Cursor cursor)
    {
        super(cursor);
        throw new UnsupportedOperationException("This constructor is not supported for SignUpStatusRequest");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        return cValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Builder
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        private CloudSignupRequest request;

        private String apiKey;

        public Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(String apiKey, CloudSignupRequest request)
        {
            this();
            this.apiKey = apiKey;
            this.request = request;
        }

        public SignUpStatusRequest build(Context context)
        {
            return new SignUpStatusRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, apiKey, request);
        }
    }

}
