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

import android.content.Context;
import android.content.res.Resources;
import androidx.fragment.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;

import com.mobidevelop.widget.SplitPaneLayout;

public abstract class DisplayUtils
{

    public static int getFragmentPlace(FragmentActivity a)
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
    public static boolean hasLeftPane(FragmentActivity a)
    {
        return getLeftPane(a) != null;
    }

    public static boolean hasCentralPane(FragmentActivity a)
    {
        return getCentralPane(a) != null;
    }

    // ///////////////////////////////////////////
    // RETRIEVE FRAGMENT IDS
    // ///////////////////////////////////////////
    public static int getLeftFragmentId(FragmentActivity a)
    {
        return R.id.left_pane_body;
    }

    public static int getCentralFragmentId(FragmentActivity a)
    {
        return R.id.central_pane_body;
    }

    public static int getMainPaneId(FragmentActivity a)
    {
        if (hasCentralPane(a)) { return getCentralFragmentId(a); }
        return getLeftFragmentId(a);
    }

    // ///////////////////////////////////////////
    // RETRIEVE PANE
    // ///////////////////////////////////////////
    public static View getLeftPane(FragmentActivity a)
    {
        return a.findViewById(R.id.left_pane);
    }

    public static View getCentralPane(FragmentActivity a)
    {
        return a.findViewById(R.id.central_pane);
    }

    public static View getMainPane(FragmentActivity a)
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

    public static void hide(View v, int id)
    {
        v.findViewById(id).setVisibility(View.GONE);
    }

    public static void show(View v)
    {
        v.setVisibility(View.VISIBLE);
    }

    public static void show(View v, int id)
    {
        v.findViewById(id).setVisibility(View.VISIBLE);
    }

    // ///////////////////////////////////////////
    // SIZE OF THE SCREEN
    // ///////////////////////////////////////////
    public static int getWidth(FragmentActivity context)
    {
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = context.getResources().getDisplayMetrics().density;
        int width = Math.round(outMetrics.widthPixels / density);

        Resources res = context.getResources();

        int coeff;
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

    public static int getPixels(Context context, int sizeInDp)
    {
        return context.getResources().getDimensionPixelSize(sizeInDp);
    }

    public static LinearLayout.LayoutParams resizeLayout(Context context, int widthInDp, int heightInDp)
    {
        return new LinearLayout.LayoutParams(getDPI(context.getResources().getDisplayMetrics(), widthInDp), getDPI(
                context.getResources().getDisplayMetrics(), heightInDp));
    }

    // ///////////////////////////////////////////
    // SPLITTER BAR
    // ///////////////////////////////////////////
    /**
     * Returns in dp
     * 
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
        return (split != null) ? Math.round(split.getSplitterPosition() / density) : Math.round(outMetrics.widthPixels
                / density);
    }

    public static int getScreenWidth(FragmentActivity context)
    {
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = context.getResources().getDisplayMetrics().density;

        return Math.round(outMetrics.widthPixels / density);
    }
}
