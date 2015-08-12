
package org.alfresco.mobile.android.application.ui.form.fields;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.api.utils.DateUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.form.FormManager;
import org.alfresco.mobile.android.application.ui.form.picker.DatePickerFragment;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;

import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by jpascal on 28/03/2015.
 */
public class DateField extends BaseField
{
    protected MaterialEditText editText;

    private boolean showTime = false;

    private Date minDate = null, maxDate = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public DateField(Context context, FormManager manager, Property property, FieldConfig configuration,
            PropertyDefinition propertyDefinition, boolean isReadMode)
    {
        super(context, manager, property, configuration, propertyDefinition, isReadMode);
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
    // READ
    // ///////////////////////////////////////////////////////////////////////////
    public String getHumanReadableReadValue()
    {
        if (originalValue == null) { return getString(R.string.form_message_empty); }
        String readValue = "";
        if (originalValue instanceof Date)
        {
            readValue = DateFormat.getMediumDateFormat(getContext()).format(originalValue);
        }
        if (originalValue instanceof GregorianCalendar)
        {
            readValue = DateFormat.getMediumDateFormat(getContext())
                    .format(((GregorianCalendar) originalValue).getTime());
            if (showTime)
            {
                readValue = readValue.concat(" ").concat(
                        DateFormat.getTimeFormat(getContext()).format(((GregorianCalendar) originalValue).getTime()));
            }
        }
        return readValue;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION VALUES
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getHumanReadableEditionValue()
    {
        if (editionValue == null) { return ""; }
        String readValue = "";
        if (editionValue instanceof Date)
        {
            GregorianCalendar tmp = new GregorianCalendar();
            tmp.setTime((Date) editionValue);
            editionValue = tmp;
        }
        if (editionValue instanceof GregorianCalendar)
        {
            readValue = DateFormat.getMediumDateFormat(getContext())
                    .format(((GregorianCalendar) editionValue).getTime());
            if (showTime)
            {
                readValue = readValue.concat(" ").concat(
                        DateFormat.getTimeFormat(getContext()).format(((GregorianCalendar) editionValue).getTime()));
            }
        }
        return readValue;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION VIEW
    // ///////////////////////////////////////////////////////////////////////////
    protected void updateEditionView()
    {
        editText.setText(getHumanReadableEditionValue());
        getFormManager().evaluateViews();
    }

    @Override
    public View setupEditionView(Object value)
    {
        editionValue = value;

        View vr = inflater.inflate(R.layout.form_date, null);
        editText = (MaterialEditText) vr.findViewById(R.id.date_picker);
        editText.setText(getHumanReadableEditionValue());

        // Asterix if required
        editText.setFloatingLabelText(getLabelText(fieldConfig.getLabel()));

        editText.setHint(getLabelText(fieldConfig.getLabel()));
        editText.setFloatingLabelAlwaysShown(true);
        editText.setFocusable(false);

        editText.setIconRight(R.drawable.ic_event_grey);

        editionView = vr;

        checkReadOnly(editionView);

        return vr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKERS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean isPickerRequired()
    {
        return true;
    }

    public void setFragment(AlfrescoFragment fr)
    {
        super.setFragment(fr);
        if (getFragment() != null && editionView != null && !isReadOnly)
        {
            editionView.findViewById(R.id.button_container).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Long time = (editionValue != null) ? (editionValue instanceof GregorianCalendar)
                            ? ((GregorianCalendar) editionValue).getTimeInMillis() : ((Date) editionValue).getTime()
                            : null;

                    DatePickerFragment
                            .newInstance(fieldConfig.getModelIdentifier(), getFragment().getTag(), time,
                                    (minDate != null) ? minDate.getTime() : null,
                                    (maxDate != null) ? maxDate.getTime() : null, showTime)
                            .show(getFragment().getFragmentManager(), DatePickerFragment.TAG);

                }
            });
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // OUTPUT VALUE
    // ///////////////////////////////////////////////////////////////////////////
    public Serializable getOutputValue()
    {
        if (editionValue == null) { return null; }
        if (editionValue instanceof Date) { return ((Date) editionValue); }
        if (editionValue instanceof GregorianCalendar) { return ((GregorianCalendar) editionValue); }
        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ERROR
    // ///////////////////////////////////////////////////////////////////////////
    public void showError()
    {
        if (isValid()) { return; }
        editText.setError(String.format(getString(R.string.form_error_message_required), fieldConfig.getLabel()));
    }
}
