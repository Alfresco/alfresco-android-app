/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import java.util.ArrayList;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.services.SiteService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.site.SitesFragment;

import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

/**
 * Fragment to display the list of sites depending on criteria like favorite,
 * all, user sites.
 * 
 * @author Jean Marie Pascal
 */
public class BrowserSitesFragment extends SitesFragment implements RefreshFragment, OnTabChangeListener
{
    public static final int MODE_LISTING = 0;

    public static final int MODE_IMPORT = 1;

    public static final String TAG = "BrowserSitesFragment";

    public static final String TAB_ALL_SITES = "All";

    public static final String TAB_MY_SITES = "My";

    public static final String TAB_FAV_SITES = "Fav";

    private String currentTabId = TAB_MY_SITES;

    private TabHost mTabHost;

    private int mode = MODE_LISTING;

    public BrowserSitesFragment()
    {
        super();
    }

    public static BrowserSitesFragment newInstance()
    {
        return new BrowserSitesFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
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
    public void onResume()
    {
        int titleId = R.string.menu_browse_sites;
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            mode = MODE_IMPORT;
            titleId = R.string.import_document_title;
        }

        mTabHost.setCurrentTabByTag(currentTabId);
        UIUtils.displayTitle(getActivity(), titleId);

        super.onResume();
    }

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
        Bundle b = getListingBundle();
        if (TAB_MY_SITES.equals(currentTabId))
        {
            b = createBundleArgs(alfSession.getPersonIdentifier(), false);
        }
        else if (TAB_FAV_SITES.equals(currentTabId))
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
        if (getActivity() instanceof MainActivity)
        {
            ((MainActivity) getActivity()).addNavigationFragment(s);
        }
        else if (getActivity() instanceof PublicDispatcherActivity)
        {
            ((PublicDispatcherActivity) getActivity()).addNavigationFragment(s);
        }
    }

    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Site>>> arg0, LoaderResult<PagingResult<Site>> results)
    {
        if (adapter == null)
        {
            adapter = new SiteAdapter(this, R.layout.sdk_list_row, new ArrayList<Site>(), mode);
        }
        super.onLoadFinished(arg0, results);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public static void getMenu(Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_SITE_LIST_REQUEST, Menu.FIRST
                + MenuActionItem.MENU_SITE_LIST_REQUEST, R.string.joinsiterequest_list_title);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_REFRESH, Menu.FIRST + MenuActionItem.MENU_REFRESH,
                R.string.refresh);
        mi.setIcon(R.drawable.ic_refresh);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    public void displayJoinSiteRequests()
    {
        JoinSiteRequestsFragment dialogft = JoinSiteRequestsFragment.newInstance(null);
        dialogft.show(getFragmentManager(), JoinSiteRequestsFragment.TAG);
    }

    /**
     * Update and replace a site object inside the listing without requesting an
     * HTTP call.
     * 
     * @param oldSite : original site inside the list
     * @param newSite : new site to add which replace the old one at the same
     *            place.
     */
    @SuppressWarnings("unchecked")
    public void update(Site oldSite, Site newSite)
    {
        try
        {
            if (adapter != null)
            {
                int position = ((ArrayAdapter<Site>) adapter).getPosition(oldSite);
                ((ArrayAdapter<Site>) adapter).remove(oldSite);
                ((ArrayAdapter<Site>) adapter).insert(newSite, position);
                adapter.notifyDataSetChanged();
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, "Unable to refresh sites objects");
        }
    }

    /**
     * Remove a site object inside the listing without requesting an HTTP call.
     * 
     * @param site : site to remove
     */
    @SuppressWarnings("unchecked")
    public void remove(Site site)
    {
        if (adapter != null)
        {
            ((ArrayAdapter<Site>) adapter).remove(site);
            if (adapter.isEmpty())
            {
                displayEmptyView();
            }
        }
    }

    /**
     * Clear all caches from service side + request a refresh of the list.
     */
    @Override
    public void refresh()
    {
        alfSession = SessionUtils.getSession(getActivity());
        if (alfSession != null)
        {
            alfSession.getServiceRegistry().getSiteService().clear();
            reloadTab();
        }
    }
}
