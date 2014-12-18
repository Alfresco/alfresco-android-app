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
package org.alfresco.mobile.android.async.activitystream;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.ListingOperationRequest;

import android.content.Context;
import android.database.Cursor;

public class ActivityStreamRequest extends ListingOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_ACTIVITY_STREAM_READ;

    private static final String PROP_USERNAME = "username";

    private static final String PROP_SITE_ID = "siteId";

    final String userName;

    final String siteIdentifier;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected ActivityStreamRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, ListingContext listingContext, String userName,
            String siteIdentifier)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, listingContext);
        this.userName = userName;
        this.siteIdentifier = siteIdentifier;

        // Save extra info
        persistentProperties.put(PROP_USERNAME, userName);
        persistentProperties.put(PROP_SITE_ID, siteIdentifier);
    }

    public ActivityStreamRequest(Cursor cursor)
    {
        super(cursor);
        this.userName = (String) persistentProperties.remove(PROP_USERNAME);
        this.siteIdentifier = (String) persistentProperties.remove(PROP_SITE_ID);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends ListingOperationRequest.Builder
    {
        protected String userName;

        protected String siteIdentifier;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(String siteIdentifier, String userName)
        {
            this();
            this.siteIdentifier = siteIdentifier;
            this.userName = userName;
        }

        public ActivityStreamRequest build(Context context)
        {
            return new ActivityStreamRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, listingContext, userName, siteIdentifier);
        }
    }

}
