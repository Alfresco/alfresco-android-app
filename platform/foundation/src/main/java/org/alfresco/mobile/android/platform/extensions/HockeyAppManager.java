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
package org.alfresco.mobile.android.platform.extensions;

import org.alfresco.mobile.android.platform.Manager;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

public abstract class HockeyAppManager extends Manager
{
    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static HockeyAppManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = Manager.getInstance(context, HockeyAppManager.class.getSimpleName());
            }

            return (HockeyAppManager) mInstance;
        }
    }

    protected HockeyAppManager(Context applicationContext)
    {
        super(applicationContext);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public abstract void checkForUpdates(FragmentActivity activity);

    public abstract void checkForCrashes(FragmentActivity activity);

    public abstract void showFeedbackActivity(FragmentActivity activity);
}
