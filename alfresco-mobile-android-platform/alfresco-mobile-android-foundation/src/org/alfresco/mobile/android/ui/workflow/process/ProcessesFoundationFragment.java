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
package org.alfresco.mobile.android.ui.workflow.process;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.ListingFilter;
import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.api.services.WorkflowService;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.workflow.process.ProcessesEvent;
import org.alfresco.mobile.android.async.workflow.process.ProcessesRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;

import android.os.Bundle;

import com.squareup.otto.Subscribe;

public class ProcessesFoundationFragment extends BaseGridFragment
{
    private static final String ARGUMENT_MENUID = "menuId";

    private static final String ARGUMENT_FILTER = "TaskFragmentFilter";

    public static final String TAG = ProcessesFoundationFragment.class.getName();

    protected List<Process> selectedItems = new ArrayList<Process>(1);

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public ProcessesFoundationFragment()
    {
        emptyListMessageId = R.string.empty_tasks;
        retrieveDataOnCreation = true;
        checkSession = false;
    }

    public static ProcessesFoundationFragment newInstance()
    {
        ListingFilter lf = new ListingFilter();
        lf.addFilter(WorkflowService.FILTER_KEY_STATUS, WorkflowService.FILTER_STATUS_ACTIVE);
        return newInstance(lf, 0);
    }

    public static ProcessesFoundationFragment newInstance(ListingFilter f)
    {
        ProcessesFoundationFragment bf = new ProcessesFoundationFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARGUMENT_FILTER, f);
        bf.setArguments(b);
        return bf;
    };

    public static ProcessesFoundationFragment newInstance(ListingFilter f, int menuId)
    {
        ProcessesFoundationFragment bf = new ProcessesFoundationFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARGUMENT_FILTER, f);
        b.putInt(ARGUMENT_MENUID, menuId);
        bf.setArguments(b);
        return bf;
    };

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new ProcessesRequest.Builder().setListingContext(listingContext);
    }

    @Subscribe
    public void onResult(ProcessesEvent event)
    {
        displayData(event);
    }
}
