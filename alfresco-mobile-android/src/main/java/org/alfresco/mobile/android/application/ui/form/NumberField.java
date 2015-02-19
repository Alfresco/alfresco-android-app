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
import java.math.BigInteger;

import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.ui.form.views.EditTextFieldView;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

public class NumberField extends BaseField
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public NumberField(Context context, Property property, FieldConfig configuration)
    {
        super(context, property, configuration);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // READ MODE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View getEditView(ModelDefinition typeDefinition, ViewGroup hookView)
    {
        EditTextFieldView v = (EditTextFieldView) super.getEditView(typeDefinition, hookView);
        v.getEditText().setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
        return v;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION MODE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Serializable getOutputValue()
    {
        String value = fieldView.getValue();
        if (TextUtils.isEmpty(value)) { return null; }
        return new BigInteger(value);
    }
}
