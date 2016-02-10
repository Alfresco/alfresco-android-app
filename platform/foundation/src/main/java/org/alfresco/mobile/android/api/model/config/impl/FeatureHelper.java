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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

/**
 * @author Jean Marie Pascal
 */
public class FeatureHelper extends HelperConfig
{
    private ArrayList<FeatureConfig> featureConfigs;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    FeatureHelper(ConfigurationImpl context, StringHelper localHelper)
    {
        super(context, localHelper);
    }

    boolean addFeatureConfig(List<Object> features)
    {
        if (features == null || features.isEmpty()) { return false; }
        featureConfigs = new ArrayList<>(features.size());
        FeatureConfig featureConfig = null;
        for (Object featureData : features)
        {
            featureConfig = FeatureConfigImpl.parse(JSONConverter.getMap(featureData), getConfiguration());
            featureConfigs.add(featureConfig);
        }
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public List<FeatureConfig> getFeatures()
    {
        if (featureConfigs == null) { return new ArrayList<>(); }
        return featureConfigs;
    }
}
