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
package org.alfresco.mobile.android.async.site;

import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.Context;

public class SiteFavoriteRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_SITE_FAVORITE_UPDATE;

    /** Site to manage. */
    protected Site site;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected SiteFavoriteRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Site site)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.site = site;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Site getSite()
    {
        return site;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return "SiteFavoriteRequest";
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        /** Site to manage. */
        protected Site site;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Site site)
        {
            this();
            this.site = site;
        }

        public SiteFavoriteRequest build(Context context)
        {
            return new SiteFavoriteRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, site);
        }
    }
}
