/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.workflow;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.browser.NodeAdapter;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ItemsFragment extends BaseListFragment implements LoaderCallbacks<LoaderResult<PagingResult<Node>>>
{
    public static final String TAG = ItemsFragment.class.getName();

    private static final String PARAM_TASK = "taskObject";

    protected List<Node> selectedItems = new ArrayList<Node>(1);

    private NodeActions nActions;

    private Task currentTask;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public ItemsFragment()
    {
        loaderId = ItemsLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_items;
        initLoader = false;
        checkSession = false;
    }

    public static ItemsFragment newInstance(Task task)
    {
        ItemsFragment bf = new ItemsFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_TASK, task);
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
        getLoaderManager().restartLoader(ItemsLoader.ID, null, this);
    }

    @Override
    public void onResume()
    {
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.task_items));
        }
        getActivity().invalidateOptionsMenu();
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<PagingResult<Node>>> onCreateLoader(int id, Bundle ba)
    {
        setListShown(false);

        bundle = (ba == null) ? getArguments() : ba;

        currentTask = (Task) bundle.get(PARAM_TASK);

        ListingContext lc = null, lcorigin = null;
        ItemsLoader st = null;
        if (bundle != null)
        {
            lcorigin = (ListingContext) bundle.getSerializable(ARGUMENT_LISTING);
            lc = copyListing(lcorigin);
            loadState = bundle.getInt(LOAD_STATE);
        }
        st = new ItemsLoader(getActivity(), alfSession, currentTask);
        calculateSkipCount(lc);
        st.setListingContext(lc);
        return st;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Node>>> arg0, LoaderResult<PagingResult<Node>> results)
    {
        if (adapter == null)
        {
            adapter = new NodeAdapter(getActivity(), R.layout.sdk_list_row, new ArrayList<Node>(0), selectedItems, -1);
        }
        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<PagingResult<Node>>> arg0)
    {
        // TODO Auto-generated method stub
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public static void getMenu(Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_WORKFLOW_ADD, Menu.FIRST + MenuActionItem.MENU_WORKFLOW_ADD,
                R.string.workflow_start);
        mi.setIcon(android.R.drawable.ic_menu_add);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Node item = (Node) l.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).equals(item);
        }
        l.setItemChecked(position, true);

        if (nActions != null)
        {
            nActions.selectNode(item);
            if (selectedItems.size() == 0)
            {
                hideDetails = true;
            }
        }
        else
        {
            selectedItems.clear();
            if (!hideDetails && DisplayUtils.hasCentralPane(getActivity()))
            {
                selectedItems.add(item);
            }
        }

        if (hideDetails)
        {
            selectedItems.clear();
        }
        else if (nActions == null)
        {
            // Show properties
            ((MainActivity) getActivity()).addPropertiesFragment(item.getIdentifier(), true);
            DisplayUtils.switchSingleOrTwo(getActivity(), true);
        }
        adapter.notifyDataSetChanged();
    }

    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        if (nActions != null) { return false; }

        Node item = (Node) l.getItemAtPosition(position);

        selectedItems.clear();
        selectedItems.add(item);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new NodeActions(ItemsFragment.this, selectedItems);
        nActions.setOnFinishModeListerner(new onFinishModeListerner()
        {
            @Override
            public void onFinish()
            {
                nActions = null;
                selectedItems.clear();
                refreshListView();
            }
        });
        getActivity().startActionMode(nActions);
        adapter.notifyDataSetChanged();
        return true;
    };
}
