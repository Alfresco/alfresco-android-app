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

import java.util.Map;

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
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

public class TaskFilterFragment extends BaseFragment
{
    public static final String TAG = TaskFilterFragment.class.getName();

    private ExpandableListView expandableList;

    private TaskFilterExpandableAdapter expListAdapter;

    private Button validate;

    private Map<Integer, Integer> selectedItems;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public static TaskFilterFragment newInstance()
    {
        return new TaskFilterFragment();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(true);
        
        View rootView = inflater.inflate(R.layout.app_task_filters, container, false);

        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);

        expandableList = (ExpandableListView) rootView.findViewById(R.id.filters_list);
        expandableList.setGroupIndicator(null);
        expListAdapter = new TaskFilterExpandableAdapter(getActivity(), selectedItems);
        expandableList.setAdapter(expListAdapter);

        expandableList.setOnChildClickListener(new OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {
                expListAdapter.select(v,  groupPosition,  childPosition);
                return false;
            }
        });
        
        validate = (Button) rootView.findViewById(R.id.validate);
        validate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createFilter();
            }
        });
        

        return rootView;
    }

    @Override
    public void onResume()
    {
        UIUtils.displayTitle(getActivity(), getString(R.string.my_tasks));
        super.onResume();
    }
    
    
    private void createFilter(){
        ListingFilter f = new ListingFilter();
        
        selectedItems = expListAdapter.getSelectedItems();
        for (Integer value : selectedItems.values())
        {
            switch (value)
            {
                case R.string.tasks_active:
                    f.addFilter(WorkflowService.FILTER_STATUS, WorkflowService.FILTER_STATUS_ACTIVE);
                    break;
                case R.string.tasks_completed:
                    f.addFilter(WorkflowService.FILTER_STATUS, WorkflowService.FILTER_STATUS_COMPLETE);
                    break;
                case R.string.tasks_due_today:
                    f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_TODAY);
                    break;
                case R.string.tasks_due_tomorrow:
                    f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_TOMORROW);
                    break;
                case R.string.tasks_due_week:
                    f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_7DAYS);
                    break;
                case R.string.tasks_due_over:
                    f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_OVERDUE);
                    break;
                case R.string.tasks_due_no_date:
                    f.addFilter(WorkflowService.FILTER_DUE, WorkflowService.FILTER_DUE_NODATE);
                    break;
                case R.string.tasks_priority_low:
                    f.addFilter(WorkflowService.FILTER_PRIORITY, WorkflowService.FILTER_PRIORITY_LOW);
                    break;
                case R.string.tasks_priority_medium:
                    f.addFilter(WorkflowService.FILTER_PRIORITY, WorkflowService.FILTER_PRIORITY_MEDIUM);
                    break;
                case R.string.tasks_priority_high:
                    f.addFilter(WorkflowService.FILTER_PRIORITY, WorkflowService.FILTER_PRIORITY_HIGH);
                    break;
                case R.string.tasks_assignee_me:
                    f.addFilter(WorkflowService.FILTER_ASSIGNEE, alfSession.getPersonIdentifier());
                    break;
                case R.string.tasks_assignee_unassigned:
                    f.addFilter(WorkflowService.FILTER_ASSIGNEE, WorkflowService.FILTER_ASSIGNEE_UNASSIGNED);
                    break;
                default:
                    break;
            }
        }

        Fragment taskFragment = TasksFragment.newInstance(f);
        FragmentDisplayer.replaceFragment(getActivity(), taskFragment, DisplayUtils.getLeftFragmentId(getActivity()),
                TasksFragment.TAG, true);
    }
}
