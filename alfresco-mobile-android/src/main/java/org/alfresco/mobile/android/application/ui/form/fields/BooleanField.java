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

package org.alfresco.mobile.android.application.ui.form.fields;

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.form.FormManager;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by jpascal on 28/03/2015.
 */
public class BooleanField extends BaseField
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public BooleanField(Context context, FormManager manager, Property property, FieldConfig configuration,
            PropertyDefinition propertyDefinition, boolean isReadMode)
    {
        super(context, manager, property, configuration, propertyDefinition, isReadMode);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VALUES
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Object getEditionValue()
    {
        return ((Switch) editionView).isChecked();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // READ
    // ///////////////////////////////////////////////////////////////////////////
    public String getHumanReadableReadValue()
    {
        if (originalValue == null) { return ""; }
        return ((Boolean) originalValue) ? getContext().getString(R.string.yes) : getContext().getString(R.string.no);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION VIEW
    // ///////////////////////////////////////////////////////////////////////////
    protected void updateEditionView()
    {
        ((Switch) editionView).setChecked((Boolean) editionValue);
    }

    public View setupEditionView(Object value)
    {
        editionValue = value != null ? (Boolean) value : false;

        View vr = inflater.inflate(R.layout.form_switch, null);
        ((Switch) vr).setChecked((Boolean) editionValue);

        // Asterix if required
        ((Switch) vr).setText(getLabelText(fieldConfig.getLabel()));

        ((Switch) vr).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                buttonView.setError(null);
                getFormManager().evaluateViews();
            }
        });

        editionView = vr;

        return vr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ERROR
    // ///////////////////////////////////////////////////////////////////////////
    public boolean isValid()
    {
        if (isRequired && (getEditionValue() != null && ((Boolean) getEditionValue()))) { return true; }
        return false;
    }

    public void showError()
    {
        if (isValid()) { return; }
        ((Switch) editionView)
                .setError(String.format(getString(R.string.form_error_message_required), fieldConfig.getLabel()));
    }
}
