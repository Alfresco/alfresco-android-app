/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.platform;

import java.lang.reflect.Method;

import org.alfresco.mobile.android.platform.configuration.ConfigUtils;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

public final class ManagerFactory
{
    private static final String BOOLEAN = "bool";

    private static final String SEPARATOR = "_";

    private static final String ISENABLE = ConfigUtils.FAMILY_MANAGER.concat("_isEnable");

    private ManagerFactory()
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected static int getBooleanId(Context context, String key)
    {
        int boolId = context.getResources().getIdentifier(
                ISENABLE.concat(SEPARATOR).concat(key).replace(".", SEPARATOR), BOOLEAN, Manager.PACKAGE_NAME);
        return boolId;
    }

    protected static boolean isEnable(Context context, String key)
    {
        try
        {
            return context.getResources().getBoolean(getBooleanId(context, key));
        }
        catch (NotFoundException e)
        {
            return false;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FACTORY
    // ///////////////////////////////////////////////////////////////////////////
    public static Manager getManager(Context context, String managerName)
    {
        if (!isEnable(context, managerName)) { return null; }
        String s = ConfigUtils.getString(context, ConfigUtils.FAMILY_MANAGER, managerName);
        if (s == null)
        {
            Log.e("ApplicationManager", "Unable to retrieve Manager definition for : " + managerName);
            return null;
        }
        return createManager(s, context);
    }

    private static Manager createManager(String className, Context context)
    {
        Manager s = null;
        try
        {
            Class<?> c = Class.forName(className);
            Method method = c.getMethod("getInstance", Context.class);
            s = (Manager) method.invoke(null, context);
        }
        catch (Exception e)
        {
            // DO Nothing
            Log.e("ApplicationManager", "Error during Operation creation : " + className);
        }
        return s;
    }
}
