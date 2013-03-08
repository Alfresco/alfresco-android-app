/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.util.Stack;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.preferences.PasscodePreferences;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

@TargetApi(13)
public class FragmentDisplayer
{

    private FragmentDisplayer()
    {
    }

    public static void loadFragment(Activity a, Integer viewId, String tag)
    {
        if (a.getFragmentManager().findFragmentByTag(tag) == null)
        {
            loadFragment(a, FragmentFactory.createInstance(tag), viewId, tag);
        }
    }

    public static void loadFragment(Activity a, Fragment f, Integer viewId, String tag)
    {
        PasscodePreferences.updateLastActivityDisplay(a);
        if (f == null)
        {
            loadFragment(a, viewId, tag);
        }
        else
        {
            if (a.getFragmentManager().findFragmentByTag(tag) == null)
            {
                FragmentTransaction t2 = a.getFragmentManager().beginTransaction();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB
                        && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR2)
                {
                    // t2.setCustomAnimations(R.anim.slide_in_right,
                    // R.anim.slide_out_left);
                }
                else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2
                        && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                {
                    t2.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                {
                    t2.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
                            R.anim.slide_out_right);
                }
                t2.add(viewId, f, tag);
                t2.commit();
            }
        }

    }

    public static void removeFragment(Activity a, Stack<String> tags)
    {
        Fragment fr;
        FragmentTransaction t2 = a.getFragmentManager().beginTransaction();
        for (String tag : tags)
        {
            fr = a.getFragmentManager().findFragmentByTag(tag);
            if (fr != null && fr.isAdded())
            {
                t2.remove(fr);
            }
        }
        t2.commit();
    }

    public static void hide(Activity a, String tag)
    {
        Fragment fr = a.getFragmentManager().findFragmentByTag(tag);
        if (fr != null && fr.isAdded())
        {
            FragmentTransaction t2 = a.getFragmentManager().beginTransaction();
            t2.hide(fr);
            t2.addToBackStack(null);
            t2.commit();
        }
    }

    public static void remove(Activity a, Fragment fr)
    {
        if (fr != null && fr.isAdded())
        {
            FragmentTransaction t2 = a.getFragmentManager().beginTransaction();
            t2.remove(fr);
            t2.addToBackStack(null);
            t2.commit();
        }
    }

    public static void remove(Activity a, Fragment fr, boolean backStack)
    {
        if (fr != null && fr.isAdded())
        {
            FragmentTransaction t2 = a.getFragmentManager().beginTransaction();
            t2.remove(fr);
            if (backStack)
            {
                t2.addToBackStack(null);
            }
            t2.commit();
        }
    }

    public static void removeFragment(Activity a, String tag)
    {
        Fragment fr = a.getFragmentManager().findFragmentByTag(tag);
        try
        {
            if (fr != null && fr.isVisible())
            {
                FragmentTransaction t2 = a.getFragmentManager().beginTransaction();
                t2.remove(fr);
                t2.commit();
            }
        }
        catch (Exception e)
        {
            // Specific use case in Honeycomb. Sometimes the fragment has not
            // been added and we must force the add.
            FragmentTransaction t2 = a.getFragmentManager().beginTransaction();
            t2.add(fr, fr.getTag());
            t2.remove(fr);
            t2.commit();
        }
    }

    public static void removeFragment(Activity a, int id)
    {
        Fragment fr = a.getFragmentManager().findFragmentById(id);
        try
        {
            if (fr != null && fr.isAdded())
            {
                FragmentTransaction t2 = a.getFragmentManager().beginTransaction();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB
                        && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR2)
                {
                    // t2.setCustomAnimations(R.anim.slide_in_right,//
                    // R.anim.slide_out_left);
                }
                else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2
                        && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                {
                    t2.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                {
                    t2.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
                            R.anim.slide_out_right);
                }
                t2.remove(fr);
                t2.commit();
            }
        }
        catch (Exception e)
        {
            // Specific use case in Honeycomb. Sometimes the fragment has not
            // been added and we must force the add.
            FragmentTransaction t2 = a.getFragmentManager().beginTransaction();
            t2.add(fr, fr.getTag());
            t2.remove(fr);
            t2.commit();
        }
    }

    public static void replaceFragment(Activity a, Integer viewId, String tag, boolean backStack)
    {
        replaceFragment(a, FragmentFactory.createInstance(tag), viewId, tag, backStack);
    }

    public static void replaceFragment(Activity a, Fragment f, Integer viewId, String tag, boolean backStack)
    {
        replaceFragment(a, f, viewId, tag, backStack, true);
    }

    public static void replaceFragment(Activity a, Fragment f, Integer viewId, String tag, boolean backStack,
            boolean hasAnimation)
    {
        PasscodePreferences.updateLastActivityDisplay(a);
        if (f == null)
        {
            replaceFragment(a, viewId, tag, backStack);
        }
        else
        {
            try
            {
                FragmentTransaction t2 = a.getFragmentManager().beginTransaction();
                if (hasAnimation && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB
                        && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR2)
                {
                    // t2.setCustomAnimations(R.anim.slide_in_right,//
                    // R.anim.slide_out_left);
                }
                else if (hasAnimation
                        && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2
                        && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                {
                    t2.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                else if (hasAnimation
                        && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                {
                    t2.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
                            R.anim.slide_out_right);
                }
                t2.replace(viewId, f, tag);
                if (backStack)
                {
                    t2.addToBackStack(tag);
                }
                t2.commit();
            }
            catch (Exception e)
            {
                // Specific use case in Honeycomb. Sometimes the fragment has
                // not been added and we must force the add.
                FragmentTransaction t2 = a.getFragmentManager().beginTransaction();
                t2.add(f, f.getTag());
                t2.remove(f);
                t2.commit();
            }
        }
    }
}
