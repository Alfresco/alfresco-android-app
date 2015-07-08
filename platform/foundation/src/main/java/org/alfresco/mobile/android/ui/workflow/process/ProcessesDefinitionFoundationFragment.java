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

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.workflow.process.ProcessDefinitionsEvent;
import org.alfresco.mobile.android.async.workflow.process.ProcessDefinitionsRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.widget.ArrayAdapter;

import com.squareup.otto.Subscribe;

public class ProcessesDefinitionFoundationFragment extends BaseGridFragment
{
    public static final String TAG = ProcessesDefinitionFoundationFragment.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public ProcessesDefinitionFoundationFragment()
    {
        emptyListMessageId = R.string.empty_process_definition;
    }

    public static ProcessesDefinitionFoundationFragment newInstance()
    {
        return new ProcessesDefinitionFoundationFragment();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onResume()
    {
        UIUtils.displayTitle(getActivity(), getString(R.string.my_tasks));
        getActivity().invalidateOptionsMenu();
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST
    // //////////////////////////////////////////////////////////////////////
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new ProcessDefinitionsRequest.Builder().setListingContext(listingContext);
    }

    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new ProcessesDefinitionAdapter(getActivity(), R.layout.row_two_lines_caption_divider,
                new ArrayList<ProcessDefinition>(0));
    }

    @Subscribe
    public void onResult(ProcessDefinitionsEvent event)
    {
        displayData(event);
    }
}
