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
package org.alfresco.mobile.android.async.person;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.ListingOperationRequest;

import android.content.Context;

public class PersonsRequest extends ListingOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_PERSON_LIST;

    final Site site;

    final String keywords;

    final String siteShortName;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected PersonsRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, ListingContext listingContext, Site site,
            String keywords, String siteShortName)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, listingContext);
        this.site = site;
        this.siteShortName = siteShortName;
        this.keywords = keywords;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Site getSite()
    {
        return site;
    }

    public String getSiteShortName()
    {
        return siteShortName;
    }

    public String getKeywords()
    {
        return keywords;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return "persons";
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends ListingOperationRequest.Builder
    {
        protected Site site;

        protected String keywords;

        protected String siteShortName;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Site site)
        {
            this();
            this.site = site;
        }

        public Builder(String siteShortName, String keywords)
        {
            this();
            this.siteShortName = siteShortName;
            this.keywords = keywords;
        }

        public PersonsRequest build(Context context)
        {
            return new PersonsRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, listingContext, site, keywords, siteShortName);
        }
    }
}
