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
package org.alfresco.mobile.android.application.fragments.workflow.task;

import java.util.List;

import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;

import android.app.Activity;
import android.app.Fragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class TaskActions extends AbstractActions<Task>
{
    public static final String TAG = TaskActions.class.getName();

    public TaskActions(Fragment f, List<Task> selectedNodes)
    {
        this.fragment = f;
        this.activity = f.getActivity();
        this.selectedItems = selectedNodes;
        for (Task nodeId : selectedNodes)
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
            title += String.format(activity.getResources().getQuantityString(R.plurals.selected_task, size), size);
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
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        Boolean b = false;
        switch (item.getItemId())
        {
            case MenuActionItem.MENU_FAVORITE_GROUP_FAVORITE:
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
}
