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
package org.alfresco.mobile.android.application.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ConfigSource;
import org.alfresco.mobile.android.api.model.config.Configuration;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.config.async.ConfigurationEvent;
import org.alfresco.mobile.android.application.config.async.ConfigurationRequest;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.utils.MessengerUtils;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Subscribe;

public class ConfigManager extends Manager
{
    private EventBusManager eventBus;

    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    private Map<Long, Configuration> configContextMap = new HashMap<Long, Configuration>();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    protected ConfigManager(Context applicationContext)
    {
        super(applicationContext);
        eventBus = EventBusManager.getInstance();
        eventBus.register(this);
    }

    public static ConfigManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new ConfigManager(context);
            }
            return (ConfigManager) mInstance;
        }
    }

    public void shutdown()
    {
        eventBus.unregister(this);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONFIG MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public Configuration getConfig(long accountId)
    {
        if (configContextMap != null)
        {
            return configContextMap.get(accountId);
        }
        else
        {
            return null;
        }
    }

    public boolean hasConfig(long accountId)
    {
        if (configContextMap != null)
        {
            return configContextMap.containsKey(accountId);
        }
        else
        {
            return false;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // RETRIEVE CONFIG
    // ///////////////////////////////////////////////////////////////////////////
    public void init(AlfrescoAccount acc)
    {
        if (acc == null)
        {
            acc = AlfrescoAccountManager.getInstance(appContext).getDefaultAccount();
        }
        if (acc == null) { return; }
        try
        {
            File configFolder = AlfrescoStorageManager.getInstance(appContext).getConfigurationFolder(acc);
            Configuration configContext = Configuration.load(new ConfigSource(appContext.getPackageName(), null), configFolder);
            configContextMap.put(acc.getId(), configContext);
            if (configContext.hasLayoutConfig())
            {
                eventBus.post(new ConfigurationMenuEvent(acc.getId()));
            }
        }
        catch (Exception e)
        {

        }
    }

    /**
     * Load the default configuration file from Repository
     * 
     * @param alfrescoSession
     */
    public boolean load(AlfrescoSession alfrescoSession)
    {
        if (alfrescoSession == null) { return false; }
        if (alfrescoSession.getServiceRegistry().getConfigService().hasConfig())
        {
            MessengerUtils.showToast(appContext, "Loading Configuration");
            Operator.with(appContext).load(new ConfigurationRequest.Builder());
            return true;
        }
        return false;
    }

    public boolean load(AlfrescoAccount acc, String profileId)
    {
        Configuration config = getConfig(acc.getId());
        config.swapProfile(profileId);
        if (config.hasLayoutConfig())
        {
            eventBus.post(new ConfigurationMenuEvent(acc.getId()));
        }
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onConfigContextEvent(ConfigurationEvent event)
    {
        if (event.hasException || event.data == null)
        {
            MessengerUtils.showToast(appContext, "No Configuration");
            return;
        }
        else
        {
            MessengerUtils.showToast(appContext, "Configuration Available");
        }

        configContextMap.put(event.accountId, event.data);

        if (event.data.hasLayoutConfig())
        {
            Log.d("EVENT", "Post onConfigurationEvent");
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
