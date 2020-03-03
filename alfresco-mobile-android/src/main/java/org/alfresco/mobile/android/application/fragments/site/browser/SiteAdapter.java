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
package org.alfresco.mobile.android.application.fragments.site.browser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.model.SiteVisibility;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.user.UsersFragment;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.site.SiteFavoriteRequest;
import org.alfresco.mobile.android.async.site.member.SiteMembershipRequest;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
import org.alfresco.mobile.android.ui.site.SitesFoundationAdapter;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.annotation.TargetApi;
import android.os.Build;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class SiteAdapter extends SitesFoundationAdapter implements OnMenuItemClickListener
{
    private static final String TAG = SitesFoundationAdapter.class.getSimpleName();

    private List<Site> selectedOptionItems = new ArrayList<Site>();

    private WeakReference<Fragment> fragmentRef;

    private int mode;

    public SiteAdapter(FragmentActivity context, int textViewResourceId, List<Site> listItems)
    {
        super(context, textViewResourceId, listItems);
    }

    public SiteAdapter(Fragment fragment, int textViewResourceId, List<Site> listItems, int mode)
    {
        super(fragment.getActivity(), textViewResourceId, listItems);
        this.fragmentRef = new WeakReference<Fragment>(fragment);
        this.mode = mode;
    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, Site item)
    {
        super.updateTopText(vh, item);

        if (mode == ListingModeFragment.MODE_IMPORT || mode == ListingModeFragment.MODE_PICK)
        {
            UIUtils.setBackground(vh.choose, null);
            return;
        }

        vh.choose.setImageResource(R.drawable.ic_more_options);
        vh.choose.setBackgroundResource(R.drawable.alfrescohololight_list_selector_holo_light);
        int d_16 = DisplayUtils.getPixels(getContext(), R.dimen.d_16);
        vh.choose.setPadding(d_16, d_16, d_16, d_16);
        vh.choose.setVisibility(View.VISIBLE);
        AccessibilityUtils.addContentDescription(vh.choose,
                String.format(getContext().getString(R.string.more_options_site), item.getTitle()));
        vh.choose.setTag(R.id.site_action, item);
        vh.choose.setOnClickListener(new OnClickListener()
        {

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public void onClick(View v)
            {
                Site item = (Site) v.getTag(R.id.site_action);
                selectedOptionItems.add(item);
                PopupMenu popup = new PopupMenu(getContext(), v);
                getMenu(popup.getMenu(), item);

                popup.setOnDismissListener(new OnDismissListener()
                {
                    @Override
                    public void onDismiss(PopupMenu menu)
                    {
                        selectedOptionItems.clear();
                    }
                });

                popup.setOnMenuItemClickListener(SiteAdapter.this);

                popup.show();
            }
        });
    }

    @Override
    protected void updateIcon(TwoLinesViewHolder vh, Site item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_site_light));
        AccessibilityUtils.addContentDescription(vh.icon, R.string.mime_site);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public void getMenu(Menu menu, Site site)
    {
        if (site.isMember())
        {
            menu.add(Menu.NONE, R.id.menu_site_leave, Menu.FIRST + 2, R.string.action_leave_site);
        }
        else if (!SiteVisibility.PRIVATE.equals(site.getVisibility()) && !site.isPendingMember())
        {
            menu.add(Menu.NONE, R.id.menu_site_join, Menu.FIRST + 1,
                    (SiteVisibility.MODERATED.equals(site.getVisibility())) ? R.string.action_join_request_site
                            : R.string.action_join_site);
        }

        if (site.isFavorite())
        {
            menu.add(Menu.NONE, R.id.menu_site_unfavorite, Menu.FIRST + 5, R.string.action_unfavorite_site);
        }
        else
        {
            menu.add(Menu.NONE, R.id.menu_site_favorite, Menu.FIRST + 4, R.string.action_favorite_site);
        }

        menu.add(Menu.NONE, R.id.menu_site_members, Menu.FIRST, R.string.members);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;
        switch (item.getItemId())
        {
            case R.id.menu_site_members:
                if (fragmentRef.get().getActivity() instanceof MainActivity)
                {
                    UsersFragment.with(fragmentRef.get().getActivity())
                            .siteShortName(selectedOptionItems.get(0).getShortName()).display();
                }
                onMenuItemClick = true;
                break;
            case R.id.menu_site_leave:
                Operator.with(fragmentRef.get().getActivity())
                        .load(new SiteMembershipRequest.Builder(selectedOptionItems.get(0), false));
                onMenuItemClick = true;
                break;
            case R.id.menu_site_join:
                Operator.with(fragmentRef.get().getActivity())
                        .load(new SiteMembershipRequest.Builder(selectedOptionItems.get(0), true));
                onMenuItemClick = true;
                break;
            case R.id.menu_site_favorite:
            case R.id.menu_site_unfavorite:
                Operator.with(fragmentRef.get().getActivity())
                        .load(new SiteFavoriteRequest.Builder(selectedOptionItems.get(0)));
                onMenuItemClick = true;
                break;
            default:
                onMenuItemClick = false;
                break;
        }
        selectedOptionItems.clear();
        return onMenuItemClick;
    }
}
