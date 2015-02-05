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
package org.alfresco.mobile.android.application.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.services.impl.LocalConfigServiceImpl;
import org.alfresco.mobile.android.api.services.impl.onpremise.OnPremiseConfigServiceImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.configuration.ConfigurationEvent;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;

import android.content.Context;
import android.text.TextUtils;

import com.squareup.otto.Subscribe;

public class ConfigManager extends Manager
{
    private EventBusManager eventBus;

    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    private Map<Long, ConfigService> currentService = new HashMap<Long, ConfigService>();

    private Map<Long, ConfigService> remoteConfigService = new HashMap<Long, ConfigService>();

    private Map<Long, ConfigService> customConfigService = new HashMap<Long, ConfigService>();

    private ConfigService embedConfigService;

    private String currentProfileId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    protected ConfigManager(Context applicationContext)
    {
        super(applicationContext);
        eventBus = EventBusManager.getInstance();
        eventBus.register(this);
    }

    public static ConfigManager getInstance(Context appContext)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new ConfigManager(appContext);
            }
            return (ConfigManager) mInstance;
        }
    }

    public void shutdown()
    {
        eventBus.unregister(this);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * We have 3 different configuration files :
     * <ul>
     * <li>Embededed : Default and defined by the application</li>
     * <li>Custom : Defined by the user</li>
     * <li>Remote : Defined by the server</li>
     * </ul>
     * Priority : Remote always wins ! Then custom then embedded
     *
     * @param acc
     */
    public void init(AlfrescoAccount acc)
    {
        // Load default embedded configuration
        // NB : Since 1.5 embedded configuration is mandatory
        if (embedConfigService == null)
        {
            embedConfigService = new LocalConfigServiceImpl(appContext);
        }

        // With no account we cant identify the right configuration.
        if (acc == null)
        {
            acc = AlfrescoAccountManager.getInstance(appContext).getDefaultAccount();
        }
        if (acc == null) { return; }

        // Load any remote or cached configuration
        try
        {
            ConfigService configService = loadCached(acc);
            if (configService == null)
            {
                ConfigService customConfigService = loadCustom(acc);
                configService = (customConfigService != null) ? customConfigService : embedConfigService;
            }
            currentService.put(acc.getId(), configService);

            // Config is available. Send an event
            if (configService.hasViewConfig())
            {
                eventBus.post(new ConfigurationMenuEvent(acc.getId()));
            }
        }
        catch (Exception e)
        {
            // Nothing to do
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // RETRIEVE CONFIGURATION
    // ///////////////////////////////////////////////////////////////////////////
    public void loadRemote(long accountId, ConfigService service)
    {
        // Check if the configuration is present
        if (service instanceof OnPremiseConfigServiceImpl)
        {
            remoteConfigService.put(accountId, service);
        }
    }

    public void setSession(long accountId, AlfrescoSession session)
    {
        if (remoteConfigService != null && remoteConfigService.containsKey(accountId))
        {
            currentService.put(accountId, remoteConfigService.get(accountId));
        }
        else if (customConfigService != null && customConfigService.containsKey(accountId))
        {
            ((LocalConfigServiceImpl) customConfigService.get(accountId)).setSession(session);
            currentService.put(accountId, customConfigService.get(accountId));
        }
        else
        {
            // We load the default embedded configuration
            if (embedConfigService == null)
            {
                embedConfigService = new LocalConfigServiceImpl(appContext);
            }
            ((LocalConfigServiceImpl) embedConfigService).setSession(session);
            currentService.put(accountId, embedConfigService);
        }
        currentProfileId = null;
        eventBus.post(new ConfigurationMenuEvent(accountId));
    }

    private ConfigService loadEmbedded()
    {
        // We load the default embedded configuration
        if (embedConfigService == null)
        {
            embedConfigService = new LocalConfigServiceImpl(appContext);
        }
        // Set Session explicitly
        ((LocalConfigServiceImpl) embedConfigService).setSession(SessionUtils.getSession(appContext));

        LoaderResult<ConfigService> result = new LoaderResult<ConfigService>();
        result.setData(embedConfigService);
        EventBusManager.getInstance().post(new ConfigurationEvent("", result, -1));

        return embedConfigService;
    }

    private ConfigService loadCustom(AlfrescoAccount account)
    {
        // We load the latest custom configuration file defined by the user
        File configFolder = AlfrescoStorageManager.getInstance(appContext).getCustomFolder(account);
        File configFile = new File(configFolder, ConfigConstants.CONFIG_FILENAME);

        // No file == No configuration
        if (!configFile.exists()) { return null; }

        ConfigService configService = new LocalConfigServiceImpl(configFolder, configFile);
        // Check if the configuration is present
        if (((LocalConfigServiceImpl) configService).hasConfiguration())
        {
            customConfigService.put(account.getId(), configService);
        }
        else
        {
            configService = null;
        }

        return configService;
    }

    private ConfigService loadCached(AlfrescoAccount account)
    {
        // We load the latest cached version of application configuration.
        File configFolder = AlfrescoStorageManager.getInstance(appContext).getConfigurationFolder(account);
        ConfigService configService = new LocalConfigServiceImpl(configFolder);

        // Check if the configuration is present
        if (((LocalConfigServiceImpl) configService).hasConfiguration())
        {
            remoteConfigService.put(account.getId(), configService);
        }
        else
        {
            configService = null;
        }

        return configService;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONFIG MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public ConfigService getConfig(long accountId, ConfigTypeIds id)
    {
        ConfigService service = getConfig(accountId);
        switch (id)
        {
            case VIEWS:
                service = (service.hasViewConfig()) ? service
                        : ((customConfigService.get(accountId) != null) ? customConfigService.get(accountId)
                                : embedConfigService);
                break;
            case FORMS:
                service = (service.hasFormConfig()) ? service : embedConfigService;
                break;
            case CREATION:
                service = (service.hasCreationConfig()) ? service : embedConfigService;
                break;
            case REPOSITORY:
                service = (service.getRepositoryConfig() != null) ? service : null;
                break;
            default:
                break;
        }

        return service;
    }

    public ConfigService getConfig(long accountId)
    {
        return (currentService != null) ? currentService.get(accountId) : null;
    }

    public boolean hasConfig(long accountId)
    {
        return (currentService != null) ? currentService.containsKey(accountId) : false;
    }

    public ConfigService getEmbeddedConfig()
    {
        return embedConfigService;
    }

    public ConfigService getCustomConfig(long accountId)
    {
        return (customConfigService != null) ? customConfigService.get(accountId) : null;
    }

    public boolean hasCustomConfig(long accountId)
    {
        return (customConfigService != null) ? customConfigService.containsKey(accountId) : false;
    }

    public ConfigService getRemoteConfig(long accountId)
    {
        return (remoteConfigService != null) ? remoteConfigService.get(accountId) : null;
    }

    public boolean hasRemoteConfig(long accountId)
    {
        return (remoteConfigService != null) ? remoteConfigService.containsKey(accountId) : false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // RETRIEVE CONFIG
    // ///////////////////////////////////////////////////////////////////////////
    public boolean swapProfile(AlfrescoAccount acc, String profileId)
    {
        ConfigService config = getConfig(acc.getId());
        currentProfileId = profileId;
        if (config.hasViewConfig())
        {
            eventBus.post(new ConfigurationMenuEvent(acc.getId()));
        }
        return true;
    }

    public ConfigScope getCurrentScope()
    {
        return (TextUtils.isEmpty(currentProfileId)) ? new ConfigScope(null) : new ConfigScope(currentProfileId);
    }

    public String getCurrentProfileId()
    {
        return currentProfileId;
    }

    public void loadAndUseCustom(AlfrescoAccount account)
    {
        currentService.put(account.getId(), loadCustom(account));
    }

    public void cleanCache(AlfrescoAccount account)
    {
        remoteConfigService.remove(account.getId());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onConfigContextEvent(ConfigurationEvent event)
    {
        if (event.hasException || event.data == null) { return; }

        if (event.data.hasViewConfig())
        {
            eventBus.post(new ConfigurationMenuEvent(event.accountId));
        }
    }

    public static class ConfigurationMenuEvent
    {
        public final long accountId;

        public ConfigurationMenuEvent(long accountId)
        {
            super();
            this.accountId = accountId;
        }
    }
}
