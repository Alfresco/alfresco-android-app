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

import java.lang.ref.WeakReference;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.site.SitesTemplate;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
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
    }

    protected static BrowserSitesPagerFragment newInstanceByTemplate(Bundle b)
    {
        BrowserSitesPagerFragment bf = new BrowserSitesPagerFragment();
        bf.setArguments(b);
        return bf;
    };

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
        SitesPagerAdapter adapter = new SitesPagerAdapter(getChildFragmentManager(), getActivity());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(SitesPagerAdapter.TAB_MY_SITES);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) v.findViewById(R.id.tabs);
        tabs.setViewPager(viewPager);
        tabs.setTextColor(getResources().getColor(android.R.color.black));

        return v;
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
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            this.extraConfiguration = new Bundle();
            menuIconId = R.drawable.ic_all_sites_light;
            menuTitleId = R.string.menu_browse_sites;
            templateArguments = new String[] { SitesTemplate.ARGUMENT_FAVORITE_SITES };
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

    private WeakReference<Activity> activity;

    public SitesPagerAdapter(FragmentManager fm, Activity activity)
    {
        super(fm);
        this.activity = new WeakReference<Activity>(activity);
    }

    @Override
    public Fragment getItem(int position)
    {
        SitesFragment.Builder builder = SitesFragment.with(activity.get());
        switch (position)
        {
            case TAB_FAV_SITES:
                builder.favorite(true);
                break;
            case TAB_MY_SITES:
                builder.favorite(false);
                break;
            default:
                break;
        }
        return builder.createFragment();
    }

    @Override
    public int getCount()
    {
        return 3;
    }

    public CharSequence getPageTitle(int position)
    {
        int titleId = 0;
        switch (position)
        {
            case TAB_FAV_SITES:
                titleId = R.string.menu_browse_favorite_sites;
                break;
            case TAB_MY_SITES:
                titleId = R.string.menu_browse_my_sites;
                break;
            default:
                titleId = R.string.menu_browse_all_sites;
                break;
        }
        return activity.get().getString(titleId).toUpperCase();
    }
}
