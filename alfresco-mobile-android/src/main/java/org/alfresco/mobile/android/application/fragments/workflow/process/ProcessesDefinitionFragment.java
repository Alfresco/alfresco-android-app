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
package org.alfresco.mobile.android.application.fragments.workflow.process;

import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.workflow.CreateTaskFragment;
import org.alfresco.mobile.android.async.workflow.process.ProcessDefinitionsEvent;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.ui.utils.UIUtils;
import org.alfresco.mobile.android.ui.workflow.process.ProcessesDefinitionFoundationFragment;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

public class ProcessesDefinitionFragment extends ProcessesDefinitionFoundationFragment
{
    public static final String TAG = ProcessesDefinitionFragment.class.getName();

    private boolean filterEnabled = true;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public ProcessesDefinitionFragment()
    {
        setHasOptionsMenu(true);
    }

    public static ProcessesDefinitionFragment newInstanceByTemplate()
    {
        return new ProcessesDefinitionFragment();
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
    @Subscribe
    public void onResult(ProcessDefinitionsEvent event)
    {
        super.onResult(event);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        if (!MenuFragmentHelper.canDisplayFragmentMenu(getActivity())) { return; }
        menu.clear();
        MenuItem mi;

        mi = menu.add(Menu.NONE, R.id.menu_workflow_add, Menu.FIRST, R.string.workflow_start);
        mi.setIcon(android.R.drawable.ic_menu_add);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuFragmentHelper.getMenu(getActivity(), menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_workflow_add:
                Intent in = new Intent(PrivateIntent.ACTION_START_PROCESS, null, getActivity(),
                        PrivateDialogActivity.class);
                in.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getAccount().getId());
                getActivity().startActivity(in);
                return true;
            default:
                break;
        }
        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView g, View v, int position, long id)
    {
        ProcessDefinition item = (ProcessDefinition) g.getItemAtPosition(position);

        CreateTaskFragment.with(getActivity()).processDefinition(item).display();
    }
}
