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

import java.util.Map;
/**
 *  Base type for Item Configuration.
 *
 * @author Jean Marie Pascal
 *
 */
public interface ItemConfig extends BaseConfig
{
    /**
     * Returns the identifier for an icon to use for the item.
     * 
     * @return
     */
    String getIconIdentifier();

    /**
     * Returns the type of the item.
     * 
     * @return
     */
    String getType();

    /**
     * Returns the parameters for the item, these will typically depend on the
     * type of item.
     * 
     * @return
     */
    Map<String, Object> getParameters();

    /**
     * Returns the value of the parameter with the given key or null if it
     * doesn’t exist.
     * 
     * @param key
     * @return
     */
    Object getParameter(String key);
}
