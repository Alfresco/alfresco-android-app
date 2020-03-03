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
package org.alfresco.mobile.android.application.fragments.site.browser;

import java.lang.ref.WeakReference;
import java.util.Map;

import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.configuration.model.view.SiteBrowserConfigModel;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.site.search.SearchSitesFragment;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.site.SitesTemplate;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

public class BrowserSitesPagerFragment extends AlfrescoFragment
{
    public static final String TAG = BrowserSitesPagerFragment.class.getName();

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public BrowserSitesPagerFragment()
    {
        reportAtCreation = false;
    }

    protected static BrowserSitesPagerFragment newInstanceByTemplate(Bundle b)
    {
        BrowserSitesPagerFragment bf = new BrowserSitesPagerFragment();
        bf.setArguments(b);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setSession(SessionUtils.getSession(getActivity()));
        SessionUtils.checkSession(getActivity(), getSession());
        View v = inflater.inflate(R.layout.app_pager_tab, container, false);

        ViewPager viewPager = (ViewPager) v.findViewById(R.id.view_pager);
        SitesPagerAdapter adapter = new SitesPagerAdapter(getChildFragmentManager(), getActivity(),
                getSession() instanceof CloudSession);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(pageChangeListener);
        pageChangeListener.onPageSelected(SitesPagerAdapter.TAB_MY_SITES);
        viewPager.setCurrentItem(SitesPagerAdapter.TAB_MY_SITES);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) v.findViewById(R.id.tabs);
        tabs.setViewPager(viewPager);
        tabs.setTextColor(getResources().getColor(android.R.color.black));

        return v;
    }

    @Override
    public String onPrepareTitle()
    {
        title = getString(R.string.menu_browse_sites);
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            title = getString(R.string.import_document_title);
        }
        return title;
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {

        }

        @Override
        public void onPageSelected(int position)
        {
            if (AnalyticsManager.getInstance(getActivity()) != null)
            {
                switch (position)
                {
                    case SitesPagerAdapter.TAB_FAV_SITES:
                        screenName = AnalyticsManager.SCREEN_SITES_FAVORITES;
                        break;
                    case SitesPagerAdapter.TAB_MY_SITES:
                        screenName = AnalyticsManager.SCREEN_SITES_MY;
                        break;
                    default:
                        screenName = (getSession() instanceof CloudSession) ? AnalyticsManager.SCREEN_SITES_ALL
                                : AnalyticsManager.SCREEN_SITES_SEARCH;
                        break;
                }
                AnalyticsHelper.reportScreen(getActivity(), screenName);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {

        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends ListingFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            this.extraConfiguration = new Bundle();
            viewConfigModel = new SiteBrowserConfigModel(configuration);
            // TODO Used ?
            templateArguments = new String[] { SitesTemplate.ARGUMENT_SHOW };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE FRAGMENT
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }

}

// ///////////////////////////////////////////////////////////////////////////
// INTERNAL CLASSES
// ///////////////////////////////////////////////////////////////////////////
class SitesPagerAdapter extends FragmentStatePagerAdapter
{
    protected static final int TAB_ALL_SITES = 2;

    protected static final int TAB_MY_SITES = 1;

    protected static final int TAB_FAV_SITES = 0;

    private WeakReference<FragmentActivity> activity;

    public boolean isCloud = false;

    public SitesPagerAdapter(FragmentManager fm, FragmentActivity activity, boolean isCloud)
    {
        super(fm);
        this.activity = new WeakReference<>(activity);
        this.isCloud = isCloud;
    }

    @Override
    public Fragment getItem(int position)
    {
        SitesFragment.Builder builder = SitesFragment.with(activity.get());
        Fragment fr;
        switch (position)
        {
            case TAB_FAV_SITES:
                fr = builder.favorite(true).createFragment();
                break;
            case TAB_MY_SITES:
                fr = builder.favorite(false).createFragment();
                break;
            default:
                fr = (isCloud) ? builder.createFragment() : SearchSitesFragment.with(activity.get()).createFragment();
                break;
        }
        return fr;
    }

    @Override
    public int getCount()
    {
        return 3;
    }

    public CharSequence getPageTitle(int position)
    {
        int titleId;
        switch (position)
        {
            case TAB_FAV_SITES:
                titleId = R.string.menu_browse_favorite_sites;
                break;
            case TAB_MY_SITES:
                titleId = R.string.menu_browse_my_sites;
                break;
            default:
                titleId = (isCloud) ? R.string.menu_browse_all_sites : R.string.menu_browse_site_finder;
                break;
        }
        return activity.get().getString(titleId).toUpperCase();
    }
}
