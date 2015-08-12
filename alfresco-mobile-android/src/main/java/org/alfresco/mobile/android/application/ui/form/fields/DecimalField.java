package org.alfresco.mobile.android.application.ui.form.fields;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.form.FormManager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by jpascal on 28/03/2015.
 */
public class DecimalField extends BaseField
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public DecimalField(Context context, FormManager manager, Property property, FieldConfig configuration,
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
        if (editionView instanceof MaterialEditText)
        {
            String value = ((MaterialEditText) editionView).getText().toString();
            return (TextUtils.isEmpty(value)) ? null : Double.parseDouble(value);
        }
        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // READ
    // ///////////////////////////////////////////////////////////////////////////
    public String getHumanReadableReadValue()
    {
        if (originalValue == null) { return getString(R.string.form_message_empty); }
        if (editionValue instanceof Double)
        {
            DecimalFormat df = new DecimalFormat("0.#");
            return df.format((Double) editionValue);
        }
        return originalValue.toString();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION VALUE
    // ///////////////////////////////////////////////////////////////////////////
    public String getHumanReadableEditionValue()
    {
        if (editionValue == null) { return null; }
        if (editionValue instanceof Double)
        {
            DecimalFormat df = new DecimalFormat("0.#");
            return df.format((Double) editionValue);
        }
        return editionValue.toString();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION VIEW
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View setupEditionView(Object value)
    {
        MaterialEditText edit = (MaterialEditText) super.setupEditionView(value);
        edit.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
                | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
        return edit;
    }

    @Override
    public Serializable getOutputValue()
    {
        String value = ((MaterialEditText) editionView).getText().toString();
        if (TextUtils.isEmpty(value)) { return null; }
        return new BigDecimal(value);
    }
}
