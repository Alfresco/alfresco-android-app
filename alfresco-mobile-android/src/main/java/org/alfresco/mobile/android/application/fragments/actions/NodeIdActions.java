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
package org.alfresco.mobile.android.application.fragments.actions;

import java.lang.ref.WeakReference;
import java.util.List;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurableActionHelper;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class NodeIdActions extends AbstractActions<String>
{
    public static final String TAG = "NodeActions";

    public NodeIdActions(Fragment f, List<String> selectedNodes)
    {
        this.fragmentRef = new WeakReference<>(f);
        this.activityRef = new WeakReference<>(f.getActivity());
        this.selectedItems = selectedNodes;
        for (String nodeId : selectedNodes)
        {
            addNode(nodeId);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    protected String createTitle()
    {
        String title = "";

        int size = selectedItems.size();
        if (size > 0)
        {
            title += String.format(getFragment().getResources().getQuantityString(R.plurals.selected_document, size),
                    size);
        }

        return title;
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////////////
    protected void getMenu(FragmentActivity activity, Menu menu)
    {
        SubMenu createMenu;

        // SYNC
        if (SyncContentManager.getInstance(getActivity()).hasActivateSync(getAccount()))
        {
            MenuItem menuItem;
            if (hasUnsyncedFiles(selectedItems)) {
                menuItem = menu.add(Menu.NONE, R.id.menu_action_sync_group_sync, Menu.FIRST, R.string.sync)
                        .setIcon(R.drawable.ic_sync_light);
            } else {
                menuItem = menu.add(Menu.NONE, R.id.menu_action_sync_group_unsync, Menu.FIRST, R.string.unsync)
                        .setIcon(R.drawable.ic_synced_dark);
            }
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            hideActionIfNecessary(menu, menuItem.getItemId(), ConfigurableActionHelper.ACTION_NODE_SYNC);
        }

        // FAVORITES
        createMenu = menu.addSubMenu(Menu.NONE, R.id.menu_action_favorite_group, Menu.FIRST + 2, R.string.favorite);
        createMenu.setIcon(R.drawable.ic_favorite_light);
        createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        hideActionIfNecessary(menu, createMenu.getItem().getItemId(), ConfigurableActionHelper.ACTION_NODE_FAVORITE);

        createMenu.add(Menu.NONE, R.id.menu_action_favorite_group_favorite, Menu.FIRST + 1, R.string.favorite);
        createMenu.add(Menu.NONE, R.id.menu_action_favorite_group_unfavorite, Menu.FIRST + 2, R.string.unfavorite);

        // LIKE
        AlfrescoSession alfSession = SessionUtils.getSession(activity);
        if (alfSession != null && alfSession.getRepositoryInfo() != null
                && alfSession.getRepositoryInfo().getCapabilities() != null
                && alfSession.getRepositoryInfo().getCapabilities().doesSupportLikingNodes())
        {
            createMenu = menu.addSubMenu(Menu.NONE, R.id.menu_action_like_group, Menu.FIRST + 3, R.string.like);
            createMenu.setIcon(R.drawable.ic_like);
            createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            hideActionIfNecessary(menu, createMenu.getItem().getItemId(), ConfigurableActionHelper.ACTION_NODE_LIKE);

            createMenu.add(Menu.NONE, R.id.menu_action_like_group_like, Menu.FIRST + 1, R.string.like);
            createMenu.add(Menu.NONE, R.id.menu_action_like_group_unlike, Menu.FIRST + 2, R.string.unlike);
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        Boolean b = false;
        switch (item.getItemId())
        {
            case R.id.menu_action_sync_group_sync:
                sync(true);
                b = true;
                break;
            case R.id.menu_action_sync_group_unsync:
                sync(false);
                b = true;
                break;
            case R.id.menu_action_favorite_group_favorite:
                favorite(true);
                b = true;
                break;
            case R.id.menu_action_favorite_group_unfavorite:
                favorite(false);
                b = true;
                break;
            case R.id.menu_action_like_group_like:
                like(true);
                b = true;
                break;
            case R.id.menu_action_like_group_unlike:
                like(false);
                b = true;
                break;
            default:
                break;
        }
        if (b)
        {
            selectedItems.clear();
            mode.finish();
        }
        return b;
    }

    private boolean hasUnsyncedFiles(List<String> selectedNodes) {
        for (String node : selectedNodes) {
            if (!SyncContentManager.getInstance(getActivity()).isSynced(getAccount(), node) && !SyncContentManager.getInstance(getActivity()).isRootSynced(getAccount(), node)) {
                return true;
            }
        }
        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    protected void hideActionIfNecessary(Menu menu, int menuItemId, int actionId)
    {
        if (!ConfigurableActionHelper.isVisible(getActivity(), getAccount(), actionId))
        {
            menu.removeItem(menuItemId);
        }
    }

    private void favorite(boolean doFavorite)
    {
        NodeActions.favorite(getFragment(), selectedItems, doFavorite);
    }

    private void sync(boolean doSync)
    {
        NodeActions.sync(getFragment(), selectedItems, doSync);
    }

    private void like(boolean doLike)
    {
        NodeActions.like(getFragment(), selectedItems, doLike);
    }

}
