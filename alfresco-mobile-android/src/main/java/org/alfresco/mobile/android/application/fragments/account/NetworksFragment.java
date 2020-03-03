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
package org.alfresco.mobile.android.application.fragments.account;

import java.util.ArrayList;
import java.util.Map;

import org.alfresco.mobile.android.api.session.CloudNetwork;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.async.session.network.NetworksEvent;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.network.CloudNetworkAdapter;
import org.alfresco.mobile.android.ui.network.CloudNetworksFragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

public class NetworksFragment extends CloudNetworksFragment
{
    public static final String TAG = "CloudNetworksFragment";

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public NetworksFragment()
    {
        super();
    }

    protected static NetworksFragment newInstanceByTemplate(Bundle b)
    {
        NetworksFragment cbf = new NetworksFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // RESULT
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new CloudNetworkAdapter(getActivity(), R.layout.row_two_lines_caption_divider,
                new ArrayList<CloudNetwork>(0));
    }

    @Override
    @Subscribe
    public void onResult(NetworksEvent event)
    {
        super.onResult(event);
    }

    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        CloudNetwork network = (CloudNetwork) l.getItemAtPosition(position);
        AlfrescoAccount currentAccount = SessionUtils.getAccount(getActivity());
        if (currentAccount != null && !currentAccount.getRepositoryId().equals(network.getIdentifier()))
        {
            EventBusManager.getInstance().post(new RequestSessionEvent(currentAccount, network.getIdentifier(), true));
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            // Analytics
            AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_SESSION,
                    AnalyticsManager.ACTION_SWITCH, AnalyticsManager.LABEL_NETWORK, 1, false);
        }
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
