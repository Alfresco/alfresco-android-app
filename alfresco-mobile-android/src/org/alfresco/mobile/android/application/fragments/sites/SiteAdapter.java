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

import org.alfresco.mobile.android.api.asynchronous.SiteFavoriteLoader;
import org.alfresco.mobile.android.api.asynchronous.SiteMembershipLoader;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.model.SiteVisibility;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class SiteAdapter extends org.alfresco.mobile.android.ui.site.SiteAdapter implements OnMenuItemClickListener
{

    private List<Site> selectedOptionItems = new ArrayList<Site>();

    private Fragment fragment;

    private int mode;

    public SiteAdapter(Activity context, int textViewResourceId, List<Site> listItems)
    {
        super(context, textViewResourceId, listItems);
    }

    public SiteAdapter(Fragment fragment, int textViewResourceId, List<Site> listItems, int mode)
    {
        super(fragment.getActivity(), textViewResourceId, listItems);
        this.fragment = fragment;
        this.mode = mode;
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Site item)
    {
        super.updateTopText(vh, item);

        if (mode == BrowserSitesFragment.MODE_IMPORT) { return; }

        UIUtils.setBackground(((View) vh.icon), getContext().getResources().getDrawable(
                R.drawable.quickcontact_badge_overlay_light));

        vh.icon.setVisibility(View.VISIBLE);
        vh.icon.setTag(R.id.site_action, item);
        vh.icon.setOnClickListener(new OnClickListener()
        {

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public void onClick(View v)
            {
                Site item = (Site) v.getTag(R.id.site_action);
                selectedOptionItems.add(item);
                PopupMenu popup = new PopupMenu(getContext(), v);
                getMenu(popup.getMenu(), item);

                if (AndroidVersion.isICSOrAbove()){
                    popup.setOnDismissListener(new OnDismissListener()
                    {
                        @Override
                        public void onDismiss(PopupMenu menu)
                        {
                            selectedOptionItems.clear();
                        }
                    });
                }

                popup.setOnMenuItemClickListener(SiteAdapter.this);

                popup.show();
            }
        });
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public void getMenu(Menu menu, Site site)
    {
        Log.d("CACHE", "AC : " + site.getShortName() + "|PM:" + site.isPendingMember() + "|M:" + site.isMember()
                + "|F:" + site.isFavorite());

        if (site.isMember())
        {
            menu.add(Menu.NONE, MenuActionItem.MENU_SITE_LEAVE, Menu.FIRST + MenuActionItem.MENU_SITE_LEAVE,
                    R.string.action_leave_site);
        }
        else if (!SiteVisibility.PRIVATE.equals(site.getVisibility()) && !site.isPendingMember())
        {
            menu.add(Menu.NONE, MenuActionItem.MENU_SITE_JOIN, Menu.FIRST + MenuActionItem.MENU_SITE_JOIN,
                    (SiteVisibility.MODERATED.equals(site.getVisibility())) ? R.string.action_join_request_site
                            : R.string.action_join_site);
        }

        if (site.isFavorite())
        {
            menu.add(Menu.NONE, MenuActionItem.MENU_SITE_UNFAVORITE, Menu.FIRST + MenuActionItem.MENU_SITE_UNFAVORITE,
                    R.string.action_unfavorite_site);
        }
        else
        {
            menu.add(Menu.NONE, MenuActionItem.MENU_SITE_FAVORITE, Menu.FIRST + MenuActionItem.MENU_SITE_FAVORITE,
                    R.string.action_favorite_site);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;
        Bundle b = new Bundle();
        switch (item.getItemId())
        {
            case MenuActionItem.MENU_SITE_LEAVE:
                b.putBoolean(SiteMembershipLoaderCallback.PARAM_ISJOINING, false);
                b.putSerializable(SiteMembershipLoaderCallback.PARAM_SITE, selectedOptionItems.get(0));
                onMenuItemClick = true;
                launchMemberShip(b);
                break;
            case MenuActionItem.MENU_SITE_JOIN:
                b.putBoolean(SiteMembershipLoaderCallback.PARAM_ISJOINING, true);
                b.putSerializable(SiteMembershipLoaderCallback.PARAM_SITE, selectedOptionItems.get(0));
                onMenuItemClick = true;
                launchMemberShip(b);
                break;
            case MenuActionItem.MENU_SITE_FAVORITE:
            case MenuActionItem.MENU_SITE_UNFAVORITE:
                b.putSerializable(SiteFavoriteLoaderCallback.PARAM_SITE, selectedOptionItems.get(0));
                onMenuItemClick = true;
                launchFavorite(b);
                break;
            default:
                onMenuItemClick = false;
                break;
        }
        return onMenuItemClick;
    }

    private void launchMemberShip(Bundle b)
    {
        if (fragment.getActivity().getLoaderManager().getLoader(SiteMembershipLoader.ID) == null)
        {
            fragment.getActivity().getLoaderManager()
                    .initLoader(SiteMembershipLoader.ID, b, new SiteMembershipLoaderCallback(fragment));
        }
        else
        {
            fragment.getActivity().getLoaderManager()
                    .restartLoader(SiteMembershipLoader.ID, b, new SiteMembershipLoaderCallback(fragment));
        }
    }

    private void launchFavorite(Bundle b)
    {
        if (fragment.getActivity().getLoaderManager().getLoader(SiteFavoriteLoader.ID) == null)
        {
            fragment.getActivity().getLoaderManager()
                    .initLoader(SiteFavoriteLoader.ID, b, new SiteFavoriteLoaderCallback(fragment));
        }
        else
        {
            fragment.getActivity().getLoaderManager()
                    .restartLoader(SiteFavoriteLoader.ID, b, new SiteFavoriteLoaderCallback(fragment));
        }
    }
}
