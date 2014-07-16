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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.mobile.android.api.constants.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ValidationConfig;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class RegexValidationRule extends ValidationRule
{
    protected Pattern pattern;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public RegexValidationRule(Context context, ValidationConfig configuration)
    {
        super(context, configuration);
        String patternValue = (String) validationConfig.getParameter(ConfigConstants.PATTERN_VALUE);
        if (TextUtils.isEmpty(patternValue)) { return; }
        try
        {
            pattern = Pattern.compile(patternValue);
        }
        catch (Exception e)
        {
            Log.w("RegexValidationRule", "Pattern [" + patternValue + "] is invalid.");
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isValid(Object object)
    {
        if (pattern == null) { return false; }
        if (object instanceof String)
        {
            Matcher matcher = pattern.matcher((String) object);
            return matcher.matches();
        }
        else
        {
            return false;
        }
    }
}
