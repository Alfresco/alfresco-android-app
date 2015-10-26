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
package org.alfresco.mobile.android.application.extension.hockeyapp;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.UpdateManager;

import org.alfresco.mobile.android.platform.extensions.HockeyAppManager;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

public class HockeyAppManagerImpl extends HockeyAppManager
{
    protected final String appID;
    
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static HockeyAppManagerImpl getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new HockeyAppManagerImpl(context);
            }

            return (HockeyAppManagerImpl) mInstance;
        }
    }

    protected HockeyAppManagerImpl(Context context)
    {
        super(context);
        appID = context.getString(R.string.hockeyapp_key);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public void checkForUpdates(FragmentActivity activity)
    {
        UpdateManager.register(activity, appID);
    }

    public void checkForCrashes(FragmentActivity activity)
    {
        CrashManager.register(activity, appID);
    }

    public void showFeedbackActivity(FragmentActivity activity)
    {
        FeedbackManager.register(activity, appID, null);
        FeedbackManager.showFeedbackActivity(activity);
    }

}
