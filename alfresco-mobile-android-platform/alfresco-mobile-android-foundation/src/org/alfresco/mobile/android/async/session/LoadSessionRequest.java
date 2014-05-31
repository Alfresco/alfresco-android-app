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
package org.alfresco.mobile.android.async.session;

import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.OperationsContentProvider;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class LoadSessionRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_SESSION_CREATE;

    public static final String SESSION_MIME = "AlfrescoSession";

    private final OAuthData data;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected LoadSessionRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, OAuthData data)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.data = data;
    }

    @Override
    protected Uri generateNotificationUri(Context context)
    {
        return context.getContentResolver().insert(OperationsContentProvider.CONTENT_URI,
                createContentValues(Operation.STATUS_PENDING));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public OAuthData getData()
    {
        return data;
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
        protected OAuthData data;

        public Builder()
        {
            requestTypeId = TYPE_ID;
            mimeType = SESSION_MIME;
        }

        public Builder(OAuthData data)
        {
            this();
            this.data = data;
        }

        public LoadSessionRequest build(Context context)
        {
            return new LoadSessionRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, data);
        }

        public Builder setOAuthData(OAuthData data)
        {
            this.data = data;
            return this;
        }
    }
}
