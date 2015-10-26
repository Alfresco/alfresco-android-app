package org.alfresco.mobile.android.application.ui.form.fields;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.api.model.config.ValidationConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.form.FormManager;
import org.alfresco.mobile.android.application.ui.form.validation.MandatoryValidationRule;
import org.alfresco.mobile.android.application.ui.form.validation.ValidationRule;
import org.alfresco.mobile.android.application.ui.form.validation.ValidationRuleFactory;
import org.alfresco.mobile.android.async.OperationEvent;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by jpascal on 28/03/2015.
 */
public abstract class BaseField
{
    protected WeakReference<FormManager> formManagerRef;

    protected WeakReference<AlfrescoFragment> fragmentRef;

    protected WeakReference<Context> contextRef;

    protected LayoutInflater inflater;

    protected FieldConfig fieldConfig;

    protected Property data;

    protected Object originalValue;

    protected Object editionValue;

    protected View readView;

    protected View editionView;

    protected boolean isReadMode;

    protected boolean isVisible = true;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    protected boolean isRequired = false;

    protected PropertyDefinition propertyDefinition;

    protected List<ValidationRule> validationRules;

    protected boolean isMultiValued = false;

    protected boolean isReadOnly = false;

    protected List<?> multiValue = new ArrayList<Object>(1);

