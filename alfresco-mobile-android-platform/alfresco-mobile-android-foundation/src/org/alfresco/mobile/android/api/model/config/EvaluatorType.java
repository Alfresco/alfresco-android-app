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
 * Enumeration Constants that represents evaluators types available.
 * 
 * @author Jean Marie Pascal
 */
public enum EvaluatorType
{
    NODE_TYPE("com.alfresco.client.evaluator.nodeType"), 
    HAS_ASPECT("com.alfresco.client.evaluator.hasAspect"), 
    MEMBER_OF_GROUP("com.alfresco.client.evaluator.memberOfGroup"), 
    MEMBER_OF_SITE("com.alfresco.client.evaluator.memberOfSite"), 
    HAS_ROLE("com.alfresco.client.evaluator.hasRole"), 
    BELONGS_TO_SITE("com.alfresco.client.evaluator.belongsToSite"), 
    HAS_PROPERTY_VALUE("com.alfresco.client.evaluator.hasPropertyValue"), 
    IS_REPOSITORY_VERSION("com.alfresco.client.evaluator.isRepositoryVersion"),
    IS_REPOSITORY_EDITION("com.alfresco.client.evaluator.isRepositoryEdition"), 
    IS_DEVICE_TYPE("com.alfresco.client.evaluator.isDeviceType"); 

    /** The value associated to an enum. */
    private final String value;

    /**
     * Instantiates a new property type.
     * 
     * @param v the value of the enum.
     */
    EvaluatorType(String v)
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
    public static EvaluatorType fromValue(String v)
    {
        for (EvaluatorType c : EvaluatorType.values())
        {
            if (c.value.equalsIgnoreCase(v)) { return c; }
        }
        return null;
    }
}
