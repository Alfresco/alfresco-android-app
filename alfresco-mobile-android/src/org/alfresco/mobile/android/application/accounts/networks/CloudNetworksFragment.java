/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.accounts.networks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.api.session.CloudNetwork;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;

import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class CloudNetworksFragment extends BaseListFragment implements
        LoaderCallbacks<LoaderResult<List<CloudNetwork>>>
{

    public static final String TAG = "CloudNetworksFragment";

    public static final String ARGUMENT_CLOUDSESSION = "CloudSession";
    
    protected CloudSession session;

    public CloudNetworksFragment()
    {
        loaderId = CloudNetworksLoader.ID;
        callback = this;
        emptyListMessageId = R.string.cloud_networks_empty;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }
    
    @Override
    public Loader<LoaderResult<List<CloudNetwork>>> onCreateLoader(int id, Bundle args)
    {
        setListShown(false);
        return new CloudNetworksLoader(getActivity(), (CloudSession) SessionUtils.getSession(getActivity()));
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<CloudNetwork>>> arg0, LoaderResult<List<CloudNetwork>> results)
    {
        if (adapter == null)
        {
            adapter = new CloudNetworkAdapter(getActivity(), alfSession, R.layout.sdk_list_comment_row,
                    new ArrayList<CloudNetwork>(0));
        }
        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            PagingResult<CloudNetwork> pagingResultFiles = new PagingResultImpl<CloudNetwork>(results.getData(), false, results.getData().size());
            displayPagingData(pagingResultFiles, loaderId, callback);
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<CloudNetwork>>> arg0)
    {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        CloudNetwork network = (CloudNetwork) l.getItemAtPosition(position);
        Account currentAccount = SessionUtils.getAccount(getActivity());
        if (currentAccount != null)
        {
            currentAccount.setRepositoryId(network.getIdentifier());
            ((MainActivity) getActivity()).loadAccount(currentAccount);
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
