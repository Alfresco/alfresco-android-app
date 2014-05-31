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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.person.PersonsEvent;
import org.alfresco.mobile.android.async.person.PersonsRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;
import org.alfresco.mobile.android.ui.person.PersonAdapter;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.squareup.otto.Subscribe;

public class SiteMembersFoundationFragment extends BaseGridFragment
{
    public static final String TAG = SiteMembersFoundationFragment.class.getName();

    protected static final String ARGUMENT_SITE = "site";

    private Site site;

    protected Map<String, Person> selectedItems = new HashMap<String, Person>(1);

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public SiteMembersFoundationFragment()
    {
        emptyListMessageId = R.string.empty_site;
    }

    public static SiteMembersFoundationFragment newInstance(Site site)
    {
        SiteMembersFoundationFragment bf = new SiteMembersFoundationFragment();
        ListingContext lc = new ListingContext();
        lc.setMaxItems(50);
        Bundle b = createBundleArgs(lc, LOAD_VISIBLE);
        b.putSerializable(ARGUMENT_SITE, site);
        bf.setArguments(b);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected void onRetrieveParameters(Bundle bundle)
    {
        site = (Site) bundle.getSerializable(ARGUMENT_SITE);
    }

    @Override
    public void onResume()
    {
        UIUtils.displayTitle(getActivity(), String.format(getString(R.string.members_of), site.getTitle()));
        super.onResume();
    }

    @Override
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new PersonsRequest.Builder(site).setListingContext(listingContext);
    }

    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new PersonAdapter(this, R.layout.sdk_grid_row, new ArrayList<Person>(0), selectedItems);
    }

    @Subscribe
    public void onResult(PersonsEvent event)
    {
        displayData(event);
    }
}
