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

import java.io.Serializable;

import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.form.views.SwitchFieldView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class BooleanField extends BaseField
{

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public BooleanField(Context context, Property property, FieldConfig configuration)
    {
        super(context, property, configuration);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // READ MODE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getHumanReadableValue()
    {
        if (property.getValue() == null) { return ""; }
        return ((Boolean) property.getValue()) ? getContext().getString(R.string.yes) : getContext().getString(
                R.string.no);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION MODE
    // ///////////////////////////////////////////////////////////////////////////
    public View getEditView(ModelDefinition typeDefinition, ViewGroup hookView)
    {
        this.modelDefinition = typeDefinition;
        fieldView = (SwitchFieldView) inflater.inflate(R.layout.form_edit_switch, hookView, false);

        // Configure Field
        fieldView.setHint(fieldConfig.getLabel());
        configureFromDefinition(fieldView);
        fieldView.setReadOnly(isReadOnly);
        if (property.getValue() != null)
        {
            ((SwitchFieldView) fieldView).setChecked((Boolean) property.getValue());
        }
        else
        {
            ((SwitchFieldView) fieldView).setChecked(false);
        }

        return fieldView;
    }

    @Override
    public Serializable getOutputValue()
    {
        return fieldView instanceof SwitchFieldView && ((SwitchFieldView) fieldView).isChecked();
    }
}
