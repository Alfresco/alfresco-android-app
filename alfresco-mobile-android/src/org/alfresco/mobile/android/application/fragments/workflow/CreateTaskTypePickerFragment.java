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

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CreateTaskTypePickerFragment extends BaseFragment implements
        LoaderCallbacks<LoaderResult<PagingResult<ProcessDefinition>>>
{
    private static final String ACTION_REVIEW = "org.alfresco.mobile.android.intent.ACTION_REVIEW";

    public static final String TAG = CreateTaskTypePickerFragment.class.getName();

    private View vRoot;

    private ProcessDefinition todo;

    private ProcessDefinition review;

    protected ProgressBar pb;

    protected View ev;

    private LinearLayout lv;

    private UpdateReceiver receiver;

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
        b.putParcelableArrayList(IntentIntegrator.EXTRA_DOCUMENTS, (ArrayList<? extends Parcelable>) docs);
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
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        vRoot = inflater.inflate(R.layout.app_task_create, container, false);

        if (alfSession == null) { return vRoot; }

        lv = (LinearLayout) vRoot.findViewById(R.id.create_task_group);
        pb = (ProgressBar) vRoot.findViewById(R.id.progressbar);
        ev = vRoot.findViewById(R.id.empty);
        TextView evt = (TextView) vRoot.findViewById(R.id.empty_text);
        evt.setText(R.string.error_general);

        // BUTTONS
        Button b = (Button) vRoot.findViewById(R.id.task_todo);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createTask(todo);
            }
        });

        b = (Button) vRoot.findViewById(R.id.task_review_approve);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createTask(review);
            }
        });

        return vRoot;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
        if (getLoaderManager().getLoader(ProcessDefinitionLoader.ID) == null)
        {
            getLoaderManager().restartLoader(ProcessDefinitionLoader.ID, null, this);
        }
    }

    @Override
    public void onResume()
    {
        if (getArguments() != null && getArguments().containsKey(IntentIntegrator.EXTRA_DOCUMENTS))
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.process_create_task));
        }
        else
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.process_choose_definition));
        }
        getActivity().invalidateOptionsMenu();

        IntentFilter intentFilter = new IntentFilter(ACTION_REVIEW);
        receiver = new UpdateReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);

        super.onResume();
    }

    @Override
    public void onPause()
    {
        if (receiver != null)
        {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        }
        super.onPause();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<PagingResult<ProcessDefinition>>> onCreateLoader(int id, Bundle ba)
    {
        displayTasks(false);
        return new ProcessDefinitionLoader(getActivity(), alfSession);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<ProcessDefinition>>> arg0,
            LoaderResult<PagingResult<ProcessDefinition>> results)
    {
        if (results.getException() != null)
        {
            displayEmptyView();
        }
        else
        {
            filter(results.getData());
            if (getArguments() != null && getArguments().containsKey(IntentIntegrator.EXTRA_DOCUMENTS))
            {
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ACTION_REVIEW));
            }
            else
            {
                displayTasks(true);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<PagingResult<ProcessDefinition>>> arg0)
    {
        // TODO Auto-generated method stub
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
        Fragment f = CreateTaskFragment.newInstance(item, getArguments() != null ? getArguments() : new Bundle());
        FragmentDisplayer.replaceFragment(getActivity(), f, DisplayUtils.getLeftFragmentId(getActivity()),
                CreateTaskFragment.TAG, getArguments() == null, getArguments() == null);
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
        ev.setVisibility(View.VISIBLE);
        lv.setVisibility(View.GONE);
        pb.setVisibility(View.GONE);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    public class UpdateReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getAction());

            if (getActivity() == null) { return; }

            if (intent.getAction().equals(ACTION_REVIEW))
            {
                createTask(review);
                return;
            }

        }
    }
}
