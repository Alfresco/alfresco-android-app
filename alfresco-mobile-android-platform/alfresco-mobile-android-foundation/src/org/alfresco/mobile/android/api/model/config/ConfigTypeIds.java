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
package org.alfresco.mobile.android.api.model.config;

/**
 * Enumeration Constants that represents the root configuration types available.
 * 
 * @author Jean Marie Pascal
 */
public enum ConfigTypeIds
{
    INFO("info"),
    REPOSITORY("repository"), 
    FEATURES("features"), 
    MENU("menu"), 
    VIEWS("views"), 
    FORMS("forms"), 
    ACTION_GROUPS("action-groups"), 
    SEARCH("search"), 
    WORKFLOW("workflow"), 
    CREATION("creation"), 
    THEME("theme"), 
    VIEW_GROUPS("view-groups"), 
    FIELDS("fields"), 
    FIELD_GROUPS("field-groups"), 
    ACTION_DEFINITIONS("action-definitions"), 
    EVALUATORS("evaluators"),
    VALIDATION_RULES("validation-rules"),
    PROFILES("profiles");

    /** The value associated to an enum. */
    private final String value;

    /**
     * Instantiates a new property type.
     * 
     * @param v the value of the enum.
     */
    ConfigTypeIds(String v)
    {
        value = v;
    }

    /**
     * Value.
     * 
     * @return the string
     */
    public String value()
    {
        return value;
    }

    /**
     * From value.
     * 
     * @param v the value of the enum.
     * @return the property type
     */
    public static ConfigTypeIds fromValue(String v)
    {
        for (ConfigTypeIds c : ConfigTypeIds.values())
        {
            if (c.value.equalsIgnoreCase(v)) { return c; }
        }
        return null;
    }
}
