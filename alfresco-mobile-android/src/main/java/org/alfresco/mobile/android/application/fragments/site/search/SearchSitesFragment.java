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
package org.alfresco.mobile.android.application.fragments.site.search;

import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.site.browser.CommonBrowserSitesFragment;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.site.SiteFavoriteEvent;
import org.alfresco.mobile.android.async.site.member.CancelPendingMembershipEvent;
import org.alfresco.mobile.android.async.site.member.SiteMembershipEvent;
import org.alfresco.mobile.android.async.site.search.SiteSearchEvent;
import org.alfresco.mobile.android.async.site.search.SiteSearchRequest;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

/**
 * Fragment to display the list of sites depending on criteria like favorite,
 * all, user sites.
 * 
 * @author Jean Marie Pascal
 */
public class SearchSitesFragment extends CommonBrowserSitesFragment
{
    public static final String TAG = SearchSitesFragment.class.getName();

    private EditText searchText;

    private String keyword;

    private ImageButton bAdd;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public SearchSitesFragment()
    {
        super();
        retrieveDataOnCreation = false;
    }

    public static SearchSitesFragment newInstanceByTemplate(Bundle b)
    {
        SearchSitesFragment cbf = new SearchSitesFragment();
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
        if (container == null) { return null; }
        setRootView(inflater.inflate(R.layout.fr_site_search, container, false));
        init(getRootView(), emptyListMessageId);

        searchText = (EditText) viewById(R.id.search_query);
        bAdd = (ImageButton) viewById(R.id.search_action);
        bAdd.setEnabled(false);
        activateSend();

        bAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                search();
            }
        });

        searchText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                activateSend();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        searchText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (event != null && (event.getAction() == KeyEvent.ACTION_DOWN)
                        && ((actionId == EditorInfo.IME_ACTION_SEARCH)
                                || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)))
                {
                    search();
                    return true;
                }
                return false;
            }
        });

        return getRootView();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (searchText.getText().length() > 0)
        {
            search();
        }
    }

    @Override
    protected OperationRequest.OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new SiteSearchRequest.Builder(keyword).setListingContext(listingContext);
    }

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

    @Override
    protected void prepareEmptyInitialView(View ev)
    {
        ((ImageView) ev.findViewById(R.id.empty_picture)).setScaleType(ImageView.ScaleType.FIT_XY);
        ev.findViewById(R.id.empty_text).setVisibility(View.GONE);
        ev.findViewById(R.id.empty_text_description).setVisibility(View.GONE);
    }

    @Override
    protected void prepareEmptyView(View ev)
    {
        ev.findViewById(R.id.empty_text).setVisibility(View.VISIBLE);
        ((TextView) ev.findViewById(R.id.empty_text)).setText(R.string.sites_empty_title);
        ev.findViewById(R.id.empty_text_description).setVisibility(View.GONE);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void activateSend()
    {
        if (searchText.getText().length() > 0)
        {
            bAdd.setEnabled(true);
        }
        else
        {
            bAdd.setEnabled(false);
        }
    }

    private void search()
    {
        if (searchText.getText().length() > 0)
        {
            keyword = searchText.getText().toString().trim();
            refresh();
            bAdd.setEnabled(false);
        }
        else
        {
            AlfrescoNotificationManager.getInstance(getActivity()).showToast(R.string.search_form_hint);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REFRESH
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void refresh()
    {
        super.refresh();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(SiteSearchEvent event)
    {
        displayData(event);
        bAdd.setEnabled(true);
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
            extraConfiguration = new Bundle();
        }

        public Builder(Activity activity, Map<String, Object> configuration)
        {
            super(activity, configuration);
            menuIconId = R.drawable.ic_site_dark;
            menuTitleId = R.string.menu_browse_sites;
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

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return SearchSitesFragment.newInstanceByTemplate(b);
        }
    }
}
