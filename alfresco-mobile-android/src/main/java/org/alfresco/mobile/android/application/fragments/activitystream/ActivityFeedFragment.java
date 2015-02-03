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
package org.alfresco.mobile.android.application.fragments.activitystream;

import java.util.ArrayList;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.CloudConstant;
import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.ActivityEntry;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.user.UserProfileFragment;
import org.alfresco.mobile.android.async.activitystream.ActivityStreamEvent;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;

import com.squareup.otto.Subscribe;

public class ActivityFeedFragment extends org.alfresco.mobile.android.ui.activitystream.ActivityStreamFragment
{
    public static final String TAG = ActivityFeedFragment.class.getName();

    private static final String TYPE_FILE_DELETE = ActivityFeedAdapter.PREFIX_FILE + "-deleted";

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public ActivityFeedFragment()
    {
        super();
        displayAsList = true;
        setHasOptionsMenu(true);
    }

    public static ActivityFeedFragment newInstanceByTemplate(Bundle b)
    {
        ActivityFeedFragment cbf = new ActivityFeedFragment();
        cbf.setArguments(b);
        b.putBoolean(ARGUMENT_BASED_ON_TEMPLATE, true);
        return cbf;
    }

    // /////////////////////////////////////////////////////////////
    // ITEM SELECTION
    // ////////////////////////////////////////////////////////////
    protected void onItemUnselected(ActivityEntry unSelectedObject)
    {
        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            FragmentDisplayer.with(getActivity()).remove(DisplayUtils.getCentralFragmentId(getActivity()));
        }
    }

    protected void onItemSelected(ActivityEntry item)
    {
        if (item.getType() != null && item.getType().startsWith(ActivityFeedAdapter.PREFIX_DATALIST)) { return; }

        // Inconsistency between cloud and on premise.
        String identifier = item.getData(CloudConstant.NODEREF_VALUE);
        if (identifier == null)
        {
            identifier = item.getData(CloudConstant.OBJECTID_VALUE);
        }

        // User Profile
        if (item.getType().startsWith(ActivityFeedAdapter.PREFIX_USER))
        {
            UserProfileFragment.with(getActivity())
                    .personId(item.getData(OnPremiseConstant.MEMEBERUSERNAME_VALUE)).display();
        }

        // Not necessary to enable touch on delete file.
        if (identifier != null && !TYPE_FILE_DELETE.equals(item.getType()))
        {
            NodeDetailsFragment.with(getActivity()).nodeId(identifier).display();
        }
    }

    protected boolean equalsItems(ActivityEntry o1, ActivityEntry o2)
    {
        if (o1 == null || o2 == null) return false;
        return o2.equals(o1);
    }

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu)
    {
        menu.clear();
        MenuFragmentHelper.getMenu(getActivity(), menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        getMenu(menu);
    }
    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST & RESULT
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new ActivityFeedAdapter(this, getSession(), R.layout.app_grid_row_activities,
                new ArrayList<ActivityEntry>(0), selectedItems);
    }

    @Subscribe
    public void onResult(ActivityStreamEvent event)
    {
        super.onResult(event);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends AlfrescoFragmentBuilder
    {
        public static final int ICON_ID = R.drawable.ic_activities_dark;
        public static final int LABEL_ID = R.string.menu_browse_activities;

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            menuIconId = ICON_ID;
            menuTitleId = LABEL_ID;
            templateArguments = new String[] { ARGUMENT_SITE_SHORTNAME, ARGUMENT_USERNAME };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };

    }

}
