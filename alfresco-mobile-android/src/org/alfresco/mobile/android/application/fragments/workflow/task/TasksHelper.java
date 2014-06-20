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
package org.alfresco.mobile.android.application.fragments.workflow.task;

import java.util.Collection;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.ListingFilter;
import org.alfresco.mobile.android.api.services.WorkflowService;
import org.alfresco.mobile.android.api.services.impl.publicapi.PublicAPIWorkflowServiceImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.workflow.process.ProcessesFragment;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.SharedPreferences;

public final class TasksHelper
{

    public static final String TASK_FILTER_PREFS = "org.alfresco.mobile.android.tasks.preferences";

    private static final String TASK_FILTER_DEFAULT = "org.alfresco.mobile.android.tasks.preferences.filter.default";

    private static final String TAG = TasksHelper.class.getName();

    private TasksHelper()
    {
    }

    public static void displayNavigationMode(final Activity activity)
    {
        SharedPreferences prefs = activity.getSharedPreferences(TASK_FILTER_PREFS, 0);
        int currentSelection = prefs.getInt(TASK_FILTER_DEFAULT, 0);
        displayNavigationMode(activity, true, currentSelection, true);
    }

    public static void displayNavigationMode(final Activity activity, final boolean backStack, int menuId)
    {
        displayNavigationMode(activity, backStack, menuId, false);
    }

    private static void displayNavigationMode(final Activity activity, final boolean backStack, int menuId,
            final boolean firstTime)
    {
        activity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        TasksShortCutAdapter adapter = new TasksShortCutAdapter(activity);

        OnNavigationListener mOnNavigationListener = new OnNavigationListener()
        {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId)
            {
                boolean isProcessFragment = false;
                SharedPreferences prefs = activity.getSharedPreferences(TASK_FILTER_PREFS, 0);
                int currentSelection = prefs.getInt(TASK_FILTER_DEFAULT, 1);

                if (!backStack && itemPosition == currentSelection) { return true; }

                ListingFilter f = new ListingFilter();
                switch (itemPosition)
                {
                    case TasksShortCutAdapter.FILTER_ACTIVE:
                        f.addFilter(WorkflowService.FILTER_KEY_STATUS, WorkflowService.FILTER_STATUS_ACTIVE);
                        break;
                    case TasksShortCutAdapter.FILTER_INITIATOR:
                        isProcessFragment = true;
                        break;
                    case TasksShortCutAdapter.FILTER_COMPLETED:
                        f.addFilter(WorkflowService.FILTER_KEY_STATUS, WorkflowService.FILTER_STATUS_COMPLETE);
                        break;
                    case TasksShortCutAdapter.FILTER_HIGH_PRIORITY:
                        f.addFilter(WorkflowService.FILTER_KEY_PRIORITY, WorkflowService.FILTER_PRIORITY_HIGH);
                        break;
                    case TasksShortCutAdapter.FILTER_DUE_TODAY:
                        f.addFilter(WorkflowService.FILTER_KEY_DUE, WorkflowService.FILTER_DUE_TODAY);
                        break;
                    case TasksShortCutAdapter.FILTER_OVERDUE:
                        f.addFilter(WorkflowService.FILTER_KEY_DUE, WorkflowService.FILTER_DUE_OVERDUE);
                    case TasksShortCutAdapter.FILTER_ASSIGNED:
                        f.addFilter(WorkflowService.FILTER_KEY_ASSIGNEE, WorkflowService.FILTER_ASSIGNEE_ME);
                        break;
                    case TasksShortCutAdapter.FILTER_CUSTOM:
                        TaskFilterFragment.with(activity).display();
                        return true;
                    default:
                        break;
                }

                if (!backStack)
                {
                    activity.getFragmentManager().popBackStack();
                }

                if (isProcessFragment)
                {
                    prefs.edit().putInt(TASK_FILTER_DEFAULT, itemPosition).commit();
                    f.addFilter(PublicAPIWorkflowServiceImpl.INCLUDE_VARIABLES, "true");

                    ListingContext lc = new ListingContext();
                    lc.setFilter(f);
                    ProcessesFragment.with(activity).itemPosition(itemPosition).setListingContext(lc).display();
                }
                else
                {
                    ListingContext lc = new ListingContext();
                    lc.setFilter(f);
                    TasksFragment.with(activity).menuId(itemPosition).setListingContext(lc).display();
                    prefs.edit().putInt(TASK_FILTER_DEFAULT, itemPosition).commit();
                }
                return true;
            }
        };
        activity.getActionBar().setListNavigationCallbacks(adapter, mOnNavigationListener);
        int currentSelection = menuId;
        activity.getActionBar().setSelectedNavigationItem(currentSelection);
    }

    public static ListingFilter createFilter(Collection<Integer> selectedItems)
    {
        ListingFilter f = new ListingFilter();
        for (Integer value : selectedItems)
        {
            switch (value)
            {
                case R.string.tasks_active:
                    f.addFilter(WorkflowService.FILTER_KEY_STATUS, WorkflowService.FILTER_STATUS_ACTIVE);
                    break;
                case R.string.tasks_completed:
                    f.addFilter(WorkflowService.FILTER_KEY_STATUS, WorkflowService.FILTER_STATUS_COMPLETE);
                    break;
                case R.string.tasks_due_today:
                    f.addFilter(WorkflowService.FILTER_KEY_DUE, WorkflowService.FILTER_DUE_TODAY);
                    break;
                case R.string.tasks_due_tomorrow:
                    f.addFilter(WorkflowService.FILTER_KEY_DUE, WorkflowService.FILTER_DUE_TOMORROW);
                    break;
                case R.string.tasks_due_week:
                    f.addFilter(WorkflowService.FILTER_KEY_DUE, WorkflowService.FILTER_DUE_7DAYS);
                    break;
                case R.string.tasks_due_over:
                    f.addFilter(WorkflowService.FILTER_KEY_DUE, WorkflowService.FILTER_DUE_OVERDUE);
                    break;
                case R.string.tasks_due_no_date:
                    f.addFilter(WorkflowService.FILTER_KEY_DUE, WorkflowService.FILTER_DUE_NODATE);
                    break;
                case R.string.tasks_priority_low:
                    f.addFilter(WorkflowService.FILTER_KEY_PRIORITY, WorkflowService.FILTER_PRIORITY_LOW);
                    break;
                case R.string.tasks_priority_medium:
                    f.addFilter(WorkflowService.FILTER_KEY_PRIORITY, WorkflowService.FILTER_PRIORITY_MEDIUM);
                    break;
                case R.string.tasks_priority_high:
                    f.addFilter(WorkflowService.FILTER_KEY_PRIORITY, WorkflowService.FILTER_PRIORITY_HIGH);
                    break;
                case R.string.tasks_assignee_me:
                    f.addFilter(WorkflowService.FILTER_KEY_ASSIGNEE, WorkflowService.FILTER_ASSIGNEE_ME);
                    break;
                case R.string.tasks_assignee_unassigned:
                    f.addFilter(WorkflowService.FILTER_KEY_ASSIGNEE, WorkflowService.FILTER_ASSIGNEE_UNASSIGNED);
                    break;
                default:
                    break;
            }
        }
        return f;
    }
}
