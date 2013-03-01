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

import org.alfresco.mobile.android.api.asynchronous.JoinSiteRequestCancelLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.JoinSiteRequest;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

/**
 * Update UI after a cancellation of join site request.
 * 
 * @author Jean Marie Pascal
 */
public class JointSiteRequestCancelLoaderCallBack implements LoaderCallbacks<LoaderResult<Void>>
{

    private static final String TAG = "JointSiteRequestLoaderCallBack";

    protected static final String PARAM_JOIN_SITE_REQUEST = "JoinSiteRequest";

    private Fragment fragment;

    public JointSiteRequestCancelLoaderCallBack(Fragment fragment)
    {
        this.fragment = fragment;
    }

    public void execute(Bundle b)
    {
        if (fragment.getActivity().getLoaderManager().getLoader(JoinSiteRequestCancelLoader.ID) == null)
        {
            fragment.getActivity().getLoaderManager().initLoader(JoinSiteRequestCancelLoader.ID, b, this);
        }
        else
        {
            fragment.getActivity().getLoaderManager().restartLoader(JoinSiteRequestCancelLoader.ID, b, this);
        }
    }

    @Override
    public Loader<LoaderResult<Void>> onCreateLoader(int id, Bundle bundle)
    {
        return new JoinSiteRequestCancelLoader(fragment.getActivity(), SessionUtils.getSession(fragment.getActivity()),
                (JoinSiteRequest) bundle.getSerializable(PARAM_JOIN_SITE_REQUEST));
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Void>> loader, LoaderResult<Void> result)
    {
        int messageId = R.string.error_general;
        JoinSiteRequest joinSiteRequest = ((JoinSiteRequestCancelLoader) loader).getJoinSiteRequest();
        if (!result.hasException())
        {
            messageId = R.string.action_cancel_join_site_request;
            if (fragment instanceof JoinSiteRequestsFragment)
            {
                ((JoinSiteRequestsFragment) fragment).remove(joinSiteRequest);
            }
            BrowserSitesFragment sitesFragment = (BrowserSitesFragment) fragment.getActivity().getFragmentManager()
                    .findFragmentByTag(BrowserSitesFragment.TAG);
            if (sitesFragment != null)
            {
                sitesFragment.refresh();
            }
        }
        else
        {
            Log.d(TAG, Log.getStackTraceString(result.getException()));
        }

        MessengerManager.showLongToast(fragment.getActivity(),
                String.format(fragment.getString(messageId), joinSiteRequest.getSiteShortName()));
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<Void>> arg0)
    {
        // Do Nothing
    }
}
