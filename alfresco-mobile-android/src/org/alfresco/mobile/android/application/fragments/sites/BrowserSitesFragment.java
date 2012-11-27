/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.sites;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.services.SiteService;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.site.SitesFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class BrowserSitesFragment extends SitesFragment implements OnTabChangeListener
{
    public static final String TAG = "BrowserSitesFragment";

    private TabHost mTabHost;

    public BrowserSitesFragment()
    {
        super();
    }

    public static BrowserSitesFragment newInstance()
    {
        BrowserSitesFragment bf = new BrowserSitesFragment();
        return bf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null) { return null; }
        View v = inflater.inflate(R.layout.app_tab_extra, container, false);

        init(v, emptyListMessageId);

        mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);
        setupTabs();
        return v;
    }

    @Override
    public void onStart()
    {
        mTabHost.setCurrentTabByTag(MY_SITES);
        getActivity().invalidateOptionsMenu();
        getActivity().setTitle(R.string.menu_browse_all_sites);
        super.onStart();
    }

    private static final String ALL_SITES = "All";

    private static final String MY_SITES = "My";

    private static final String FAV_SITES = "Fav";

    private void setupTabs()
    {
        mTabHost.setup(); // you must call this before adding your tabs!

        mTabHost.addTab(newTab(ALL_SITES, R.string.menu_browse_all_sites, android.R.id.tabcontent));
        mTabHost.addTab(newTab(MY_SITES, R.string.menu_browse_my_sites, android.R.id.tabcontent));
        mTabHost.addTab(newTab(FAV_SITES, R.string.menu_browse_favorite_sites, android.R.id.tabcontent));
        mTabHost.setOnTabChangedListener(this);
    }

    private TabSpec newTab(String tag, int labelId, int tabContentId)
    {
        TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setContent(tabContentId);
        tabSpec.setIndicator(this.getText(labelId));
        return tabSpec;
    }

    @Override
    public void onTabChanged(String tabId)
    {
        if (SessionUtils.getSession(getActivity()) == null) { return; }
        Bundle b = getListingBundle();
        if (MY_SITES.equals(tabId))
        {
            b = createBundleArgs(alfSession.getPersonIdentifier(), false);
        }
        else if (FAV_SITES.equals(tabId))
        {
            b = createBundleArgs(alfSession.getPersonIdentifier(), true);
        }
        reload(b, loaderId, this);

    }

    private static Bundle getListingBundle()
    {
        ListingContext lc = new ListingContext();
        lc.setSortProperty(SiteService.SORT_PROPERTY_TITLE);
        lc.setIsSortAscending(true);
        return createBundleArgs(lc, LOAD_AUTO);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Site s = (Site) l.getItemAtPosition(position);
        ((MainActivity) getActivity()).addNavigationFragment(s);
    }

    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }
}
