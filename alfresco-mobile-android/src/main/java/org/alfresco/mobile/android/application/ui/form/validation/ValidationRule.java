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

import java.lang.ref.WeakReference;

import org.alfresco.mobile.android.api.model.config.ValidationConfig;

import android.content.Context;

public abstract class ValidationRule
{
    protected WeakReference<Context> contextRef;

    protected ValidationConfig validationConfig;

    protected String errorMessage = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public ValidationRule(Context context, ValidationConfig configuration)
    {
        this.contextRef = new WeakReference<>(context);
        this.validationConfig = configuration;
        errorMessage = validationConfig.getErrorMessage();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean isValid(Object object)
    {
        return false;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected Context getContext()
    {
        return contextRef.get();
    }
}
