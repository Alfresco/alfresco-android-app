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

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.TypeDefinition;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.micromobs.android.floatlabel.FloatLabelEditText;

public class TextMultiLineFieldBuilder extends TextFieldBuilder
{
    /**
     * Used by the configurationManager
     * 
     * @param activity
     * @param configuration
     */
    public TextMultiLineFieldBuilder(Context context, Property property, FieldConfig configuration)
    {
        super(context, property, configuration);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FIELD CONFIGURATION
    // ///////////////////////////////////////////////////////////////////////////
    public View getReadOnlyView()
    {
        String value = getReadValue();
        if (TextUtils.isEmpty(value)) { return null; }

        View vr = inflater.inflate(R.layout.form_field_textmultiline, null);
        TextView tv = (TextView) vr.findViewWithTag("propertyName");
        tv.setText(fieldConfig.getLabel());
        tv = (TextView) vr.findViewWithTag("propertyValue");
        tv.setText(value);
        tv.setClickable(true);
        tv.setFocusable(true);
        return vr;
    }
    
    public View getEditView(TypeDefinition typeDefinition, ViewGroup hookView)
    {
        View editText = super.getEditView(typeDefinition, hookView);
        if (editText instanceof FloatLabelEditText)
        {
            ((FloatLabelEditText) editText).setMultiLine(true);
        }
        return editText;
    }
}
