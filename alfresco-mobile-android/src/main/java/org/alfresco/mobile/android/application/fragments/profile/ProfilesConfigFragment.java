/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.fragments.profile;

import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.config.ProfileConfig;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;

public class ProfilesConfigFragment extends BaseGridFragment
{
    public static final String TAG = ProfilesConfigFragment.class.getName();

    private List<ProfileConfig> profileListing;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public ProfilesConfigFragment()
    {
        emptyListMessageId = R.string.profiles_empty;
        requiredSession = false;
        checkSession = false;
        retrieveDataOnCreation = false;
        displayAsList = true;
    }

    protected static ProfilesConfigFragment newInstanceByTemplate(Bundle b)
    {
        ProfilesConfigFragment cbf = new ProfilesConfigFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        retrieveProfiles();
        super.onActivityCreated(savedInstanceState);

        // Dsiable refresh
        refreshHelper.setEnabled(false);
        refreshHelper = null;
    }

    @Override
    public void onResume()
    {
        UIUtils.displayTitle(getActivity(), getString(R.string.profiles_switch));
        super.onResume();
    }

    // //////////////////////////////////////////////////////////////////////
    // REQUEST
    // //////////////////////////////////////////////////////////////////////
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return null;
    }

    @Override
    public void refresh()
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // RESULT
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new ProfileConfigAdapter(getActivity(), R.layout.row_two_lines_caption_divider, profileListing);
    }

    private void retrieveProfiles()
    {
        profileListing = ConfigManager.getInstance(getActivity()).getConfig(getAccount().getId()).getProfiles();
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        ProfileConfig profile = (ProfileConfig) l.getItemAtPosition(position);
        ConfigManager.getInstance(getActivity()).swapProfile(getAccount(), profile.getIdentifier());
        getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Analytics
        AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_SESSION,
                AnalyticsManager.ACTION_SWITCH, AnalyticsManager.LABEL_PROFILE, 1, false);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }

}
