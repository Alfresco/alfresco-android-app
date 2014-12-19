/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.api.model.config.impl;

import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ProfileConfig;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
/**
 * 
 * @author Jean Marie Pascal
 *
 */
public class ProfileConfigImpl extends BaseConfigImpl implements ProfileConfig
{
    private boolean isDefault = false;

    private String rootViewId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    ProfileConfigImpl(BaseConfigData data)
    {
        super(data.identifier, data.label, data.description);
    }

    static ProfileConfig parse(String identifier, Map<String, Object> json, ConfigurationImpl configuration)
    {
        BaseConfigData data = new BaseConfigData(identifier, json, configuration);
        ProfileConfigImpl profileConfig = new ProfileConfigImpl(data);

        if (json.containsKey(ConfigConstants.DEFAULT_VALUE))
        {
            profileConfig.isDefault = JSONConverter.getBoolean(json, ConfigConstants.DEFAULT_VALUE);
        }

        profileConfig.rootViewId = JSONConverter.getString(json, ConfigConstants.ROOTVIEW_ID_VALUE);

        return profileConfig;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isDefault()
    {
        return isDefault;
    }

    @Override
    public String getRootViewId()
    {
        return rootViewId;
    }
}
