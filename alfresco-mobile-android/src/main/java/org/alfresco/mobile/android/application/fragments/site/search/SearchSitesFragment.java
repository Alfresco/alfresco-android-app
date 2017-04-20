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
package org.alfresco.mobile.android.application.fragments.site.search;

import java.util.Date;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.site.browser.CommonBrowserSitesFragment;
import org.alfresco.mobile.android.application.providers.search.HistorySearch;
import org.alfresco.mobile.android.application.providers.search.HistorySearchInlineCursorAdapter;
import org.alfresco.mobile.android.application.providers.search.HistorySearchManager;
import org.alfresco.mobile.android.application.providers.search.HistorySearchSchema;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.site.SiteFavoriteEvent;
import org.alfresco.mobile.android.async.site.member.CancelPendingMembershipEvent;
import org.alfresco.mobile.android.async.site.member.SiteMembershipEvent;
import org.alfresco.mobile.android.async.site.search.SiteSearchEvent;
import org.alfresco.mobile.android.async.site.search.SiteSearchRequest;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.squareup.otto.Subscribe;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Fragment to display the list of sites depending on criteria like favorite,
 * all, user sites.
 * 
 * @author Jean Marie Pascal
 */
public class SearchSitesFragment extends CommonBrowserSitesFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String TAG = SearchSitesFragment.class.getName();

    private MaterialAutoCompleteTextView searchText;

    private String keyword;

    private ImageButton bAdd;

    private HistorySearchInlineCursorAdapter searchAdapter;

    private HistorySearch historySearchItem;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public SearchSitesFragment()
    {
        super();
        retrieveDataOnCreation = false;
        loadState = LOAD_VISIBLE;
        reportAtCreation = false;
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

        searchText = (MaterialAutoCompleteTextView) viewById(R.id.search_query);
        searchAdapter = new HistorySearchInlineCursorAdapter(this, null, R.layout.row_two_lines_search);
        getLoaderManager().initLoader(0, null, this);
        searchText.setAdapter(searchAdapter);
        searchText.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                TwoLinesViewHolder vh = (TwoLinesViewHolder) view.getTag();
                HistorySearch tmphistorySearchItem = HistorySearchManager.retrieveHistorySearch(getActivity(),
                        (Long) vh.choose.getTag());
                searchText.setText(tmphistorySearchItem.getQuery());
                historySearchItem = tmphistorySearchItem;
                search();
            }
        });

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
                historySearchItem = null;
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
    protected void prepareEmptyInitialView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        emptyImageView.setImageResource(R.drawable.ic_empty_search_sites);
        firstEmptyMessage.setVisibility(View.GONE);
        secondEmptyMessage.setVisibility(View.GONE);
    }

    @Override
    protected void prepareEmptyView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        emptyImageView.setImageResource(R.drawable.ic_empty_search_sites);
        firstEmptyMessage.setVisibility(View.VISIBLE);
        firstEmptyMessage.setText(R.string.sites_search_empty_title);
        secondEmptyMessage.setVisibility(View.VISIBLE);
        secondEmptyMessage.setText(R.string.sites_search_empty_description);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void activateSend()
    {
        bAdd.setEnabled(true);
        getLoaderManager().restartLoader(0, null, this);
    }

    private void search()
    {
        searchText.dismissDropDown();
        keyword = searchText.getText().toString().trim();
        // Save history or update

        if (historySearchItem == null)
        {
            historySearchItem = HistorySearchManager.retrieveHistorySearchByQuery(getActivity(), getAccount().getId(),
                    HistorySearch.TYPE_SITE, keyword);
        }

        if (historySearchItem == null)
        {
            HistorySearchManager.createHistorySearch(getActivity(), getAccount().getId(), HistorySearch.TYPE_SITE, 0,
                    keyword, keyword, new Date().getTime());
        }
        else
        {
            HistorySearchManager.update(getActivity(), historySearchItem.getId(), historySearchItem.getAccountId(),
                    historySearchItem.getType(), historySearchItem.getAdvanced(), historySearchItem.getDescription(),
                    historySearchItem.getQuery(), new Date().getTime());
        }

        refresh();
        bAdd.setEnabled(false);
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
    // LOADERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String searchTextValue = "*";
        if (searchText != null && !TextUtils.isEmpty(searchText.getText().toString()))
        {
            searchTextValue = searchText.getText().toString();
        }

        if (searchText == null || TextUtils.isEmpty(searchText.getText().toString()))
        {
            return new CursorLoader(getActivity(), HistorySearchManager.CONTENT_URI, HistorySearchManager.COLUMN_ALL,
                    HistorySearchSchema.COLUMN_ACCOUNT_ID + " = " + getAccount().getId() + " AND "
                            + HistorySearchSchema.COLUMN_TYPE + " = " + HistorySearch.TYPE_SITE,
                    null, HistorySearchSchema.COLUMN_LAST_REQUEST_TIMESTAMP + " DESC " + " LIMIT 5");
        }
        else
        {
            return new CursorLoader(getActivity(), HistorySearchManager.CONTENT_URI, HistorySearchManager.COLUMN_ALL,
                    HistorySearchSchema.COLUMN_ACCOUNT_ID + " = " + getAccount().getId() + " AND "
                            + HistorySearchSchema.COLUMN_TYPE + " = " + HistorySearch.TYPE_SITE + " AND "
                            + HistorySearchSchema.COLUMN_DESCRIPTION + " LIKE " + "'%" + searchTextValue + "%'",
                    null,
                    HistorySearchSchema.COLUMN_LAST_REQUEST_TIMESTAMP + " DESC " + " LIMIT 5");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        Log.d("DATA", data.isClosed() + "");
        if (!data.isClosed())
        {
            searchAdapter.changeCursor(data);
        }
        // searchText.setAdapter(searchAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        searchAdapter.changeCursor(null);
    }

    public void setSearchValue(String searchValue)
    {
        if (searchText != null)
        {
            searchText.setText(searchValue);
            searchText.setSelection(searchValue.length());
        }
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
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends ListingFragmentBuilder
    {

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
