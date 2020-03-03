/*******************************************************************************
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
import org.alfresco.mobile.android.application.configuration.model.view.ActivitiesConfigModel;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.user.UserProfileFragment;
import org.alfresco.mobile.android.async.activitystream.ActivityStreamEvent;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.activitystream.ActivityStreamFragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

public class ActivityFeedFragment extends ActivityStreamFragment
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
        screenName = AnalyticsManager.SCREEN_ACTIVITIES;
    }

    public static ActivityFeedFragment newInstanceByTemplate(Bundle b)
    {
        ActivityFeedFragment cbf = new ActivityFeedFragment();
        cbf.setArguments(b);
        b.putBoolean(ARGUMENT_BASED_ON_TEMPLATE, true);
        return cbf;
    }

    // /////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ////////////////////////////////////////////////////////////
    @Override
    protected void prepareEmptyView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        emptyImageView.setImageResource(R.drawable.ic_empty_activities);
        firstEmptyMessage.setText(R.string.activities_list_empty_title);
        secondEmptyMessage.setVisibility(View.VISIBLE);
        secondEmptyMessage.setText(R.string.activities_list_empty_description);
    }

    @Override
    public String onPrepareTitle()
    {
        return getString(ActivitiesConfigModel.LABEL_ID);
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

    @Override
    public void onListItemClick(GridView g, View v, int position, long id)
    {
        ActivityEntry entry = (ActivityEntry) g.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedEntry.isEmpty())
        {
            hideDetails = selectedEntry.get(0).equals(entry);
        }
        g.setItemChecked(position, true);

        selectedEntry.clear();
        if (!hideDetails && DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedEntry.add(entry);
        }

        if (hideDetails)
        {
            selectedEntry.clear();
            onItemUnselected(entry);
            displayTitle();
        }
        else
        {
            onItemSelected(entry);
        }
        adapter.notifyDataSetChanged();
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
            UserProfileFragment.with(getActivity()).personId(item.getData(OnPremiseConstant.MEMEBERUSERNAME_VALUE))
                    .display();
        }

        // Not necessary to enable touch on delete file.
        if (identifier != null && !TYPE_FILE_DELETE.equals(item.getType()))
        {
            NodeDetailsFragment.with(getActivity()).nodeId(identifier).display();
        }
    }

    protected boolean equalsItems(ActivityEntry o1, ActivityEntry o2)
    {
        return !(o1 == null || o2 == null) && o2.equals(o1);
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
        if (!MenuFragmentHelper.canDisplayFragmentMenu(getActivity())) { return; }
        getMenu(menu);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST & RESULT
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<ActivityEntry> onAdapterCreation()
    {
        return new ActivityFeedAdapter(this, R.layout.row_two_lines_caption_divider_circle,
                new ArrayList<ActivityEntry>(0), selectedEntry);
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
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            viewConfigModel = new ActivitiesConfigModel();
            templateArguments = new String[] { ARGUMENT_SITE_SHORTNAME, ARGUMENT_USERNAME };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

    }

}
