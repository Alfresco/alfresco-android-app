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
package org.alfresco.mobile.android.application.fragments.workflow.task;

import java.util.ArrayList;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.configuration.ConfigurableActionHelper;
import org.alfresco.mobile.android.application.configuration.model.view.TasksConfigModel;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.async.workflow.process.start.StartProcessEvent;
import org.alfresco.mobile.android.async.workflow.task.TasksEvent;
import org.alfresco.mobile.android.async.workflow.task.complete.CompleteTaskEvent;
import org.alfresco.mobile.android.async.workflow.task.delegate.ReassignTaskEvent;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.template.ListingTemplate;
import org.alfresco.mobile.android.ui.utils.UIUtils;
import org.alfresco.mobile.android.ui.workflow.task.TasksFoundationAdapter;
import org.alfresco.mobile.android.ui.workflow.task.TasksFoundationFragment;

import com.squareup.otto.Subscribe;

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

public class TasksFragment extends TasksFoundationFragment
{
    private static final String ARGUMENT_MENU_ID = "menuId";

    private static final String ARGUMENT_FILTER = "TaskFragmentFilter";

    public static final String TAG = TasksFragment.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public TasksFragment()
    {
        emptyListMessageId = R.string.empty_tasks;
        loadState = LOAD_VISIBLE;
        setHasOptionsMenu(true);
        reportAtCreation = false;
        screenName = AnalyticsManager.SCREEN_TASKS_LISTING;
    }

    public static TasksFragment newInstanceByTemplate(Bundle b)
    {
        TasksFragment cbf = new TasksFragment();
        b.putBoolean(ARGUMENT_BASED_ON_TEMPLATE, true);
        BundleUtils.addIfNotNull(b, LOAD_STATE, LOAD_VISIBLE);
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void displayTitle()
    {
        if (getArguments().containsKey(ARGUMENT_MENU_ID))
        {
            TasksHelper.displayNavigationMode((AlfrescoActivity) getActivity(), false,
                    getArguments().getInt(ARGUMENT_MENU_ID));
            getActionBar().setDisplayUseLogoEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowCustomEnabled(false);
        }
        else
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.my_tasks));
        }
        getActivity().invalidateOptionsMenu();
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

    // ///////////////////////////////////////////////////////////////////////////
    // LOADERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new TasksFoundationAdapter(getActivity(), R.layout.row_two_lines_caption_divider, new ArrayList<Task>(0),
                selectedItems);
    }

    @Override
    @Subscribe
    public void onResult(TasksEvent request)
    {
        super.onResult(request);
    }

    @Override
    protected View.OnClickListener onPrepareFabClickListener()
    {
        if (ConfigurableActionHelper.isVisible(getContext(), getAccount(), ConfigurableActionHelper.ACTION_NODE_REVIEW))
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
        else
        {
            return null;
        }
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
        }
        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
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
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.with(getActivity()).remove(DisplayUtils.getCentralFragmentId(getActivity()));
            }
            selectedItems.clear();
        }
        else
        {
            TaskDetailsFragment.with(getActivity()).task(item).display();
        }

        if (adapter != null)
        {
            adapter.notifyDataSetChanged();
        }
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
            viewConfigModel = new TasksConfigModel(configuration);
            templateArguments = new String[] { ListingTemplate.ARGUMENT_HAS_FILTER, TasksConfigModel.FILTER_KEY_STATUS,
                    TasksConfigModel.FILTER_KEY_DUE, TasksConfigModel.FILTER_KEY_PRIORITY,
                    TasksConfigModel.FILTER_KEY_ASSIGNEE };
        }

        protected void retrieveCustomArgument(Map<String, Object> properties, Bundle b)
        {
            // Add Listing Filter as arguments for the view.
            TasksFoundationFragment.addFilter(properties, b);
        }

        public Builder retrieveFilter(Intent intent)
        {
            TasksFoundationFragment.addFilter(intent, extraConfiguration);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder menuId(int menuId)
        {
            extraConfiguration.putInt(ARGUMENT_MENU_ID, menuId);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            if (b.containsKey(ListingTemplate.ARGUMENT_HAS_FILTER)
                    || (extraConfiguration != null && extraConfiguration.containsKey(ListingTemplate.ARGUMENT_LISTING)))
            {
                AnalyticsHelper.reportScreen(getActivity(), AnalyticsManager.SCREEN_TASKS_FILTER_LISTING);
                return newInstanceByTemplate(b);
            }
            else
            {
                TasksHelper.displayNavigationMode((AlfrescoActivity) activity.get());
                return null;
            }
        }
    }
}
