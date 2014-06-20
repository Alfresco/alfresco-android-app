/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.utils.thirdparty.split.SplitPaneLayout;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;

public abstract class DisplayUtils
{

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
        if (hasCentralPane(a)) { return getCentralFragmentId(a); }
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
        if (hasCentralPane(a)) { return getCentralPane(a); }
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
    /**
     * Utility method to display or not the central panel in 7" configuration.<br/>
     * Portrait mode is single panel instead of landscape is 2 panels.
     * 
     * @param activity :
     * @param isNull : Indicate if there's already a fragment in the central
     *            panel.
     */
    public static void switchSingleOrTwo(Activity activity, boolean isNull)
    {
        if (activity.getResources().getBoolean(R.bool.tablet_middle) && hasCentralPane(activity))
        {
            /*
             * Fragment fr =
             * activity.getFragmentManager().findFragmentById(DisplayUtils
             * .getCentralFragmentId(activity)); SplitPaneLayout split =
             * (SplitPaneLayout) activity.findViewById(R.id.master_pane); if
             * ((fr != null && !isNull) || (fr == null && isNull)) {
             * //split.setSplitterPosition
             * (getDPI(activity.getResources().getDisplayMetrics(), 48));
             * //split.getChildAt(0).setVisibility(View.GONE);
             * //split.getChildAt(1).setVisibility(View.VISIBLE);
             * split.setSplitterPositionPercent(0f);
             * //DisplayUtils.getLeftPane(activity).setVisibility(View.GONE);
             * //DisplayUtils
             * .getCentralPane(activity).setVisibility(View.VISIBLE); } else if
             * ((fr == null && !isNull) || (fr != null && isNull)) {
             * //split.setSplitterPositionPercent(33f);
             * //split.getChildAt(0).setVisibility(View.VISIBLE);
             * //split.getChildAt(1).setVisibility(View.GONE);
             * split.setSplitterPositionPercent(100f);
             * //DisplayUtils.getLeftPane(activity).setVisibility(View.VISIBLE);
             * //DisplayUtils.getCentralPane(activity).setVisibility(View.GONE);
             * }
             */
        }
    }

    // ///////////////////////////////////////////
    // SIZE OF THE SCREEN
    // ///////////////////////////////////////////
    public static int getWidth(Activity context)
    {
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = context.getResources().getDisplayMetrics().density;
        int width = Math.round(outMetrics.widthPixels / density);

        Resources res = context.getResources();

        int coeff = 150;
        if (width < 320)
        {
            coeff = res.getInteger(R.integer.width_320);
        }
        else if (width < 480)
        {
            coeff = res.getInteger(R.integer.width_480);
        }
        else if (width < 600)
        {
            coeff = res.getInteger(R.integer.width_600);
        }
        else if (width < 720)
        {
            coeff = res.getInteger(R.integer.width_720);
        }
        else if (width < 1000)
        {
            coeff = res.getInteger(R.integer.width_1000);
        }
        else
        {
            coeff = res.getInteger(R.integer.width_max);
        }

        return coeff;
    }

    public static int getDPI(DisplayMetrics dm, int sizeInDp)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDp, dm);
    }

    // ///////////////////////////////////////////
    // SPLITTER BAR
    // ///////////////////////////////////////////
    /**
     * Returns in dp
     * @param context
     * @return
     */
    public static int getSplitterWidth(MainActivity context)
    {
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = context.getResources().getDisplayMetrics().density;
        
        SplitPaneLayout split = (SplitPaneLayout) context.findViewById(R.id.master_pane);
        return (split != null) ?  Math.round(split.getSplitterPosition() / density)  :  Math.round(outMetrics.widthPixels / density);
    }
    
    public static int getScreenWidth(Activity context)
    {
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = context.getResources().getDisplayMetrics().density;
        int width = Math.round(outMetrics.widthPixels / density);
        
        return width;
    }
}
