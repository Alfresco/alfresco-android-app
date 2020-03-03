
package org.alfresco.mobile.android.application.ui.form.fields;

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.ui.form.FormManager;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import android.content.Context;
import android.graphics.Paint;
import androidx.fragment.app.FragmentActivity;
import android.view.View;

/**
 * Created by jpascal on 28/03/2015.
 */
public class FolderPathField extends BaseField
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public FolderPathField(Context context, FormManager manager, Property property, FieldConfig configuration,
            PropertyDefinition propertyDefinition, boolean isReadMode)
    {
        super(context, manager, property, configuration, propertyDefinition, isReadMode);
    }

    public View setupdReadView()
    {
        if (originalValue == null) { return null; }
        View vr = inflater.inflate(R.layout.row_two_line_inverse, null);
        vr.setFocusable(false);
        TwoLinesViewHolder vh = HolderUtils.configure(vr, fieldConfig.getLabel(), getHumanReadableReadValue(), -1);
        vh.bottomText.setClickable(true);
        vh.bottomText.setFocusable(true);
        vh.bottomText.setMaxLines(10);
        vh.bottomText.setPaintFlags(vh.bottomText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        vh.bottomText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (v.getContext() instanceof FragmentActivity)
                {
                    DocumentFolderBrowserFragment.with((FragmentActivity) v.getContext()).path((String) originalValue)
                            .shortcut(true).display();
                }
            }
        });
        readView = vr;
        return vr;
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
