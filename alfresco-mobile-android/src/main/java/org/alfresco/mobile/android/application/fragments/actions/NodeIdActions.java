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
package org.alfresco.mobile.android.application.fragments.actions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.sync.SyncFragment;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.async.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.operation.OperationWaitingDialogFragment;

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
        this.fragmentRef = new WeakReference<Fragment>(f);
        this.activityRef = new WeakReference<Activity>(f.getActivity());
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
    protected void getMenu(Activity activity, Menu menu)
    {
        SubMenu createMenu;

        createMenu = menu.addSubMenu(Menu.NONE, R.id.menu_action_favorite_group, Menu.FIRST + 135, R.string.favorite);
        createMenu.setIcon(R.drawable.ic_favorite_light);
        createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        createMenu.add(Menu.NONE, R.id.menu_action_favorite_group_favorite, Menu.FIRST + 1, R.string.favorite);
        createMenu.add(Menu.NONE, R.id.menu_action_favorite_group_unfavorite, Menu.FIRST + 2, R.string.unfavorite);

        createMenu = menu.addSubMenu(Menu.NONE, R.id.menu_action_like_group, Menu.FIRST + 3, R.string.like);
        createMenu.setIcon(R.drawable.ic_like);
        createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        createMenu.add(Menu.NONE, R.id.menu_action_like_group_like, Menu.FIRST + 1, R.string.like);
        createMenu.add(Menu.NONE, R.id.menu_action_like_group_unlike, Menu.FIRST + 2, R.string.unlike);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        Boolean b = false;
        switch (item.getItemId())
        {
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

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void favorite(boolean doFavorite)
    {
        List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(selectedItems.size());
        for (String nodeId : selectedItems)
        {
            requestsBuilder.add(new FavoriteNodeRequest.Builder(nodeId, doFavorite, true)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        }
        String operationId = Operator.with(getActivity(), SessionUtils.getAccount(getActivity())).load(requestsBuilder);

        if (getFragment() instanceof DocumentFolderBrowserFragment || getFragment() instanceof SyncFragment)
        {
            int titleId = R.string.unfavorite;
            int iconId = R.drawable.ic_unfavorite_dark;
            if (doFavorite)
            {
                titleId = R.string.favorite;
                iconId = R.drawable.ic_favorite_light;
            }
            OperationWaitingDialogFragment.newInstance(FavoriteNodeRequest.TYPE_ID, iconId,
                    getFragment().getString(titleId), null, null, selectedItems.size(), operationId).show(
                    getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
        }
    }

    private void like(boolean doLike)
    {
        List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(selectedItems.size());
        for (String node : selectedItems)
        {
            requestsBuilder.add(new LikeNodeRequest.Builder(node, doLike)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        }
        Operator.with(getActivity(), SessionUtils.getAccount(getActivity())).load(requestsBuilder);

        if (getFragment() instanceof DocumentFolderBrowserFragment)
        {
            int titleId = R.string.unlike;
            int iconId = R.drawable.ic_unlike;
            if (doLike)
            {
                titleId = R.string.like;
                iconId = R.drawable.ic_like;
            }
            OperationWaitingDialogFragment.newInstance(LikeNodeRequest.TYPE_ID, iconId,
                    getActivity().getString(titleId), null, null, selectedItems.size(), null).show(
                    getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
        }
    }

}
