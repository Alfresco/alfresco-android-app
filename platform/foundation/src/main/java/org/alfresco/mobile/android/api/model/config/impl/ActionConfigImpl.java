/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.api.model.config.impl;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ActionConfig;

/**
 * @author Jean Marie Pascal
 */
public class ActionConfigImpl extends ItemConfigImpl implements ActionConfig, Serializable
{
    protected String evaluatorId;

    private boolean isEnable = true;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public ActionConfigImpl(String identifier, String label, String type, Map<String, Object> properties,
            boolean enable)
    {
        super(identifier, null, label, null, type, null, properties);
        isEnable = enable;
    }

    public ActionConfigImpl(String identifier, String iconIdentifier, String label, String type,
            Map<String, Object> properties, boolean enable)
    {
        super(identifier, null, label, null, type, null, properties);
        isEnable = enable;
    }

    public ActionConfigImpl(String identifier, String label, String type, String evaluatorId)
    {
        super(identifier, null, label, null, type, evaluatorId, null);
        this.evaluatorId = evaluatorId;
    }

    public ActionConfigImpl(String identifier, String iconIdentifier, String label, String description, String type,
            Map<String, Object> properties, boolean enable, String evaluatorId)
    {
        super(identifier, iconIdentifier, label, description, type, evaluatorId, properties);
        this.evaluatorId = evaluatorId;
        isEnable = enable;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public String getEvaluator()
    {
        return evaluatorId;
    }

    @Override
    public boolean isEnable()
    {
        return isEnable;
    }
}
