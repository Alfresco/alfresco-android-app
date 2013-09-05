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
package org.alfresco.mobile.android.application.fragments.workflow.process;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.ListingFilter;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.api.services.WorkflowService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.workflow.task.TaskActions;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;

public class ProcessTasksFragment extends BaseListFragment implements LoaderCallbacks<LoaderResult<PagingResult<Task>>>
{

    private static final String PARAM_PROCESS = "TaskProcess";

    public static final String TAG = ProcessTasksFragment.class.getName();

    protected List<Task> selectedItems = new ArrayList<Task>(1);

    private TaskActions nActions;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public ProcessTasksFragment()
    {
        loaderId = ProcessTasksLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_tasks;
        initLoader = false;
        checkSession = false;
    }

    public static ProcessTasksFragment newInstance()
    {
        ProcessTasksFragment bf = new ProcessTasksFragment();
        return bf;
    }

    public static ProcessTasksFragment newInstance(String processIdentifier)
    {
        ProcessTasksFragment bf = new ProcessTasksFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_PROCESS, processIdentifier);
        bf.setArguments(b);
        return bf;
    };

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
            getDialog().setTitle(R.string.tasks_history);
        }
        setRetainInstance(false);
        if (container != null)
        {
            container.setVisibility(View.VISIBLE);
        }
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);

        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v == null && getDialog() != null)
        {
            v = inflater.inflate(R.layout.sdk_list, container, false);
            init(v, emptyListMessageId);
        }
        v.setBackgroundColor(Color.WHITE);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(ProcessTasksLoader.ID, null, this);
        lv.setDivider(null);
    }

    @Override
    public void onResume()
    {
        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_validate);
        }
        getActivity().invalidateOptionsMenu();
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<PagingResult<Task>>> onCreateLoader(int id, Bundle ba)
    {
        setListShown(false);

        bundle = (ba == null) ? getArguments() : ba;

        ListingContext lc = null, lcorigin = null;
        ProcessTasksLoader st = null;
        ListingFilter lf = new ListingFilter();
        lf.addFilter(WorkflowService.FILTER_KEY_STATUS, WorkflowService.FILTER_STATUS_ANY);
        lf.addFilter(WorkflowService.FILTER_KEY_ASSIGNEE, WorkflowService.FILTER_ASSIGNEE_ALL);
        String processIdentifier = bundle.getString(PARAM_PROCESS);
        if (bundle != null)
        {
            lcorigin = (ListingContext) bundle.getSerializable(ARGUMENT_LISTING);
            lc = copyListing(lcorigin);
            loadState = bundle.getInt(LOAD_STATE);
            st = new ProcessTasksLoader(getActivity(), alfSession, processIdentifier);
        }
        else
        {
            st = new ProcessTasksLoader(getActivity(), alfSession, processIdentifier);
        }
        calculateSkipCount(lc);
        if (lc == null)
        {
            lc = new ListingContext();
        }
        lc.setFilter(lf);
        st.setListingContext(lc);
        return st;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Task>>> arg0, LoaderResult<PagingResult<Task>> results)
    {
        if (adapter == null)
        {
            adapter = new ProcessTasksAdapter(getActivity(), R.layout.app_task_history_row, new ArrayList<Task>(0));
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
    public void onLoaderReset(Loader<LoaderResult<PagingResult<Task>>> arg0)
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
        Task item = (Task) l.getItemAtPosition(position);

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
            ((MainActivity) getActivity()).addTaskDetailsFragment(item, true);
            DisplayUtils.switchSingleOrTwo(getActivity(), true);
        }
        adapter.notifyDataSetChanged();
    }

    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        if (nActions != null) { return false; }

        Task item = (Task) l.getItemAtPosition(position);

        selectedItems.clear();
        selectedItems.add(item);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new TaskActions(ProcessTasksFragment.this, selectedItems);
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
    }
}
