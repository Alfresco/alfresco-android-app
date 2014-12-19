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
package org.alfresco.mobile.android.async.site.member;

import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.Context;

public class SiteMembershipRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_SITE_MEMBERSHIP_CREATE;

    /** Site to manage. */
    protected Site site;

    /** Determine if user wants to join sites or not. */
    protected Boolean isJoining;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected SiteMembershipRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Site site, Boolean isJoining)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.isJoining = isJoining;
        this.site = site;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Boolean isJoining()
    {
        return isJoining;
    }

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
        return "siteMembers";
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        /** Site to manage. */
        protected Site site;

        /** Determine if user wants to join sites or not. */
        protected Boolean isJoining;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Site site, Boolean isJoining)
        {
            this();
            this.isJoining = isJoining;
            this.site = site;
        }

        public SiteMembershipRequest build(Context context)
        {
            return new SiteMembershipRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, site, isJoining);
        }
    }
}