    protected boolean hasAllowableValues = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public BaseField(Context context, FormManager manager, Property property, FieldConfig fieldConfig,
            PropertyDefinition propertyDefinition, boolean isReadMode)
    {
        this.contextRef = new WeakReference<>(context);
        this.inflater = LayoutInflater.from(context);
        this.fieldConfig = fieldConfig;
        this.data = property;
        this.originalValue = (data != null) ? data.getValue() : null;
        this.formManagerRef = new WeakReference<>(manager);
        this.isReadMode = isReadMode;
        this.propertyDefinition = propertyDefinition;

        validationRules = new ArrayList<>(fieldConfig.getValidationRules().size());
        for (ValidationConfig validationConfig : fieldConfig.getValidationRules())
        {
            ValidationRule rule = ValidationRuleFactory.createValidationRule(getContext(), validationConfig);
            // Mandatory is a specific rule managed by a specific UI
            // controller.
            if (rule instanceof MandatoryValidationRule)
            {
                isRequired = true;
                continue;
            }
            validationRules.add(rule);
        }

        // Define if its mandatory
        // 2 options : By config or By model
        if (propertyDefinition != null && propertyDefinition.isRequired())
        {
            isRequired = true;
        }
        else if (propertyDefinition != null && !propertyDefinition.isRequired() && isRequired)
        {
            isRequired = true;
        }
        else if (propertyDefinition != null && propertyDefinition.isRequired() && !isRequired)
        {
            isRequired = true;
        }
        else
        {
            isRequired = false;
        }

        // Multivalue
        if (property != null)
        {
            this.isMultiValued = property.isMultiValued();
            if (this.isMultiValued)
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

        // Readonly by model or by config
        if (fieldConfig.getParameter(ConfigConstants.READ_ONLY_VALUE) != null)
        {
            this.isReadOnly = (Boolean) fieldConfig.getParameter(ConfigConstants.READ_ONLY_VALUE);
        }
        if (propertyDefinition != null)
        {
            boolean definitionReadOnly = propertyDefinition.isReadOnly();
            if (!isReadOnly && definitionReadOnly)
            {
                this.isReadOnly = true;
            }
        }

        // Allowable values detections
        if (propertyDefinition != null && !propertyDefinition.getAllowableValues().isEmpty())
        {
            this.hasAllowableValues = true;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // READ
    // ///////////////////////////////////////////////////////////////////////////
    public String getLabel()
    {
        return fieldConfig.getLabel();
    }

    public Property getData()
    {
        return data;
    }

    public String getHumanReadableReadValue()
    {
        if (originalValue == null) { return getString(R.string.form_message_empty); }
        return getStringValue(getContext(), originalValue);
    }

    public Object getReadValue()
    {
        return originalValue;
    }

    public View setupdReadView()
    {
        if (isMultiValued)
        {
            // Multi Value
            if (multiValue == null || multiValue.isEmpty()) { return null; }

            readView = inflater.inflate(R.layout.form_read_multivalue, null);
            TextView fieldNameView = (TextView) readView.findViewWithTag("propertyName");
            fieldNameView.setText(fieldConfig.getLabel());
            LinearLayout multiValueGroup = (LinearLayout) readView.findViewById(R.id.group_multivalues);
            for (int i = 0; i < multiValue.size(); i++)
            {
                String value = getStringValue(getContext(), multiValue.get(i));

                View singleView = inflater.inflate(R.layout.form_read_row, null);
                TwoLinesViewHolder vh = HolderUtils.configure(singleView, i + ".", value, -1);
                vh.bottomText.setId(UIUtils.generateViewId());
                vh.topText.setId(UIUtils.generateViewId());
                multiValueGroup.addView(singleView);
            }
        }
        else
        {
            if (originalValue == null) { return null; }
            readView = inflater.inflate(R.layout.form_read_row, null);
            TwoLinesViewHolder vh = HolderUtils.configure(readView, fieldConfig.getLabel(), getHumanReadableReadValue(),
                    -1);
            vh.bottomText.setId(UIUtils.generateViewId());
            vh.topText.setId(UIUtils.generateViewId());

            readView.setFocusable(false);
        }
        return readView;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION VALUE
    // ///////////////////////////////////////////////////////////////////////////
    public String getHumanReadableEditionValue()
    {
        if (editionValue == null) { return null; }
        return editionValue.toString();
    }

    public Object getEditionValue()
    {
        return editionValue;
    }

    public void setEditionValue(Object object)
    {
        editionValue = object;
        updateEditionView();
    }

    public boolean hasEditionValueChanged()
    {
        if (originalValue == null && getEditionValue() == null)
        {
            return false;
        }
        else if (originalValue != null && originalValue.equals(getEditionValue()))
        {
            // Value has not changed
            // Special case where previous value already present
            return true;
        }
        else
        {
            return true;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION VIEW
    // ///////////////////////////////////////////////////////////////////////////
    public View getEditionView()
    {
        return editionView;
    }

    protected void updateEditionView()
    {
        if (getHumanReadableEditionValue() != null)
        {
            ((MaterialEditText) editionView).setText(getHumanReadableEditionValue());
        }
    }

    public View setupEditionView(Object value)
    {
        editionValue = value;

        View vr = inflater.inflate(R.layout.form_edit_text, null);
        ((MaterialEditText) vr).setText(getHumanReadableEditionValue());

        if (getHumanReadableEditionValue() != null)
        {
            ((MaterialEditText) vr).setFloatingLabelAlwaysShown(true);
        }

        // Asterix if required
        ((MaterialEditText) vr).setFloatingLabelText(getLabelText(fieldConfig.getLabel()));
        ((MaterialEditText) vr).setHint(getLabelText(fieldConfig.getLabel()));

        ((MaterialEditText) vr).addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                getFormManager().evaluateViews();
                if (hasValidationRuleError())
                {
                    showValidationError();
                }
                else
                {
                    ((MaterialEditText) editionView).setError(null);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        checkReadOnly(editionView);

        editionView = vr;

        return vr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VALIDATION & ERROR
    // ///////////////////////////////////////////////////////////////////////////
    public boolean isRequired()
    {
        return isRequired;
    }

    public boolean isValid()
    {
        if (isRequired && getEditionValue() != null && !hasValidationRuleError()) { return true; }
        return false;
    }

    protected String getValidationError()
    {
        if (validationRules == null) { return null; }
        for (ValidationRule rule : validationRules)
        {
            if (rule == null || getEditionValue() == null)
            {
                continue;
            }
            if (!rule.isValid(getEditionValue())) { return rule.getErrorMessage(); }
        }
        if (isRequired) { return String.format(getString(R.string.form_error_message_required),
                fieldConfig.getLabel()); }
        return null;
    }

    protected boolean hasValidationRuleError()
    {
        if (validationRules == null) { return false; }
        for (ValidationRule rule : validationRules)
        {
            if (rule == null || getEditionValue() == null)
            {
                continue;
            }
            if (!rule.isValid(getEditionValue())) { return true; }
        }
        return false;
    }

    public void showError()
    {
        if (isValid()) { return; }
        ((MaterialEditText) editionView).setError(getValidationError());
    }

    public void showValidationError()
    {
        ((MaterialEditText) editionView).setError(getValidationError());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKERS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean isPickerRequired()
    {
        return false;
    }

    public void setFragment(AlfrescoFragment fr)
    {
        this.fragmentRef = new WeakReference<>(fr);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // OUTPUT VALUE
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Generally the String representation of the editionValue
     *
     * @return
     */
    public Serializable getOutputValue()
    {
        if (isReadMode) { return (Serializable) getReadValue(); }
        if (!isReadMode && isReadOnly) { return null; }
        return isVisible ? (Serializable) getEditionValue() : null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REFRESH
    // ///////////////////////////////////////////////////////////////////////////
    public void refreshEditionView()
    {
        // We reaffect the same value to update the view with the latest edition
        // value
        setEditionValue(getEditionValue());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected AlfrescoFragment getFragment()
    {
        return (fragmentRef != null) ? fragmentRef.get() : null;
    }

    protected Context getContext()
    {
        return (contextRef != null) ? contextRef.get() : null;
    }

    protected FormManager getFormManager()
    {
        return (formManagerRef != null) ? formManagerRef.get() : null;
    }

    protected String getString(int stringId)
    {
        return (getContext() != null) ? getContext().getString(stringId) : null;
    }

    protected String getLabelText(String value)
    {
        if (propertyDefinition == null) { return value; }
        if (TextUtils.isEmpty(value)) { return value; }
        return propertyDefinition.isRequired() ? value + " *" : value;
    }

    public boolean isMultiValued()
    {
        return isMultiValued;
    }

    protected void checkReadOnly(View v)
    {
        if (isReadOnly && v != null)
        {
            v.setEnabled(false);
            v.setFocusable(false);
            v.setClickable(false);
        }
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
    // EXTRA
    // ///////////////////////////////////////////////////////////////////////////
    public boolean requireExtra()
    {
        return false;
    }

    public void setExtra(Bundle b)
    {
        // TO Be Implemented by subclass
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKER/LIST MODE
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Display the list of value for this field.
     *
     * @param fr
     * @return
     */
    public ListAdapter getListAdapter(AlfrescoFragment fr)
    {
        return null;
    }

    /**
     * Open a picker fragment associated to the field.
     *
     * @param fr
     * @param tag
     */
    public void startPicker(AlfrescoFragment fr, String tag)
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

    public Object getValuePicked()
    {
        return getEditionValue();
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
