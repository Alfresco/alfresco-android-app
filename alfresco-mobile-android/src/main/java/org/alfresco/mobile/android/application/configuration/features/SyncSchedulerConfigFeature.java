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

import java.math.BigInteger;

import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.services.impl.AlfrescoServiceRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.sync.SyncContentManager;

import android.app.Activity;
import android.util.Log;

/**
 * Created by jpascal on 25/04/2016.
 */
public class SyncSchedulerConfigFeature extends AbstractConfigFeature
{
    public SyncSchedulerConfigFeature(Activity appContext)
    {
        super(appContext);
    }

    public static final String PARAM_INTERVAL = "interval";

    @Override
    public String getFeatureType()
    {
        return FeatureConfig.FEATURE_SCHEDULER_SYNC;
    }

    @Override
    public String getFeaturePrefix()
    {
        return FeatureConfig.FEATURE_SCHEDULER_SYNC;
    }

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
                    // When server config has been removed we revert analytics
                    // to true
                    userVisibileByConfig(acc);
                }
                else
                {
                    // embedded case
                    syncDefault(feature, acc);
                }
            }
            else
            {
                // embeded case
                syncDefault(feature, acc);
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
        Log.d("[SCHEDULER]", "Evaluation");
        BigInteger interval = (BigInteger) feature.getParameter(PARAM_INTERVAL);

        // When sync scheduler enable via server config
        if (feature.isEnable())
        {
            if (!isEnable())
            {
                enable();
            }
            SyncContentManager.getInstance(activity).syncPeriodically(acc, getIntervalInSeconds(interval).longValue());
            Log.d("[SCHEDULER]", interval.longValue() + "min");
        }
        // When analytics disable via server config
        else if (!feature.isEnable())
        {
            if (isEnable())
            {
                disable();
                syncDefault(feature, acc);
            }
            else
            {
                SyncContentManager.getInstance(activity).syncPeriodically(acc,
                        getIntervalInSeconds(interval).longValue());
                Log.d("[SCHEDULER]", interval.longValue() + "min");
            }
        }
    }

    private void syncDefault(FeatureConfig feature, AlfrescoAccount acc)
    {
        ConfigService configS = ConfigManager.getInstance(activity).getConfig(acc.getId(), ConfigTypeIds.FEATURES);
        BigInteger interval = BigInteger.valueOf(SyncContentManager.DEFAULT_INTERVAL);
        if (feature != null && feature.getParameter(PARAM_INTERVAL) != null)
        {
            // Feature config is defined in minutes
            interval = getIntervalInSeconds((BigInteger) feature.getParameter(PARAM_INTERVAL));
        }
        else if (configS != null)
        {
            for (FeatureConfig f : configS.getFeatureConfig())
            {
                if (FeatureConfig.FEATURE_SCHEDULER_SYNC.equals(f.getType()))
                {
                    interval = getIntervalInSeconds((BigInteger) f.getParameter(PARAM_INTERVAL));
                    break;
                }
            }
        }

        // Enforce min interval of 1 hour
        if (interval.longValue() <= SyncContentManager.MIN_INTERVAL)
        {
            interval = BigInteger.valueOf(SyncContentManager.MIN_INTERVAL);
        }

        SyncContentManager.getInstance(activity).syncPeriodically(acc, interval.longValue());
        Log.d("[SCHEDULER]", " Default Interval: " + interval);
    }

    private BigInteger getIntervalInSeconds(BigInteger interval)
    {
        long i = interval.longValue() * 60;
        return BigInteger.valueOf(i);
    }
}
