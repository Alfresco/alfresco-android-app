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
package org.alfresco.mobile.android.ui.site;

import java.util.ArrayList;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.site.member.SitesPendingMembershipEvent;
import org.alfresco.mobile.android.async.site.member.SitesPendingMembershipRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;

import android.widget.ArrayAdapter;

import com.squareup.otto.Subscribe;

public class SitesPendingMembershipFoundationFragment extends BaseGridFragment
{
    public static final String TAG = "SitesFragment";

    protected Boolean favorite = null;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public SitesPendingMembershipFoundationFragment()
    {
        emptyListMessageId = R.string.empty_joinsiterequest;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new SitesPendingMembershipRequest.Builder();
    }

    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new SitesPendingMembershipFoundationAdapter(this, R.layout.sdk_list_button_row, new ArrayList<Site>(0));
    }

    @Subscribe
    public void onResult(SitesPendingMembershipEvent event)
    {
        displayData(event);
    }
}
