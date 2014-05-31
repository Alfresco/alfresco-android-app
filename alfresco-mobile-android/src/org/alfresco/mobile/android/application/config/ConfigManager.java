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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ConfigContext;
import org.alfresco.mobile.android.application.config.async.ConfigContextEvent;
import org.alfresco.mobile.android.application.config.async.ConfigContextRequest;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.utils.MessengerUtils;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Subscribe;

public class ConfigManager extends Manager
{
    private EventBusManager eventBus;

    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    private Map<Long, ConfigContext> configContextMap = new HashMap<Long, ConfigContext>();

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
    public ConfigContext getConfig(long accountId)
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
    /**
     * Load the default configuration file from Repository
     */
    public void load()
    {
        //
        MessengerUtils.showToast(appContext, "Loading Configuration");
        Operator.with(appContext).load(new ConfigContextRequest.Builder());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onConfigContextEvent(ConfigContextEvent event)
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
