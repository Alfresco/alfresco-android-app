/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.manager;

import java.lang.reflect.Method;

import org.alfresco.mobile.android.application.R;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class ReportManager
{
    private ReportManager()
    {
    }

    private static final String CRASHMANAGER_CLASS = "net.hockeyapp.android.CrashManager";

    private static final String UPDATEMANAGER_CLASS = "net.hockeyapp.android.UpdateManager";

    private static final String METHOD_REGISTER = "register";

    public static void checkForCrashes(Activity context)
    {
        try
        {
            // Retrieve key from resource. If no key, do nothing.
            String appKey = (String) context.getText(R.string.hockeyapp_key);
            if (appKey == null || appKey.length() == 0) { return; }

            // Use reflection to retrieve Hockeyapp class an execute.
            Class<?> class1 = Class.forName(CRASHMANAGER_CLASS);
            Method method = class1.getMethod(METHOD_REGISTER, Context.class, String.class);
            method.invoke(null, context, appKey);
        }
        catch (Exception e)
        {
            // If error happens do nothing...
            Log.d("ReportManager", e.getMessage());
        }
    }

    public static void checkForUpdates(Activity context)
    {
        try
        {
            // Retrieve key from resource. If no key, do nothing.
            String appKey = (String) context.getText(R.string.hockeyapp_key);
            if (appKey == null || appKey.length() == 0) { return; }

            // Use reflection to retrieve Hockeyapp class an execute.
            Class<?> class1 = Class.forName(UPDATEMANAGER_CLASS);
            Method method = class1.getMethod(METHOD_REGISTER, Activity.class, String.class);
            method.invoke(null, context, appKey);
        }
        catch (Exception e)
        {
            // If error happens do nothing...
            Log.d("ReportManager", e.getMessage());
        }
    }

}
