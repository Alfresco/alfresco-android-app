package org.alfresco.mobile.android.application.fragments.sites;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.services.SiteService;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
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

    public static BrowserSitesFragment newInstance()
    {
        BrowserSitesFragment bf = new BrowserSitesFragment();
        return bf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getsession(getActivity());
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
        super.onStart();
        mTabHost.setCurrentTabByTag(MY_SITES);
        getActivity().invalidateOptionsMenu();
        DisplayUtils.hideLeftTitlePane(getActivity());
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
        if (SessionUtils.getsession(getActivity()) == null) { return; }
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
}
