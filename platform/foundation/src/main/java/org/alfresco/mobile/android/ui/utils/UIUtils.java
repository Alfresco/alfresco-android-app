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
package org.alfresco.mobile.android.ui.utils;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.platform.utils.SessionUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Utility around UI Management.
 * 
 * @author Jean Marie Pascal
 */
public class UIUtils
{

    /**
     * Set the background view with the drawable associated.
     * 
     * @param v
     * @param background
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setBackground(View v, Drawable background)
    {
        if (v == null) { return; }
        if (AndroidVersion.isJBOrAbove())
        {
            v.setBackground(background);
        }
        else
        {
            v.setBackgroundDrawable(background);
        }
    }

    /**
     * Retrieve screen dimension.
     * 
     * @param activity
     * @return
     */
    public static int[] getScreenDimension(FragmentActivity activity)
    {
        int width = 0;
        int height = 0;

        Display display = activity.getWindowManager().getDefaultDisplay();
        if (AndroidVersion.isHCMR2OrAbove())
        {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        }
        else
        {
            width = display.getWidth(); // deprecated
            height = display.getHeight(); // deprecated
        }

        return new int[] { width, height };
    }

    private static final Pattern NAME_PATTERN = Pattern
            .compile("(.*[\"\\*\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)");

    public static boolean hasInvalidName(String name)
    {
        Matcher matcher = NAME_PATTERN.matcher(name);
        return matcher.matches();
    }

    public static void displayTitle(int titleId, AppCompatActivity activity)
    {
        displayTitle(activity, activity.getString(titleId));
        ActionBar bar = activity.getSupportActionBar();
        bar.setDisplayUseLogoEnabled(true);
        bar.setLogo(R.drawable.ic_application_logo);
    }

    public static void displayTitle(int titleId, AppCompatActivity activity, boolean isUpEnable)
    {
        displayTitle(activity, activity.getString(titleId), isUpEnable);
        ActionBar bar = activity.getSupportActionBar();
        bar.setDisplayUseLogoEnabled(true);
        bar.setLogo(R.drawable.ic_application_logo);
    }

    public static void displayTitle(FragmentActivity activity, int titleId)
    {
        displayTitle(activity, activity.getString(titleId));
    }

    public static void displayTitle(FragmentActivity activity, int titleId, boolean isUpEnable)
    {
        displayTitle(activity, activity.getString(titleId), isUpEnable);
    }

    public static void displayTitle(FragmentActivity activity, String title)
    {
        displayTitle(activity, title, true);
    }

    public static void displayTitle(FragmentActivity activity, String title, boolean isUpEnable)
    {
        if (activity instanceof AppCompatActivity)
        {
            ActionBar bar = ((AppCompatActivity) activity).getSupportActionBar();

            bar.setDisplayShowTitleEnabled(false);
            bar.setDisplayShowCustomEnabled(true);
            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
            bar.setDisplayUseLogoEnabled(false);
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            bar.setHomeButtonEnabled(isUpEnable);

            // If MenuFragment is visible => up is disable.
            Fragment fr = activity.getSupportFragmentManager()
                    .findFragmentByTag("org.alfresco.mobile.android.application.fragments.menu.MainMenuFragment");
            if (fr != null && fr.isVisible())
            {
                bar.setDisplayHomeAsUpEnabled(false);
                bar.setHomeButtonEnabled(false);
            }
            else
            {
                bar.setDisplayHomeAsUpEnabled(isUpEnable);
            }

            View v = bar.getCustomView();
            if (v == null)
            {
                LayoutInflater inflater = (LayoutInflater) activity
                        .getSystemService(FragmentActivity.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.app_header_row, null);
            }
            v.setVisibility(View.VISIBLE);

            TextView tv = (TextView) v.findViewById(R.id.toptext);
            if (SessionUtils.getAccount(activity) != null
                    && AlfrescoAccountManager.getInstance(activity).hasMultipleAccount())
            {
                tv.setText(SessionUtils.getAccount(activity).getTitle());
                tv.setVisibility(View.VISIBLE);
            }
            else
            {
                tv.setVisibility(View.GONE);
            }
            tv = (TextView) v.findViewById(R.id.bottomtext);
            tv.setText(title);

            if (bar.getCustomView() == null)
            {
                bar.setCustomView(v);
            }

            activity.invalidateOptionsMenu();
        }
    }

    public static String getAccountLabel(AlfrescoAccount account)
    {
        String label = "";
        if (account != null)
        {
            label = account.getTitle();
            if (label == null || label.isEmpty())
            {
                label = account.getUsername();
            }
        }
        return label;

    }

    /**
     * Init the validation button form.
     *
     * @param vRoot
     * @param actionId
     * @return
     */
    public static Button initValidation(View vRoot, int actionId)
    {
        Button bcreate = (Button) vRoot.findViewById(R.id.validate_action);
        bcreate.setText(actionId);
        return bcreate;
    }

    public static Button initCancel(View vRoot, int actionId)
    {
        Button bcreate = (Button) vRoot.findViewById(R.id.cancel);
        bcreate.setText(actionId);
        return bcreate;
    }

    public static Button initCancel(View vRoot, int actionId, boolean hide)
    {
        Button bcreate = (Button) vRoot.findViewById(R.id.cancel);
        bcreate.setText(actionId);
        if (hide)
        {
            bcreate.setVisibility(View.GONE);
        }
        return bcreate;
    }

    public static Button initValidation(View vRoot, int actionId, boolean hideCancel)
    {
        Button bcreate = initValidation(vRoot, actionId);
        if (hideCancel)
        {
            vRoot.findViewById(R.id.cancel).setVisibility(View.GONE);
        }
        return bcreate;
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId()
    {
        for (;;)
        {
            final int result = sNextGeneratedId.get();
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) { return result; }
        }
    }

    public static void hideKeyboard(FragmentActivity activity)
    {
        if (activity.getWindow().getCurrentFocus() == null) { return; }
        InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(activity.getWindow().getCurrentFocus().getWindowToken(), 0);
    }

    public static void showKeyboard(FragmentActivity activity, View v)
    {
        InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(v, 0);
    }

    public static void hideKeyboard(FragmentActivity activity, View v)
    {
        InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static String getParentDirPath(String fileOrDirPath)
    {
        boolean endsWithSlash = fileOrDirPath.endsWith(File.separator);
        return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf(File.separatorChar,
                endsWithSlash ? fileOrDirPath.length() - 2 : fileOrDirPath.length() - 1));
    }
}
