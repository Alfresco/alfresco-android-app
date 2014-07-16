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
package org.alfresco.mobile.android.application.ui.form.validation;

import java.lang.reflect.Constructor;

import org.alfresco.mobile.android.api.model.config.ValidationConfig;
import org.alfresco.mobile.android.platform.configuration.ConfigUtils;

import android.content.Context;
import android.util.Log;

public class ValidationRuleFactory
{
    private static final String TAG = ValidationRuleFactory.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // FACTORY
    // ///////////////////////////////////////////////////////////////////////////
    public static ValidationRule createValidationRule(Context context, ValidationConfig validationConfig)
    {
        String className = ConfigUtils.getString(context, ConfigUtils.FAMILY_VALIDATION, validationConfig.getType());
        if (className == null) { return null; }
        return createValidationRule(className, context, validationConfig);
    }

    // ////////////////////////////////////////////////////
    // CREATION
    // ////////////////////////////////////////////////////
    private static ValidationRule createValidationRule(String className, Context context, ValidationConfig validationConfig)
    {
        ValidationRule s = null;
        try
        {
            Constructor<?> t = Class.forName(className).getDeclaredConstructor(Context.class, ValidationConfig.class);
            s = (ValidationRule) t.newInstance(context, validationConfig);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            Log.e(TAG, "Error during ValidationRule creation : " + className);
        }
        return s;
    }
}
