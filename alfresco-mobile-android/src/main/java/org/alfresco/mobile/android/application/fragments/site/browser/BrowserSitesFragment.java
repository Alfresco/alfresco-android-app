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
package org.alfresco.mobile.android.application.fragments.site.browser;

import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.services.SiteService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.async.site.SiteFavoriteEvent;
import org.alfresco.mobile.android.async.site.SitesEvent;
import org.alfresco.mobile.android.async.site.SitesRequest;
import org.alfresco.mobile.android.async.site.member.CancelPendingMembershipEvent;
import org.alfresco.mobile.android.async.site.member.SiteMembershipEvent;
import org.alfresco.mobile.android.async.site.member.SitesPendingMembershipEvent;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.site.SitesTemplate;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.squareup.otto.Subscribe;

/**
 * Fragment to display the list of sites depending on criteria like favorite,
 * all, user sites.
 * 
 * @author Jean Marie Pascal
 */
public class BrowserSitesFragment extends CommonBrowserSitesFragment implements OnTabChangeListener
{
    public static final String TAG = BrowserSitesFragment.class.getName();

    protected static final String TAB_ALL_SITES = "All";

    protected static final String TAB_MY_SITES = "My";

    protected static final String TAB_FAV_SITES = "Fav";

    private String currentTabId = TAB_MY_SITES;

    private TabHost mTabHost;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public BrowserSitesFragment()
    {
        super();
        retrieveDataOnCreation = false;
    }

    public static BrowserSitesFragment newInstanceByTemplate(Bundle b)
    {
        BrowserSitesFragment cbf = new BrowserSitesFragment();
        cbf.setArguments(b);
        b.putBoolean(ARGUMENT_BASED_ON_TEMPLATE, true);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(super.onCreateView(inflater, container, savedInstanceState));

        // Init Tab
        mTabHost = (TabHost) getRootView().findViewById(android.R.id.tabhost);
        setupTabs();

        return getRootView();
    }

    @Override
    public void onResume()
    {
        mTabHost.setCurrentTabByTag(currentTabId);
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TAB MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public String getCurrentTabId()
    {
        return currentTabId;
    }

    private void setupTabs()
    {
        mTabHost.setup();

        mTabHost.addTab(newTab(TAB_FAV_SITES, R.string.menu_browse_favorite_sites, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_MY_SITES, R.string.menu_browse_my_sites, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_ALL_SITES, R.string.menu_browse_all_sites, android.R.id.tabcontent));
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
        currentTabId = tabId;
        reloadTab();
    }

    private void reloadTab()
    {
        if (SessionUtils.getSession(getActivity()) == null) { return; }

        refreshHelper.setRefreshing();

        SitesRequest.Builder builder;
        if (TAB_MY_SITES.equals(currentTabId))
        {
            builder = new SitesRequest.Builder(false);
            isFavoriteListing = false;
            isMemberSite = true;
        }
        else if (TAB_FAV_SITES.equals(currentTabId))
        {
            builder = new SitesRequest.Builder(true);
            isFavoriteListing = true;
            isMemberSite = false;
        }
        else
        {
            builder = new SitesRequest.Builder(null);
            isFavoriteListing = false;
            isMemberSite = false;
        }
        performRequest(builder.setListingContext(getListing()));
    }

    private static ListingContext getListing()
    {
        ListingContext lc = new ListingContext();
        lc.setSortProperty(SiteService.SORT_PROPERTY_TITLE);
        lc.setIsSortAscending(true);
        return lc;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REFRESH
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void refresh()
    {
        if (getSession() != null)
        {
            getSession().getServiceRegistry().getSiteService().clear();
            reloadTab();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    @Subscribe
    public void onResult(SitesEvent event)
    {
        super.onResult(event);
    }

    @Subscribe
    public void onCancelPendingMembershipEvent(CancelPendingMembershipEvent event)
    {
        super.onCancelPendingMembershipEvent(event);
    }

    @Subscribe
    public void onSiteFavoriteEvent(SiteFavoriteEvent event)
    {
        super.onSiteFavoriteEvent(event);
    }

    @Subscribe
    public void onSiteMembershipEvent(SiteMembershipEvent event)
    {
        super.onSiteMembershipEvent(event);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends ListingFragmentBuilder
    {

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS & HELPERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
        }

        public Builder(Activity activity, Map<String, Object> configuration)
        {
            super(activity, configuration);
            menuIconId = R.drawable.ic_all_sites_light;
            menuTitleId = R.string.menu_browse_sites;
            templateArguments = new String[] { SitesTemplate.ARGUMENT_FAVORITE_SITES };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return BrowserSitesFragment.newInstanceByTemplate(b);
        };

    }
}
