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
package org.alfresco.mobile.android.ui.person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.person.PersonsEvent;
import org.alfresco.mobile.android.async.person.PersonsRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.squareup.otto.Subscribe;

public class PeopleFragment extends BaseGridFragment implements PeopleTemplate
{
    public static final String TAG = PeopleFragment.class.getName();

    protected String siteShortName;

    protected String keywords;

    protected Map<String, Person> selectedItems = new HashMap<String, Person>(1);

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public PeopleFragment()
    {
        emptyListMessageId = R.string.empty_users;
        enableTitle = true;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected void onRetrieveParameters(Bundle bundle)
    {
        super.onRetrieveParameters(bundle);
        siteShortName = bundle.getString(ARGUMENT_SITE_SHORTNAME);
        keywords = bundle.getString(ARGUMENT_KEYWORDS);
    }

    @Override
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new PersonsRequest.Builder(siteShortName, keywords).setListingContext(listingContext);
    }

    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new PeopleAdapter(this, R.layout.sdk_grid_row, new ArrayList<Person>(0), selectedItems);
    }

    @Subscribe
    public void onResult(PersonsEvent event)
    {
        displayData(event);
    }
}
