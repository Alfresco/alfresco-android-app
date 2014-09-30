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
package org.alfresco.mobile.android.application.fragments.workflow;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.workflow.process.ProcessDefinitionsEvent;
import org.alfresco.mobile.android.async.workflow.process.ProcessDefinitionsRequest;
import org.alfresco.mobile.android.platform.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

public class CreateTaskTypePickerFragment extends AlfrescoFragment
{
    private static final String ACTION_REVIEW = "org.alfresco.mobile.android.intent.ACTION_REVIEW";

    public static final String TAG = CreateTaskTypePickerFragment.class.getName();

    private ProcessDefinition todo;

    private ProcessDefinition review;

    protected View pb;

    protected View ev;

    private LinearLayout lv;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateTaskTypePickerFragment()
    {
    }

    public static CreateTaskTypePickerFragment newInstance(List<Document> docs)
    {
        CreateTaskTypePickerFragment bf = new CreateTaskTypePickerFragment();
        Bundle b = new Bundle();
        b.putParcelableArrayList(PrivateIntent.EXTRA_DOCUMENTS, (ArrayList<? extends Parcelable>) docs);
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(true);

        container.setVisibility(View.VISIBLE);
        setSession(SessionUtils.getSession(getActivity()));
        SessionUtils.checkSession(getActivity(), getSession());
        setRootView(inflater.inflate(R.layout.app_task_create, container, false));

        lv = (LinearLayout) viewById(R.id.create_task_group);
        pb = (View) viewById(R.id.progressbar);
        ev = viewById(R.id.empty);
        TextView evt = (TextView) viewById(R.id.empty_text);
        evt.setText(R.string.error_general);

        // BUTTONS
        Button b = (Button) viewById(R.id.task_todo);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createTask(todo);
            }
        });

        b = (Button) viewById(R.id.task_review_approve);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createTask(review);
            }
        });

        return getRootView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Operator.with(getActivity()).load(new ProcessDefinitionsRequest.Builder());
        displayTasks(false);
    }

    @Override
    public void onResume()
    {
        if (getArguments() != null && getArguments().containsKey(PrivateIntent.EXTRA_DOCUMENTS))
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.process_create_task));
        }
        else
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.process_choose_definition));
        }
        getActivity().invalidateOptionsMenu();
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADERS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(ProcessDefinitionsEvent event)
    {
        if (event.exception != null)
        {
            displayEmptyView();
            CloudExceptionUtils.handleCloudException(getActivity(), event.exception, false);
        }
        else
        {
            filter(event.data);
            if (getArguments() != null && getArguments().containsKey(PrivateIntent.EXTRA_DOCUMENTS))
            {
                CreateTaskFragment.with(getActivity()).processDefinition(review)
                        .documents(getArguments().getParcelableArrayList(PrivateIntent.EXTRA_DOCUMENTS)).back(false).display();
            }
            else
            {
                displayTasks(true);
            }
        }
    }

    private void filter(PagingResult<ProcessDefinition> processDefinitions)
    {
        for (ProcessDefinition processDef : processDefinitions.getList())
        {
            if (WorkflowModel.FAMILY_PROCESS_ADHOC.contains(processDef.getKey()))
            {
                todo = processDef;
                continue;
            }

            if (WorkflowModel.FAMILY_PROCESS_PARALLEL_REVIEW.contains(processDef.getKey()))
            {
                review = processDef;
                continue;
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private void createTask(ProcessDefinition item)
    {
        CreateTaskFragment.with(getActivity()).processDefinition(item).display();
    }

    private void displayTasks(Boolean shown)
    {
        if (ev == null || lv == null || pb == null) { return; }

        if (shown)
        {
            lv.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }
        else
        {
            ev.setVisibility(View.GONE);
            lv.setVisibility(View.GONE);
            pb.setVisibility(View.VISIBLE);
        }
    }

    private void displayEmptyView()
    {
        if (ev == null || lv == null || pb == null) { return; }

        ev.setVisibility(View.VISIBLE);
        lv.setVisibility(View.GONE);
        pb.setVisibility(View.GONE);
    }
}
