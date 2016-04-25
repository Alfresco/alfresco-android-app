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
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.services.impl.AlfrescoServiceRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.sync.SyncContentManager;

import android.app.Activity;

/**
 * Created by jpascal on 25/04/2016.
 */
public class SyncCellularConfigFeature extends AbstractConfigFeature
{
    public SyncCellularConfigFeature(Activity appContext)
    {
        super(appContext);
    }

    @Override
    public String getFeatureType()
    {
        return FeatureConfig.FEATURE_CELLULAR_SYNC;
    }

    @Override
    public String getFeaturePrefix()
    {
        return FeatureConfig.FEATURE_CELLULAR_SYNC;
    }

    @Override
    public void check(AlfrescoSession session, AlfrescoAccount acc, FeatureConfig feature)
    {
        try
        {
            // Analytics
            if (session instanceof RepositorySession && session.getServiceRegistry() instanceof AlfrescoServiceRegistry)
            {
                ConfigService configService = ((AlfrescoServiceRegistry) session.getServiceRegistry())
                        .getConfigService();
                if (configService != null)
                {
                    if (feature == null && isProtected(acc))
                    {
                        // When server config has been removed we revert
                        userVisibileByConfig(acc);
                    }
                    else if (feature != null)
                    {
                        checkFeatureState(feature, acc);
                    }
                }
                else if (isProtected(acc))
                {
                    // When server config has been removed we revert user
                    // visibility
                    userVisibileByConfig(acc);
                }
            }
        }
        catch (Exception e)
        {
            // DO Nothing
        }
    }

    @Override
    public void checkFeatureState(FeatureConfig feature, AlfrescoAccount acc)
    {
        // When analytics enable via server config
        if (feature.isEnable() && !isEnable(acc))
        {
            userVisibileByConfig(acc);
        }
        // When analytics disable via server config
        else if (!feature.isEnable())
        {
            if (isEnable())
            {
                AnalyticsManager.getInstance(activity).reportEvent(AnalyticsManager.CATEGORY_SETTINGS,
                        AnalyticsManager.ACTION_SYNC_CELLULAR, AnalyticsManager.LABEL_DISABLE_BY_CONFIG, 1);
            }

            if (!isProtected(acc))
            {
                userProtectedByConfig(acc);
                SyncContentManager.getInstance(activity).setWifiOnlySync(acc, true);
            }
        }
    }
}
