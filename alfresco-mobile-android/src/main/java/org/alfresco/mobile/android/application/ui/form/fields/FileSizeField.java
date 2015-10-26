
package org.alfresco.mobile.android.application.ui.form.fields;

import java.math.BigInteger;

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.ui.form.FormManager;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;

/**
 * Created by jpascal on 28/03/2015.
 */
public class FileSizeField extends BaseField
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public FileSizeField(Context context, FormManager manager, Property property, FieldConfig configuration,
            PropertyDefinition propertyDefinition, boolean isReadMode)
    {
        super(context, manager, property, configuration, propertyDefinition, isReadMode);
    }

    @Override
    public String getHumanReadableReadValue()
    {
        if (originalValue == null) { return Formatter.formatFileSize(getContext(), 0L); }
        if (originalValue instanceof String)
        {
            long value = Long.parseLong((String) originalValue);
            return Formatter.formatFileSize(getContext(), value);
        }
        return Formatter.formatFileSize(getContext(), ((BigInteger) originalValue).longValue());

    }

    // ///////////////////////////////////////////////////////////////////////////
    // VALUES
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Object getEditionValue()
    {
        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEW GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
    public View setupEditionView(Object value)
    {
        return null;
    }

}
