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

import java.lang.ref.WeakReference;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;

public final class FragmentDisplayer
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTANTS
    // ///////////////////////////////////////////////////////////////////////////
    private static final String TAG = FragmentDisplayer.class.getSimpleName();

    protected static final int ACTION_ADD = 0;

    protected static final int ACTION_REPLACE = 1;

    protected static final int ACTION_REMOVE = 2;

    protected static final int ACTION_CLEAN = 3;

    public static final int PANEL_LEFT = -100;

    public static final int PANEL_CENTRAL = -200;

    public static final int PANEL_DIALOG = -300;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    private FragmentDisplayer()
    {

    }

    public static Creator with(FragmentActivity activity)
    {
        return new Creator(activity);
    }

    public static Creator load(AlfrescoFragmentBuilder builder)
    {
        return new Creator(builder);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILITY
    // ///////////////////////////////////////////////////////////////////////////
    public static void clearCentralPane(FragmentActivity a)
    {
        if (DisplayUtils.hasCentralPane(a))
        {
            FragmentDisplayer.with(a).remove(DisplayUtils.getCentralFragmentId(a));
            FragmentDisplayer.with(a).remove(android.R.id.tabcontent);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static final int[] SLIDE = new int[] { R.anim.anim_slide_in_right, R.anim.anim_slide_out_left,
            R.anim.anim_slide_in_left, R.anim.anim_slide_out_right };

    public static class Creator
    {
        private int action;

        private AlfrescoFragmentBuilder builder;

        private int targetId;

        private WeakReference<FragmentActivity> activity;

        private WeakReference<Fragment> fragmentRef;

        private boolean backStack = true;

        private int[] animation = SLIDE;

        private String tag;

        private boolean hasAnimation = true;

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        protected Creator()
        {
        }

        public Creator(AlfrescoFragmentBuilder builder)
        {
            this();
            this.activity = new WeakReference<>(builder.getActivity());
            this.builder = builder;
            this.action = ACTION_REPLACE;
            this.backStack = builder.hasBackStack();
        }

        public Creator(FragmentActivity activity)
        {
            this();
            this.activity = new WeakReference<>(activity);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Creator back(boolean hasBackStack)
        {
            this.backStack = hasBackStack;
            return this;
        }

        public Creator load(Fragment frag)
        {
            this.fragmentRef = new WeakReference<Fragment>(frag);
            this.action = ACTION_ADD;
            return this;
        }

        public Creator replace(Fragment frag)
        {
            this.fragmentRef = new WeakReference<Fragment>(frag);
            this.action = ACTION_REPLACE;
            return this;
        }

        public Creator animate(int[] animation)
        {
            this.animation = animation;
            if (animation == null)
            {
                hasAnimation = false;
            }
            return this;
        }

        public Creator removeAll()
        {
            this.action = ACTION_CLEAN;
            hasAnimation = false;
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // EXECUTION
        // ///////////////////////////////////////////////////////////////////////////
        public void into(int targetId)
        {
            this.targetId = targetId;
            execute();
        }

        public void asDialog()
        {
            this.targetId = PANEL_DIALOG;
            execute();
        }

        public void remove(Fragment fr)
        {
            try
            {
                this.fragmentRef = new WeakReference<Fragment>(fr);
                this.action = ACTION_REMOVE;
                this.backStack = false;
                execute();
            }
            catch (Exception e)
            {
                // Specific use case in Honeycomb. Sometimes the fragment has
                // not been added and we must force the add.
                FragmentTransaction t2 = activity.get().getSupportFragmentManager().beginTransaction();
                t2.add(fr, fr.getTag());
                t2.remove(fr);
                t2.commit();
            }

        }

        public void remove(String fragmentTag)
        {
            remove(activity.get().getSupportFragmentManager().findFragmentByTag(fragmentTag));
        }

        public void remove(int viewId)
        {
            remove(activity.get().getSupportFragmentManager().findFragmentById(viewId));
        }

        // ///////////////////////////////////////////////////////////////////////////
        // Creation
        // ///////////////////////////////////////////////////////////////////////////
        private void execute()
        {
            try
            {
                if (activity.get() instanceof MainActivity && DisplayUtils.hasCentralPane(activity.get())
                        && targetId == PANEL_LEFT)
                {
                    FragmentDisplayer.clearCentralPane(activity.get());
                }

                Fragment frag = null;
                // Create Fragment
                if (builder != null)
                {
                    frag = builder.createFragment();
                }
                else if (fragmentRef != null)
                {
                    frag = fragmentRef.get();
                }

                // If null we consider the fragment creation is done elsewhere.
                if (frag == null) { return; }

                // Create Tag based on Fragment className
                tag = frag.getClass().getName();

                //Special case : Show as Dialog
                if (PANEL_DIALOG == targetId)
                {
                    if (frag instanceof DialogFragment)
                    {
                        ((DialogFragment) frag).show(activity.get().getSupportFragmentManager(), tag);
                    }
                    return;
                }

                // Create Transaction
                FragmentTransaction transaction = activity.get().getSupportFragmentManager().beginTransaction();

                // Set Animation
                if (hasAnimation)
                {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2
                            && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    {
                        transaction.setCustomAnimations(animation[0], animation[1]);
                    }
                    else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    {
                        transaction.setCustomAnimations(animation[0], animation[1], animation[2], animation[3]);
                    }
                }

                // Define target
                switch (targetId)
                {
                    case PANEL_LEFT:
                        targetId = DisplayUtils.getLeftFragmentId(activity.get());
                        break;
                    case PANEL_CENTRAL:
                        targetId = DisplayUtils.getFragmentPlace(activity.get());
                        break;
                    default:
                        break;
                }

                switch (action)
                {
                    case ACTION_ADD:
                        transaction.add(targetId, frag, tag);
                        break;
                    case ACTION_REPLACE:
                        transaction.replace(targetId, frag, tag);
                        break;
                    case ACTION_REMOVE:
                        hasAnimation = false;
                        transaction.remove(frag);
                        break;
                    case ACTION_CLEAN:

                        break;

                    default:
                        break;
                }

                // BackStack
                if (backStack)
                {
                    transaction.addToBackStack(tag);
                }

                // Commit
                transaction.commit();
            }
            catch (Exception e)
            {
                Log.w(TAG, Log.getStackTraceString(e));
            }
        }
    }
}
