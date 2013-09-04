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

import org.alfresco.mobile.android.api.model.Process;
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
import android.view.Window;
import android.widget.ImageView;

public class ProcessDiagramFragment extends BaseFragment
{

    public static final String TAG = ProcessDiagramFragment.class.getName();

    public static final String ARGUMENT_PROCESSID = "processId";

    private String processId;

    private ImageView preview;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public static Bundle createBundleArgs(String processId)
    {
        Bundle args = new Bundle();
        args.putString(ARGUMENT_PROCESSID, processId);
        return args;
    }

    public static BaseFragment newInstance(String processId)
    {
        ProcessDiagramFragment bf = new ProcessDiagramFragment();
        bf.setArguments(createBundleArgs(processId));
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        setRetainInstance(false);

        if (container != null)
        {
            container.setVisibility(View.VISIBLE);
        }
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);

        View v = inflater.inflate(R.layout.app_process_preview, container, false);
        if (alfSession == null) { return v; }

        processId = getArguments().getString(ARGUMENT_PROCESSID);
        if (processId == null || processId.isEmpty()) { return null; }
        preview = (ImageView) v.findViewById(R.id.preview);
        int iconId = R.drawable.ic_px;

        RenditionManager renditionManager = ApplicationManager.getInstance(getActivity()).getRenditionManager(
                getActivity());
        renditionManager.displayDiagram((ImageView) preview, iconId, processId);
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
