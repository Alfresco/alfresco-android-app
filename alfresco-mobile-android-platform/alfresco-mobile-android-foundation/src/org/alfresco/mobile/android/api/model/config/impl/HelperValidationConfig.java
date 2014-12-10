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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.config.ValidationConfig;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

/**
 * @author Jean Marie Pascal
 */
public class HelperValidationConfig extends HelperConfig
{
    private LinkedHashMap<String, ValidationConfig> validationConfigIndex;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    HelperValidationConfig(ConfigurationImpl context, HelperStringConfig localHelper)
    {
        super(context, localHelper);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INIT
    // ///////////////////////////////////////////////////////////////////////////
    void addValidation(Map<String, Object> validations)
    {
        validationConfigIndex = new LinkedHashMap<String, ValidationConfig>(validations.size());
        ValidationConfigData data = null;
        for (Entry<String, Object> entry : validations.entrySet())
        {
            data = new ValidationConfigData(entry.getKey(), JSONConverter.getMap(entry.getValue()), getConfiguration());
            validationConfigIndex.put(data.identifier, new ValidationConfigImpl(data.identifier, data.iconIdentifier,
                    data.label, data.description, data.type, data.properties, data.errorId));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public ValidationConfig getValidationRuleById(String id)
    {
        if (validationConfigIndex == null || validationConfigIndex.isEmpty()) { return null; }
        return validationConfigIndex.get(id);
    }

}
