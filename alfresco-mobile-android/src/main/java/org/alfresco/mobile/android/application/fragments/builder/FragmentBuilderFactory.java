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
package org.alfresco.mobile.android.application.fragments.builder;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.alfresco.mobile.android.platform.configuration.ConfigUtils;

import androidx.fragment.app.FragmentActivity;
import android.util.Log;

public class FragmentBuilderFactory
{
    private static final String TAG = FragmentBuilderFactory.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // FACTORY
    // ///////////////////////////////////////////////////////////////////////////
    public static AlfrescoFragmentBuilder createViewConfig(FragmentActivity activity, String viewTemplate,
            Map<String, Object> configuration)
    {
        String s = ConfigUtils.getString(activity, ConfigUtils.FAMILY_VIEW, viewTemplate);
        if (s == null) {
            return null;
        }
        return createBaseViewConfig(s, activity, configuration);
    }

    // ////////////////////////////////////////////////////
    // CREATION
    // ////////////////////////////////////////////////////
    private static AlfrescoFragmentBuilder createBaseViewConfig(String className, FragmentActivity activity,
            Map<String, Object> configuration)
    {
        AlfrescoFragmentBuilder s = null;
        try
        {
            Constructor<?> t = Class.forName(className).getDeclaredConstructor(FragmentActivity.class, Map.class);
            s = (AlfrescoFragmentBuilder) t.newInstance(activity, configuration);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            Log.e(TAG, "Error during BaseViewConfig creation : " + className);
        }
        return s;
    }
}
