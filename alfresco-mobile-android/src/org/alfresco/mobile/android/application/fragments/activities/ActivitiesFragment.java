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
package org.alfresco.mobile.android.application.fragments.activities;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.constants.CloudConstant;
import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.ActivityEntry;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.activitystream.ActivityStreamFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;

import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ActivitiesFragment extends ActivityStreamFragment implements RefreshFragment
{
    public static final String TAG = ActivitiesFragment.class.getName();

    private static final String TYPE_FILE_DELETE = ActivityEventAdapter.PREFIX_FILE + "-deleted";

    private List<ActivityEntry> selectedEntry = new ArrayList<ActivityEntry>(1);

    public ActivitiesFragment()
    {
        super();
    }

    public static ActivitiesFragment newInstance()
    {
        ActivitiesFragment bf = new ActivitiesFragment();
        Bundle settings = new Bundle();
        settings.putInt(BaseListAdapter.DISPLAY_ICON, BaseListAdapter.DISPLAY_ICON_CREATOR);
        bf.setArguments(settings);
        return bf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        UIUtils.displayTitle(getActivity(), getString(R.string.menu_browse_activities));
        super.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        ActivityEntry item = (ActivityEntry) l.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedEntry.isEmpty())
        {
            hideDetails = selectedEntry.get(0).equals(item);
            selectedEntry.clear();
        }
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        l.setItemChecked(position, true);
        v.setSelected(true);

        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedEntry.add(item);
        }

        if (hideDetails)
        {
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
            }
            selectedEntry.clear();
        }
        else
        {
            if (item.getType() != null && item.getType().startsWith(ActivityEventAdapter.PREFIX_DATALIST)) { return; }

            // Inconsistency between cloud and on premise.
            String identifier = item.getData(CloudConstant.NODEREF_VALUE);
            if (identifier == null)
            {
                identifier = item.getData(CloudConstant.OBJECTID_VALUE);
            }

            // User Profile
            if (item.getType().startsWith(ActivityEventAdapter.PREFIX_USER))
            {
                ((MainActivity) getActivity()).addPersonProfileFragment(item
                        .getData(OnPremiseConstant.MEMEBERUSERNAME_VALUE));
            }

            // Not necessary to enable touch on delete file.
            if (identifier != null && !TYPE_FILE_DELETE.equals(item.getType()))
            {
                ((MainActivity) getActivity()).addPropertiesFragment(identifier);
                DisplayUtils.switchSingleOrTwo(getActivity(), true);
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu)
    {
        MenuItem mi = menu.add(Menu.NONE, MenuActionItem.MENU_REFRESH, Menu.FIRST + MenuActionItem.MENU_REFRESH,
                R.string.refresh);
        mi.setIcon(R.drawable.ic_refresh);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public void refresh()
    {
        refresh(loaderId, callback);
    }

    // //////////////////////////////////////////////////////////////////////
    // LOADER
    // //////////////////////////////////////////////////////////////////////
    @SuppressWarnings("rawtypes")
    public void onLoadFinished(Loader<LoaderResult<PagingResult<ActivityEntry>>> arg0,
            LoaderResult<PagingResult<ActivityEntry>> results)
    {
        if (adapter == null)
        {
            adapter = new ActivityEventAdapter(this, alfSession, R.layout.sdk_list_row,
                    new ArrayList<ActivityEntry>(0), selectedEntry);
            ((BaseListAdapter) adapter).setFragmentSettings(getArguments());
        }

        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
    }

    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }
}
