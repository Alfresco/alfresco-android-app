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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyType;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesFragment;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesPickerFragment;
import org.alfresco.mobile.android.application.ui.form.adapter.MultiValuedStringAdapter;
import org.alfresco.mobile.android.application.ui.form.picker.AllowablePickerFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DatePickerFragment;
import org.alfresco.mobile.android.application.ui.form.views.EditTextFieldView;

import android.app.Fragment;
import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class TextField extends BaseField
{
    boolean hasMultipleLines = false, secret = false;

    private Map<String, Object> pickerValue;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public TextField(Context context, Property property, FieldConfig configuration)
    {
        super(context, property, configuration);

        if (configuration.getParameter(ConfigConstants.SHOW_MULTIPLE_LINES_VALUE) != null)
        {
            hasMultipleLines = (Boolean) configuration.getParameter(ConfigConstants.SHOW_MULTIPLE_LINES_VALUE);
        }

        if (configuration.getParameter(ConfigConstants.SECRET_VALUE) != null)
        {
            secret = (Boolean) configuration.getParameter(ConfigConstants.SECRET_VALUE);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTING MODE
    // ///////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    public ListAdapter getListAdapter(Fragment fr)
    {
        if (isMultiValued)
        {
            return new MultiValuedStringAdapter(fr, R.layout.app_item_row, new ArrayList<String>(
                    (List<String>) multiValue), true);
        }
        else
        {
            return null;
        }
    }

    public void remove(Object object)
    {
        if (isMultiValued && object instanceof String)
        {
            multiValue.remove((String) object);
            setPropertyValue(multiValue);
        }
    }

    @SuppressWarnings("unchecked")
    public void add(Object object)
    {
        if (object != null && isMultiValued && object instanceof String)
        {
            if (multiValue.isEmpty())
            {
                multiValue = new ArrayList<Object>();
            }
            ((List<String>) multiValue).add(getStringValue(getContext(), object));
            setPropertyValue(multiValue);
        }
    }

    public void startPicker(Fragment fr, String tag)
    {
        super.initPicker(fr);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // READ MODE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getHumanReadableValue()
    {
        // Manage multi value
        if (isMultiValued) { return String.format(
                MessageFormat.format(getContext().getString(R.string.picker_multi_value_selected), multiValue.size()),
                Integer.toString(multiValue.size()), fieldConfig.getLabel()); }

        // Manage single value
        if (originalValue == null) { return null; }
        if (PropertyType.BOOLEAN.equals(property.getType()))
        {
            return ((Boolean) property.getValue()) ? getContext().getString(R.string.yes) : getContext().getString(R.string.no);
        }
        else if (PropertyType.DATETIME.equals(property.getType()))
        {
            String value = DateFormat.getMediumDateFormat(getContext()).format(
                    ((GregorianCalendar) originalValue).getTime());
            if (fieldConfig.getParameter(ConfigConstants.SHOW_TIME_VALUE) != null
                    && (Boolean) fieldConfig.getParameter(ConfigConstants.SHOW_TIME_VALUE))
            {
                value = value.concat(" "
                        + DateFormat.getTimeFormat(getContext()).format(((GregorianCalendar) originalValue).getTime()));
            }
            return value;
        }
        return super.getHumanReadableValue();
    }

    @Override
    public View createReadableView()
    {
        if (hasMultipleLines)
        {
            String value = getHumanReadableValue();
            if (TextUtils.isEmpty(value)) { return null; }

            View vr = inflater.inflate(R.layout.form_read_textmultiline, null);
            TextView tv = (TextView) vr.findViewWithTag("propertyName");
            tv.setText(fieldConfig.getLabel());
            tv = (TextView) vr.findViewWithTag("propertyValue");
            tv.setText(value);
            tv.setClickable(true);
            tv.setFocusable(true);
            return vr;
        }
        else
        {
            return super.createReadableView();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION MODE
    // ///////////////////////////////////////////////////////////////////////////
    public Object getOutputValue()
    {
        if (property == null) { return null; }
        if (property.isMultiValued())
        {
            return multiValue;
        }
        else
        {
            return super.getOutputValue();
        }
    }

    public void clearEditView()
    {
        fieldView.clear();
    }

    @Override
    public View getEditView(ModelDefinition typeDefinition, ViewGroup hookView)
    {
        View editText = super.getEditView(typeDefinition, hookView);
        if (hasMultipleLines && editText instanceof EditTextFieldView)
        {
            ((EditTextFieldView) editText).setMultiLine(true);
        }
        return editText;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKER MODE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void initPicker(final Fragment fr)
    {
        super.initPicker(fr);
        if (getFragment() != null && fieldView != null && !isReadOnly)
        {
            if (hasAllowableValues)
            {
                fieldView.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        // INFO Change here for multi selection
                        AllowablePickerFragment picker = AllowablePickerFragment.newInstance(
                                fieldConfig.getModelIdentifier(), EditPropertiesFragment.TAG, true,
                                fieldConfig.getLabel());
                        picker.setPropertyDefinition(getOutputValue(), propertyDefinition);
                        picker.show(fr.getFragmentManager(), DatePickerFragment.TAG);
                    }
                });
            }
            else if (isMultiValued)
            {
                fieldView.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        EditPropertiesPickerFragment.newInstance(fieldConfig.getModelIdentifier()).show(
                                fr.getFragmentManager(), EditPropertiesPickerFragment.TAG);
                    }
                });
            }
        }
    }

    public boolean requiresPicker()
    {
        return hasAllowableValues || isMultiValued;
    }

    public Object getValuePicked()
    {
        if (hasAllowableValues)
        {
            return pickerValue;
        }
        else if (isMultiValued)
        {
            return multiValue;
        }
        else
        {
            return getOutputValue();
        }
    }

    @SuppressWarnings("unchecked")
    public void setPropertyValue(Object object)
    {
        if (isMultiValued && object instanceof List)
        {
            multiValue = (List<String>) object;
            fieldView.setText(getHumanReadableValue());
        }
        else if (hasAllowableValues && object instanceof Map)
        {
            // TODO Fix how we retrieve values from a list of choice.
            // Check get(0)
            pickerValue = (Map<String, Object>) object;
            for (Entry<String, Object> entry : pickerValue.entrySet())
            {
                fieldView.setText(getStringValue(getContext(), entry.getValue()));
            }
        }
        else
        {
            super.setPropertyValue(object);
        }
    }

}
