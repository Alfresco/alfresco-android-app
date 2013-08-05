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

import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ProcessDiagramFragment extends BaseFragment
{

    public static final String TAG = ProcessDiagramFragment.class.getName();

    public static final String ARGUMENT_TASK = "task";

    private Task task;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public static Bundle createBundleArgs(Task task)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_TASK, task);
        return args;
    }

    public static ProcessDiagramFragment newInstance(Task t)
    {
        ProcessDiagramFragment bf = new ProcessDiagramFragment();
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

        View v = inflater.inflate(R.layout.app_preview, container, false);
        if (alfSession == null) { return v; }

        task = (Task) getArguments().get(ARGUMENT_TASK);
        if (task == null) { return null; }
        ImageView preview = (ImageView) v.findViewById(R.id.preview);
        int iconId = R.drawable.mime_folder;

        RenditionManager renditionManager = ApplicationManager.getInstance(getActivity()).getRenditionManager(
                getActivity());
        renditionManager.displayDiagram((ImageView) preview, iconId, task.getProcessIdentifier());
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
