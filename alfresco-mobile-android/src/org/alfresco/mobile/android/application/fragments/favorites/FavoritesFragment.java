/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.favorites;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.FavoritesLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.browser.NodeAdapter;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.app.ActionBar;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

@SuppressWarnings("rawtypes")
public class FavoritesFragment extends BaseListFragment implements
        LoaderCallbacks<LoaderResult<PagingResult>>, RefreshFragment
{
    public static final String TAG = FavoritesFragment.class.getName();

    public static final int MODE_DOCUMENTS = FavoritesLoader.MODE_DOCUMENTS;

    public static final int MODE_FOLDERS = FavoritesLoader.MODE_FOLDERS;

    public static final int MODE_BOTH = FavoritesLoader.MODE_BOTH;
    
    private static final String PARAM_MODE = "FavoriteMode";

    
    protected List<Node> selectedItems = new ArrayList<Node>(1);

    private int mode = MODE_DOCUMENTS; 

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public FavoritesFragment()
    {
        loaderId = FavoritesLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_favorites;
    }

    public static FavoritesFragment newInstance(int mode)
    {
        FavoritesFragment bf = new FavoritesFragment();
        ListingContext lc = new ListingContext();
        lc.setMaxItems(50);
        Bundle b = createBundleArgs(lc, LOAD_MANUAL);
        b.putInt(PARAM_MODE, mode);
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
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        int titleId = R.string.menu_favorites;
        if (getActivity().getActionBar() != null)
        {
            getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            getActivity().getActionBar().setDisplayShowTitleEnabled(true);
            getActivity().setTitle(titleId);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<PagingResult>> onCreateLoader(int id, Bundle ba)
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
            mode  =  bundle.getInt(PARAM_MODE);
        }
        calculateSkipCount(lc);
        FavoritesLoader loader = new FavoritesLoader(getActivity(), alfSession, mode);
        loader.setListingContext(lc);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult>> arg0,
            LoaderResult<PagingResult> results)
    {
        if (adapter == null)
        {
            adapter = new NodeAdapter(getActivity(), R.layout.sdk_list_row, new ArrayList<Node>(0), selectedItems,
                    ListingModeFragment.MODE_LISTING);
        }
        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
        ((NodeAdapter) adapter).setActivateThumbnail(true);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<PagingResult>> arg0)
    {
        // DO Nothing
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Node item = (Node) l.getItemAtPosition(position);

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
            if (item.isFolder())
            {
                ((BaseActivity) getActivity()).addBrowserFragment((String) ((Folder) item).getPropertyValue(PropertyIds.PATH));
            }
            else
            {
                // Show properties
                ((MainActivity) getActivity()).addPropertiesFragment(item.getIdentifier());
                DisplayUtils.switchSingleOrTwo(getActivity(), true);
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public static void getMenu(Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_REFRESH, Menu.FIRST + MenuActionItem.MENU_REFRESH,
                R.string.refresh);
        mi.setIcon(R.drawable.ic_refresh);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public void refresh()
    {
        refresh(loaderId, callback);
    }
}
