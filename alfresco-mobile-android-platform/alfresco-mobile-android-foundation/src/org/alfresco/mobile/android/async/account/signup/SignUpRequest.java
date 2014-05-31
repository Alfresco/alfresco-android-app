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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class SignUpRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    // ////////////////////////////////////////////////////
    // CONSTANTS
    // ////////////////////////////////////////////////////

    public static final int TYPE_ID = OperationRequestIds.ID_ACCOUNT_SIGNUP;

    // ////////////////////////////////////////////////////
    // MEMBERS
    // ////////////////////////////////////////////////////
    final String firstName;

    final String lastName;

    final String emailAddress;

    final String password;

    final String apiKey;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected SignUpRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, String firstName, String lastName, String emailAddress,
            String password, String apiKey)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.apiKey = apiKey;
    }

    public SignUpRequest(Cursor cursor)
    {
        super(cursor);
        throw new UnsupportedOperationException("This constructor is not supported for SignUpOperationRequest");
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
        private String firstName;

        private String lastName;

        private String emailAddress;

        private String password;

        private String apiKey;

        public Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(String firstName, String lastName, String emailAddress, String password, String apiKey)
        {
            this();
            this.firstName = firstName;
            this.lastName = lastName;
            this.emailAddress = emailAddress;
            this.password = password;
            this.apiKey = apiKey;
        }

        public SignUpRequest build(Context context)
        {
            return new SignUpRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, firstName, lastName, emailAddress, password, apiKey);
        }
    }

}
