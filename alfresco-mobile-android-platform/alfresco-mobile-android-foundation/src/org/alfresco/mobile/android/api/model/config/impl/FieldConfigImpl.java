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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.api.model.config.ValidationConfig;

/**
 * @author Jean Marie Pascal
 */
public class FieldConfigImpl extends ItemConfigImpl implements FieldConfig
{
    protected String modelIdentifier;

    private List<ValidationConfig> validationRules;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    FieldConfigImpl(String identifier, String label, String type, String evaluatorId)
    {
        super(identifier, null, label, null, type, evaluatorId, null);
    }

    FieldConfigImpl(String identifier, String iconIdentifier, String label, String description, String type,
            Map<String, Object> properties, ArrayList<String> forms, String evaluatorId, String modelIdentifier,
            List<ValidationConfig> validationRules)
    {
        super(identifier, iconIdentifier, label, description, type, evaluatorId, properties);
        this.modelIdentifier = modelIdentifier;
        this.validationRules = validationRules;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getModelIdentifier()
    {
        return modelIdentifier;
    }

    @Override
    public List<ValidationConfig> getValidationRules()
    {
        if (validationRules == null) return new ArrayList<ValidationConfig>(0);
        return validationRules;
    }
}
