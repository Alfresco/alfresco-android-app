
package org.alfresco.mobile.android.application.ui.form;

import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.PropertyType;
import org.alfresco.mobile.android.application.ui.form.fields.FormFieldTypes;

import android.text.TextUtils;

/**
 * Created by jpascal on 28/03/2015.
 */
public enum FieldTypeRegistry
{
    TEXT(FormFieldTypes.FIELD_TEXT), MULTI_LINE_TEXT(FormFieldTypes.FIELD_TEXT.concat(".multilines")), FILESIZE(
            FormFieldTypes.FIELD_FILESIZE), DATETIME(FormFieldTypes.FIELD_DATETIME), NUMBER(
                    FormFieldTypes.FIELD_NUMBER), FIELD_FOLDER_PATH(FormFieldTypes.FIELD_FOLDER_PATH), FIELD_PATH(
                            FormFieldTypes.FIELD_PATH), DECIMAL(FormFieldTypes.FIELD_DECIMAL), BOOLEAN(
                                    FormFieldTypes.FIELD_BOOLEAN), TAG(FormFieldTypes.FIELD_TAGS);

    private final String value;

    FieldTypeRegistry(String v)
    {
        value = v;
    }

    public static FieldTypeRegistry fromValue(String v)
    {
        for (FieldTypeRegistry c : FieldTypeRegistry.values())
        {
            if (c.value.equals(v)) { return c; }
        }
        throw new IllegalArgumentException(v);
    }

    public String value()
    {
        return value;
    }

    public static String getFieldType(ModelDefinition definition, String identifier)
    {
        if (definition == null || TextUtils.isEmpty(identifier)
                || definition.getPropertyDefinition(identifier) == null) { return FormFieldTypes.FIELD_TEXT; }
        PropertyType type = definition.getPropertyDefinition(identifier).getType();
        switch (type)
        {
            case BOOLEAN:
                return FormFieldTypes.FIELD_BOOLEAN;
            case DATETIME:
                return FormFieldTypes.FIELD_DATETIME;
            case DECIMAL:
                return FormFieldTypes.FIELD_DECIMAL;
            case ID:
                return FormFieldTypes.FIELD_TEXT;
            case INTEGER:
                return FormFieldTypes.FIELD_NUMBER;
            default:
                return FormFieldTypes.FIELD_TEXT;
        }
    }
}
