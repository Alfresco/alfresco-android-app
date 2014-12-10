/*******************************************************************************
 * Copyright (C) 005-014 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version .0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-.0
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

import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.ViewGroupConfig;
/**
 * 
 * @author Jean Marie Pascal
 *
 */
public class ViewGroupConfigImpl extends ViewConfigImpl implements ViewGroupConfig
{
    private LinkedHashMap<String, ViewConfig> childrenIndex;

    private ArrayList<ViewConfig> children;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    ViewGroupConfigImpl(String identifier, String label, String type, ArrayList<ViewConfig> children,
            String evaluatorId)
    {
        super(identifier, label, type, evaluatorId);
        this.children = (children == null) ? new ArrayList<ViewConfig>(0) : children;
    }

    ViewGroupConfigImpl(String identifier,  String iconIdentifier, String label, String description, String type, Map<String, Object> properties,
            LinkedHashMap<String, ViewConfig> childrenIndex, ArrayList<String> forms, String evaluatorId)
    {
        super(identifier, iconIdentifier, label, description, type, properties, forms, evaluatorId);
        this.childrenIndex = (childrenIndex == null) ? new LinkedHashMap<String, ViewConfig>(0) : childrenIndex;
        this.children = new ArrayList<ViewConfig>(this.childrenIndex.values());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public int getChildCount()
    {
        return (children == null) ? 0 : children.size();
    }

    public ViewConfig getChildAt(int index)
    {
        return (children == null) ? null : children.get(index);
    }

    public ViewConfig getChildById(String id)
    {
        return (childrenIndex == null) ? null : childrenIndex.get(id);
    }

    public void setChildren(ArrayList<ViewConfig> children)
    {
        this.children = children;
    }

    @Override
    public List<ViewConfig> getItems()
    {
        return children;
    }
}
