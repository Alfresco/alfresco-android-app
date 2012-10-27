/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.sites;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.services.SiteService;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.site.SitesFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class SiteFragment extends SitesFragment
{

    public static final String TAG = "SiteFragment";

    public SiteFragment()
    {
    }

    public static SiteFragment newInstance()
    {
        SiteFragment bf = new SiteFragment();
        Bundle b = getListingBundle();
        bf.setArguments(b);
        return bf;
    }

    public static SiteFragment newInstance(String username)
    {
        return newInstance(username, false);
    }

    public static SiteFragment newInstance(String username, Boolean favorite)
    {
        SiteFragment bf = new SiteFragment();
        Bundle b = createBundleArgs(username, favorite);
        b.putAll(getListingBundle());
        bf.setArguments(b);
        return bf;
    }
    
    private static Bundle getListingBundle(){
        ListingContext lc = new ListingContext();
        lc.setSortProperty(SiteService.SORT_PROPERTY_TITLE);
        lc.setIsSortAscending(true);
        return createBundleArgs(lc, LOAD_AUTO);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Site s = (Site) l.getItemAtPosition(position);
        ((MainActivity) getActivity()).addNavigationFragment(s);
    }
}
