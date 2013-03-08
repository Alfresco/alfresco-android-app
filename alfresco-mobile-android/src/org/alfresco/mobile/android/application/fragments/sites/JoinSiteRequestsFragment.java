/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.sites;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.JoinSiteRequestsLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.JoinSiteRequest;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;

import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * This Fragment is responsible to display the list of join site request. <br/>
 * 
 * @author Jean Marie Pascal
 */
public class JoinSiteRequestsFragment extends BaseListFragment implements
        LoaderCallbacks<LoaderResult<List<JoinSiteRequest>>>
{
    /** Public Fragment TAG. */
    public static final String TAG = "JoinSiteRequestsFragment";

    public JoinSiteRequestsFragment()
    {
        loaderId = JoinSiteRequestsLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_joinsiterequest;
    }

    public static JoinSiteRequestsFragment newInstance(Bundle b)
    {
        JoinSiteRequestsFragment fr = new JoinSiteRequestsFragment();
        fr.setArguments(b);
        return fr;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        title = getString(R.string.joinsiterequest_list_title);
        Dialog d = super.onCreateDialog(savedInstanceState);
        
        setListShown(false);
        
        return d;
    }

    @Override
    public Loader<LoaderResult<List<JoinSiteRequest>>> onCreateLoader(int id, Bundle args)
    {
        return new JoinSiteRequestsLoader(getActivity(), alfSession);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<JoinSiteRequest>>> loader,
            LoaderResult<List<JoinSiteRequest>> results)
    {
        if (adapter == null)
        {
            adapter = new JoinSiteRequestAdapter(this, R.layout.app_list_button_row, new ArrayList<JoinSiteRequest>(0));
        }
        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(new PagingResultImpl<JoinSiteRequest>(results.getData(), false, results.getData().size()), loaderId, callback);
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<JoinSiteRequest>>> loader)
    {
        //Do Nothing
    }

    @SuppressWarnings("unchecked")
    public void remove(JoinSiteRequest joinSiteRequest)
    {
        if (adapter != null)
        {
            ((ArrayAdapter<JoinSiteRequest>) adapter).remove(joinSiteRequest);
            if (adapter.isEmpty()){
                displayEmptyView();
            }
        }
    }

}
