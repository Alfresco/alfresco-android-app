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
package org.alfresco.mobile.android.application.fragments.site.browser;

import java.util.ArrayList;

import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.model.SiteVisibility;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.site.JoinSiteRequestsFragment;
import org.alfresco.mobile.android.async.site.SiteFavoriteEvent;
import org.alfresco.mobile.android.async.site.member.CancelPendingMembershipEvent;
import org.alfresco.mobile.android.async.site.member.SiteMembershipEvent;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.ui.site.SitesFoundationFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

public abstract class CommonBrowserSitesFragment extends SitesFoundationFragment
{
    protected boolean isFavoriteListing = false;

    protected boolean isMemberSite = false;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public CommonBrowserSitesFragment()
    {
        super();
        enableTitle = false;
        mode = MODE_LISTING;
        setHasOptionsMenu(true);
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null) { return null; }
        setRootView(inflater.inflate(R.layout.app_tab_extra, container, false));
        init(getRootView(), emptyListMessageId);
        return getRootView();
    }

    @Override
    public void onResume()
    {
        titleId = R.string.menu_browse_sites;
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            mode = MODE_IMPORT;
            titleId = R.string.import_document_title;
        }
        else if (getActivity() instanceof PrivateDialogActivity)
        {
            mode = MODE_PICK;
        }

        UIUtils.displayTitle(getActivity(), titleId);

        super.onResume();

        getActivity().invalidateOptionsMenu();
    }

    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new SiteAdapter(this, R.layout.sdk_grid_row, new ArrayList<Site>(), mode);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTION
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        Site s = (Site) l.getItemAtPosition(position);
        DocumentFolderBrowserFragment.with(getActivity()).site(s).display();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public static void getMenu(Context context, Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, R.id.menu_site_list_request, Menu.FIRST, R.string.joinsiterequest_list_title);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuFragmentHelper.getMenu(context, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_site_list_request:
                JoinSiteRequestsFragment.with(getActivity()).displayAsDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SITES MEMBERSHIP
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Update and replace a site object inside the listing without requesting an
     * HTTP call.
     * 
     * @param oldSite : original site inside the list
     * @param newSite : new site to add which replace the old one at the same
     *            place.
     */
    @SuppressWarnings("unchecked")
    public void update(Site oldSite, Site newSite)
    {
        try
        {
            if (adapter != null)
            {
                int position = ((ArrayAdapter<Site>) adapter).getPosition(oldSite);
                if (position == -1)
                {
                    refresh();
                }
                else
                {
                    ((ArrayAdapter<Site>) adapter).remove(oldSite);
                    if (newSite != null)
                    {
                        ((ArrayAdapter<Site>) adapter).insert(newSite, position);
                    }
                    adapter.notifyDataSetChanged();
                }

            }
        }
        catch (Exception e)
        {
            Log.w(TAG, "Unable to refresh sites objects");
        }
    }

    /**
     * Remove a site object inside the listing without requesting an HTTP call.
     * 
     * @param site : site to remove
     */
    @SuppressWarnings("unchecked")
    public void remove(Site site)
    {
        if (adapter != null)
        {
            ((ArrayAdapter<Site>) adapter).remove(site);
            if (adapter.isEmpty())
            {
                displayEmptyView();
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onCancelPendingMembershipEvent(CancelPendingMembershipEvent event)
    {
        if (!event.hasException)
        {
            refresh();
        }
    }

    @Subscribe
    public void onSiteFavoriteEvent(SiteFavoriteEvent event)
    {
        int messageId = R.string.error_general;
        Site site = event.oldSite;
        if (!event.hasException)
        {
            Site updatedSite = event.data;
            if (updatedSite.isFavorite())
            {
                messageId = R.string.action_favorite_site_validation;
                update(site, updatedSite);
            }
            else
            {
                messageId = R.string.action_unfavorite_site_validation;
                if (isFavoriteListing == true)
                {
                    remove(site);
                }
                else
                {
                    update(site, updatedSite);
                }
            }
        }
        else
        {
            messageId = event.oldSite.isFavorite() ? R.string.action_unfavorite_site_error
                    : R.string.action_favorite_error;
            Log.w(TAG, Log.getStackTraceString(event.exception));
        }

        AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(
                String.format(getString(messageId), site.getTitle()));
    }

    @Subscribe
    public void onSiteMembershipEvent(SiteMembershipEvent event)
    {
        int messageId = R.string.error_general;
        Site site = event.oldSite;
        if (!event.hasException)
        {
            Site updatedSite = event.data;
            if (event.isJoining)
            {
                messageId = (SiteVisibility.PUBLIC.equals(site.getVisibility())) ? R.string.action_join_site_validation
                        : R.string.action_join_request_site_validation;
                update(site, updatedSite);
            }
            else
            {
                messageId = R.string.action_leave_site_validation;
                if (isFavoriteListing == true)
                {
                    remove(site);
                }
                else
                {
                    update(site, isMemberSite ? null : updatedSite);
                }
            }
        }
        else
        {
            messageId = (event.isJoining) ? R.string.action_join_site_error : R.string.action_leave_site_error;
            Log.w(TAG, Log.getStackTraceString(event.exception));
        }
        AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(
                String.format(getString(messageId), site.getTitle()));
    }
}
