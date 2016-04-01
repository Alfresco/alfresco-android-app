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
package org.alfresco.mobile.android.application.mdm.mobileiron;

import java.net.MalformedURLException;
import java.net.URL;

import org.alfresco.mobile.android.platform.exception.AlfrescoAppException;
import org.alfresco.mobile.android.platform.extensions.MobileIronManager;
import org.alfresco.mobile.android.platform.mdm.MDMManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

public class MobileIronManagerImpl extends MobileIronManager
{
    private static final String TAG = MobileIronManagerImpl.class.getSimpleName();

    public static final String WRAPPED_KEY = "com.mobileiron.wrapped";

    private Bundle configBundle;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static MobileIronManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new MobileIronManagerImpl(context);
            }

            return (MobileIronManager) mInstance;
        }
    }

    protected MobileIronManagerImpl(Context context)
    {
        super(context);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public void requestConfig(FragmentActivity activity, String applicationId)
    {
        try
        {
            Intent intent = new Intent("com.mobileiron.REQUEST_CONFIG");
            intent.putExtra("packageName", applicationId);
            activity.startService(MDMManager.createExplicitFromImplicitIntent(activity, intent));
        }
        catch (Exception e)
        {
            Log.i(TAG, "Error during request config");
        }
    }

    @Override
    public boolean isWrapped()
    {
        try
        {
            return Boolean.parseBoolean(System.getProperty(WRAPPED_KEY, "false"));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public void setConfig(Bundle b)
    {
        configBundle = new Bundle(b);

        // Verification : Configuration presents ?
        for (int i = 0; i < MANDATORUY_CONFIGURATION_KEYS.length; i++)
        {
            if (!b.containsKey(MANDATORUY_CONFIGURATION_KEYS[i])) { throw new AlfrescoAppException(
                    MANDATORUY_CONFIGURATION_KEYS[i] + " parameter is missing."); }
        }

        // Next configuration respect the format

        // REPOSITORY URL
        String repositoryURL = (String) b.get(ALFRESCO_REPOSITORY_URL);
        if (TextUtils.isEmpty(
                repositoryURL)) { throw new AlfrescoAppException(ALFRESCO_REPOSITORY_URL + " can't be empty."); }

        URL u = null;
        try
        {
            u = new URL(repositoryURL);
        }
        catch (MalformedURLException e)
        {
            throw new AlfrescoAppException(ALFRESCO_REPOSITORY_URL + " seems to be wrong.");
        }

        // USERNAME
        String username = (String) b.get(ALFRESCO_USERNAME);
        if (TextUtils.isEmpty(username)) { throw new AlfrescoAppException(ALFRESCO_USERNAME + " can't be empty."); }
    }

    @Override
    public Object getConfig(String id)
    {
        if (configBundle == null) { return null; }
        return configBundle.get(id);
    }

}
