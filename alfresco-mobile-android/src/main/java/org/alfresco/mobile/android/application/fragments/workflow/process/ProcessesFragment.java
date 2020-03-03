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
package org.alfresco.mobile.android.application.fragments.workflow.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.workflow.task.TaskDetailsFragment;
import org.alfresco.mobile.android.application.fragments.workflow.task.TasksHelper;
import org.alfresco.mobile.android.async.workflow.process.ProcessesEvent;
import org.alfresco.mobile.android.async.workflow.process.start.StartProcessEvent;
import org.alfresco.mobile.android.async.workflow.task.complete.CompleteTaskEvent;
import org.alfresco.mobile.android.async.workflow.task.delegate.ReassignTaskEvent;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.utils.UIUtils;
import org.alfresco.mobile.android.ui.workflow.process.ProcessesFoundationFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

public class ProcessesFragment extends ProcessesFoundationFragment
{
    private static final String ARGUMENT_MENUID = "menuId";

    private static final String ARGUMENT_FILTER = "TaskFragmentFilter";

    public static final String TAG = ProcessesFragment.class.getName();

    protected List<Process> selectedItems = new ArrayList<Process>(1);

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public ProcessesFragment()
    {
        emptyListMessageId = R.string.empty_tasks;
        loadState = LOAD_VISIBLE;
        setHasOptionsMenu(true);
    }

    public static ProcessesFragment newInstanceByTemplate(Bundle b)
    {
        ProcessesFragment bf = new ProcessesFragment();
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onResume()
    {
        if (getArguments().containsKey(ARGUMENT_MENUID))
        {
            TasksHelper.displayNavigationMode((AlfrescoActivity) getActivity(), false,
                    getArguments().getInt(ARGUMENT_MENUID));
           getActionBar().setDisplayShowTitleEnabled(false);
           getActionBar().setDisplayShowCustomEnabled(true);
            // getActionBar().setCustomView(null);
        }
        else
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.my_tasks));
        }
        getActivity().invalidateOptionsMenu();

        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new ProcessesAdapter(getActivity(), R.layout.row_two_lines_caption_divider, new ArrayList<Process>(0),
                selectedItems);
    }

    @Subscribe
    public void onResult(ProcessesEvent event)
    {
        super.onResult(event);
    }

    @Override
    protected void prepareEmptyView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        emptyImageView.setImageResource(R.drawable.ic_empty_tasks);
        firstEmptyMessage.setText(R.string.tasks_list_empty_title);
        secondEmptyMessage.setVisibility(View.VISIBLE);
        secondEmptyMessage.setText(R.string.tasks_list_empty_description);
    }

    @Override
    protected View.OnClickListener onPrepareFabClickListener()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onOptionMenuItemSelected(R.id.menu_workflow_add);
            }
        };
    }
    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public static void getMenu(Context context, Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, R.id.menu_workflow_add, Menu.FIRST, R.string.workflow_start);
        mi.setIcon(android.R.drawable.ic_menu_add);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuFragmentHelper.getMenu(context, menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return !onOptionMenuItemSelected(item.getItemId()) ? false : super.onOptionsItemSelected(item);
    }

    private boolean onOptionMenuItemSelected(int itemId)
    {
        switch (itemId)
        {
            case R.id.menu_workflow_add:
                Intent in = new Intent(PrivateIntent.ACTION_START_PROCESS, null, getActivity(),
                        PrivateDialogActivity.class);
                in.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getAccount().getId());
                getActivity().startActivity(in);
                return true;
            default:
                break;
        }
        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
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
                FragmentDisplayer.with(getActivity()).remove(DisplayUtils.getCentralFragmentId(getActivity()));
            }
            selectedItems.clear();
        }
        else
        {
            // Show properties
            TaskDetailsFragment.with(getActivity()).process(item).display();
        }
        adapter.notifyDataSetChanged();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onProcessStarted(StartProcessEvent event)
    {
        refresh();
    }

    @Subscribe
    public void onTaskCompleted(CompleteTaskEvent event)
    {
        refresh();
    }

    @Subscribe
    public void onTaskDelegateCompleted(ReassignTaskEvent event)
    {
        refresh();
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
        private static final String ARGUMENT_MENUID = "menuId";

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

        public ListingFragmentBuilder itemPosition(int itemPosition)
        {
            extraConfiguration.putInt(ARGUMENT_MENUID, itemPosition);
            return this;
        }
    }
}
