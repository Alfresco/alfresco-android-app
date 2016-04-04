/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.api.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ActionConfig;
import org.alfresco.mobile.android.api.model.config.ConfigInfo;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.model.config.CreationConfig;
import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.alfresco.mobile.android.api.model.config.FormConfig;
import org.alfresco.mobile.android.api.model.config.ProfileConfig;
import org.alfresco.mobile.android.api.model.config.RepositoryConfig;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.impl.ConfigInfoImpl;
import org.alfresco.mobile.android.api.model.config.impl.ConfigurationImpl;
import org.alfresco.mobile.android.api.model.config.impl.StringHelper;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.utils.JsonUtils;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;

/**
 * Retrieve information offline
 * 
 * @author Jean Marie Pascal
 */
public class LocalConfigServiceImpl implements ConfigService
{
    private static final String TAG = LocalConfigServiceImpl.class.getSimpleName();

    private ConfigurationImpl configuration;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public LocalConfigServiceImpl()
    {
    }

    public LocalConfigServiceImpl(File configFolder)
    {
        this.configuration = ConfigurationImpl.load(null, null, configFolder);
    }

    public LocalConfigServiceImpl(File configFolder, File configFile)
    {
        this.configuration = ConfigurationImpl.load(configFile, null, configFolder);
    }

    public LocalConfigServiceImpl(Context context)
    {
        super();
        this.configuration = load(context);
    }

    private static ConfigurationImpl load(Context context)
    {
        // Create the configuration Object
        ConfigurationImpl config = null;
        try
        {

            File configFile = null, localizedFile = null;

            // CONFIGURATION
            long lastUpdate = context.getPackageManager()
                    .getPackageInfo(context.getApplicationContext().getPackageName(), 0).lastUpdateTime;

            File assetFolder = AlfrescoStorageManager.getInstance(context).getConfigurationFolder();
            String configName = context.getString(R.string.config_asset_prefix) + "_"
                    + context.getString(R.string.config_asset_file);
            configFile = new File(assetFolder, configName);

            if (!configFile.exists() || configFile.lastModified() < lastUpdate)
            {
                String assetfilePath = context.getString(R.string.config_asset_path) + configName;
                org.alfresco.mobile.android.api.utils.IOUtils.copyFile(context.getAssets().open(assetfilePath),
                        configFile);
            }

            // LOCALIZATION
            String localizedName = context.getString(R.string.asset_folder_prefix) + "_"
                    + context.getString(R.string.config_asset_messages_file);
            localizedFile = new File(assetFolder, localizedName);

            if (!localizedFile.exists() || localizedFile.lastModified() < lastUpdate)
            {
                try
                {
                    org.alfresco.mobile.android.api.utils.IOUtils.copyFile(
                            context.getAssets()
                                    .open(context.getString(R.string.config_asset_messages_path) + localizedName),
                            localizedFile);
                }
                catch (IOException e)
                {
                    // No messages file. we ignore translation
                }
            }
            StringHelper stringConfig = StringHelper.load(localizedFile);

            // Try to retrieve configuration data
            FileInputStream inputStream = new FileInputStream(configFile);
            Map<String, Object> json = JsonUtils.parseObject(inputStream, "UTF-8");

            // Try to retrieve the configInfo if present
            ConfigInfo info = null;
            if (json.containsKey(ConfigTypeIds.INFO.value()))
            {
                info = ConfigInfoImpl.parseJson(JSONConverter.getMap(json.get(ConfigTypeIds.INFO.value())));
            }

            // Finally create the configuration
            config = ConfigurationImpl.parseJson(null, json, info, stringConfig);
            if (AlfrescoAccountManager.getInstance(context).getDefaultAccount() != null)
            {
                config.setPersonId(AlfrescoAccountManager.getInstance(context).getDefaultAccount().getUsername());
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return config;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INFO
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasConfiguration()
    {
        return (configuration != null);
    }

    @Override
    public ConfigInfo getConfigInfo()
    {
        if (configuration == null) { return null; }
        return configuration.getConfigInfo();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PROFILES
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public List<ProfileConfig> getProfiles()
    {
        if (configuration == null) { return new ArrayList<>(0); }
        return configuration.getProfiles();
    }

    @Override
    public ProfileConfig getDefaultProfile()
    {
        if (configuration == null) { return null; }
        return configuration.getDefaultProfile();
    }

    @Override
    public ProfileConfig getProfile(String profileId)
    {
        if (configuration == null) { return null; }
        return configuration.getProfile(profileId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REPOSITORY
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public RepositoryConfig getRepositoryConfig()
    {
        return (configuration == null) ? null : configuration.getRepositoryConfig();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEWS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public ViewConfig getViewConfig(String viewId, ConfigScope scope)
    {
        if (configuration == null) { return null; }
        return configuration.getViewConfig(viewId, scope);
    }

    @Override
    public ViewConfig getViewConfig(String viewId)
    {
        if (configuration == null) { return null; }
        return configuration.getViewConfig(viewId);
    }

    @Override
    public boolean hasViewConfig()
    {
        return configuration != null && configuration.hasViewConfig();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEWS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public ActionConfig getActionConfig(String viewId, ConfigScope scope)
    {
        if (configuration == null) { return null; }
        return configuration.getActionConfig(viewId, scope);
    }

    @Override
    public ActionConfig getActionConfig(String viewId)
    {
        if (configuration == null) { return null; }
        return configuration.getActionConfig(viewId);
    }

    @Override
    public boolean hasActionConfig()
    {
        return configuration != null && configuration.hasActionConfig();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FORMS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean hasFormConfig()
    {
        return configuration != null && configuration.hasFormConfig();
    }

    @Override
    public FormConfig getFormConfig(String formId)
    {
        if (configuration == null) { return null; }
        return configuration.getFormConfig(formId, null);
    }

    @Override
    public FormConfig getFormConfig(String formId, ConfigScope scope)
    {
        if (configuration == null) { return null; }
        return configuration.getFormConfig(formId, scope);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATION
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean hasCreationConfig()
    {
        return configuration != null && configuration.getCreationConfig() != null;
    }

    @Override
    public CreationConfig getCreationConfig(ConfigScope scope)
    {
        if (configuration == null) { return null; }
        return configuration.getCreationConfig(scope);
    }

    @Override
    public CreationConfig getCreationConfig()
    {
        if (configuration == null) { return null; }
        return configuration.getCreationConfig();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FEATURE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean hasFeatureConfig()
    {
        return configuration != null && configuration.getFeatureConfig() != null;
    }

    @Override
    public List<FeatureConfig> getFeatureConfig()
    {
        if (configuration == null) { return new ArrayList<>(0); }
        return configuration.getFeatureConfig();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SESSION
    // ///////////////////////////////////////////////////////////////////////////
    public void setSession(AlfrescoSession session)
    {
        if (configuration == null) { return; }
        configuration.setSession(session);
    }

    // ////////////////////////////////////////////////////
    // CACHING
    // ////////////////////////////////////////////////////
    public void clear()
    {
        // Must be implemented in subclass.
    }

    // ////////////////////////////////////////////////////
    // Save State - serialization / deserialization
    // ////////////////////////////////////////////////////
    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int arg1)
    {
    }
}
