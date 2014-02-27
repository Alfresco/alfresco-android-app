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
package org.alfresco.mobile.android.application.activity;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.UIUtils;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

/**
 * It's the first screen seens by the user when the application starts. Display
 * the first step of account creation wizard.
 * 
 * @author Jean Marie Pascal
 */
public class HomeScreenFragment extends DialogFragment
{
    public static final String TAG = HomeScreenFragment.class.getName();

    private View rootView;

    public HomeScreenFragment()
    {
        setStyle(android.R.style.Theme_Holo_Light_Dialog, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public void onStart()
    {
        setRetainInstance(true);
        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_alfresco);
        }
        super.onStart();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().setTitle(R.string.app_name);
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }
        else
        {
            UIUtils.displayTitle(getActivity(), R.string.app_name, false);
        }

        rootView = inflater.inflate(R.layout.app_homescreen, container, false);

        TextView tv = (TextView) rootView.findViewById(R.id.help_guide);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        return rootView;
    }
}
