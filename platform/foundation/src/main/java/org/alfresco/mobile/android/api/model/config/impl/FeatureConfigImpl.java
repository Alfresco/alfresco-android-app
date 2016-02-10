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
package org.alfresco.mobile.android.api.model.config.impl;

import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

/**
 * @author Jean Marie Pascal
 */
public class FeatureConfigImpl extends ItemConfigImpl implements FeatureConfig
{
    private boolean isEnable = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    FeatureConfigImpl()
    {
        super();
    }

    FeatureConfigImpl(String identifier, String iconIdentifier, String label, String description, String type,
            Map<String, Object> properties)
    {
        super(identifier, iconIdentifier, label, description, type, null, properties);
    }

    static FeatureConfigImpl parse(Map<String, Object> json, ConfigurationImpl configuration)
    {
        ItemConfigData data = new ItemConfigData(JSONConverter.getString(json, ConfigConstants.ID_VALUE), json,
                configuration);
        FeatureConfigImpl featureConfig = new FeatureConfigImpl(data.identifier, data.iconIdentifier, data.label,
                data.description, data.type, data.properties);
        if (json.containsKey(ConfigConstants.ENABLE_VALUE))
        {
            featureConfig.isEnable = JSONConverter.getBoolean(json, ConfigConstants.ENABLE_VALUE);
        }
        return featureConfig;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isEnable()
    {
        return isEnable;
    }
}
