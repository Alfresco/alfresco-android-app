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
import org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by jpascal on 25/04/2016.
 */
public class PasscodeConfigFeature extends AbstractConfigFeature
{
    public PasscodeConfigFeature(Activity appContext)
    {
        super(appContext);
    }

    @Override
    public String getFeatureType()
    {
        return FeatureConfig.FEATURE_PASSCODE;
    }

    @Override
    public String getFeaturePrefix()
    {
        return FeatureConfig.FEATURE_PASSCODE;
    }

    @Override
    public void checkFeatureState(FeatureConfig feature, AlfrescoAccount acc)
    {
        // When analytics enable via server config
        if (feature.isEnable() && !isProtected(acc))
        {
            userProtectedByConfig(acc);

            // Request Passcode Definition

            AnalyticsManager.getInstance(activity).reportEvent(AnalyticsManager.CATEGORY_SETTINGS,
                    AnalyticsManager.ACTION_PASSCODE, AnalyticsManager.LABEL_ENABLE_BY_CONFIG, 1);
        }
        else if (!feature.isEnable() && isProtected(acc))
        {
            userVisibileByConfig(acc);

            AnalyticsManager.getInstance(activity).reportEvent(AnalyticsManager.CATEGORY_SETTINGS,
                    AnalyticsManager.ACTION_PASSCODE, AnalyticsManager.LABEL_DISABLE_BY_CONFIG, 1);
        }

        if (isProtected() && !PasscodePreferences.hasPasscode(activity))
        {
            Intent i = new Intent(activity, PassCodeActivity.class);
            i.setAction(PassCodeActivity.REQUEST_DEFINE_PASSCODE);
            activity.startActivityForResult(i, PassCodeActivity.REQUEST_CODE_PASSCODE);
        }
    }

}
