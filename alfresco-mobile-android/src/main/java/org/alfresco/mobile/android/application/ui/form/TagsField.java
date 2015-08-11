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

import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.Tag;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.widget.TagsSpanRenderer;
import org.alfresco.mobile.android.async.OperationEvent;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.tag.TagsEvent;
import org.alfresco.mobile.android.async.tag.TagsOperationRequest;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jmpergar.awesometext.AwesomeTextHandler;

public class TagsField extends BaseField
{
    private TextView tv;

    private static final String COMMA_PATTERN = "(¤[\\p{L}0-9-_ #$;,^\\*\\.]+¤¤)";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public TagsField(Context context, Property property, FieldConfig configuration)
    {
        super(context, property, configuration);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUIRE REST API
    // ///////////////////////////////////////////////////////////////////////////
    public boolean requireAsync()
    {
        return true;
    }

    public OperationRequest.OperationBuilder requestData(Object extra)
    {
        return new TagsOperationRequest.Builder((Node) extra);
    }

    public void setOperationData(OperationEvent event)
    {
        if (event instanceof TagsEvent)
        {
            PagingResult<Tag> result = ((TagsEvent) event).data;

            // No Tags but taggable aspect present
            // Hide tags group
            if (result.getTotalItems() == 0)
            {
                ((ViewGroup) tv.getParent().getParent().getParent()).setVisibility(View.GONE);
                return;
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < result.getTotalItems(); i++)
            {
                builder.append(" ¤").append(result.getList().get(i).getValue()).append("¤¤ ");
            }
            setPropertyValue(builder.toString());

            AwesomeTextHandler awesomeTextViewHandler = new AwesomeTextHandler();
            awesomeTextViewHandler.addViewSpanRenderer(COMMA_PATTERN, new TagsSpanRenderer()).setView(tv);
        }
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
        final String value = getContext().getString(R.string.load_more_progress);
        View vr = inflater.inflate(R.layout.form_read_textmultiline, null);
        tv = (TextView) vr.findViewWithTag("propertyName");
        tv.setText(fieldConfig.getLabel());
        tv.setVisibility(View.GONE);
        tv = (TextView) vr.findViewWithTag("propertyValue");
        tv.setText(value);
        tv.setLineSpacing(0f, 1.1f);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 0, 0, 0);
        tv.setClickable(false);
        tv.setFocusable(false);
        return vr;
    }

    public void setPropertyValue(Object object)
    {
        if (tv != null)
        {
            tv.setText(getStringValue(getContext(), object));
        }
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
