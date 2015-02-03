/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.application.configuration;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.ViewGroupConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.builder.FragmentBuilderFactory;
import org.alfresco.mobile.android.ui.template.ViewTemplate;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainMenuConfigManager extends BaseConfigManager
{
    private static final String TAG = MainMenuConfigManager.class.getName();

    private ViewConfig rootMenuViewConfig;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public MainMenuConfigManager(Activity activity, ConfigService configService, ViewGroup vRoot)
    {
        super(activity, configService);
        if (configService != null)
        {
            String profileId = getCurrentProfile();
            if (profileId == null)
            {
                profileId = configService.getDefaultProfile().getIdentifier();
            }
            rootMenuViewConfig = configService.getViewConfig(configService.getProfile(profileId).getRootViewId(),
                    configManager.getCurrentScope());
        }
        this.vRoot = (ViewGroup) vRoot.findViewById(R.id.custom_menu_group);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GENERATION
    // ///////////////////////////////////////////////////////////////////////////
    public void createMenu()
    {
        vRoot.removeAllViews();
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
            fragmentBuilder.createMenuItem(buttonView);
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
}
