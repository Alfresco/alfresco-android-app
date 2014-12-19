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

import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ValidationConfig;
/**
 * 
 * @author Jean Marie Pascal
 *
 */
public class ValidationConfigImpl extends ItemConfigImpl implements ValidationConfig
{
    private String errorMessage;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    ValidationConfigImpl(String identifier, String label, String type, String evaluatorId)
    {
        super(identifier, null, label, null, type, null, null);
    }

    ValidationConfigImpl(String identifier, String iconIdentifier, String label, String description, String type,
            Map<String, Object> properties, String errorId)
    {
        super(identifier, iconIdentifier, label, description, type, null, properties);
        this.errorMessage = errorId;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getErrorMessage()
    {
        return errorMessage;
    }
}
