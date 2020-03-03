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
package org.alfresco.mobile.android.application.fragments.search;

import java.lang.ref.WeakReference;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions;
import org.alfresco.mobile.android.application.providers.search.HistorySearchProvider;

import android.net.Uri;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class HistorySearchActions extends AbstractActions<Long>
{
    public static final String TAG = "NodeActions";

    public HistorySearchActions(Fragment f, List<Long> selectedNodes)
    {
        this.fragmentRef = new WeakReference<>(f);
        this.activityRef = new WeakReference<>(f.getActivity());
        this.selectedItems = selectedNodes;
        for (Long itemCursor : selectedNodes)
        {
            addNode(itemCursor);
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
            title += String.format(getFragment().getResources().getQuantityString(R.plurals.selected_search, size),
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

        createMenu = menu.addSubMenu(Menu.NONE, R.id.menu_historysearch_delete, Menu.FIRST , R.string.delete);
        createMenu.setIcon(R.drawable.ic_delete);
        createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        Boolean b = false;
        switch (item.getItemId())
        {
            case R.id.menu_historysearch_delete:
                delete();
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
    private void delete()
    {
        for (Long searchId : selectedItems)
        {
            Uri uri = Uri.parse(HistorySearchProvider.CONTENT_URI + "/" + searchId);
            getActivity().getContentResolver().delete(uri, null, null);
        }
    }
}
