/*******************************************************************************
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.application.ui.form;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

public class PathField extends FolderPathField
{
    public static final String EXTRA_PARENT_FOLDER = "extraParentFolder";

    private TextView tv;

    private String pathValue;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public PathField(Context context, Property property, FieldConfig configuration)
    {
        super(context, property, configuration);
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
            pathValue = parentFolder.getPropertyValue(PropertyIds.PATH);
            if (!TextUtils.isEmpty(pathValue))
            {
                tv.setText(pathValue);
            }
        }
    }

    @Override
    public View createReadableView()
    {
        pathValue = "";

        View vr = inflater.inflate(R.layout.form_read_row, null);
        tv = (TextView) vr.findViewWithTag("propertyName");
        tv.setText(fieldConfig.getLabel());
        tv = (TextView) vr.findViewWithTag("propertyValue");
        tv.setText(pathValue);
        tv.setClickable(true);
        tv.setFocusable(true);
        tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (v.getContext() instanceof Activity)
                {
                    DocumentFolderBrowserFragment.with((Activity) v.getContext()).path(pathValue).shortcut(true)
                            .display();
                }
            }
        });
        return vr;
    }

}
