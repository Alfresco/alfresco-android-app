/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.managers;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.extension.analytics.GAnalyticsManagerImpl;
import org.alfresco.mobile.android.application.fragments.config.ConfigMenuEditorFragment;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.extensions.DevToolsManager;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesCheckboxViewHolder;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DevToolsManagerImpl extends DevToolsManager
{
    public boolean enableManualDispatch = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static DevToolsManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new DevToolsManagerImpl(context);
            }

            return (DevToolsManager) mInstance;
        }
    }

    protected DevToolsManagerImpl(Context context)
    {
        super(context);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void displayMenuConfig(FragmentActivity activity)
    {
        ConfigMenuEditorFragment.with(activity).display();
    }

    @Override
    public void generateMenu(final FragmentActivity activity, ViewGroup root)
    {
        TwoLinesViewHolder vh = HolderUtils.configure(root, R.layout.row_two_lines_borderless, "Menu Editor",
                "Tools to edit and manage menu and profiles", -1);

        ((ViewGroup) vh.icon.getParent().getParent()).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DevToolsManager.getInstance(activity).displayMenuConfig(activity);
            }
        });

        View v = LayoutInflater.from(root.getContext()).inflate(R.layout.row_two_lines_checkbox, root, false);
        final TwoLinesCheckboxViewHolder cvh = HolderUtils.configure(v, "Analytics RealTime",
                "Switch ON to enable faster realtime analytics", enableManualDispatch);
        root.addView(v);

        cvh.choose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                enableManualDispatch = cvh.choose.isChecked();
                if (AnalyticsManager.getInstance(activity) != null)
                {
                    ((GAnalyticsManagerImpl) AnalyticsManager.getInstance(activity))
                            .enableManualDispatch(cvh.choose.isChecked());
                }
            }
        });
    }

}
