/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
package org.alfresco.mobile.android.platform.utils;

import java.io.Serializable;

import android.os.Bundle;
import android.text.TextUtils;

public class BundleUtils
{

    public static void addIfNotNull(Bundle b, String key, Serializable value)
    {
        if (value != null)
        {
            b.putSerializable(key, value);
        }
    }

    public static void addIfNotEmpty(Bundle b, Bundle extra)
    {
        if (extra != null)
        {
            b.putAll(extra);
        }
    }

    public static void addIfNotEmpty(Bundle b, String key, String value)
    {
        if (!TextUtils.isEmpty(value))
        {
            b.putString(key, value);
        }
    }

    public static void addIfNotNull(Bundle b, String key, Integer value)
    {
        if (value != null)
        {
            b.putInt(key, value);
        }
    }

    public static String getString(Bundle arguments, String key)
    {
        if (arguments.containsKey(key))
        {
            return arguments.getString(key);
        }
        else
        {
            return null;
        }
    }

    public static Boolean getBoolean(Bundle arguments, String key)
    {
        if (arguments != null && arguments.containsKey(key))
        {
            return arguments.getBoolean(key);
        }
        else
        {
            return null;
        }
    }

    public static Long getLong(Bundle arguments, String key)
    {
        if (arguments.containsKey(key))
        {
            return arguments.getLong(key);
        }
        else
        {
            return null;
        }
    }

    public static Integer getInt(Bundle arguments, String key)
    {
        if (arguments.containsKey(key))
        {
            return arguments.getInt(key);
        }
        else
        {
            return null;
        }
    }

    public static Serializable getSerializable(Bundle arguments, String key)
    {
        if (arguments.containsKey(key))
        {
            return arguments.getSerializable(key);
        }
        else
        {
            return null;
        }
    }

}
