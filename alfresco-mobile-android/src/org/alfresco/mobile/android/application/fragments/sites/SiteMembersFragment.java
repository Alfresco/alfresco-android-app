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
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/**
 * @since 1.3.0
 * @author jpascal
 *
 */
public class SiteMembersFragment extends BaseListFragment implements
        LoaderCallbacks<LoaderResult<PagingResult<Person>>>
{
    private static final String PARAM_SITE = "site";

    public static final String TAG = SiteMembersFragment.class.getName();

    private List<Person> selectedItems = new ArrayList<Person>(1);

    private Site site;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public SiteMembersFragment()
    {
        loaderId = SiteMembersLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_site;
    }

    public static SiteMembersFragment newInstance(Site site)
    {
        SiteMembersFragment bf = new SiteMembersFragment();
        ListingContext lc = new ListingContext();
        lc.setMaxItems(50);
        Bundle b = createBundleArgs(lc, LOAD_MANUAL);
        b.putSerializable(PARAM_SITE, site);
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        
        site = (Site) bundle.getSerializable(PARAM_SITE);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        UIUtils.displayTitle(getActivity(), String.format(getString(R.string.members_of), site.getTitle()));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<PagingResult<Person>>> onCreateLoader(int id, Bundle ba)
    {
        if (!hasmore)
        {
            setListShown(false);
        }

        // Case Init & case Reload
        bundle = (ba == null) ? getArguments() : ba;

        ListingContext lc = null, lcorigin = null;

        if (bundle != null)
        {
            lcorigin = (ListingContext) bundle.getSerializable(ARGUMENT_LISTING);
            lc = copyListing(lcorigin);
            loadState = bundle.getInt(LOAD_STATE);
            site = (Site) bundle.getSerializable(PARAM_SITE);
        }
        calculateSkipCount(lc);
        SiteMembersLoader loader = new SiteMembersLoader(getActivity(), alfSession, site);
        loader.setListingContext(lc);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Person>>> arg0,
            LoaderResult<PagingResult<Person>> results)
    {
        if (adapter == null)
        {
            adapter = new SiteMembersAdapter(this, R.layout.sdk_list_row, new ArrayList<Person>(0), selectedItems);
        }
        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<PagingResult<Person>>> arg0)
    {

    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Person item = (Person) l.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).equals(item);
            selectedItems.clear();
        }
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        l.setItemChecked(position, true);
        v.setSelected(true);

        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedItems.add(item);
        }

        if (hideDetails)
        {
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
            }
            selectedItems.clear();
        }
        else
        {
            // Show properties
            ((MainActivity) getActivity()).addPersonProfileFragment(item.getIdentifier());
            DisplayUtils.switchSingleOrTwo(getActivity(), true);
        }
    }

}
