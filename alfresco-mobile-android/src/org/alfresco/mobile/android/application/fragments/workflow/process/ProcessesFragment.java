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
import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.api.services.WorkflowService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.workflow.task.TasksHelper;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ProcessesFragment extends BaseListFragment implements
        LoaderCallbacks<LoaderResult<PagingResult<Process>>>, RefreshFragment
{

    private static final String PARAM_MENUID = "menuId";

    private static final String PARAM_FILTER = "TaskFragmentFilter";

    public static final String TAG = ProcessesFragment.class.getName();

    protected List<Process> selectedItems = new ArrayList<Process>(1);

    private TasksFragmentReceiver receiver;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public ProcessesFragment()
    {
        loaderId = ProcessesLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_tasks;
        initLoader = true;
        checkSession = false;
    }

    public static ProcessesFragment newInstance()
    {
        ListingFilter lf = new ListingFilter();
        lf.addFilter(WorkflowService.FILTER_KEY_STATUS, WorkflowService.FILTER_STATUS_ACTIVE);
        return newInstance(lf, 0);
    }

    public static ProcessesFragment newInstance(ListingFilter f)
    {
        ProcessesFragment bf = new ProcessesFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_FILTER, f);
        bf.setArguments(b);
        return bf;
    };

    public static ProcessesFragment newInstance(ListingFilter f, int menuId)
    {
        ProcessesFragment bf = new ProcessesFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_FILTER, f);
        b.putInt(PARAM_MENUID, menuId);
        bf.setArguments(b);
        return bf;
    };

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        if (getArguments().containsKey(PARAM_MENUID))
        {
            TasksHelper.displayNavigationMode(getActivity(), false, getArguments().getInt(PARAM_MENUID));
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
            getActivity().getActionBar().setDisplayShowCustomEnabled(true);
            getActivity().getActionBar().setCustomView(null);
        }
        else
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.my_tasks));
        }
        getActivity().invalidateOptionsMenu();

        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_TASK_COMPLETED);
        intentFilter.addAction(IntentIntegrator.ACTION_START_PROCESS_COMPLETED);
        intentFilter.addAction(IntentIntegrator.ACTION_TASK_DELEGATE_COMPLETED);
        receiver = new TasksFragmentReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);

        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<PagingResult<Process>>> onCreateLoader(int id, Bundle ba)
    {
        setListShown(false);

        bundle = (ba == null) ? getArguments() : ba;

        ListingContext lc = null, lcorigin = null;
        ListingFilter lf = null;
        ProcessesLoader st = null;
        if (bundle != null)
        {
            lf = (ListingFilter) bundle.getSerializable(PARAM_FILTER);
            lcorigin = (ListingContext) bundle.getSerializable(ARGUMENT_LISTING);
            lc = copyListing(lcorigin);
            if (lc == null && lf != null)
            {
                lc = new ListingContext();
            }
            lc.setFilter(lf);
            loadState = bundle.getInt(LOAD_STATE);
            st = new ProcessesLoader(getActivity(), alfSession);
        }
        else
        {
            st = new ProcessesLoader(getActivity(), alfSession);
        }
        calculateSkipCount(lc);
        st.setListingContext(lc);
        return st;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Process>>> arg0,
            LoaderResult<PagingResult<Process>> results)
    {
        if (adapter == null)
        {
            adapter = new ProcessesAdapter(getActivity(), R.layout.sdk_list_row, new ArrayList<Process>(0),
                    selectedItems);
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
    public void onLoaderReset(Loader<LoaderResult<PagingResult<Process>>> arg0)
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

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_REFRESH, 1000 + MenuActionItem.MENU_REFRESH, R.string.refresh);
        mi.setIcon(R.drawable.ic_refresh);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Process item = (Process) l.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).equals(item);
        }
        l.setItemChecked(position, true);

        selectedItems.clear();
        if (!hideDetails && DisplayUtils.hasCentralPane(getActivity()))
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
            ((MainActivity) getActivity()).addProcessDetailsFragment(item, !DisplayUtils.hasCentralPane(getActivity()));
            DisplayUtils.switchSingleOrTwo(getActivity(), true);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void refresh()
    {
        reload(bundle, loaderId, this);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    public class TasksFragmentReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getAction());

            if (getActivity() == null) { return; }

            if (intent.getExtras() != null)
            {
                ProcessesFragment tasksFragment = (ProcessesFragment) getFragmentManager().findFragmentByTag(
                        ProcessesFragment.TAG);

                if (intent.getAction().equals(IntentIntegrator.ACTION_TASK_COMPLETED)
                        || intent.getAction().equals(IntentIntegrator.ACTION_START_PROCESS_COMPLETED)
                        || intent.getAction().equals(IntentIntegrator.ACTION_TASK_DELEGATE_COMPLETED))
                {
                    tasksFragment.refresh();
                    return;
                }

            }
        }
    }
}
