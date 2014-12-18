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
package org.alfresco.mobile.android.async.clean;

import org.alfresco.mobile.android.async.impl.BaseOperationRequest;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;

import android.content.ContentValues;
import android.content.Context;

public class CleanSyncFavoriteRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 1010;

    public static final String MIME_SYNC = "UnSync";

    final AlfrescoAccount account;

    final boolean deleteContent;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected CleanSyncFavoriteRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, AlfrescoAccount account, boolean deleteContent)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.deleteContent = deleteContent;
        this.account = account;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return account.getUrl();
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        return cValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        protected boolean deleteContent;

        protected AlfrescoAccount account;

        protected Builder()
        {
        }

        public Builder(AlfrescoAccount account, boolean deleteContent)
        {
            this();
            this.deleteContent = deleteContent;
            this.account = account;
            this.requestTypeId = TYPE_ID;
        }

        public CleanSyncFavoriteRequest build(Context context)
        {
            return new CleanSyncFavoriteRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, account, deleteContent);
        }
    }
}
