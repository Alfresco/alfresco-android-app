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

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.asynchronous.SiteMembershipLoader;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.model.SiteVisibility;
import org.alfresco.mobile.android.api.services.impl.AbstractSiteServiceImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

/**
 * Update UI after a join/leave site background action.
 * 
 * @author Jean Marie Pascal
 */
public class SiteMembershipLoaderCallback implements LoaderCallbacks<LoaderResult<Site>>
{
    private static final String TAG = "SiteMembershipLoaderCallback";

    public static final String PARAM_SITE = "site";

    public static final String PARAM_ISJOINING = "isJoining";

    public static final String PARAM_MESSAGE = "message";

    private Fragment fragment;

    public SiteMembershipLoaderCallback(Fragment fragment)
    {
        this.fragment = fragment;
    }

    @Override
    public Loader<LoaderResult<Site>> onCreateLoader(int id, Bundle bundle)
    {
        return new SiteMembershipLoader(fragment.getActivity(), SessionUtils.getSession(fragment.getActivity()),
                (Site) bundle.get(PARAM_SITE), (Boolean) bundle.get(PARAM_ISJOINING));
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Site>> loader, LoaderResult<Site> result)
    {
        int messageId = R.string.error_general;
        Site site = ((SiteMembershipLoader) loader).getOldSite();
        if (!result.hasException())
        {
            Site updatedSite = result.getData();
            if (((SiteMembershipLoader) loader).isJoining())
            {
                messageId = (SiteVisibility.PUBLIC.equals(site.getVisibility())) ? R.string.action_join_site_validation
                        : R.string.action_join_request_site_validation;
                if (fragment instanceof BrowserSitesFragment)
                {
                    ((BrowserSitesFragment) fragment).update(site, updatedSite);
                }
            }
            else
            {
                messageId = R.string.action_leave_site_validation;
                if (fragment instanceof BrowserSitesFragment
                        && BrowserSitesFragment.TAB_MY_SITES
                                .equals(((BrowserSitesFragment) fragment).getCurrentTabId()))
                {
                    ((BrowserSitesFragment) fragment).remove(site);
                }
                else if (fragment instanceof BrowserSitesFragment)
                {
                    ((BrowserSitesFragment) fragment).update(site, updatedSite);
                }
            }
        }
        else
        {
            messageId = ((SiteMembershipLoader) loader).isJoining() ? R.string.action_join_site_error
                    : R.string.action_leave_site_error;

            Log.w(TAG, Log.getStackTraceString(result.getException()));
        }

        MessengerManager.showLongToast(fragment.getActivity(),
                String.format(fragment.getString(messageId), site.getTitle()));
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<Site>> arg0)
    {

    }

}
