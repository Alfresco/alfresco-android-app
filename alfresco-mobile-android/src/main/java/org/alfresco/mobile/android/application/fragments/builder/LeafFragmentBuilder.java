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
package org.alfresco.mobile.android.application.fragments.builder;

import java.util.Map;

import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;

import androidx.fragment.app.FragmentActivity;

public abstract class LeafFragmentBuilder extends AlfrescoFragmentBuilder
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public LeafFragmentBuilder(FragmentActivity activity)
    {
        super(activity);
        this.hasBackStack = !DisplayUtils.hasCentralPane(activity);
    }

    public LeafFragmentBuilder(FragmentActivity activity, Map<String, Object> configuration)
    {
        super(activity, configuration);
        this.hasBackStack = !DisplayUtils.hasCentralPane(activity);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DISPLAY
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void display()
    {
        // Clear Central Panel ?
        FragmentDisplayer.clearCentralPane(getActivity());

        // Display Fragment
        FragmentDisplayer.load(this).back(hasBackStack).into(FragmentDisplayer.PANEL_CENTRAL);
    }
}
