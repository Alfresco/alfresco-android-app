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

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.person.PersonProfileFragment;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ProcessDetailsFragment extends BaseFragment
{

    public static final String TAG = ProcessDetailsFragment.class.getName();

    public static final String ARGUMENT_TASK = "task";

    private Task task;

    private Person initiator;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public static Bundle createBundleArgs(Task task)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_TASK, task);
        return args;
    }

    public static ProcessDetailsFragment newInstance(Task t)
    {
        ProcessDetailsFragment bf = new ProcessDetailsFragment();
        bf.setArguments(createBundleArgs(t));
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);

        if (container != null)
        {
            container.setVisibility(View.VISIBLE);
        }
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);

        View v = inflater.inflate(R.layout.app_process_details, container, false);
        if (alfSession == null) { return v; }

        // Retrieve Task from intent
        task = (Task) getArguments().get(ARGUMENT_TASK);
        if (task == null) { return null; }

        // Display Diagram
        RenditionManager renditionManager = ApplicationManager.getInstance(getActivity()).getRenditionManager(
                getActivity());
        ImageView preview = (ImageView) v.findViewById(R.id.preview);
        int iconId = R.drawable.mime_256_generic;
        if (task.getProcessDefinitionIdentifier().startsWith(WorkflowModel.KEY_PREFIX_ACTIVITI))
        {
            renditionManager.displayDiagram((ImageView) preview, iconId, task.getProcessIdentifier());
        }
        else
        {
            v.findViewById(R.id.preview_group).setVisibility(View.GONE);
        }

        // Display Initiator
        initiator = null;
        if (task.getData().containsKey(OnPremiseConstant.WORKFLOWINSTANCE_VALUE))
        {
            Process p = (Process) task.getData().get(OnPremiseConstant.WORKFLOWINSTANCE_VALUE);
            initiator = (Person) p.getData().get(OnPremiseConstant.INITIATOR_VALUE);
        }

        if (initiator != null)
        {
            preview = (ImageView) v.findViewById(R.id.initiatorIcon);
            iconId = R.drawable.ic_person;
            renditionManager.display((ImageView) preview, initiator.getIdentifier(), iconId);
            preview.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    PersonProfileFragment.newInstance(initiator.getIdentifier()).show(getFragmentManager(),
                            PersonProfileFragment.TAG);
                }
            });

            TextView tv = (TextView) v.findViewById(R.id.initiator_name);
            tv.setText(initiator.getFullName());
        }
        else
        {
            v.findViewById(R.id.initiator_group).setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), R.string.process_start_title);
        }
        super.onResume();
    }
}
