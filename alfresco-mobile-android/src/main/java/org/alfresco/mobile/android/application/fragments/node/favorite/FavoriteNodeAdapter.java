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
package org.alfresco.mobile.android.application.fragments.node.favorite;

import java.lang.ref.WeakReference;
import java.util.List;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurableActionHelper;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.node.browser.ProgressNodeAdapter;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.platform.utils.SessionUtils;

import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @since 1.8
 * @author Jean Marie Pascal
 */
public class FavoriteNodeAdapter extends ProgressNodeAdapter
{
    // Used by Favorites & Sync Fragments
    public FavoriteNodeAdapter(Fragment fr, int textViewResourceId, List<Node> listItems, List<Node> selectedItems,
            int mode)
    {
        super(fr, textViewResourceId, listItems, selectedItems, mode);
        fragmentRef = new WeakReference<>(fr);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public void getMenu(Menu menu, Node node)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, R.id.menu_node_details, Menu.FIRST, R.string.action_view_properties);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if (ConfigurableActionHelper.isVisible(getActivity(), SessionUtils.getAccount(getActivity()),
                SessionUtils.getSession(getActivity()), node, ConfigurableActionHelper.ACTION_NODE_FAVORITE))
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_favorite_group_unfavorite, Menu.FIRST + 50,
                    R.string.action_unfavorite_site);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;
        switch (item.getItemId())
        {
            case R.id.menu_node_details:
                onMenuItemClick = true;
                NodeDetailsFragment.with(getActivity()).nodeId(selectedOptionItems.get(0).getIdentifier()).display();
                if (DisplayUtils.hasCentralPane(getActivity()))
                {
                    selectedItems.add(selectedOptionItems.get(0));
                }
                notifyDataSetChanged();
                break;
            case R.id.menu_action_favorite_group_unfavorite:
                onMenuItemClick = true;
                NodeActions.favorite(getFragment(), false, selectedOptionItems);
                break;
            default:
                onMenuItemClick = false;
                break;
        }
        selectedOptionItems.clear();
        return onMenuItemClick;
    }
}
