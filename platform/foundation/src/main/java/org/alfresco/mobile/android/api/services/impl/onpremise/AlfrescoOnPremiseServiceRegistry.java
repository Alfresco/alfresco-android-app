/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.api.services.impl.onpremise;

import java.io.File;
import java.util.Map;

import org.alfresco.mobile.android.api.model.impl.RepositoryVersionHelper;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.services.impl.AlfrescoServiceRegistry;
import org.alfresco.mobile.android.api.services.impl.LocalConfigServiceImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;

import android.util.Log;

/**
 * @author Jean Marie Pascal
 */
public class AlfrescoOnPremiseServiceRegistry extends OnPremiseServiceRegistry implements AlfrescoServiceRegistry
{
    private static final String TAG = AlfrescoOnPremiseServiceRegistry.class.getName();

    protected ConfigService configService;

    public AlfrescoOnPremiseServiceRegistry(AlfrescoSession session)
    {
        super(session);
    }

    @Override
    public void init()
    {
        boolean initConfiguration = true;
        if (session.getParameter(ConfigService.CONFIGURATION_INIT) != null
                && ConfigService.CONFIGURATION_INIT_NONE.equals(session.getParameter(ConfigService.CONFIGURATION_INIT)))
        {
            initConfiguration = false;
        }
        if (initConfiguration)
        {
            initConfigService();
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // / Available dependending Alfresco server version
    // ////////////////////////////////////////////////////////////////////////////////////
    public ConfigService initConfigService()
    {
        try
        {
            if (configService == null && RepositoryVersionHelper.isAlfrescoProduct(session))
            {
                this.configService = new OnPremiseConfigServiceImpl(session).load();
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return configService;
    }

    @Override
    public ConfigService getConfigService()
    {
        return configService;
    }

    public static ConfigService getConfigService(Map<String, Object> parameters)
    {
        File configFolder = null;
        if (parameters == null) { return null; }
        if (parameters.containsKey(ConfigService.CONFIGURATION_FOLDER))
        {
            configFolder = new File((String) parameters.get(ConfigService.CONFIGURATION_FOLDER));
        }
        return new LocalConfigServiceImpl(configFolder);
    }
}
