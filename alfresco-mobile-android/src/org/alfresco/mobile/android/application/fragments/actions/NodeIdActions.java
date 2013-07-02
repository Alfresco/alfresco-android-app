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
package org.alfresco.mobile.android.application.fragments.actions;

import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.favorites.FavoritesSyncFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.application.operations.batch.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.app.Activity;
import android.app.Fragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class NodeIdActions extends AbstractActions<String>
{
    public static final String TAG = "NodeActions";

    public NodeIdActions(Fragment f, List<String> selectedNodes)
    {
        this.fragment = f;
        this.activity = f.getActivity();
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
            title += String.format(activity.getResources().getQuantityString(R.plurals.selected_document, size), size);
        }

        return title;
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////////////
    protected void getMenu(Activity activity, Menu menu)
    {
        SubMenu createMenu;

        createMenu = menu.addSubMenu(Menu.NONE, MenuActionItem.MENU_FAVORITE_GROUP, Menu.FIRST
                + MenuActionItem.MENU_FAVORITE_GROUP, R.string.favorite);
        createMenu.setIcon(R.drawable.ic_favorite_dark);
        createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        createMenu.add(Menu.NONE, MenuActionItem.MENU_FAVORITE_GROUP_FAVORITE, Menu.FIRST
                + MenuActionItem.MENU_FAVORITE_GROUP_FAVORITE, R.string.favorite);
        createMenu.add(Menu.NONE, MenuActionItem.MENU_FAVORITE_GROUP_UNFAVORITE, Menu.FIRST
                + MenuActionItem.MENU_FAVORITE_GROUP_UNFAVORITE, R.string.unfavorite);

        createMenu = menu.addSubMenu(Menu.NONE, MenuActionItem.MENU_LIKE_GROUP, Menu.FIRST
                + MenuActionItem.MENU_LIKE_GROUP, R.string.like);
        createMenu.setIcon(R.drawable.ic_like);
        createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        createMenu.add(Menu.NONE, MenuActionItem.MENU_LIKE_GROUP_LIKE,
                Menu.FIRST + MenuActionItem.MENU_LIKE_GROUP_LIKE, R.string.like);
        createMenu.add(Menu.NONE, MenuActionItem.MENU_LIKE_GROUP_UNLIKE, Menu.FIRST
                + MenuActionItem.MENU_LIKE_GROUP_UNLIKE, R.string.unlike);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        Boolean b = false;
        switch (item.getItemId())
        {
            case MenuActionItem.MENU_FAVORITE_GROUP_FAVORITE:
                favorite(true);
                b = true;
                break;
            case MenuActionItem.MENU_FAVORITE_GROUP_UNFAVORITE:
                favorite(false);
                b = true;
                break;
            case MenuActionItem.MENU_LIKE_GROUP_LIKE:
                like(true);
                b = true;
                break;
            case MenuActionItem.MENU_LIKE_GROUP_UNLIKE:
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

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void favorite(boolean doFavorite)
    {
        OperationsRequestGroup group = new OperationsRequestGroup(activity, SessionUtils.getAccount(activity));
        for (String node : selectedItems)
        {
            group.enqueue(new FavoriteNodeRequest(null, node, doFavorite)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        }

        BatchOperationManager.getInstance(activity).enqueue(group);

        if (fragment instanceof ChildrenBrowserFragment || fragment instanceof FavoritesSyncFragment)
        {
            int titleId = R.string.unfavorite;
            int iconId = R.drawable.ic_unfavorite_dark;
            if (doFavorite)
            {
                titleId = R.string.favorite;
                iconId = R.drawable.ic_favorite_dark;
            }
            OperationWaitingDialogFragment.newInstance(FavoriteNodeRequest.TYPE_ID, iconId,
                    fragment.getString(titleId), null, null, selectedItems.size()).show(
                    fragment.getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
        }
    }

    private void like(boolean doLike)
    {
        OperationsRequestGroup group = new OperationsRequestGroup(activity, SessionUtils.getAccount(activity));
        for (String node : selectedItems)
        {
            group.enqueue(new LikeNodeRequest(null, node, doLike)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        }
        BatchOperationManager.getInstance(activity).enqueue(group);

        if (fragment instanceof ChildrenBrowserFragment)
        {
            int titleId = R.string.unlike;
            int iconId = R.drawable.ic_unlike;
            if (doLike)
            {
                titleId = R.string.like;
                iconId = R.drawable.ic_like;
            }
            OperationWaitingDialogFragment.newInstance(LikeNodeRequest.TYPE_ID, iconId, fragment.getString(titleId),
                    null, null, selectedItems.size()).show(fragment.getActivity().getFragmentManager(),
                    OperationWaitingDialogFragment.TAG);
        }
    }

}
