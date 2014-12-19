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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesPickerFragment;
import org.alfresco.mobile.android.application.fragments.person.UserSearchFragment;
import org.alfresco.mobile.android.application.ui.form.adapter.AuthorityAdapter;
import org.alfresco.mobile.android.application.ui.form.views.PickerFieldView;
import org.alfresco.mobile.android.ui.ListingModeFragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class AuthorityField extends BaseField
{
    public static final String OUTPUT_OBJECT = "object";

    public static final String OUTPUT_ID = "id";

    public static final String OUTPUT_FULLNAME = "fullName";

    private String authorityType;

    private Map<String, Person> persons = new HashMap<String, Person>(0);

    private Boolean allowMultipleSelection = false;

    private String outputValue = OUTPUT_ID;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Used by the configurationManager
     * 
     * @param activity
     * @param configuration
     */
    public AuthorityField(Context context, Property property, FieldConfig configuration)
    {
        super(context, property, configuration);
        if (configuration.getParameter(ConfigConstants.AUTHORITY_VALUE) != null)
        {
            authorityType = (String) configuration.getParameter(ConfigConstants.AUTHORITY_VALUE);
        }
        if (configuration.getParameter(ConfigConstants.ALLOW_MULTIPLE_SELECTION_VALUE) != null)
        {
            allowMultipleSelection = (Boolean) configuration
                    .getParameter(ConfigConstants.ALLOW_MULTIPLE_SELECTION_VALUE);
        }
        if (configuration.getParameter(ConfigConstants.OUTPUT_VALUE) != null)
        {
            outputValue = (String) configuration.getParameter(ConfigConstants.OUTPUT_VALUE);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTING MODE
    // ///////////////////////////////////////////////////////////////////////////
    public ListAdapter getListAdapter(Fragment fr)
    {
        return new AuthorityAdapter(fr, R.layout.app_item_row, new ArrayList<Person>(persons.values()), outputValue);
    }

    public void remove(Object object)
    {
        if (object instanceof Person)
        {
            persons.remove(((Person) object).getIdentifier());
        }
        setPropertyValue(persons);
    }

    public void startPicker(Fragment fr, String tag)
    {
        super.initPicker(fr);
        DialogFragment df = (DialogFragment) UserSearchFragment.with(getFragment().getActivity())
                .fieldId(fieldConfig.getModelIdentifier()).fragmentTag(tag).singleChoice(!allowMultipleSelection)
                .mode(ListingModeFragment.MODE_PICK).createFragment();
        df.show(getFragment().getFragmentManager(), UserSearchFragment.TAG);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // READ MODE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getHumanReadableValue()
    {
        if (property.getValue() == null) { return ""; }
        return super.getHumanReadableValue();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION MODE
    // ///////////////////////////////////////////////////////////////////////////
    public View getEditView(ModelDefinition typeDefinition, ViewGroup hookView)
    {
        this.modelDefinition = typeDefinition;
        fieldView = (PickerFieldView) inflater.inflate(R.layout.form_edit_picker, hookView, false);

        // Configure
        fieldView.setText((getOutputValue() == null) ? "" : getOutputValue().toString());
        fieldView.setHint(fieldConfig.getLabel());
        configureFromDefinition(fieldView);
        fieldView.setReadOnly(isReadOnly);

        return fieldView;
    }

    @Override
    public Object getOutputValue()
    {
        if (persons.values() == null || persons.values().isEmpty()) { return null; }

        if (OUTPUT_FULLNAME.equals(outputValue))
        {
            List<String> names = new ArrayList<String>(persons.size());
            for (Person person : persons.values())
            {
                names.add(person.getIdentifier());
            }
            return (allowMultipleSelection) ? names : names.get(0);
        }
        else if (OUTPUT_OBJECT.equals(outputValue))
        {
            return (allowMultipleSelection) ? new ArrayList<Person>(persons.values()) : new ArrayList<Person>(
                    persons.values()).get(0);
        }
        else
        {
            List<String> ids = new ArrayList<String>(persons.size());
            for (Person person : persons.values())
            {
                ids.add(person.getIdentifier());
            }
            return (allowMultipleSelection) ? ids : ids.get(0);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKER MODE
    // ///////////////////////////////////////////////////////////////////////////
    public Object getValuePicked()
    {
        return persons;
    }

    public boolean requiresPicker()
    {
        return true;
    }

    @Override
    public void initPicker(final Fragment fr)
    {
        super.initPicker(fr);
        if (getFragment() != null && fieldView != null && !isReadOnly)
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void setPropertyValue(Object object)
    {
        if (object instanceof Map || object == null)
        {
            persons = (Map<String, Person>) object;
            String values = "";

            Object outputVal = getOutputValue();
            if (outputVal instanceof List)
            {
                values = String.format(MessageFormat.format(getContext()
                        .getString(R.string.picker_multi_value_selected), ((List) outputVal).size()), Integer
                        .toString(((List) outputVal).size()), fieldConfig.getLabel());
            }
            else if (outputVal instanceof Person)
            {
                values = getEditValue((Person) outputVal);
            }
            else if (outputVal instanceof String)
            {
                values = (String) outputVal;
            }

            fieldView.setText(values);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private String getEditValue(Person person)
    {
        if (person == null) { return ""; }
        return person.getFullName();
    }
}
