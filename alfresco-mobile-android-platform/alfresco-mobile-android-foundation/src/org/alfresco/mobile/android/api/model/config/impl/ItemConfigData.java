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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
/**
 * 
 * @author Jean Marie Pascal
 *
 */
public class ItemConfigData extends BaseConfigData
{
    final Map<String, Object> properties;

    final String iconIdentifier;

    final String type;

    final String evaluatorId;

    final ArrayList<Object> items;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    ItemConfigData(String identifier, Map<String, Object> json, ConfigurationImpl configuration)
    {
        super(identifier, json, configuration);
        this.iconIdentifier = JSONConverter.getString(json, ConfigConstants.ICON_ID_VALUE);
        this.type = JSONConverter.getString(json, ConfigConstants.TYPE_VALUE);
        this.properties = (json.containsKey(ConfigConstants.PARAMS_VALUE)) ? JSONConverter.getMap(json
                .get(ConfigConstants.PARAMS_VALUE)) : new HashMap<String, Object>(0);
        this.evaluatorId = JSONConverter.getString(json, ConfigConstants.EVALUATOR);

        if (json.containsKey(ConfigConstants.ITEMS_VALUE))
        {
            List<Object> childrenObject = JSONConverter.getList(json.get(ConfigConstants.ITEMS_VALUE));
            ArrayList<Object> fieldsGroupId = new ArrayList<Object>(childrenObject.size());
            for (Object child : childrenObject)
            {
                fieldsGroupId.add(child);
            }
            this.items = fieldsGroupId;
        }
        else
        {
            this.items = new ArrayList<Object>(0);
        }
    }
}
