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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.api.model.config.FieldGroupConfig;
/**
 * 
 * @author Jean Marie Pascal
 *
 */
public class FieldsGroupConfigImpl extends FieldConfigImpl implements FieldGroupConfig
{
    private LinkedHashMap<String, FieldConfig> childrenIndex;

    private ArrayList<FieldConfig> children;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    FieldsGroupConfigImpl(String identifier, String label, String type, ArrayList<FieldConfig> children,
            String evaluatorId)
    {
        super(identifier, label, type, evaluatorId);
        this.children = (children == null) ? new ArrayList<FieldConfig>(0) : children;
    }

    FieldsGroupConfigImpl(String identifier,  String iconIdentifier, String label, String description, String type, Map<String, Object> properties,
            LinkedHashMap<String, FieldConfig> childrenIndex, ArrayList<String> forms, String evaluatorId, String modelIdentifier)
    {
        super(identifier, iconIdentifier, label, description, type, properties, forms, evaluatorId, modelIdentifier, null);
        this.childrenIndex = (childrenIndex == null) ? new LinkedHashMap<String, FieldConfig>(0) : childrenIndex;
        this.children = new ArrayList<FieldConfig>(this.childrenIndex.values());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public int getChildCount()
    {
        return (children == null) ? 0 : children.size();
    }

    public FieldConfig getChildAt(int index)
    {
        return (children == null) ? null : children.get(index);
    }

    public FieldConfig getChildById(String id)
    {
        return (childrenIndex == null) ? null : childrenIndex.get(id);
    }

    public void setChildren(ArrayList<FieldConfig> children)
    {
        this.children = children;
    }

    @Override
    public List<FieldConfig> getItems()
    {
        return children;
    }
    
    
}
