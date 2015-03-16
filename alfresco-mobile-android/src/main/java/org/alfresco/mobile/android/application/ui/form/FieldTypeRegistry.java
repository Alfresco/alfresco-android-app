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
package org.alfresco.mobile.android.application.ui.form;

import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.PropertyType;

import android.text.TextUtils;

public class FieldTypeRegistry
{
    public static final String FIELD_TEXT = "org.alfresco.client.form.field.text";

    public static final String FIELD_FILESIZE = "org.alfresco.client.form.field.fileSize";

    public static final String FIELD_DATETIME = "org.alfresco.client.form.field.dateTime";

    public static final String FIELD_NUMBER = "org.alfresco.client.form.field.number";

    public static final String FIELD_DECIMAL = "org.alfresco.client.form.field.decimal";

    public static final String FIELD_BOOLEAN = "org.alfresco.client.form.field.boolean";

    public static final String DEFAULT_FIELD = FIELD_TEXT;

    public static String getFieldType(ModelDefinition definition, String identifier)
    {
        if (definition == null || TextUtils.isEmpty(identifier) || definition.getPropertyDefinition(identifier) == null) { return FIELD_TEXT; }
        PropertyType type = definition.getPropertyDefinition(identifier).getType();
        switch (type)
        {
            case BOOLEAN:
                return FIELD_BOOLEAN;
            case DATETIME:
                return FIELD_DATETIME;
            case DECIMAL:
                return FIELD_DECIMAL;
            case ID:
                return FIELD_TEXT;
            case INTEGER:
                return FIELD_NUMBER;
            default:
                return FIELD_TEXT;
        }
    }
}
