/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ConfigConstants;
import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesPickerFragment;
import org.alfresco.mobile.android.application.ui.form.adapter.NodeFieldAdapter;
import org.alfresco.mobile.android.application.ui.form.picker.DocumentPickerFragment;
import org.alfresco.mobile.android.application.ui.form.views.PickerFieldView;

import android.app.Fragment;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class NodeField extends BaseField
{
    public static final String OUTPUT_OBJECT = "object";

    public static final String OUTPUT_ID = "id";

    private Map<String, Node> nodes = new HashMap<String, Node>(0);

    private Boolean allowMultipleSelection = false;

    private String outputValue = OUTPUT_ID;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public NodeField(Context context, Property property, FieldConfig configuration)
    {
        super(context, property, configuration);
        if (configuration.getParameter(ConfigConstants.ALLOW_MULTIPLE_SELECTION_VALUE) != null)
        {
            allowMultipleSelection = (Boolean) configuration
                    .getParameter(ConfigConstants.ALLOW_MULTIPLE_SELECTION_VALUE);
        }
        if (configuration.getParameter(ConfigConstants.OUTPUT_VALUE) != null)
        {
            outputValue = (String) configuration.getParameter(ConfigConstants.OUTPUT_VALUE);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTING MODE
    // ///////////////////////////////////////////////////////////////////////////
    public ListAdapter getListAdapter(Fragment fr)
    {
        return new NodeFieldAdapter(fr, R.layout.app_item_row, new ArrayList<Node>(nodes.values()), outputValue);
    }

    public void remove(Object object)
    {
        if (object instanceof Node)
        {
            nodes.remove(((Node) object).getIdentifier());
        }
        setPropertyValue(nodes);
    }

    public void startPicker(Fragment fr, String tag)
    {
        super.initPicker(fr);
        if (fr.getActivity() instanceof PrivateDialogActivity)
        {
            ((PrivateDialogActivity) fr.getActivity()).setFieldId(fieldConfig.getModelIdentifier());
        }
        DocumentPickerFragment.with(fr.getActivity()).display();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // READ MODE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getHumanReadableValue()
    {
        if (property.getValue() == null) { return ""; }
        return super.getHumanReadableValue();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EDITION MODE
    // ///////////////////////////////////////////////////////////////////////////
    public View getEditView(ModelDefinition typeDefinition, ViewGroup hookView)
    {
        this.modelDefinition = typeDefinition;
        fieldView = (PickerFieldView) inflater.inflate(R.layout.form_edit_picker, hookView, false);

        // Configure
        fieldView.setText((getOutputValue() == null) ? "" : getOutputValue().toString());
        fieldView.setHint(fieldConfig.getLabel());
        configureFromDefinition(fieldView);
        fieldView.setReadOnly(isReadOnly);

        return fieldView;
    }

    @Override
    public Object getOutputValue()
    {
        if (nodes.values() == null || nodes.values().isEmpty()) { return null; }

        if (OUTPUT_ID.equals(outputValue))
        {
            List<String> names = new ArrayList<String>(nodes.size());
            for (Node node : nodes.values())
            {
                names.add(node.getIdentifier());
            }
            return (allowMultipleSelection) ? names : names.get(0);
        }
        else if (OUTPUT_OBJECT.equals(outputValue))
        {
            return (allowMultipleSelection) ? new ArrayList<Node>(nodes.values()) : new ArrayList<Node>(nodes.values())
                    .get(0);
        }
        else
        {
            List<String> values = new ArrayList<String>(nodes.size());
            for (Node node : nodes.values())
            {
                if (node.getProperty(outputValue) != null)
                {
                    values.add(BaseField.getStringValue(getContext(), node.getProperty(outputValue).getValue()));
                }
                else
                {
                    values.add(null);
                }
            }
            return (allowMultipleSelection) ? values : values.get(0);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKER MODE
    // ///////////////////////////////////////////////////////////////////////////
    public Object getValuePicked()
    {
        return nodes;
    }

    public boolean requiresPicker()
    {
        return true;
    }

    @Override
    public void initPicker(final Fragment fr)
    {
        super.initPicker(fr);
        if (getFragment() != null && fieldView != null && !isReadOnly)
        {
            fieldView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    EditPropertiesPickerFragment.newInstance(fieldConfig.getModelIdentifier()).show(
                            fr.getActivity().getFragmentManager(), EditPropertiesPickerFragment.TAG);
                }
            });
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void setPropertyValue(Object object)
    {
        if (object instanceof Map || object == null)
        {
            nodes = (Map<String, Node>) object;
            String values = "";

            Object outputVal = getOutputValue();
            if (outputVal instanceof List)
            {
                values = String.format(MessageFormat.format(getContext()
                        .getString(R.string.picker_multi_value_selected), ((List) outputVal).size()), Integer
                        .toString(((List) outputVal).size()), fieldConfig.getLabel());
            }
            else if (outputVal instanceof Node)
            {
                values = getEditValue((Node) outputVal);
            }
            else if (outputVal instanceof String)
            {
                values = (String) outputVal;
            }

            fieldView.setText(values);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private String getEditValue(Node node)
    {
        if (node == null) { return ""; }
        return node.getName();
    }
}
