/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.mobile.android.platform.mdm;

import java.util.List;

import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.extensions.MobileIronManager;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.platform.utils.BundleUtils;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by jpascal on 26/02/2015.
 * 
 * @since 1.5
 */
public class MDMManager extends Manager implements MDMConstants
{
    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTANTS
    // ///////////////////////////////////////////////////////////////////////////
    protected MobileIronManager mobileIronManager;

    protected RestrictionsManager restrictionsManager;

    protected Bundle restrictions = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static MDMManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new MDMManager(context);
            }

            return (MDMManager) mInstance;
        }
    }

    protected MDMManager(Context applicationContext)
    {
        super(applicationContext);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasConfig()
    {
        // MobileIron enforce some configuration
        if (mobileIronManager != null) { return true; }

        // Android for Work doesn't enforce configuration
        return (restrictions != null && !restrictions.isEmpty());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void requestConfig(FragmentActivity activity, String applicationId)
    {
        // Which Provider ?
        if (MobileIronManager.getInstance(activity) != null)
        {
            // MobileIron Build
            mobileIronManager = MobileIronManager.getInstance(activity);
            mobileIronManager.requestConfig(activity, applicationId);
        }
        else if (AndroidVersion.isLollipopOrAbove())
        {
            // Android For Work
            restrictionsManager = (RestrictionsManager) activity.getSystemService(Context.RESTRICTIONS_SERVICE);
            restrictions = restrictionsManager.getApplicationRestrictions();
            if (restrictions != null && !restrictions.isEmpty())
            {
                EventBusManager.getInstance().post(new MDMEvent());
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SPECIFIC KEY
    // ///////////////////////////////////////////////////////////////////////////
    public String getAlfrescoURL()
    {
        return getConfig(MDMConstants.ALFRESCO_REPOSITORY_URL);
    }

    public String getUsername()
    {
        return getConfig(MDMConstants.ALFRESCO_USERNAME);
    }

    public String getShareURL()
    {
        return getConfig(MDMConstants.ALFRESCO_SHARE_URL);
    }

    public String getDescription()
    {
        return getConfig(MDMConstants.ALFRESCO_DISPLAY_NAME);
    }

    public String getProfile()
    {
        return getConfig(MDMConstants.ALFRESCO_USER_PROFILE);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONFIG
    // ///////////////////////////////////////////////////////////////////////////
    public void setConfig(Bundle b)
    {
        if (b == null) { return; }
        if (mobileIronManager != null)
        {
            mobileIronManager.setConfig(b);
        }
        else if (restrictions != null)
        {
            BundleUtils.addIfNotEmpty(restrictions, b);
        }
    }

    public String getConfig(String id)
    {
        if (mobileIronManager != null)
        {
            return (String) mobileIronManager.getConfig(id);
        }
        else if (restrictions != null)
        {
            return restrictions.getString(id);
        }
        else
        {
            return "";
        }
    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent)
    {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) { return null; }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
