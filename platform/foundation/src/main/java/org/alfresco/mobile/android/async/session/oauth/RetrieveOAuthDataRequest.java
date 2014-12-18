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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.utils.messages.Messagesl18n;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;

public class RetrieveOAuthDataRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    // ////////////////////////////////////////////////////
    // CONSTANTS
    // ////////////////////////////////////////////////////
    protected static final String ARGUMENT_CODE = "code";

    protected static final String ARGUMENT_APIKEY = "apiKey";

    protected static final String ARGUMENT_APISECRET = "apiSecret";

    protected static final String ARGUMENT_CALLBACK_URL = "callback";

    protected static final String ARGUMENT_OPERATION = "operation";

    protected static final String ARGUMENT_BASEURL = "baseUrl";

    public static final int OPERATION_REFRESH_TOKEN = 10;

    public static final int OPERATION_ACCESS_TOKEN = 1;

    public static final int TYPE_ID = OperationRequestIds.ID_OAUTH;

    private final Bundle bundle;

    private final CloudSession session;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected RetrieveOAuthDataRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Bundle bundle, CloudSession session)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        checkValues(bundle);
        this.bundle = bundle;
        this.session = session;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Bundle getOAuthBundle()
    {
        return bundle;
    }

    public CloudSession getSession()
    {
        return session;
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
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("serial")
    private static final List<String> KEYS = new ArrayList<String>(4)
    {
        {
            add(ARGUMENT_CODE);
            add(ARGUMENT_APIKEY);
            add(ARGUMENT_APISECRET);
            add(ARGUMENT_CALLBACK_URL);
        }
    };

    private static void checkValues(Bundle b)
    {
        for (String key : KEYS)
        {
            if (!b.containsKey(key) || b.getString(key) == null || b.getString(key).isEmpty()) { throw new IllegalArgumentException(
                    String.format(Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), key)); }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Builder
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        protected Bundle b;

        protected String code;

        protected String apiKey;

        protected String apiSecret;

        protected String callBackURL;

        protected String baseURL;

        private int operationKind;

        private CloudSession session;

        public Builder()
        {
            requestTypeId = TYPE_ID;
            b = new Bundle();
        }

        public Builder(CloudSession session)
        {
            this();
            this.session = session;
        }

        public Builder(int operationKind, String baseURL, String code, String apiKey, String apiSecret, String callBack)
        {
            this();
            this.operationKind = operationKind;
            this.code = code;
            this.baseURL = baseURL;
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
            this.callBackURL = callBack;
            this.baseURL = baseURL;
        }

        public RetrieveOAuthDataRequest build(Context context)
        {
            b.putString(RetrieveOAuthDataRequest.ARGUMENT_CODE, "code");
            b.putString(RetrieveOAuthDataRequest.ARGUMENT_APIKEY, apiKey);
            b.putString(RetrieveOAuthDataRequest.ARGUMENT_APISECRET, apiSecret);
            b.putString(RetrieveOAuthDataRequest.ARGUMENT_CALLBACK_URL, callBackURL);
            b.putString(RetrieveOAuthDataRequest.ARGUMENT_BASEURL, baseURL);
            b.putInt(RetrieveOAuthDataRequest.ARGUMENT_OPERATION, operationKind);
            b.putString(RetrieveOAuthDataRequest.ARGUMENT_CODE, code);
            return new RetrieveOAuthDataRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, b, session);
        }
    }
}
