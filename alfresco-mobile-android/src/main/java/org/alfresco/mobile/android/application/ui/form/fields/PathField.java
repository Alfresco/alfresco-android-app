
package org.alfresco.mobile.android.application.ui.form.fields;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.ui.form.FormManager;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

/**
 * Created by jpascal on 28/03/2015.
 */
public class PathField extends BaseField
{
    public static final String EXTRA_PARENT_FOLDER = "extraParentFolder";

    private String pathValue;

    private TwoLinesViewHolder vh;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public PathField(Context context, FormManager manager, Property property, FieldConfig configuration,
            PropertyDefinition propertyDefinition, boolean isReadMode)
    {
        super(context, manager, property, configuration, propertyDefinition, isReadMode);
    }

    public View setupdReadView()
    {
        View vr = inflater.inflate(R.layout.row_two_line_inverse, null);
        vr.setFocusable(false);
        vh = HolderUtils.configure(vr, fieldConfig.getLabel(), getHumanReadableReadValue(), -1);
        vh.bottomText.setClickable(true);
        vh.bottomText.setFocusable(true);
        vh.bottomText.setPaintFlags(vh.bottomText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        vh.bottomText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (v.getContext() instanceof FragmentActivity)
                {
                    DocumentFolderBrowserFragment.with((FragmentActivity) v.getContext()).path(pathValue).shortcut(true)
                            .display();
                }
            }
        });

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
        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEW GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
    public View setupEditionView(Object value)
    {
        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUIRE EXTRA
    // ///////////////////////////////////////////////////////////////////////////
    public boolean requireExtra()
    {
        return true;
    }

    public void setExtra(Bundle b)
    {
        if (b.containsKey(EXTRA_PARENT_FOLDER))
        {
            Folder parentFolder = (Folder) b.getSerializable(EXTRA_PARENT_FOLDER);
            if (parentFolder == null) { return; }
            pathValue = parentFolder.getPropertyValue(PropertyIds.PATH);
            if (vh != null && !TextUtils.isEmpty(pathValue))
            {
                vh.bottomText.setText(pathValue);
            }
        }
    }

}
