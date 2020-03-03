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
package org.alfresco.mobile.android.application.configuration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.ViewGroupConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.builder.FragmentBuilderFactory;
import org.alfresco.mobile.android.ui.template.ViewTemplate;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainMenuConfigManager extends BaseConfigManager
{
    private static final String TAG = MainMenuConfigManager.class.getName();

    private ViewConfig rootMenuViewConfig;

    private HashMap<String, List<WeakReference<Button>>> menuIndex = new HashMap<>();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public MainMenuConfigManager(FragmentActivity activity, ConfigService configService, ViewGroup vRoot)
    {
        super(activity, configService);
        if (configService != null)
        {
            String profileId = getCurrentProfile();
            if (profileId == null)
            {
                profileId = configService.getDefaultProfile().getIdentifier();
            }
            if (configService.getProfile(profileId) != null)
            {
                rootMenuViewConfig = configService.getViewConfig(configService.getProfile(profileId).getRootViewId(),
                        configManager.getCurrentScope());
            }
        }
        this.vRoot = (ViewGroup) vRoot.findViewById(R.id.custom_menu_group);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GENERATION
    // ///////////////////////////////////////////////////////////////////////////
    public void createMenu()
    {
        if (rootMenuViewConfig == null)
        {
            vRoot.removeAllViews();
            vRoot.invalidate();
            return;
        }
        vRoot.removeAllViews();
        vRoot.invalidate();
        createMenu(rootMenuViewConfig, vRoot, LayoutInflater.from(getActivity()));
    }

    private void createMenu(ViewConfig viewConfig, ViewGroup hookView, LayoutInflater li)
    {
        TextView header = null;

        // CREATION
        if (viewConfig instanceof ViewGroupConfig && ((ViewGroupConfig) viewConfig).getItems().size() > 0)
        {
            // Header
            if (!TextUtils.isEmpty(viewConfig.getLabel()))
            {
                header = (TextView) li.inflate(R.layout.app_main_menu_header, hookView, false);
                header.setId(UIUtils.generateViewId());
                header.setText(viewConfig.getLabel());
                hookView.addView(header);
            }

            // Add Children
            for (ViewConfig config : ((ViewGroupConfig) viewConfig).getItems())
            {
                createMenu(config, hookView, li);
            }
        }
        else
        {
            Button menuItem = (Button) li.inflate(R.layout.app_main_menu_item, hookView, false);
            menuItem.setId(UIUtils.generateViewId());
            configureView(viewConfig, menuItem);
            hookView.addView(menuItem);
            addViewIntoIndex(viewConfig.getType(), menuItem);
        }
    }

    private void configureView(ViewConfig config, Button buttonView)
    {
        try
        {
            Map<String, Object> parameters = config.getParameters();
            String label = config.getLabel();
            if (!TextUtils.isEmpty(label))
            {
                if (parameters == null)
                {
                    parameters = new HashMap<String, Object>(1);
                }
                parameters.put(ViewTemplate.ARGUMENT_LABEL, label);
            }
            AlfrescoFragmentBuilder fragmentBuilder = FragmentBuilderFactory.createViewConfig(getActivity(),
                    config.getType(), parameters);
            fragmentBuilder.createMenuItem(config, buttonView);
            if (!TextUtils.isEmpty(label))
            {
                buttonView.setText(label);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            Log.e(TAG, "Error during menu creation : " + config.getIdentifier());
        }
    }

    private void addViewIntoIndex(String typeId, Button button)
    {
        if (menuIndex.containsKey(typeId))
        {
            menuIndex.get(typeId).add(new WeakReference<>(button));
        }
        else
        {
            ArrayList<WeakReference<Button>> values = new ArrayList<>();
            values.add(new WeakReference<>(button));
            menuIndex.put(typeId, values);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public List<View> getViewsByType(String type)
    {
        List<View> views = new ArrayList<>(0);
        if (menuIndex.containsKey(type))
        {
            for (WeakReference<Button> b : menuIndex.get(type))
            {
                views.add(b.get());
            }
        }
        return views;
    }
}
