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
package org.alfresco.mobile.android.application.fragments.workflow.process;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.workflow.task.TaskDetailsFragment;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.workflow.process.ProcessDefinitionsEvent;
import org.alfresco.mobile.android.async.workflow.task.TasksRequest;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

public class ProcessTasksFragment extends BaseGridFragment
{
    private static final String ARGUMENT_PROCESS = "TaskProcess";

    public static final String TAG = ProcessTasksFragment.class.getName();

    protected List<Task> selectedItems = new ArrayList<Task>(1);

    private String processId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public ProcessTasksFragment()
    {
        emptyListMessageId = R.string.empty_tasks;
        retrieveDataOnCreation = false;
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
        b.putSerializable(ARGUMENT_PROCESS, processIdentifier);
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
        setSession(SessionUtils.getSession(getActivity()));
        SessionUtils.checkSession(getActivity(), getSession());

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
        SessionUtils.checkSession(getActivity(), getSession());
        super.onActivityCreated(savedInstanceState);

        processId = getArguments().getString(ARGUMENT_PROCESS);

        Operator.with(getActivity(), SessionUtils.getAccount(getActivity())).load(new TasksRequest.Builder(processId));
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
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new TasksRequest.Builder(processId).setListingContext(listingContext);
    }

    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new ProcessTasksAdapter(getActivity(), R.layout.app_task_history_row, new ArrayList<Task>(0));
    }

    @Subscribe
    public void onResult(ProcessDefinitionsEvent event)
    {
        displayData(event);
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
    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        Task item = (Task) l.getItemAtPosition(position);

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
            selectedItems.clear();
        }
        else
        {
            TaskDetailsFragment.with(getActivity()).task(item).display();
        }
        adapter.notifyDataSetChanged();
    }
}
