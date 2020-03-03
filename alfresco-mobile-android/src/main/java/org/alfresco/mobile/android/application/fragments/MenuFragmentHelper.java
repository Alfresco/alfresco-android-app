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
package org.alfresco.mobile.android.application.fragments;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MenuFragmentHelper
{
    // TODO Move this method to RefreshHelper after implementing dynamic
    // actionItem creation.
    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    public static MenuItem getMenu(Context context, Menu menu)
    {
        if (menu == null || context == null) { return null; }
        if (AccessibilityUtils.isEnabled(context))
        {
            MenuItem mi = menu.add(Menu.NONE, R.id.menu_refresh, Menu.FIRST + 40, R.string.refresh);
            mi.setIcon(R.drawable.ic_refresh);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return mi;
        }
        else
        {
            return null;
        }
    }

    public static boolean canDisplayFragmentMenu(FragmentActivity activity)
    {
        if (activity != null && activity instanceof MainActivity && ((MainActivity) activity).isSlideMenuVisible()) { return false; }
        return true;
    }

}
