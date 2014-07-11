/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.application.config.form;

import java.util.GregorianCalendar;

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyType;
import org.alfresco.mobile.android.api.model.config.FieldConfig;

import android.content.Context;
import android.text.format.DateFormat;

public class TextFieldBuilder extends FieldTypeBuilder
{
    /**
     * Used by the configurationManager
     * 
     * @param activity
     * @param configuration
     */
    public TextFieldBuilder(Context context, Property property, FieldConfig configuration)
    {
        super(context, property, configuration);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FIELD CONFIGURATION
    // ///////////////////////////////////////////////////////////////////////////
    public String getReadValue()
    {
        if (property.getValue() == null) { return null; }
        if (PropertyType.DATETIME.equals(property.getType()))
        {
            return DateFormat.getMediumDateFormat(getContext()).format(
                    ((GregorianCalendar) property.getValue()).getTime())
                    + " "
                    + DateFormat.getTimeFormat(getContext())
                            .format(((GregorianCalendar) property.getValue()).getTime());
        }
        else
        {
            return property.getValue().toString();
        }
    }
}