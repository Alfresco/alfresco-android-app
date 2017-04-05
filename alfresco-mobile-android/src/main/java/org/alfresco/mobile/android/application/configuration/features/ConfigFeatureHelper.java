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

import java.util.List;

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.services.impl.AlfrescoServiceRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;

import android.app.Activity;

/**
 * Created by jpascal on 21/04/2016.
 */
public class ConfigFeatureHelper
{
    public static void check(Activity activity, AlfrescoAccount acc, AlfrescoSession session)
    {
        try
        {
            FeatureConfig passcodeFeature = null, cellularFeature = null, dataProtectionFeature = null,
                    syncSchedulerFeature = null;

            if (session instanceof RepositorySession && session.getServiceRegistry() instanceof AlfrescoServiceRegistry)
            {
                ConfigService configService = ((AlfrescoServiceRegistry) session.getServiceRegistry())
                        .getConfigService();
                if (configService != null)
                {
                    List<FeatureConfig> configs = configService.getFeatureConfig();

                    for (FeatureConfig feature : configs)
                    {
                        if (FeatureConfig.FEATURE_PASSCODE.equals(feature.getType()))
                        {
                            passcodeFeature = feature;
                        }
                        else if (FeatureConfig.FEATURE_CELLULAR_SYNC.equals(feature.getType()))
                        {
                            cellularFeature = feature;
                        }
                        else if (FeatureConfig.FEATURE_DATA_PROTECTION.equals(feature.getType()))
                        {
                            dataProtectionFeature = feature;
                        }
                        else if (FeatureConfig.FEATURE_SCHEDULER_SYNC.equals(feature.getType()))
                        {
                            syncSchedulerFeature = feature;
                        }
                    }
                }

                new SyncCellularConfigFeature(activity).check(session, acc, cellularFeature);
                new SyncSchedulerConfigFeature(activity).check(session, acc, syncSchedulerFeature);

                if (OnPremiseConstant.ALFRESCO_EDITION_ENTERPRISE.equals(session.getRepositoryInfo().getEdition()))
                {
                    new PasscodeConfigFeature(activity).check(session, acc, passcodeFeature);
                    new DataProtectionConfigFeature(activity).check(session, acc, dataProtectionFeature);
                }
            }
        }
        catch (Exception e)
        {
            // DO Nothing
        }
    }

}
