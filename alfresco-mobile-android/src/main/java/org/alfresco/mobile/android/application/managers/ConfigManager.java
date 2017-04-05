/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.managers;

import java.io.File;
import java.util.HashMap;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.ViewGroupConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.services.impl.LocalConfigServiceImpl;
import org.alfresco.mobile.android.api.services.impl.onpremise.OnPremiseConfigServiceImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.configuration.model.view.SyncConfigModel;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.configuration.ConfigurationEvent;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;

import com.squareup.otto.Subscribe;

import android.content.Context;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

public class ConfigManager extends Manager
{
    private EventBusManager eventBus;

    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    private HashMap<Long, ConfigService> currentService = new HashMap<>();

    private LongSparseArray<ConfigService> remoteConfigService = new LongSparseArray<>();

    private LongSparseArray<ConfigService> customConfigService = new LongSparseArray<>();

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

            if (SessionUtils.getSession(appContext) != null && configService instanceof LocalConfigServiceImpl)
            {
                ((LocalConfigServiceImpl) configService).setSession(SessionUtils.getSession(appContext));
            }

            if (configService != null)
            {
                currentService.put(acc.getId(), configService);
            }
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
        if (remoteConfigService != null && remoteConfigService.get(accountId) != null)
        {
            currentService.put(accountId, remoteConfigService.get(accountId));
        }
        else if (customConfigService != null && customConfigService.get(accountId) != null)
        {
            ((LocalConfigServiceImpl) customConfigService.get(accountId)).setSession(session);
            if (customConfigService.get(accountId) != null)
            {
                currentService.put(accountId, customConfigService.get(accountId));
            }
        }
        else
        {
            // We load the default embedded configuration
            if (embedConfigService == null)
            {
                embedConfigService = new LocalConfigServiceImpl(appContext);
            }
            ((LocalConfigServiceImpl) embedConfigService).setSession(session);
            if (embedConfigService != null)
            {
                currentService.put(accountId, embedConfigService);
            }

            // Horrible hack to reset profile sync activation as its not
            // implemented as feature
            if (hasSyncView(embedConfigService, "default"))
            {
                SyncContentManager.getInstance(appContext).setActivateSync(accountId, true);
            }
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
        if (!configFile.exists()) { return loadEmbedded(); }

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
        if (service == null) { return null; }
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
            case FEATURES:
                service = (service.getFeatureConfig() != null) ? service : null;
                break;
            case ACTIONS:
                service = (service.hasActionConfig()) ? service : null;
                break;
            default:
                break;
        }

        return service;
    }

    public ConfigService getConfig(long accountId)
    {
        // Double check we use the remote instead of local
        if (getRemoteConfig(accountId) != null && currentService != getRemoteConfig(accountId))
        {
            currentService.put(accountId, remoteConfigService.get(accountId));
        }
        return (currentService != null) ? currentService.get(accountId) : null;
    }

    public boolean hasConfig(long accountId)
    {
        return (currentService != null) && currentService.get(accountId) != null;
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
        return (customConfigService != null) && customConfigService.get(accountId) != null;
    }

    public ConfigService getRemoteConfig(long accountId)
    {
        return (remoteConfigService != null) ? remoteConfigService.get(accountId) : null;
    }

    public boolean hasRemoteConfig(long accountId)
    {
        return (remoteConfigService != null) && remoteConfigService.get(accountId) != null;
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
            eventBus.post(new ConfigurationProfileEvent(acc.getId()));
        }
        return true;
    }

    public boolean hasSyncView(long accountId, String profileId)
    {
        ConfigService config = getConfig(accountId);
        return hasSyncView(config, profileId);
    }

    private boolean hasSyncView(ConfigService config, String profileId)
    {
        return config.hasViewConfig() && parseViewConfigSearchingSyncView(
                config.getViewConfig(config.getProfile(profileId).getRootViewId(), new ConfigScope(profileId)));
    }

    private boolean parseViewConfigSearchingSyncView(ViewConfig viewConfig) {
        if (viewConfig instanceof ViewGroupConfig && ((ViewGroupConfig) viewConfig).getItems().size() > 0) {
            for (ViewConfig config : ((ViewGroupConfig) viewConfig).getItems()) {
                if (SyncConfigModel.TYPE_ID.equals(config.getType())) {
                    return true;
                }
                if (config instanceof ViewGroupConfig && ((ViewGroupConfig) config).getItems().size() > 0) {
                    parseViewConfigSearchingSyncView(config);
                }
            }
        }
        return false;
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
        if (loadCustom(account) != null)
        {
            currentService.put(account.getId(), loadCustom(account));
        }
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

    @Subscribe
    public void onSessionRequested(RequestSessionEvent event)
    {
        if (event.accountToLoad == null) { return; }
        if (hasConfig(event.accountToLoad.getId()))
        {
            currentService.remove(event.accountToLoad.getId());
            remoteConfigService.remove(event.accountToLoad.getId());
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

    public static class ConfigurationProfileEvent
    {
        public final long accountId;

        public ConfigurationProfileEvent(long accountId)
        {
            super();
            this.accountId = accountId;
        }
    }
}
