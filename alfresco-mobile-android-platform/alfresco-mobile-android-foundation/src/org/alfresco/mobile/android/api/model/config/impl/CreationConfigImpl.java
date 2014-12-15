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

import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.CreationConfig;
import org.alfresco.mobile.android.api.model.config.ItemConfig;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
/**
 * 
 * @author Jean Marie Pascal
 *
 */
public class CreationConfigImpl implements CreationConfig
{

    protected List<ItemConfig> creatableMimetypes = new ArrayList<ItemConfig>(0);

    protected List<ItemConfig> creatableDocumentTypes = new ArrayList<ItemConfig>(0);

    protected List<ItemConfig> creatableFolderTypes = new ArrayList<ItemConfig>(0);

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    CreationConfigImpl()
    {
    }

    static CreationConfig parse(Map<String, Object> json, ConfigurationImpl configuration)
    {
        CreationConfigImpl creationConfig = new CreationConfigImpl();

        if (json.containsKey(ConfigConstants.MIME_TYPES_VALUE))
        {
            creationConfig.creatableMimetypes = retrieveListItemConfig(
                    JSONConverter.getList(json.get(ConfigConstants.MIME_TYPES_VALUE)), configuration);
        }
        
        if (json.containsKey(ConfigConstants.DOCUMENT_TYPES_VALUE))
        {
            creationConfig.creatableDocumentTypes = retrieveListItemConfig(
                    JSONConverter.getList(json.get(ConfigConstants.DOCUMENT_TYPES_VALUE)), configuration);
        }
        
        if (json.containsKey(ConfigConstants.FOLDER_TYPES_VALUE))
        {
            creationConfig.creatableFolderTypes = retrieveListItemConfig(
                    JSONConverter.getList(json.get(ConfigConstants.FOLDER_TYPES_VALUE)), configuration);
        }

        return creationConfig;
    }

    private static List<ItemConfig> retrieveListItemConfig(List<Object> list, ConfigurationImpl configuration)
    {
        List<ItemConfig> listItems = new ArrayList<ItemConfig>(list.size());
        Map<String, Object> json;
        for (Object object : list)
        {
            json = JSONConverter.getMap(object);
            ItemConfigData data = new ItemConfigData(null, json, configuration);

            listItems.add(new ItemConfigImpl(data.identifier, data.iconIdentifier, data.label, data.description, data.type, data.evaluatorId, data.properties));
        }
        return listItems;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public List<ItemConfig> getCreatableMimeTypes()
    {
        return creatableMimetypes;
    }

    @Override
    public List<ItemConfig> getCreatableDocumentTypes()
    {
        return creatableDocumentTypes;
    }

    @Override
    public List<ItemConfig> getCreatableFolderTypes()
    {
        return creatableFolderTypes;
    }

}
