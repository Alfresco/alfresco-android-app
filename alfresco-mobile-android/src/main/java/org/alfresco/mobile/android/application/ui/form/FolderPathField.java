/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.ui.form;

import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;

import android.content.Context;
import android.graphics.Paint;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class FolderPathField extends BaseField
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public FolderPathField(Context context, Property property, FieldConfig configuration)
    {
        super(context, property, configuration);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // READ MODE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getHumanReadableValue()
    {
        // Manage single value
        if (originalValue == null) { return null; }
        return super.getHumanReadableValue();
    }

    @Override
    public View createReadableView()
    {
        final String value = getHumanReadableValue();
        if (TextUtils.isEmpty(value)) { return null; }

        View vr = inflater.inflate(R.layout.form_read_row, null);
        TextView tv = (TextView) vr.findViewWithTag("propertyName");
        tv.setText(fieldConfig.getLabel());
        tv = (TextView) vr.findViewWithTag("propertyValue");
        tv.setText(value);
        tv.setClickable(true);
        tv.setFocusable(true);
        tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (v.getContext() instanceof FragmentActivity)
                {
                    DocumentFolderBrowserFragment.with((FragmentActivity) v.getContext()).path(value).shortcut(true)
                            .display();
                }
            }
        });
        return vr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION MODE
    // ///////////////////////////////////////////////////////////////////////////
    public Object getOutputValue()
    {
        if (property == null) { return super.getOutputValue(); }
        if (property.isMultiValued())
        {
            return multiValue;
        }
        else
        {
            return super.getOutputValue();
        }
    }

    @Override
    public View getEditView(ModelDefinition typeDefinition, ViewGroup hookView)
    {
        return null;
    }

    public boolean requiresPicker()
    {
        return false;
    }

}
