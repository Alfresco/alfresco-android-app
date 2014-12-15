/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.api.model.config.impl;

import java.lang.ref.WeakReference;

/**
 * @author Jean Marie Pascal
 */
public class HelperConfig
{
    protected WeakReference<ConfigurationImpl> contextRef;

    protected WeakReference<HelperStringConfig> HelperStringRef;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    HelperConfig(ConfigurationImpl context, HelperStringConfig localHelper)
    {
        contextRef = new WeakReference<ConfigurationImpl>(context);
        HelperStringRef = new WeakReference<HelperStringConfig>(localHelper);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected ConfigurationImpl getConfiguration()
    {
        if (contextRef == null) { return null; }
        return contextRef.get();
    }

    protected boolean hasConfiguration()
    {
        if (contextRef == null) { return false; }
        return contextRef.get() != null;
    }

    protected boolean hasEvaluatorHelper()
    {
        if (contextRef == null) { return false; }
        return contextRef.get().getEvaluatorHelper() != null;
    }

    protected HelperEvaluatorConfig getEvaluatorHelper()
    {
        if (contextRef == null) { return null; }
        return contextRef.get().getEvaluatorHelper();
    }

    protected HelperStringConfig getLocaleHelper()
    {
        if (HelperStringRef == null) { return null; }
        return HelperStringRef.get();
    }

}
