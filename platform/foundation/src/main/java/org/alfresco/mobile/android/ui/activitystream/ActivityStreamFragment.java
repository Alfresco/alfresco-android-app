/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.ui.activitystream;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.ActivityEntry;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.activitystream.ActivityStreamEvent;
import org.alfresco.mobile.android.async.activitystream.ActivityStreamRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.squareup.otto.Subscribe;

public class ActivityStreamFragment extends BaseGridFragment implements ActivityStreamTemplate
{
    public static final String TAG = ActivityStreamFragment.class.getName();

    protected List<ActivityEntry> selectedEntry = new ArrayList<>(1);

    protected String siteShortName = null;

    protected String userName = null;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public ActivityStreamFragment()
    {
        emptyListMessageId = R.string.empty_actvity;
        enableTitle = true;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    protected void onRetrieveParameters(Bundle bundle)
    {
        super.onRetrieveParameters(bundle);
        siteShortName = (String) bundle.getSerializable(ARGUMENT_SITE_SHORTNAME);
        userName = (String) bundle.getSerializable(ARGUMENT_USERNAME);
    }

    @Override
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new ActivityStreamRequest.Builder(siteShortName, userName).setListingContext(listingContext);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTENER
    // ///////////////////////////////////////////////////////////////////////////
    protected ArrayAdapter<ActivityEntry> onAdapterCreation()
    {
        return new ActivityStreamAdapter(ActivityStreamFragment.this, R.layout.row_two_lines_caption_divider,
                new ArrayList<ActivityEntry>(0), selectedEntry);
    }

    @Subscribe
    public void onResult(ActivityStreamEvent event)
    {
        displayData(event);
    }
}
