/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.workflow.process;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskFragment;
import org.alfresco.mobile.android.application.fragments.workflow.ProcessDefinitionLoader;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ProcessesDefinitionFragment extends BaseListFragment implements
        LoaderCallbacks<LoaderResult<PagingResult<ProcessDefinition>>>
{

    public static final String TAG = ProcessesDefinitionFragment.class.getName();

    private boolean filterEnabled = true;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public ProcessesDefinitionFragment()
    {
        loaderId = ProcessDefinitionLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_process_definition;
        initLoader = false;
        checkSession = false;
    }

    public static ProcessesDefinitionFragment newInstance()
    {
        ProcessesDefinitionFragment bf = new ProcessesDefinitionFragment();
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(ProcessDefinitionLoader.ID, null, this);
    }

    @Override
    public void onResume()
    {
        UIUtils.displayTitle(getActivity(), getString(R.string.my_tasks));
        getActivity().invalidateOptionsMenu();
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOADERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<PagingResult<ProcessDefinition>>> onCreateLoader(int id, Bundle ba)
    {
        setListShown(false);

        bundle = (ba == null) ? getArguments() : ba;

        ListingContext lc = null, lcorigin = null;
        ProcessDefinitionLoader st = null;
        if (bundle != null)
        {
            lcorigin = (ListingContext) bundle.getSerializable(ARGUMENT_LISTING);
            lc = copyListing(lcorigin);
            loadState = bundle.getInt(LOAD_STATE);
            st = new ProcessDefinitionLoader(getActivity(), alfSession);
        }
        else
        {
            st = new ProcessDefinitionLoader(getActivity(), alfSession);
        }
        calculateSkipCount(lc);
        st.setListingContext(lc);
        return st;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<ProcessDefinition>>> arg0,
            LoaderResult<PagingResult<ProcessDefinition>> results)
    {
        if (adapter == null)
        {
            adapter = new ProcessesDefinitionAdapter(getActivity(), R.layout.sdk_list_row,
                    new ArrayList<ProcessDefinition>(0));
        }
        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            if (filterEnabled)
            {
                displayPagingData(filter(results.getData()), loaderId, callback);
            }
            else
            {
                displayPagingData(results.getData(), loaderId, callback);
            }
        }
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<PagingResult<ProcessDefinition>>> arg0)
    {
        // Nothing special
    }
    
    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }

    private PagingResultImpl<ProcessDefinition> filter(PagingResult<ProcessDefinition> processDefinitions)
    {
        List<ProcessDefinition> definitions = new ArrayList<ProcessDefinition>(processDefinitions.getTotalItems());
        for (ProcessDefinition processDef : processDefinitions.getList())
        {
            if (WorkflowModel.FAMILY_PROCESS_ADHOC.contains(processDef.getKey())
                    || WorkflowModel.FAMILY_PROCESS_PARALLEL_REVIEW.contains(processDef.getKey()))
            {
                definitions.add(processDef);
            }
        }
        return new PagingResultImpl<ProcessDefinition>(definitions, false, definitions.size());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public static void getMenu(Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_WORKFLOW_ADD, Menu.FIRST + MenuActionItem.MENU_WORKFLOW_ADD,
                R.string.workflow_start);
        mi.setIcon(android.R.drawable.ic_menu_add);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        ProcessDefinition item = (ProcessDefinition) l.getItemAtPosition(position);
        // Show properties
        Fragment f = CreateTaskFragment.newInstance(item);
        FragmentDisplayer.replaceFragment(getActivity(), f, DisplayUtils.getLeftFragmentId(getActivity()),
                CreateTaskFragment.TAG, true, true);
    }
}
