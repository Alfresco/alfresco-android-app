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

import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.model.view.SitesConfigModel;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.site.search.SearchSitesFragment;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.site.SiteFavoriteEvent;
import org.alfresco.mobile.android.async.site.SitesEvent;
import org.alfresco.mobile.android.async.site.member.CancelPendingMembershipEvent;
import org.alfresco.mobile.android.async.site.member.SiteMembershipEvent;
import org.alfresco.mobile.android.async.site.search.SiteSearchEvent;
import org.alfresco.mobile.android.async.site.search.SiteSearchRequest;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

    private static final String ARGUMENT_KEYWORD = "siteKeywords";

    protected String keywords;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public SitesFragment()
    {
        super();
        retrieveDataOnCreation = true;
        loadState = LOAD_VISIBLE;
        reportAtCreation = false;
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
        keywords = BundleUtils.getString(bundle, ARGUMENT_KEYWORD);

    }

    @Override
    protected void prepareEmptyView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        if (keywords == null)
        {
            emptyImageView.setImageResource(
                    isFavoriteListing ? R.drawable.ic_empty_sites_favorite : R.drawable.ic_empty_sites_my);
            emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
            firstEmptyMessage
                    .setText(isFavoriteListing ? R.string.sites_favorites_empty_title : R.string.sites_my_empty_title);
            secondEmptyMessage.setVisibility(View.VISIBLE);
            secondEmptyMessage.setText(isFavoriteListing ? R.string.sites_favorites_empty_description
                    : R.string.sites_my_empty_description);
        }
        else
        {
            emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
            emptyImageView.setImageResource(R.drawable.ic_empty_search_sites);
            firstEmptyMessage.setVisibility(View.VISIBLE);
            firstEmptyMessage.setText(R.string.sites_search_empty_title);
            secondEmptyMessage.setVisibility(View.VISIBLE);
            secondEmptyMessage.setText(R.string.sites_search_empty_description);
        }
    }

    @Override
    protected OperationRequest.OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        if (keywords == null)
        {
            return super.onCreateOperationRequest(listingContext);
        }
        else
        {
            return new SiteSearchRequest.Builder(keywords).setListingContext(listingContext);
        }
    }

    @Override
    public String onPrepareTitle()
    {
        if (keywords != null) { return String.format(getString(R.string.search_title), keywords); }
        return super.onPrepareTitle();
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
    @Subscribe
    public void onResult(SiteSearchEvent event)
    {
        displayData(event);
    }

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
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends ListingFragmentBuilder
    {
        boolean showFinder = false;

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS & HELPERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity activity, Map<String, Object> configuration)
        {
            super(activity, configuration);
            viewConfigModel = new SitesConfigModel(configuration);
        }

        @Override
        protected void retrieveCustomArgument(Map<String, Object> properties, Bundle b)
        {
            if (properties.containsKey(ARGUMENT_SHOW))
            {
                String show = JSONConverter.getString(properties, ARGUMENT_SHOW);
                if (SHOW_MY_SITES.equals(show))
                {
                    b.putBoolean(ARGUMENT_SHOW, false);
                }
                else if (SHOW_FAVORITE_SITES.equals(show))
                {
                    b.putBoolean(ARGUMENT_SHOW, true);
                }
                else if (SHOW_FINDER.equals(show))
                {
                    showFinder = true;
                }
            }
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder favorite(boolean onlyFavorites)
        {
            extraConfiguration.putBoolean(ARGUMENT_SHOW, onlyFavorites);
            return this;
        }

        public Builder keywords(String keywords)
        {
            extraConfiguration.putString(ARGUMENT_KEYWORD, keywords);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            if (showFinder)
            {
                return SearchSitesFragment.newInstanceByTemplate(b);
            }
            else
            {
                return SitesFragment.newInstanceByTemplate(b);
            }
        }
    }
}
