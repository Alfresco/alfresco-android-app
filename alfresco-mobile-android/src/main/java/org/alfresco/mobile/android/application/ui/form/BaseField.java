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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.api.model.config.ValidationConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.form.validation.MandatoryValidationRule;
import org.alfresco.mobile.android.application.ui.form.validation.ValidationRule;
import org.alfresco.mobile.android.application.ui.form.validation.ValidationRuleFactory;
import org.alfresco.mobile.android.application.ui.form.views.AlfrescoFieldView;
import org.alfresco.mobile.android.application.ui.form.views.EditTextFieldView;
import org.alfresco.mobile.android.application.ui.form.views.PickerFieldView;
import org.alfresco.mobile.android.async.OperationEvent;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Fragment;
import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public abstract class BaseField
{
    protected WeakReference<Context> contextRef;

    protected FieldConfig fieldConfig;

    protected Property property;

    protected LayoutInflater inflater;

    protected ModelDefinition modelDefinition;

    protected PropertyDefinition propertyDefinition;

    private Fragment fr;

    protected boolean isMandatory = false;

    protected boolean isReadOnly = false;

    protected boolean hasAllowableValues = false;

    protected AlfrescoFieldView fieldView;

    protected AlfrescoFieldView pickerField;

    protected boolean isMultiValued = false;

    protected Object originalValue;

    protected List<?> multiValue = new ArrayList<Object>(1);

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Used by the configurationManager
     * 
     * @param context
     * @param configuration
     */
    public BaseField(Context context, Property property, FieldConfig configuration)
    {
        this.contextRef = new WeakReference<Context>(context);
        this.fieldConfig = configuration;
        this.property = property;
        this.inflater = LayoutInflater.from(context);
        if (property != null)
        {
            this.isMultiValued = property.isMultiValued();
            if (!this.isMultiValued)
            {
                this.originalValue = property.getValue();
            }
            else
            {
                if (property.getValue() instanceof List)
                {
                    this.multiValue = property.getValue();
                }
                else if (property.getValue() == null || property instanceof Collection)
                {
                    this.multiValue = new ArrayList<Object>(0);
                }
            }
        }

        if (configuration.getParameter(ConfigConstants.READ_ONLY_VALUE) != null)
        {
            this.isReadOnly = (Boolean) configuration.getParameter(ConfigConstants.READ_ONLY_VALUE);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKER/LIST MODE
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Init a picker generally the onClickListener associated to the view.
     * 
     * @param fr
     */
    public void initPicker(Fragment fr)
    {
        this.fr = fr;
    }

    /**
     * Display the list of value for this field.
     * 
     * @param fr
     * @return
     */
    public ListAdapter getListAdapter(Fragment fr)
    {
        return null;
    }

    /**
     * Open a picker fragment associated to the field.
     * 
     * @param fr
     * @param tag
     */
    public void startPicker(Fragment fr, String tag)
    {

    }

    /**
     * Remove a value object to the field.
     * 
     * @param object
     */
    public void remove(Object object)
    {
    }

    /**
     * Add a value object to the field.
     * 
     * @param rawValue
     */
    public void add(Object rawValue)
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUIRE REST API
    // ///////////////////////////////////////////////////////////////////////////
    public boolean requireAsync()
    {
        return false;
    }

    public OperationRequest.OperationBuilder requestData(Object extra)
    {
        return null;
    }

    public void setOperationData(OperationEvent event)
    {
        // TO Be Implemented by subclass
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VALUES
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Return the final value.
     * 
     * @return the object associated to the field. Can be anything (Model
     *         object, Date, String, int,...)
     */
    public Object getOutputValue()
    {
        return fieldView.getValue();
    }

    public boolean hasChanged()
    {
        if (originalValue == null && fieldView.getValue() == null)
        {
            // Value has been cleared
            return false;
        }
        else if (originalValue != null && originalValue.equals(getOutputValue()))
        {
            // Value has not changed
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Return the value previously picked by the user.
     * 
     * @return
     */
    public Object getValuePicked()
    {
        return fieldView.getValue();
    }

    /**
     * It's not necessary the value of the field. It can be a like '3 values
     * selected' in case of multi value field.
     * 
     * @return the String representation of the property value.
     */
    public String getHumanReadableValue()
    {
        if (originalValue == null) { return null; }
        return originalValue.toString();
    }

    /**
     * Return the label of the field
     * 
     * @return
     */
    public String getLabel()
    {
        return fieldConfig.getLabel();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // READ MODE
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * By default it's a simple Layout with 2 TextView (key/value)
     * 
     * @return the view associated to the property Value.
     */
    public View createReadableView()
    {
        View vr;
        if (isMultiValued)
        {
            // Multi Value
            if (multiValue == null || multiValue.isEmpty()) { return null; }

            vr = inflater.inflate(R.layout.form_read_multivalue, null);
            TextView fieldNameView = (TextView) vr.findViewWithTag("propertyName");
            fieldNameView.setText(fieldConfig.getLabel());
            LinearLayout multiValueGroup = (LinearLayout) vr.findViewById(R.id.group_multivalues);
            for (int i = 0; i < multiValue.size(); i++)
            {
                String value = getStringValue(getContext(), multiValue.get(i));
                View singleView = inflater.inflate(R.layout.form_read_row, null);
                TextView tv = (TextView) singleView.findViewWithTag("propertyName");
                tv.setText(i + ".");
                tv = (TextView) singleView.findViewWithTag("propertyValue");
                tv.setText(value);
                tv.setClickable(true);
                tv.setFocusable(true);
                multiValueGroup.addView(singleView);
            }
        }
        else
        {
            // Single Value
            String value = getHumanReadableValue();
            if (TextUtils.isEmpty(value)) { return null; }

            vr = inflater.inflate(R.layout.form_read_row, null);
            TextView tv = (TextView) vr.findViewWithTag("propertyName");
            tv.setText(fieldConfig.getLabel());
            tv = (TextView) vr.findViewWithTag("propertyValue");
            tv.setText(value);
            tv.setClickable(true);
            tv.setFocusable(true);
        }

        return vr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION MODE
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Create the UI associated to the property value. ModelDefinition adjust
     * some extra behaviours.
     * 
     * @param typeDefinition
     * @param hookView : Future Parent View to attach the newly generated view.
     * @return
     */
    public View getEditView(ModelDefinition typeDefinition, ViewGroup hookView)
    {
        return getEditView(typeDefinition, hookView, false);
    }

    public View getEditView(ModelDefinition typeDefinition, ViewGroup hookView, boolean enforceEdition)
    {
        this.modelDefinition = typeDefinition;
        if (modelDefinition != null && modelDefinition.getPropertyDefinition(fieldConfig.getModelIdentifier()) != null)
        {
            propertyDefinition = modelDefinition.getPropertyDefinition(fieldConfig.getModelIdentifier());
        }

        // Select the right UI depending on criteria (list or not)
        AlfrescoFieldView tmpFieldView;
        if (propertyDefinition != null && !propertyDefinition.getAllowableValues().isEmpty() && !enforceEdition)
        {
            tmpFieldView = (PickerFieldView) inflater.inflate(R.layout.form_edit_picker, hookView, false);
            hasAllowableValues = true;
            tmpFieldView.setText(getHumanReadableValue());
        }
        else if (isMultiValued && !enforceEdition)
        {
            tmpFieldView = (PickerFieldView) inflater.inflate(R.layout.form_edit_picker, hookView, false);
            isMultiValued = true;
            tmpFieldView.setText(getHumanReadableValue());
        }
        else
        {
            tmpFieldView = (EditTextFieldView) inflater.inflate(R.layout.form_edit_text, hookView, false);
            if (!enforceEdition)
            {
                tmpFieldView.setText(getHumanReadableValue());
            }
        }

        // Configure Field
        tmpFieldView.setContentDescription(fieldConfig.getModelIdentifier());
        tmpFieldView.setHint(fieldConfig.getLabel());
        configureFromDefinition(tmpFieldView);
        tmpFieldView.setReadOnly(isReadOnly);

        if (enforceEdition)
        {
            pickerField = tmpFieldView;
        }
        else
        {
            fieldView = tmpFieldView;
        }

        tmpFieldView.setId(UIUtils.generateViewId());

        return tmpFieldView;
    }

    /**
     * Add extra control to the field based on the Model/PropertyDefinition of
     * the field.
     * 
     * @param fieldView
     */
    protected void configureFromDefinition(AlfrescoFieldView fieldView)
    {
        // Just in case of we forget to retrieve the propertyDefinition
        if (propertyDefinition == null && modelDefinition != null
                && modelDefinition.getPropertyDefinition(fieldConfig.getModelIdentifier()) != null)
        {
            propertyDefinition = modelDefinition.getPropertyDefinition(fieldConfig.getModelIdentifier());
        }

        // Retrieve Information from Definition
        if (propertyDefinition != null)
        {
            boolean definitionReadOnly = propertyDefinition.isReadOnly();

            // Special case where definition is more important than config
            if (!isReadOnly && definitionReadOnly)
            {
                isReadOnly = true;
            }

            // Is Required
            fieldView.setMandatory(propertyDefinition.isRequired());

            // Validation
            List<ValidationRule> rules = new ArrayList<ValidationRule>(fieldConfig.getValidationRules().size());
            for (ValidationConfig validationConfig : fieldConfig.getValidationRules())
            {
                ValidationRule rule = ValidationRuleFactory.createValidationRule(getContext(), validationConfig);

                // Mandatory is a specific rule managed by a specific UI
                // controller.
                if (rule instanceof MandatoryValidationRule)
                {
                    fieldView.setMandatory(true);
                    continue;
                }
                rules.add(rule);
            }
            fieldView.setValidationRules(rules);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKER MODE
    // ///////////////////////////////////////////////////////////////////////////
    public void setPropertyValue(Object object)
    {
        if (fieldView != null)
        {
            fieldView.setText(getStringValue(getContext(), object));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected Context getContext()
    {
        return (contextRef != null) ? contextRef.get() : null;
    }

    protected Fragment getFragment()
    {
        return fr;
    }

    public boolean requiresPicker()
    {
        return false;
    }

    public boolean isMultiValued()
    {
        return isMultiValued;
    }

    public boolean isValid()
    {
        if (!isMandatory)
        {
            return true;
        }
        else
        {
            // Case : Mandatory + empty
            if (getOutputValue() == null)
            {
                return false;
            }
            else if (isMultiValued && (getOutputValue() instanceof List && ((List) getOutputValue()).isEmpty())) { return false; }
        }
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // String rendition
    // ///////////////////////////////////////////////////////////////////////////
    public static String getStringValue(Context context, Object object)
    {
        if (object == null) { return null; }
        if (object instanceof Boolean)
        {
            return ((Boolean) (object)) ? context.getString(R.string.yes) : context.getString(R.string.no);
        }
        else if (object instanceof GregorianCalendar)
        {
            return DateFormat.getMediumDateFormat(context).format(((GregorianCalendar) object).getTime()) + " "
                    + DateFormat.getTimeFormat(context).format(((GregorianCalendar) object).getTime());
        }
        else
        {
            return object.toString();
        }
    }

}
