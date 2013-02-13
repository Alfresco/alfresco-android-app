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

import org.alfresco.mobile.android.api.constants.CloudConstant;
import org.alfresco.mobile.android.api.model.ActivityEntry;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.loaders.NodeLoader;
import org.alfresco.mobile.android.application.loaders.NodeLoaderCallback;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.activitystream.ActivityEventAdapter;
import org.alfresco.mobile.android.ui.activitystream.ActivityStreamFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;

import android.app.LoaderManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ActivitiesFragment extends ActivityStreamFragment implements RefreshFragment
{
    public static final String TAG = "ActivitiesFragment";

    private static final String TYPE_FILE_DELETE = ActivityEventAdapter.PREFIX_FILE + "-deleted";

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
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart()
    {
        getActivity().setTitle(R.string.menu_browse_activities);
        getActivity().invalidateOptionsMenu();
        super.onStart();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        ActivityEntry item = (ActivityEntry) l.getItemAtPosition(position);

        if (item.getType() != null && item.getType().startsWith(ActivityEventAdapter.PREFIX_DATALIST)
                || item.getType().startsWith(ActivityEventAdapter.PREFIX_FOLDER)) { return; }

        // Inconsistency between cloud and on premise.
        String identifier = item.getData(CloudConstant.NODEREF_VALUE);
        if (identifier == null)
        {
            identifier = item.getData(CloudConstant.OBJECTID_VALUE);
        }

        // Not necessary to enable touch on delete file.
        if (identifier != null && !TYPE_FILE_DELETE.equals(item.getType()))
        {
            NodeLoaderCallback call = new NodeLoaderCallback(getActivity(), alfSession, identifier);
            LoaderManager lm = getLoaderManager();
            lm.restartLoader(NodeLoader.ID, null, call);
            lm.getLoader(NodeLoader.ID).forceLoad();
            DisplayUtils.switchSingleOrTwo(getActivity(), true);
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

    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }
}
