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

package org.alfresco.mobile.android.application.configuration.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.impl.ViewConfigImpl;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;

import android.content.Context;

/**
 * Created by jpascal on 10/08/2015.
 */
public class ViewConfigModel
{
    protected String type;

    protected String id;

    protected int iconModelResId;

    protected String iconId;

    protected int iconResId;

    protected int labelId;

    protected int descriptionId;

    protected List<ConfigParameterModel> parameters;

    public ViewConfigModel()
    {
        this.parameters = createParameters();
    }

    protected List<ConfigParameterModel> createParameters()
    {
        return new ArrayList<>(0);
    }

    public String getType()
    {
        return type;
    }

    public String getSimpleType()
    {
        return type.substring(ConfigurationConstant.PREFIX.length() + 1, type.length());
    }

    public int getIconResId()
    {
        return iconResId;
    }

    public int getIconModelResId()
    {
        return iconModelResId;
    }

    public String getLabel(Context context)
    {
        return context.getString(labelId);
    }

    public int getLabelId()
    {
        return labelId;
    }

    public boolean hasParameters()
    {
        return !parameters.isEmpty();
    }

    public List<ConfigParameterModel> getParameters()
    {
        return parameters;
    }

    public int getDescriptionId()
    {
        return descriptionId;
    }

    public String getIconId()
    {
        return iconId;
    }

    public ViewConfig createViewConfig(String id, Context context)
    {
        return createViewConfig(id, getLabel(context), null);
    }

    public ViewConfig createViewConfig(String id, String name, Map<String, Object> properties)
    {
        if (properties != null)
        {
            refresh(properties);
        }
        return new ViewConfigImpl(id, iconId, name, null, type, properties, null, null);
    }

    public ViewConfig createViewConfig(Context context, Map<String, Object> properties)
    {
        if (properties != null)
        {
            refresh(properties);
        }
        return new ViewConfigImpl(id, iconId, getLabel(context), null, type, properties, null, null);
    }

    protected void refresh(Map<String, Object> properties)
    {

    }

}
