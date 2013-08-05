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

import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StartProcessFragment extends BaseFragment
{
    public static final String TAG = StartProcessFragment.class.getName();

    private static final String PARAM_PROCESS_DEFINITION = "processDefinition";
    
    private View vRoot;
    
    private ProcessDefinition processDefinition;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public StartProcessFragment()
    {
    }
    
    public static StartProcessFragment newInstance(ProcessDefinition processDefinition)
    {
        StartProcessFragment bf = new StartProcessFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_PROCESS_DEFINITION, processDefinition);
        bf.setArguments(b);
        return bf;
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //Retrieve parameters
        if (getArguments() == null  && !getArguments().containsKey(PARAM_PROCESS_DEFINITION))  { return null;}
        processDefinition = (ProcessDefinition) getArguments().getSerializable(PARAM_PROCESS_DEFINITION);
        
        
        setRetainInstance(false);
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        vRoot = inflater.inflate(R.layout.app_start_process_tablet, container, false);
        if (alfSession == null) { return vRoot; }
        return vRoot;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        getActivity().invalidateOptionsMenu();
        super.onActivityCreated(savedInstanceState);
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
