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

import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.managers.ConfigManager;

import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;

public class BaseConfigManager implements ConfigurationConstant
{
    protected ViewGroup vRoot;

    protected WeakReference<FragmentActivity> activity;

    protected ConfigService configService;

    protected ConfigManager configManager;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public BaseConfigManager(FragmentActivity activity, ConfigService configService)
    {
        this.configManager = ConfigManager.getInstance(activity);
        this.configService = configService;
        this.activity = new WeakReference<>(activity);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILITY
    // ///////////////////////////////////////////////////////////////////////////
    public FragmentActivity getActivity()
    {
        return activity.get();
    }

    public ConfigScope getConfigScope()
    {
        return configManager.getCurrentScope();
    }
    
    public String getCurrentProfile()
    {
        return configManager.getCurrentProfileId();
    }

}
