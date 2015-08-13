
package org.alfresco.mobile.android.application.ui.form.fields;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesFragment;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesPickerFragment;
import org.alfresco.mobile.android.application.ui.form.FormManager;
import org.alfresco.mobile.android.application.ui.form.adapter.MultiValuedStringAdapter;
import org.alfresco.mobile.android.application.ui.form.picker.AllowablePickerFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DatePickerFragment;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ListAdapter;

import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by jpascal on 28/03/2015.
 */
public class TextField extends BaseField
{
    protected int editLayoutId;

    protected int readLayoutId;

    private Map<String, Object> pickerValue;

    protected MaterialEditText pickerEditText;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public TextField(Context context, FormManager manager, Property property, FieldConfig configuration,
            PropertyDefinition propertyDefinition, boolean isReadMode)
    {
        super(context, manager, property, configuration, propertyDefinition, isReadMode);
        editLayoutId = R.layout.form_edit_text;
        readLayoutId = R.layout.form_read_row;
    }

    @Override
    public String getHumanReadableReadValue()
    {
        if (isMultiValued) { return String.format(
                MessageFormat.format(getContext().getString(R.string.picker_multi_value_selected), multiValue.size()),
                Integer.toString(multiValue.size()), fieldConfig.getLabel()); }
        return super.getHumanReadableReadValue();
    }

    public View setupdReadView()
    {
        if (originalValue == null) { return null; }
        View vr = inflater.inflate(readLayoutId, null);
        TwoLinesViewHolder vh = HolderUtils.configure(vr, fieldConfig.getLabel(), getHumanReadableReadValue(), -1);
        vr.setFocusable(false);
        vh.bottomText.setId(UIUtils.generateViewId());
        vh.topText.setId(UIUtils.generateViewId());

        readView = vr;

        return vr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VALUES
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Object getEditionValue()
    {
        if (isMultiValued)
        {
            return multiValue;
        }
        else if (editionView instanceof MaterialEditText)
        {
            editionValue = ((MaterialEditText) editionView).getText().toString();
            return TextUtils.isEmpty((String) editionValue) ? null : editionValue;
        }
        else if (pickerEditText != null)
        {
            editionValue = pickerEditText.getText().toString();
            return TextUtils.isEmpty((String) editionValue) ? null : editionValue;
        }
        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEW GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void setEditionValue(Object object)
    {
        if (isMultiValued && object instanceof List)
        {
            multiValue = (List<String>) object;
            pickerEditText.setText(getHumanReadableEditionValue());
        }
        else if (hasAllowableValues && object instanceof Map)
        {
            // TODO Fix how we retrieve values from a list of choice.
            // Check get(0)
            pickerValue = (Map<String, Object>) object;
            for (Map.Entry<String, Object> entry : pickerValue.entrySet())
            {
                pickerEditText.setText(getStringValue(getContext(), entry.getValue()));
            }
        }
        else
        {
            super.setEditionValue(object);
        }
    }

    public View setupEditionView(Object value)
    {
        editionValue = value;

        if (hasAllowableValues || isMultiValued) { return setupMultiValueEditionView(); }

        View vr = inflater.inflate(editLayoutId, null);
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

        editionView = vr;

        return vr;
    }

    protected View setupMultiValueEditionView()
    {
        View vr = inflater.inflate(R.layout.form_picker, null);
        pickerEditText = (MaterialEditText) vr.findViewById(R.id.picker);
        pickerEditText.setText(getHumanReadableEditionValue());

        // Asterix if required
        pickerEditText.setFloatingLabelText(getLabelText(fieldConfig.getLabel()));

        pickerEditText.setHint(getLabelText(fieldConfig.getLabel()));
        pickerEditText.setFloatingLabelAlwaysShown(true);
        pickerEditText.setFocusable(false);

        pickerEditText.setIconRight(R.drawable.ic_menu_expand);

        editionView = vr;

        checkReadOnly(editionView);

        return vr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTING MODE
    // ///////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    public ListAdapter getListAdapter(AlfrescoFragment fr)
    {
        if (isMultiValued)
        {
            return new MultiValuedStringAdapter(fr, R.layout.row_two_lines, new ArrayList<>((List<String>) multiValue),
                    true);
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
            multiValue.remove(object);
            setupEditionView(multiValue);
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
            setupEditionView(multiValue);
        }
    }

    public void startPicker(AlfrescoFragment fr, String tag)
    {
        super.setFragment(getFragment());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKER MODE
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public Serializable getOutputValue()
    {
        if (data == null) { return super.getOutputValue(); }
        if (data.isMultiValued())
        {
            return (Serializable) multiValue;
        }
        else
        {
            return super.getOutputValue();
        }
    }

    @Override
    public void setFragment(AlfrescoFragment fr)
    {
        super.setFragment(fr);
        if (getFragment() != null && editionView != null && !isReadOnly)
        {
            if (hasAllowableValues)
            {
                editionView.findViewById(R.id.picker_container).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        // INFO Change here for multi selection
                        AllowablePickerFragment picker = AllowablePickerFragment.newInstance(
                                fieldConfig.getModelIdentifier(), EditPropertiesFragment.TAG, true,
                                fieldConfig.getLabel());
                        picker.setPropertyDefinition(getOutputValue(), propertyDefinition);
                        picker.show(getFragment().getFragmentManager(), DatePickerFragment.TAG);
                    }
                });
            }
            else if (isMultiValued)
            {
                editionView.findViewById(R.id.picker_container).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        EditPropertiesPickerFragment.newInstance(fieldConfig.getModelIdentifier())
                                .show(getFragment().getFragmentManager(), EditPropertiesPickerFragment.TAG);
                    }
                });
            }
        }
    }

    public boolean isPickerRequired()
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
}
