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

import java.util.Date;
import java.util.GregorianCalendar;

import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.api.utils.DateUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DatePickerFragment;
import org.alfresco.mobile.android.application.ui.form.views.PickerFieldView;

import android.app.Fragment;
import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class DateTimeField extends BaseField
{
    protected GregorianCalendar calendar;

    private boolean showTime = false;

    private Date minDate = null, maxDate = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Used by the configurationManager
     * 
     * @param context
     * @param configuration
     */
    public DateTimeField(Context context, Property property, FieldConfig configuration)
    {
        super(context, property, configuration);
        if (configuration.getParameter(ConfigConstants.SHOW_TIME_VALUE) != null)
        {
            showTime = (Boolean) configuration.getParameter(ConfigConstants.SHOW_TIME_VALUE);
        }
        if (configuration.getParameter(ConfigConstants.MIN_DATE_VALUE) != null)
        {
            minDate = DateUtils.parseDate((String) configuration.getParameter(ConfigConstants.MIN_DATE_VALUE));
        }
        if (configuration.getParameter(ConfigConstants.MAX_DATE_VALUE) != null)
        {
            maxDate = DateUtils.parseDate((String) configuration.getParameter(ConfigConstants.MAX_DATE_VALUE));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // READ MODE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getHumanReadableValue()
    {
        if (property.getValue() == null) { return ""; }
        String readValue = DateFormat.getMediumDateFormat(getContext()).format(
                ((GregorianCalendar) property.getValue()).getTime());
        if (showTime)
        {
            readValue = readValue.concat(" ").concat(
                    DateFormat.getTimeFormat(getContext()).format(((GregorianCalendar) property.getValue()).getTime()));
        }
        return readValue;

    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION MODE
    // ///////////////////////////////////////////////////////////////////////////
    public View getEditView(ModelDefinition typeDefinition, ViewGroup hookView)
    {
        this.modelDefinition = typeDefinition;
        if (modelDefinition != null && modelDefinition.getPropertyDefinition(fieldConfig.getModelIdentifier()) != null)
        {
            propertyDefinition = modelDefinition.getPropertyDefinition(fieldConfig.getModelIdentifier());
        }

        if (calendar == null && property.getValue() != null)
        {
            calendar = property.getValue();
        }

        fieldView = (PickerFieldView) inflater.inflate(R.layout.form_edit_picker, hookView, false);

        // Configure
        fieldView.setText(getEditValue(calendar));
        fieldView.setHint(fieldConfig.getLabel());
        configureFromDefinition(fieldView);
        fieldView.setReadOnly(isReadOnly);

        // keep reference

        return fieldView;
    }

    @Override
    public Object getOutputValue()
    {
        if (fieldView instanceof PickerFieldView && TextUtils.isEmpty(((PickerFieldView) fieldView).getText())) { return null; }
        if (calendar != null)
        {
            return calendar;
        }
        else if (property.getValue() != null)
        {
            return (GregorianCalendar) property.getValue();
        }
        else
        {
            return null;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKER MODE
    // ///////////////////////////////////////////////////////////////////////////
    public void setPropertyValue(Object object)
    {
        if (object instanceof GregorianCalendar || object == null)
        {
            this.calendar = (GregorianCalendar) object;
            if (fieldView != null)
            {
                fieldView.setText(getEditValue(calendar));
            }
        }
    }

    public boolean requiresPicker()
    {
        return true;
    }

    @Override
    public void initPicker(Fragment fr)
    {
        super.initPicker(fr);
        if (getFragment() != null && fieldView != null && !isReadOnly)
        {
            fieldView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    DatePickerFragment.newInstance(fieldConfig.getModelIdentifier(), EditPropertiesFragment.TAG,
                            (calendar != null) ? calendar.getTimeInMillis() : null,
                            (minDate != null) ? minDate.getTime() : null, (maxDate != null) ? maxDate.getTime() : null,
                            showTime).show(getFragment().getFragmentManager(), DatePickerFragment.TAG);
                }
            });
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected String getEditValue(GregorianCalendar calendar)
    {
        if (calendar == null) { return ""; }
        String readValue = DateFormat.getMediumDateFormat(getContext()).format(calendar.getTime());
        if (showTime)
        {
            readValue = readValue.concat(" ").concat(DateFormat.getTimeFormat(getContext()).format(calendar.getTime()));
        }
        return readValue;
    }
}
