
package org.alfresco.mobile.android.application.ui.form;

import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.ui.form.fields.BaseField;
import org.alfresco.mobile.android.application.ui.form.fields.BooleanField;
import org.alfresco.mobile.android.application.ui.form.fields.DateField;
import org.alfresco.mobile.android.application.ui.form.fields.DecimalField;
import org.alfresco.mobile.android.application.ui.form.fields.FileSizeField;
import org.alfresco.mobile.android.application.ui.form.fields.FolderPathField;
import org.alfresco.mobile.android.application.ui.form.fields.MultiLineTextField;
import org.alfresco.mobile.android.application.ui.form.fields.NumberField;
import org.alfresco.mobile.android.application.ui.form.fields.PathField;
import org.alfresco.mobile.android.application.ui.form.fields.TagsField;
import org.alfresco.mobile.android.application.ui.form.fields.TextField;

import android.content.Context;

/**
 * Created by jpascal on 28/03/2015.
 */
public class FieldTypeFactory
{
    public static BaseField createField(Context context, FormManager manager, String dataType, Property property,
            FieldConfig fieldConfig, ModelDefinition typeDefinition, boolean isReadMode)
    {
        FieldTypeRegistry type = null;
        try
        {
            type = FieldTypeRegistry.fromValue(dataType);
        }
        catch (IllegalArgumentException e)
        {
            // Unsupported field
            return null;
        }

        PropertyDefinition propertyDefinition = null;
        if (typeDefinition != null && typeDefinition.getPropertyDefinition(fieldConfig.getModelIdentifier()) != null)
        {
            propertyDefinition = typeDefinition.getPropertyDefinition(fieldConfig.getModelIdentifier());
        }

        switch (type)
        {
            case TAG:
                return new TagsField(context, manager, property, fieldConfig, propertyDefinition, isReadMode);
            case FILESIZE:
                return new FileSizeField(context, manager, property, fieldConfig, propertyDefinition, isReadMode);
            case FIELD_PATH:
                return new PathField(context, manager, property, fieldConfig, propertyDefinition, isReadMode);
            case FIELD_FOLDER_PATH:
                return new FolderPathField(context, manager, property, fieldConfig, propertyDefinition, isReadMode);
            case BOOLEAN:
                return new BooleanField(context, manager, property, fieldConfig, propertyDefinition, isReadMode);
            case DECIMAL:
                return new DecimalField(context, manager, property, fieldConfig, propertyDefinition, isReadMode);
            case NUMBER:
                return new NumberField(context, manager, property, fieldConfig, propertyDefinition, isReadMode);
            case DATETIME:
                return new DateField(context, manager, property, fieldConfig, propertyDefinition, isReadMode);
            case TEXT:
                // Specialize type depending on parameters
                if (fieldConfig.getParameter(ConfigConstants.SHOW_MULTIPLE_LINES_VALUE) != null)
                {
                    Boolean hasMultipleLines = (Boolean) fieldConfig
                            .getParameter(ConfigConstants.SHOW_MULTIPLE_LINES_VALUE);
                    if (hasMultipleLines)
                    {
                        return new MultiLineTextField(context, manager, property, fieldConfig, propertyDefinition,
                                isReadMode);
                    }
                    else
                    {
                        return new TextField(context, manager, property, fieldConfig, propertyDefinition, isReadMode);
                    }
                }
            default:
                return new TextField(context, manager, property, fieldConfig, propertyDefinition, isReadMode);
        }

    }
}
