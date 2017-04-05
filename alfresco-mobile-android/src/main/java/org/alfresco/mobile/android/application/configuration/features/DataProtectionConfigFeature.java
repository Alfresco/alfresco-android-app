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

package org.alfresco.mobile.android.application.configuration.features;

import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;

import android.app.Activity;

/**
 * Created by jpascal on 25/04/2016.
 */
public class DataProtectionConfigFeature extends AbstractConfigFeature
{
    public DataProtectionConfigFeature(Activity appContext)
    {
        super(appContext);
    }

    @Override
    public String getFeatureType()
    {
        return FeatureConfig.FEATURE_DATA_PROTECTION;
    }

    @Override
    public String getFeaturePrefix()
    {
        return FeatureConfig.FEATURE_DATA_PROTECTION;
    }

    @Override
    public void checkFeatureState(FeatureConfig feature, AlfrescoAccount acc)
    {
        // When analytics enable via server config
        if (feature.isEnable() && !isProtected(acc))
        {
            userProtectedByConfig(acc);

            // Request Data Protection
            if (!DataProtectionManager.getInstance(activity).hasDataProtectionEnable())
            {
                DataProtectionManager.getInstance(activity).encrypt(acc);
            }

            AnalyticsManager.getInstance(activity).reportEvent(AnalyticsManager.CATEGORY_SETTINGS,
                    AnalyticsManager.ACTION_DATA_PROTECTION, AnalyticsManager.LABEL_ENABLE_BY_CONFIG, 1);
        }
        else if (!feature.isEnable() && isProtected(acc))
        {
            userVisibileByConfig(acc);

            AnalyticsManager.getInstance(activity).reportEvent(AnalyticsManager.CATEGORY_SETTINGS,
                    AnalyticsManager.ACTION_DATA_PROTECTION, AnalyticsManager.LABEL_DISABLE_BY_CONFIG, 1);
        }
    }

}
