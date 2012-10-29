/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments;

import org.alfresco.mobile.android.application.R;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.widget.TextView;

public abstract class DisplayUtils
{
    
    // ///////////////////////////////////////////
    // TITLE
    // ///////////////////////////////////////////
    public static void setTitleFragmentPlace(Activity a, int title)
    {
        getTitleFragmentPlace(a).setVisibility(View.VISIBLE);
        getTitleFragmentPlace(a).setText(title);
    }

    public static void setTitleFragmentPlace(Activity a, String title)
    {
        getTitleFragmentPlace(a).setVisibility(View.VISIBLE);
        getTitleFragmentPlace(a).setText(title);
    }

    public static void hideTitleFragmentPlace(Activity a)
    {
        getTitleFragmentPlace(a).setVisibility(View.GONE);
    }

    public static TextView getTitleFragmentPlace(Activity a)
    {
        return (TextView) a.findViewById(getFragmentTitlePlace(a));
    }

    public static int getFragmentTitlePlace(Activity a)
    {
        int id = R.id.left_pane_title;
        if (DisplayUtils.hasCentralPane(a))
        {
            id = R.id.central_pane_title;
        }
        return id;
    }

    public static void setLeftTitle(Activity a, String title)
    {
        a.findViewById(R.id.left_pane_title).setVisibility(View.VISIBLE);
        if (a.findViewById(R.id.left_pane_title) != null)
            a.findViewById(R.id.left_pane_title).setVisibility(View.VISIBLE);
        ((TextView) a.findViewById(R.id.left_pane_title)).setText(title);
    }

    public static void hideLeftTitlePane(Activity a)
    {
        a.findViewById(R.id.left_pane_title).setVisibility(View.GONE);
    }

    public static int getFragmentPlace(Activity a)
    {
        int id = R.id.left_pane_body;
        if (DisplayUtils.hasCentralPane(a))
        {
            id = R.id.central_pane_body;
        }
        return id;
    }

    // ///////////////////////////////////////////
    // FLAGS
    // ///////////////////////////////////////////
    public static boolean hasLeftPane(Activity a)
    {
        return getLeftPane(a) != null;
    }

    public static boolean hasCentralPane(Activity a)
    {
        return getCentralPane(a) != null;
    }


    // ///////////////////////////////////////////
    // RETRIEVE FRAGMENT IDS
    // ///////////////////////////////////////////
    public static int getLeftFragmentId(Activity a)
    {
        return R.id.left_pane_body;
    }

    public static int getCentralFragmentId(Activity a)
    {
        return R.id.central_pane_body;
    }

    public static int getMainPaneId(Activity a)
    {
        if (hasCentralPane(a)) return getCentralFragmentId(a);
        return getLeftFragmentId(a);
    }

    // ///////////////////////////////////////////
    // RETRIEVE PANE
    // ///////////////////////////////////////////
    public static View getLeftPane(Activity a)
    {
        return a.findViewById(R.id.left_pane);
    }

    public static View getCentralPane(Activity a)
    {
        return a.findViewById(R.id.central_pane);
    }

    public static View getMainPane(Activity a)
    {
        if (hasCentralPane(a)) return getCentralPane(a);
        return getLeftPane(a);
    }

    // ///////////////////////////////////////////
    // SHOW / HIDE
    // ///////////////////////////////////////////
    public static void hide(View v)
    {
        v.setVisibility(View.GONE);
    }

    public static void show(View v)
    {
        v.setVisibility(View.VISIBLE);
    }

    // ///////////////////////////////////////////
    // SHOW / HIDE 2 OR SINGLE PANE
    // ///////////////////////////////////////////
    public static void switchSingleOrTwo(Activity activity, boolean isNull)
    {
        if (activity.getResources().getBoolean(R.bool.tablet_middle) && hasCentralPane(activity))
        {
            Fragment fr = activity.getFragmentManager().findFragmentById(DisplayUtils.getCentralFragmentId(activity));
            if ((fr != null && !isNull) || (fr == null && isNull))
            {
                DisplayUtils.getLeftPane(activity).setVisibility(View.GONE);
                DisplayUtils.getCentralPane(activity).setVisibility(View.VISIBLE);
            }
            else if ((fr == null && !isNull) || (fr != null && isNull))
            {
                DisplayUtils.getLeftPane(activity).setVisibility(View.VISIBLE);
                DisplayUtils.getCentralPane(activity).setVisibility(View.GONE);
            }
        }
    }
}
