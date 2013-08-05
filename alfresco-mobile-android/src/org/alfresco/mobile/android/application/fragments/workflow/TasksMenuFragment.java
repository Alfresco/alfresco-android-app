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
package org.alfresco.mobile.android.application.fragments.workflow;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.ListingFilter;
import org.alfresco.mobile.android.api.services.WorkflowService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class TasksMenuFragment extends BaseFragment
{
    public static final String TAG = TasksMenuFragment.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public static BaseFragment newInstance()
    {
        return new TasksMenuFragment();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.app_task_menu, container, false);
        
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        
        initClickListener(rootView);
        return rootView;
    }
    

    @Override
    public void onResume()
    {
        UIUtils.displayTitle(getActivity(), getString(R.string.my_tasks));
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private void initClickListener(View rootView)
    {
        for (Integer buttonId : TASK_FILTERS)
        {
            rootView.findViewById(buttonId).setOnClickListener(menuClickListener);
        }
    }

    private static final List<Integer> TASK_FILTERS = new ArrayList<Integer>(7)
    {
        private static final long serialVersionUID = 1L;
        {
            add(R.id.tasks_active);
            add(R.id.tasks_completed);

            add(R.id.tasks_due_today);
            add(R.id.tasks_due_tomorrow);
            add(R.id.tasks_due_week);
            add(R.id.tasks_due_over);
            add(R.id.tasks_due_no_date);

            add(R.id.tasks_priority_low);
            add(R.id.tasks_priority_medium);
            add(R.id.tasks_priority_high);

            add(R.id.tasks_assignee_me);
            add(R.id.tasks_assignee_unassigned);
        }
    };

    private OnClickListener menuClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            ListingFilter f = new ListingFilter();
            switch (v.getId())
            {
                case R.id.tasks_active:
                    f.addFilter(WorkflowService.FILTER_STATUS, WorkflowService.FILTER_STATUS_ACTIVE);
                    break;
                case R.id.tasks_completed:
                    f.addFilter(WorkflowService.FILTER_STATUS, WorkflowService.FILTER_STATUS_COMPLETE);
                    break;
                case R.id.tasks_due_today:
                    f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_TODAY);
                    break;
                case R.id.tasks_due_tomorrow:
                    f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_TOMORROW);
                    break;
                case R.id.tasks_due_week:
                    f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_7DAYS);
                    break;
                case R.id.tasks_due_over:
                    f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_OVERDUE);
                    break;
                case R.id.tasks_due_no_date:
                    f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_NODATE);
                    break;
                case R.id.tasks_priority_low:
                    f.addFilter(WorkflowService.FILTER_PRIORITY, WorkflowService.FILTER_PRIORITY_LOW);
                    break;
                case R.id.tasks_priority_medium:
                    f.addFilter(WorkflowService.FILTER_PRIORITY, WorkflowService.FILTER_PRIORITY_MEDIUM);
                    break;
                case R.id.tasks_priority_high:
                    f.addFilter(WorkflowService.FILTER_PRIORITY, WorkflowService.FILTER_PRIORITY_HIGH);
                    break;
                case R.id.tasks_assignee_me:
                    f.addFilter(WorkflowService.FILTER_ASSIGNEE, alfSession.getPersonIdentifier());
                    break;
                case R.id.tasks_assignee_unassigned:
                    f.addFilter(WorkflowService.FILTER_ASSIGNEE, WorkflowService.FILTER_ASSIGNEE_UNASSIGNED);
                    break;
                default:
                    break;
            }

            Fragment taskFragment = TasksFragment.newInstance(f);
            FragmentDisplayer.replaceFragment(getActivity(), taskFragment, DisplayUtils.getLeftFragmentId(getActivity()),
                    TasksFragment.TAG, true);

        }
    };
}
