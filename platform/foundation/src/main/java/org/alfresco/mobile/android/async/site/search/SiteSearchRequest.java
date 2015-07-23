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
package org.alfresco.mobile.android.async.site.search;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.ListingOperationRequest;

import android.content.Context;

public class SiteSearchRequest extends ListingOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_SITE_SEARCH;

    final String keyword;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected SiteSearchRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, ListingContext listingContext, String keyword)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, listingContext);
        this.keyword = keyword;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return "siteSearch";
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends ListingOperationRequest.Builder
    {
        protected String keyword = null;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(String keyword)
        {
            this();
            this.keyword = keyword;
        }

        public SiteSearchRequest build(Context context)
        {
            return new SiteSearchRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, listingContext, keyword);
        }
    }
}
