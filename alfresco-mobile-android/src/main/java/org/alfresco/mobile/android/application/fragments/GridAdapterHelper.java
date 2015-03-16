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
package org.alfresco.mobile.android.application.fragments;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.widgets.BaseShortcutActivity;
import org.alfresco.mobile.android.ui.GridFragment;

import android.app.Activity;
import android.content.Context;
import android.widget.GridView;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public class GridAdapterHelper
{

    public static final int DISPLAY_LIST = 0;

    public static final int DISPLAY_LIST_LARGE = 1;

    public static final int DISPLAY_GRID = 2;

    private GridAdapterHelper()
    {

    }

    public static int getDisplayItemLayout(Activity activity, GridView gv, int displayMode)
    {
        int displayItemLayout = R.layout.app_grid_large_progress_row;

        if (activity instanceof PublicDispatcherActivity || activity instanceof PrivateDialogActivity
                || activity instanceof BaseShortcutActivity)
        {
            gv.setColumnWidth(DisplayUtils.getDPI(activity.getResources().getDisplayMetrics(), 1000));
            return R.layout.app_grid_large_progress_row;
        }

        switch (displayMode)
        {
            case DISPLAY_LIST:
                gv.setColumnWidth(DisplayUtils.getDPI(activity.getResources().getDisplayMetrics(), 240));
                displayItemLayout = R.layout.app_grid_large_progress_row;
                break;
            case DISPLAY_LIST_LARGE:
                gv.setColumnWidth(DisplayUtils.getDPI(activity.getResources().getDisplayMetrics(), 320));
                displayItemLayout = R.layout.sdk_grid_row;
                break;
            case DISPLAY_GRID:
                gv.setColumnWidth(DisplayUtils.getDPI(activity.getResources().getDisplayMetrics(), 240));
                displayItemLayout = R.layout.sdk_grid_row;
                break;
            default:
                break;
        }
        return displayItemLayout;
    }

    public static int[] getGridLayoutId(Context context, GridFragment fragment)
    {
        // Specific part for dynaminc resize
        // First init ==> always
        int width = 1000;
        int columnWidth = 2048;

        int layoutId = R.layout.sdk_grid_row;
        int flagLayoutId = R.id.app_grid_progress;

        if (context instanceof MainActivity)
        {
            width = DisplayUtils.getSplitterWidth((MainActivity) context);
            columnWidth = 240;
            if (width <= 480)
            {
                layoutId = R.layout.sdk_grid_row;
                flagLayoutId = R.id.app_grid_progress;
                columnWidth = 240;
            }
            else if (width < 600)
            {
                layoutId = R.layout.app_grid_card_repo;
                flagLayoutId = R.id.app_grid_card;
                columnWidth = 240;
            }
            else if (width < 800)
            {
                layoutId = R.layout.app_grid_card_repo;
                flagLayoutId = R.id.app_grid_card;
                columnWidth = 240;
            }
            else if (width < 1000)
            {
                layoutId = R.layout.app_grid_tiles_repo;
                flagLayoutId = R.id.app_grid_tiles;
                columnWidth = 240;
            }
            else
            {
                layoutId = R.layout.app_grid_tiles_repo;
                flagLayoutId = R.id.app_grid_tiles;
                columnWidth = 240;
            }
        }

        if (fragment != null)
        {
            fragment.setColumnWidth(DisplayUtils.getDPI(context.getResources().getDisplayMetrics(), columnWidth));
        }

        return new int[] { layoutId, flagLayoutId };
    }
}
