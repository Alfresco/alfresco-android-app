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
package org.alfresco.mobile.android.application.config.form;

import java.lang.reflect.Constructor;

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.platform.configuration.ConfigUtils;

import android.content.Context;
import android.util.Log;

public class FieldTypeFactory
{
    private static final String TAG = FieldTypeFactory.class.getName();

    public static final String DEFAULT_FIELD = "com.alfresco.client.field.text";
    
    // ///////////////////////////////////////////////////////////////////////////
    // FACTORY
    // ///////////////////////////////////////////////////////////////////////////
    public static FieldTypeBuilder createFieldControlType(Context context, Property property, FieldConfig fieldConfig)
    {
        String className = ConfigUtils.getString(context, ConfigUtils.FAMILY_FORM, fieldConfig.getType());
        if (className == null) { return null; }
        return createFieldControlType(className, context, property, fieldConfig);
    }
    
    public static FieldTypeBuilder createFieldControlType(Context context, String fieldType, Property property, FieldConfig fieldConfig)
    {
        String className = ConfigUtils.getString(context, ConfigUtils.FAMILY_FORM, fieldType);
        if (className == null) { return null; }
        return createFieldControlType(className, context, property, fieldConfig);
    }

    // ////////////////////////////////////////////////////
    // CREATION
    // ////////////////////////////////////////////////////
    private static FieldTypeBuilder createFieldControlType(String className, Context context, Property property, FieldConfig fieldConfig)
    {
        FieldTypeBuilder s = null;
        try
        {
            Constructor<?> t = Class.forName(className).getDeclaredConstructor(Context.class, Property.class, FieldConfig.class);
            s = (FieldTypeBuilder) t.newInstance(context, property, fieldConfig);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            Log.e(TAG, "Error during FieldControlTypeBuilder creation : " + className);
        }
        return s;
    }
}
