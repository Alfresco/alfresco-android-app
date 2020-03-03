
/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
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

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ModelDefinition;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.api.model.config.FieldGroupConfig;
import org.alfresco.mobile.android.api.model.config.FormConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.BaseConfigManager;
import org.alfresco.mobile.android.application.ui.form.fields.BaseField;
import org.alfresco.mobile.android.application.ui.form.fields.FormFieldTypes;
import org.alfresco.mobile.android.application.ui.form.fields.PathField;
import org.alfresco.mobile.android.async.OperationEvent;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by jpascal on 28/03/2015.
 */
public class FormManager extends BaseConfigManager
{
    protected ViewGroup vRoot;

    protected AlfrescoFragment fr;

    protected WeakReference<FragmentActivity> activity;

    private Map<String, BaseField> fieldsIndex;

    private ArrayList<BaseField> fieldsOrderIndex = new ArrayList<>();

    private ArrayList<BaseField> mandatoryFields = new ArrayList<>(0);

    private List<String> modelRequested = new ArrayList<>();

    private HashMap<String, RequestHolder> operationFieldIndex = new HashMap<>();

    private BaseField currentPickerField;

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private Node node;

    private Folder parentFolder;

    private ModelDefinition typeDefinition;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public FormManager(AlfrescoFragment fr, ConfigService configService, ViewGroup vRoot)
    {
        super(fr.getActivity(), configService);
        this.activity = new WeakReference<>(fr.getActivity());
        this.fr = fr;
        this.vRoot = vRoot;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public void prepare(Node node, Folder parentFolder)
    {
        this.node = node;
        this.parentFolder = parentFolder;
    }

    public void prepare(ModelDefinition typeDefinition, Node node)
    {
        this.node = node;
        this.typeDefinition = typeDefinition;
    }

    public void displayReadForm()
    {
        vRoot.removeAllViews();
        View rootView = generateForm(ConfigConstants.VIEW_NODE_PROPERTIES, LayoutInflater.from(getActivity()), false);

        vRoot.addView(rootView);
    }

    public View displayEditForm()
    {
        vRoot.removeAllViews();
        return generateForm(ConfigConstants.VIEW_EDIT_PROPERTIES, LayoutInflater.from(getActivity()), true);
    }

    public HashMap<String, Serializable> getValues()
    {
        HashMap<String, Serializable> props = new HashMap<>(fieldsIndex.size());
        for (Map.Entry<String, BaseField> entry : fieldsIndex.entrySet())
        {
            if (entry.getValue().hasEditionValueChanged())
            {
                props.put(entry.getKey(), entry.getValue().getOutputValue());
            }
        }
        return props;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean checkValidation()
    {
        if (mandatoryFields.isEmpty()) { return true; }
        boolean isValid = true;
        boolean requiredFocus = false;
        for (BaseField field : mandatoryFields)
        {
            if (!field.isValid())
            {
                field.showError();
                isValid = false;

                // Focus the first view that is invalid
                if (!requiredFocus)
                {
                    field.getEditionView().requestFocus();
                    requiredFocus = true;
                }
            }
        }
        return isValid;
    }

    public void setPropertyValue(String propertyId, Object object)
    {
        fieldsIndex.get(propertyId).setEditionValue(object);
    }

    public BaseField getField(String propertyId)
    {
        return fieldsIndex.get(propertyId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
    protected View generateForm(String formIdentifier, LayoutInflater li, boolean isEdition)
    {
        ViewGroup rootView = (ViewGroup) li.inflate(R.layout.form_root, null);
        ViewGroup hookView = rootView;
        fieldsIndex = new HashMap<>();

        // Retrieve form config by Id
        ConfigScope scope = configManager.getCurrentScope();
        scope.add(ConfigScope.NODE, node);
        FormConfig config = configService.getFormConfig(formIdentifier, scope);

        ViewGroup groupRoot = null;
        BaseField field;

        if (config.getGroups() != null && config.getGroups().size() > 0)
        {
            // Header
            if (!TextUtils.isEmpty(config.getLabel()))
            {
                ViewGroup headerView = (ViewGroup) li.inflate(R.layout.form_header, null);
                TextView tv = (TextView) headerView.findViewById(R.id.title);
                tv.setText(config.getLabel());
                hookView = (ViewGroup) headerView.findViewById(R.id.group_panel);
                rootView.addView(headerView);
            }

            // Add Children
            for (FieldGroupConfig group : config.getGroups())
            {
                createPropertyFields(group, hookView, li, isEdition);
            }
        }

        // Now time to isVisible everyone
        evaluateViews();

        return rootView;
    }

    private void createPropertyFields(FieldGroupConfig group, ViewGroup hookView, LayoutInflater li, boolean isEdition)
    {
        ViewGroup groupview = hookView;
        ViewGroup grouprootview = null;

        // Header
        if (!TextUtils.isEmpty(group.getLabel()))
        {
            grouprootview = (ViewGroup) li.inflate(R.layout.form_header, null);
            TextView tv = (TextView) grouprootview.findViewById(R.id.title);
            tv.setText(group.getLabel());
            groupview = (ViewGroup) grouprootview.findViewById(R.id.group_panel);
            hookView.addView(grouprootview);
        }

        // For each properties, display the line associated
        for (FieldConfig fieldConfig : group.getItems())
        {
            if (fieldConfig instanceof FieldGroupConfig)
            {
                createPropertyFields((FieldGroupConfig) fieldConfig, groupview, li, isEdition);
                continue;
            }

            // Retrieve the Field builder based on config
            Property nodeProp = node.getProperty(fieldConfig.getModelIdentifier());
            View fieldView = null;
            BaseField field = generateField(nodeProp, fieldConfig, groupview, isEdition);
            if (field == null)
            {
                continue;
            }

            fieldsOrderIndex.add(field);
            fieldsIndex.put(fieldConfig.getModelIdentifier(), field);
            // Mark All fields in edition mode
            if (isEdition)
            {
                // Mark required Field
                if (field.isRequired())
                {
                    mandatoryFields.add(field);
                }
            }

            // If requires fragment for pickers.
            if (field.isPickerRequired())
            {
                field.setFragment(fr);
            }

            if (field.requireExtra())
            {
                Bundle b = new Bundle();
                b.putSerializable(PathField.EXTRA_PARENT_FOLDER, parentFolder);
                field.setExtra(b);
            }

            // If requires Async
            if (field.requireAsync() && !modelRequested.contains(fieldConfig.getModelIdentifier()))
            {
                String requestId = Operator.with(getActivity()).load(field.requestData(node));
                RequestHolder holder = new RequestHolder(field, fieldConfig.getModelIdentifier(), requestId);
                modelRequested.add(fieldConfig.getModelIdentifier());
                operationFieldIndex.put(requestId, holder);
            }

        }

        if (groupview.getChildCount() == 0 && grouprootview != null)
        {
            grouprootview.setVisibility(View.GONE);
        }

    }

    private BaseField generateField(Property property, FieldConfig fieldConfig, ViewGroup hookView, boolean isEdition)
    {
        // First we prepare the field
        String dataType = FormFieldTypes.FIELD_TEXT;
        if (!TextUtils.isEmpty(fieldConfig.getType()))
        {
            dataType = fieldConfig.getType();
        }
        else if (typeDefinition != null && typeDefinition.getPropertyDefinition(fieldConfig.getIdentifier()) != null)
        {
            dataType = FieldTypeRegistry.getFieldType(typeDefinition, fieldConfig.getIdentifier());
        }

        BaseField field = FieldTypeFactory.createField(getActivity(), this, dataType, property, fieldConfig,
                typeDefinition, !isEdition);
        if (field == null) { return null; }

        // Then we create the view
        View fieldView = (isEdition) ? field.setupEditionView(property != null ? property.getValue() : null)
                : field.setupdReadView();

        // If a view has been generated we kept it.
        if (fieldView != null)
        {
            fieldView.setId(UIUtils.generateViewId());
            hookView.addView(fieldView);
        }

        return field;
    }

    public void evaluateViews()
    {
        for (BaseField field : fieldsOrderIndex)
        {
            // field.evaluateVisibility();
        }
    }

    public void refreshViews()
    {
        for (BaseField field : fieldsOrderIndex)
        {
            field.refreshEditionView();
        }
    }

    /**
     * Unsupported + Required field
     */
    public void abort()
    {
        getActivity().getSupportFragmentManager().popBackStack();

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title(R.string.form_message_unsupported_title).cancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        dialog.dismiss();
                    }
                }).content(Html.fromHtml(getActivity().getString(R.string.form_message_unsupported_description)))
                .positiveText(R.string.ok).callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                    }
                });
        builder.show();
    }

    public void setOperationData(String requestId, OperationEvent event)
    {
        if (!operationFieldIndex.containsKey(requestId)) { return; }

        // TODO Display Error Message
        if (operationFieldIndex.containsKey(requestId) && event.hasException) { return; }

        RequestHolder holder = operationFieldIndex.get(requestId);
        holder.field.setOperationData(event);

        // Update Index
        operationFieldIndex.remove(requestId);
        modelRequested.remove(holder.modelId);
    }

    public Object getValuePicked(String propertyId)
    {
        return fieldsIndex.get(propertyId).getValuePicked();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public FragmentActivity getActivity()
    {
        return activity.get();
    }

    public void setCurrentPickerField(BaseField field)
    {
        currentPickerField = field;
    }

    public BaseField getCurrentPickerField()
    {
        return currentPickerField;
    }

    public Node getNode()
    {
        return node;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INNER CLASS
    // ///////////////////////////////////////////////////////////////////////////
    private static class RequestHolder
    {
        public final BaseField field;

        public final String modelId;

        public final String requestId;

        public RequestHolder(BaseField field, String modelId, String requestId)
        {
            this.field = field;
            this.modelId = modelId;
            this.requestId = requestId;
        }
    }

}
