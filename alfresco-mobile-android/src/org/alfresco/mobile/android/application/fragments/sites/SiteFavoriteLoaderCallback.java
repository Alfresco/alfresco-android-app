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
import org.alfresco.mobile.android.api.asynchronous.SiteFavoriteLoader;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

/**
 * Update UI after a favorite/unfavorite background action.
 * 
 * @author Jean Marie Pascal
 */
public class SiteFavoriteLoaderCallback implements LoaderCallbacks<LoaderResult<Site>>
{

    private static final String TAG = "SiteFavoriteLoaderCallback";

    public static final String PARAM_SITE = "site";

    private Fragment fragment;

    public SiteFavoriteLoaderCallback(Fragment fragment)
    {
        this.fragment = fragment;
    }

    @Override
    public Loader<LoaderResult<Site>> onCreateLoader(int id, Bundle bundle)
    {
        return new SiteFavoriteLoader(fragment.getActivity(), SessionUtils.getSession(fragment.getActivity()),
                (Site) bundle.get(PARAM_SITE));
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Site>> loader, LoaderResult<Site> result)
    {
        int messageId = R.string.error_general;
        Site site = ((SiteFavoriteLoader) loader).getOldSite();
        if (!result.hasException())
        {
            Site updatedSite = result.getData();
            if (updatedSite.isFavorite())
            {
                messageId = R.string.action_favorite_site_validation;
                if (fragment instanceof BrowserSitesFragment)
                {
                    ((BrowserSitesFragment) fragment).update(site, updatedSite);
                }
            }
            else
            {
                messageId = R.string.action_unfavorite_site_validation;
                if (fragment instanceof BrowserSitesFragment
                        && BrowserSitesFragment.TAB_FAV_SITES.equals(((BrowserSitesFragment) fragment)
                                .getCurrentTabId()))
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
            messageId = ((SiteFavoriteLoader) loader).getOldSite().isFavorite() ? R.string.action_unfavorite_site_error
                    : R.string.action_favorite_error;

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
