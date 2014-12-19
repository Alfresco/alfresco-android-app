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

import org.alfresco.mobile.android.api.model.config.FieldGroupConfig;
import org.alfresco.mobile.android.api.model.config.FormConfig;
/**
 * 
 * @author Jean Marie Pascal
 *
 */
public class FormConfigImpl extends ItemConfigImpl implements FormConfig
{
    private ArrayList<FieldGroupConfig> children;

    protected String evaluatorId;
    
    protected String layoutId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    FormConfigImpl()
    {
        super();
    }

    public FormConfigImpl(String identifier, String iconIdentifier, String label, String description, String type,
            Map<String, Object> properties, List<FieldGroupConfig> children, String evaluatorId, String layoutId)
    {
        super(identifier, iconIdentifier, label, description, type, evaluatorId, properties);
        this.children = new ArrayList<FieldGroupConfig>(children);
        this.evaluatorId = evaluatorId;
        this.layoutId = layoutId;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getLayout()
    {
        return layoutId;
    }

    @Override
    public List<FieldGroupConfig> getGroups()
    {
        return children;
    }

}
