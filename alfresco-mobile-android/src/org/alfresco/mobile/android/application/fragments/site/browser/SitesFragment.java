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

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.async.site.SiteFavoriteEvent;
import org.alfresco.mobile.android.async.site.SitesEvent;
import org.alfresco.mobile.android.async.site.member.SiteMembershipEvent;
import org.alfresco.mobile.android.async.site.member.SitesPendingMembershipEvent;
import org.alfresco.mobile.android.ui.site.SitesTemplate;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.squareup.otto.Subscribe;

/**
 * Fragment to display the list of sites depending on criteria like favorite,
 * all, user sites.
 * 
 * @author Jean Marie Pascal
 */
public class SitesFragment extends CommonBrowserSitesFragment
{
    public static final String TAG = SitesFragment.class.getName();

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public SitesFragment()
    {
        super();
        retrieveDataOnCreation = true;
    }

    public static SitesFragment newInstanceByTemplate(Bundle b)
    {
        SitesFragment cbf = new SitesFragment();
        cbf.setArguments(b);
        b.putBoolean(ARGUMENT_BASED_ON_TEMPLATE, true);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected void onRetrieveParameters(Bundle bundle)
    {
        super.onRetrieveParameters(bundle);
        if (favorite != null)
        {
            isFavoriteListing = favorite;
            isMemberSite = !favorite;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REFRESH
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void refresh()
    {
        super.refresh();
        if (getSession() != null)
        {
            getSession().getServiceRegistry().getSiteService().clear();
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
    public void onCancelPendingMembershipEvent(SitesPendingMembershipEvent event)
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
            extraConfiguration = new Bundle();
        }

        public Builder(Activity activity, Map<String, Object> configuration)
        {
            super(activity, configuration);
            menuIconId = R.drawable.ic_all_sites_light;
            menuTitleId = R.string.menu_browse_sites;
            templateArguments = new String[] { SitesTemplate.ARGUMENT_FAVORITE_SITES };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder favorite(boolean onlyFavorites)
        {
            extraConfiguration.putSerializable(ARGUMENT_FAVORITE_SITES, onlyFavorites);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return SitesFragment.newInstanceByTemplate(b);
        };
    }
}
