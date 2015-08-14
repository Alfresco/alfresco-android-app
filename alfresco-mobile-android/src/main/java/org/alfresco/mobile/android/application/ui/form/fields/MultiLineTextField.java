package org.alfresco.mobile.android.application.ui.form.fields;

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.form.FormManager;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Context;
import android.text.InputType;
import android.view.View;

/**
 * Created by jpascal on 28/03/2015.
 */
public class MultiLineTextField extends TextField
{
    protected boolean hideLabel = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public MultiLineTextField(Context context, FormManager manager, Property property, FieldConfig configuration,
            PropertyDefinition propertyDefinition, boolean isReadMode)
    {
        super(context, manager, property, configuration, propertyDefinition, isReadMode);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VALUES
    // ///////////////////////////////////////////////////////////////////////////
    public View setupdReadView()
    {
        if (originalValue == null) { return null; }
        View vr = inflater.inflate(R.layout.row_two_line_inverse, null);
        TwoLinesViewHolder tvh = HolderUtils.configure(vr, fieldConfig.getLabel(), getHumanReadableReadValue(), -1);
        tvh.bottomText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        tvh.bottomText.setSingleLine(false);
        tvh.bottomText.setId(UIUtils.generateViewId());
        tvh.topText.setId(UIUtils.generateViewId());
        if (hideLabel)
        {
            tvh.topText.setVisibility(View.GONE);
        }
        vr.setFocusable(false);

        readView = vr;

        return vr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEW GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
}
