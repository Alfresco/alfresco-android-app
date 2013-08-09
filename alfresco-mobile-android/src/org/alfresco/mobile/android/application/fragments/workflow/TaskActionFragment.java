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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.workflow.task.complete.CompleteTaskRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class TaskActionFragment extends BaseFragment
{
    public static final String TAG = TaskActionFragment.class.getName();

    private static final String ARGUMENT_TASK = "TaskObject";

    private Task task;

    private boolean isReviewTask = false;

    private EditText comment;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public TaskActionFragment()
    {
    }

    public static TaskActionFragment newInstance()
    {
        TaskActionFragment bf = new TaskActionFragment();
        return bf;
    }

    public static TaskActionFragment newInstance(Task task)
    {
        TaskActionFragment bf = new TaskActionFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARGUMENT_TASK, task);
        bf.setArguments(b);
        return bf;
    };

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        task = (Task) getArguments().get(ARGUMENT_TASK);
        if (task == null) { return null; }

        setRetainInstance(false);

        if (container != null)
        {
            container.setVisibility(View.VISIBLE);
        }
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);

        View v = null;
        if (task.getEndedAt() == null)
        {
            v = inflater.inflate(R.layout.app_task_action, container, false);

            Button validation = (Button) v.findViewById(R.id.action_approve);
            Button reject = (Button) v.findViewById(R.id.action_reject);
            comment = (EditText) v.findViewById(R.id.task_comment);

            if (WorkflowModel.TASK_REVIEW.equals(task.getKey())
                    || WorkflowModel.TASK_ACTIVITI_REVIEW.equals(task.getKey()))
            {
                isReviewTask = true;
                reject.setVisibility(View.VISIBLE);
            }
            else
            {
                reject.setVisibility(View.GONE);
                validation.setText(R.string.task_done);
            }

            validation.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    completeTask(task, isReviewTask, true);
                }
            });

            reject.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    completeTask(task, isReviewTask, false);
                }
            });
        }
        else
        {
            v = inflater.inflate(R.layout.app_task_properties, container, false);
        }

        if (alfSession == null) { return v; }

        return v;
    }

    private void completeTask(Task task, boolean isReviewTask, boolean isApprove)
    {
        // Prepare Variables
        Map<String, Serializable> variables = new HashMap<String, Serializable>(3);
        if (isReviewTask)
        {
            String outcome = (isApprove) ? WorkflowModel.TRANSITION_APPROVE
                    : WorkflowModel.TRANSITION_REJECT;
            outcome = (task.getProcessDefinitionIdentifier().startsWith(WorkflowModel.KEY_PREFIX_ACTIVITI)) ? outcome : outcome.toLowerCase();
            variables.put(WorkflowModel.PROP_REVIEW_OUTCOME, outcome);
        }

        if (comment.getText().length() > 0)
        {
            variables.put(WorkflowModel.PROP_COMMENT, comment.getText().toString());
        }

        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new CompleteTaskRequest(task, variables).setNotificationTitle(task.getName())
                .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));

        OperationWaitingDialogFragment.newInstance(CompleteTaskRequest.TYPE_ID, R.drawable.ic_workflow,
                getString(R.string.task_completing), null, null, 0).show(getActivity().getFragmentManager(),
                OperationWaitingDialogFragment.TAG);

        BatchOperationManager.getInstance(getActivity()).enqueue(group);
    }

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
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.my_tasks));
        }
        getActivity().invalidateOptionsMenu();
        super.onResume();
    }

}
