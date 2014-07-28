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
package org.alfresco.mobile.android.application.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.config.ActionConfig;
import org.alfresco.mobile.android.api.model.config.ConfigInfo;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.model.config.CreationConfig;
import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.alfresco.mobile.android.api.model.config.FormConfig;
import org.alfresco.mobile.android.api.model.config.MenuConfig;
import org.alfresco.mobile.android.api.model.config.ProcessConfig;
import org.alfresco.mobile.android.api.model.config.ProfileConfig;
import org.alfresco.mobile.android.api.model.config.RepositoryConfig;
import org.alfresco.mobile.android.api.model.config.SearchConfig;
import org.alfresco.mobile.android.api.model.config.TaskConfig;
import org.alfresco.mobile.android.api.model.config.ThemeConfig;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.impl.ConfigInfoImpl;
import org.alfresco.mobile.android.api.model.config.impl.ConfigurationImpl;
import org.alfresco.mobile.android.api.model.config.impl.HelperStringConfig;
import org.alfresco.mobile.android.api.services.impl.ConfigServiceImpl;
import org.alfresco.mobile.android.api.utils.JsonUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
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
public class LocalConfigServiceImpl extends ConfigServiceImpl
{
    private static final String TAG = LocalConfigServiceImpl.class.getSimpleName();

    private ConfigurationImpl configuration;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public LocalConfigServiceImpl(Context context, AlfrescoAccount account)
    {
        super();
        this.configuration = load(context, account);
    }

    private static ConfigurationImpl load(Context context, AlfrescoAccount account)
    {
        // Create the configuration Object
        ConfigurationImpl config = null;
        try
        {

            File configFile = null, localizedFile = null;

            // CONFIGURATION
            long lastUpdate = context.getPackageManager().getPackageInfo(
                    context.getApplicationContext().getPackageName(), 0).lastUpdateTime;

            File assetFolder = AlfrescoStorageManager.getInstance(context).getConfigurationFolder(account);
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
                String assetfilePath = context.getString(R.string.config_asset_messages_path) + localizedName;
                org.alfresco.mobile.android.api.utils.IOUtils.copyFile(context.getAssets().open(assetfilePath),
                        localizedFile);
            }
            HelperStringConfig stringConfig = HelperStringConfig.load(localizedFile);

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
        if (configuration == null) { return null; }
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
    // FEATURE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public List<FeatureConfig> getFeatureConfig()
    {
        return (configuration == null) ? null : configuration.getFeatureConfig();
    }

    @Override
    public List<FeatureConfig> getFeatureConfig(ConfigScope scope)
    {
        if (configuration == null) { return null; }
        return configuration.getFeatureConfig(scope);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public List<MenuConfig> getMenuConfig(String menuId)
    {
        if (configuration == null) { return null; }
        return configuration.getMenuConfig(menuId);
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
        if (configuration == null) { return false; }
        return configuration.hasViewConfig();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FORMS
    // ///////////////////////////////////////////////////////////////////////////
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
    // WORKFLOW
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public List<ProcessConfig> getProcessConfig()
    {
        if (configuration == null) { return null; }
        return configuration.getProcessConfig();
    }

    @Override
    public List<TaskConfig> getTaskConfig()
    {
        if (configuration == null) { return null; }
        return configuration.getTaskConfig();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATION
    // ///////////////////////////////////////////////////////////////////////////
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
    // ACTION
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public List<ActionConfig> getActionConfig(String groupId, Node node)
    {
        if (configuration == null) { return null; }
        return configuration.getActionConfig(groupId, node);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SEARCH
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public SearchConfig getSearchConfig(Node node)
    {
        if (configuration == null) { return null; }
        return configuration.getSearchConfig(node);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // THEME
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public ThemeConfig getThemeConfig()
    {
        if (configuration == null) { return null; }
        return configuration.getThemeConfig();
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
