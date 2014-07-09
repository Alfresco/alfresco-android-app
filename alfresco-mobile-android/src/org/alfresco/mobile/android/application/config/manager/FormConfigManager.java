package org.alfresco.mobile.android.application.config.manager;

import org.alfresco.mobile.android.api.constants.ConfigConstants;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.TypeDefinition;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.api.model.config.FieldGroupConfig;
import org.alfresco.mobile.android.api.model.config.FormConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.config.form.FieldTypeBuilder;
import org.alfresco.mobile.android.application.config.form.FieldTypeFactory;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FormConfigManager extends BaseConfigManager
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public FormConfigManager(Activity activity, ConfigService configService, ViewGroup vRoot)
    {
        super(activity, configService);
        this.vRoot = vRoot;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean displayProperties(Node node)
    {
        try
        {
            vRoot.removeAllViews();
            FormConfig formConfig = (FormConfig) configService
                    .getFormConfig(ConfigConstants.VIEW_NODE_PROPERTIES);
            generateProperties(formConfig, vRoot, LayoutInflater.from(getActivity()), false, node, null);
        }
        catch (Exception e)
        {
            Log.d("FormConfig", Log.getStackTraceString(e));
            return false;
        }
        return true;
    }

    public boolean displayEditForm(TypeDefinition typeDefinition, Node node)
    {
        try
        {
            vRoot.removeAllViews();
            FormConfig rootMenuViewConfig = (FormConfig) configService
                    .getFormConfig(ConfigConstants.VIEW_EDIT_PROPERTIES);
            generateProperties(rootMenuViewConfig, vRoot, LayoutInflater.from(getActivity()), true, node, typeDefinition);
        }
        catch (Exception e)
        {
            Log.d("FormConfig", Log.getStackTraceString(e));
            return false;
        }
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DRIVEN BY OBJECT
    // ///////////////////////////////////////////////////////////////////////////
    private void generateProperties(FormConfig formConfig, ViewGroup hookView, LayoutInflater li, boolean isEdition,
            Node node, TypeDefinition typeDefinition)
    {
        ViewGroup groupview = hookView;

        // Retrieve form config by Id
        ConfigScope scope = configManager.getCurrentScope();
        scope.add(ConfigScope.NODE, node);
        FormConfig config = configService.getFormConfig(formConfig.getIdentifier(), scope);

        // CREATION
        if (config.getGroups() != null && config.getGroups().size() > 0)
        {
            // Header
            if (!TextUtils.isEmpty(config.getLabel()))
            {
                ViewGroup grouprootview = (ViewGroup) li.inflate(R.layout.sdk_property_title, null);
                TextView tv = (TextView) grouprootview.findViewById(R.id.title);
                tv.setText(config.getLabel());
                groupview = (ViewGroup) grouprootview.findViewById(R.id.group_panel);
                hookView.addView(grouprootview);
            }

            // Add Children
            for (FieldGroupConfig group : config.getGroups())
            {
                createPropertiesView(group, groupview, li, isEdition, node, typeDefinition);
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
    private void createPropertiesView(FieldGroupConfig group, ViewGroup hookView, LayoutInflater li, boolean isEdition,
            Node currentNode, TypeDefinition typeDefinition)
    {
        ViewGroup groupview = hookView;

        // Header
        if (!TextUtils.isEmpty(group.getLabel()))
        {
            ViewGroup grouprootview = (ViewGroup) li.inflate(R.layout.sdk_property_title, null);
            TextView tv = (TextView) grouprootview.findViewById(R.id.title);
            tv.setText(group.getLabel());
            groupview = (ViewGroup) grouprootview.findViewById(R.id.group_panel);
            hookView.addView(grouprootview);
        }

        // For each properties, display the line associated
        for (FieldConfig fieldConfig : group.getItems())
        {
            Property nodeProp = currentNode.getProperty(fieldConfig.getModelIdentifier());
            View fieldView = null;
            FieldTypeBuilder fieldBuilder = getBuilder(nodeProp, fieldConfig);
            if (fieldBuilder == null)
            {
                continue;
            }
            if (isEdition)
            {
                fieldView = fieldBuilder.getEditView(typeDefinition, groupview);
            }
            else if (nodeProp.getValue() != null)
            {
                fieldView = fieldBuilder.getReadOnlyView();
            }
            if (fieldView != null)
            {
                groupview.addView(fieldView);
            }
        }
    }

    protected FieldTypeBuilder getBuilder(Property property, FieldConfig fieldConfig)
    {
        FieldTypeBuilder fieldTypeBuilder = null;
        if (!TextUtils.isEmpty(fieldConfig.getType()))
        {
            fieldTypeBuilder = FieldTypeFactory.createFieldControlType(getActivity(), property, fieldConfig);
        }
        else
        {
            fieldTypeBuilder = FieldTypeFactory.createFieldControlType(getActivity(), FieldTypeFactory.DEFAULT_FIELD, property, fieldConfig);
        }
        return fieldTypeBuilder;
    }
}
