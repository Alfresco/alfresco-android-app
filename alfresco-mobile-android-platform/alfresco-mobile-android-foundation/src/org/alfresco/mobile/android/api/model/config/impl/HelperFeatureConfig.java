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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
/**
 * 
 * @author Jean Marie Pascal
 *
 */
public class HelperFeatureConfig extends HelperConfig
{
    private LinkedHashMap<String, FeatureConfig> featureIndex;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    HelperFeatureConfig(ConfigurationImpl context, HelperStringConfig localHelper)
    {
        super(context, localHelper);
    }

    HelperFeatureConfig(ConfigurationImpl context, HelperStringConfig localHelper,
            LinkedHashMap<String, FeatureConfig> featureIndex)
    {
        super(context, localHelper);
        this.featureIndex = featureIndex;
    }

    public boolean addFeatures(List<Object> featuresMap)
    {
        if (featuresMap == null || featuresMap.isEmpty()){return false;}
        featureIndex = new LinkedHashMap<String, FeatureConfig>(featuresMap.size());
        FeatureConfig featureConfig = null;
        for (Object object : featuresMap)
        {
            featureConfig = FeatureConfigImpl.parse(JSONConverter.getMap(object), getConfiguration());
            if (featureConfig == null)
            {
                continue;
            }
            featureIndex.put(featureConfig.getIdentifier(), featureConfig);
        }
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public FeatureConfig getFeatureConfigById(String profileId)
    {
        if (featureIndex == null) { return null; }
        return (featureIndex.containsKey(profileId)) ? featureIndex.get(profileId) : null;
    }

    public List<FeatureConfig> getFeatureConfig()
    {
        return (featureIndex != null) ? new ArrayList<FeatureConfig>(featureIndex.values())
                : new ArrayList<FeatureConfig>(0);
    }
    
    
    public List<FeatureConfig> getFeatureConfig(ConfigScope scope)
    {
        //TODO ConfigScope !
        return (featureIndex != null) ? new ArrayList<FeatureConfig>(featureIndex.values())
                : new ArrayList<FeatureConfig>(0);
    }
}
