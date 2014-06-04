package org.alfresco.mobile.android.application.config.manager;

import java.util.GregorianCalendar;

import org.alfresco.mobile.android.api.constants.ConfigConstants;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyType;
import org.alfresco.mobile.android.api.model.config.Configuration;
import org.alfresco.mobile.android.api.model.config.FormConfig;
import org.alfresco.mobile.android.api.model.config.FormFieldConfig;
import org.alfresco.mobile.android.api.model.config.FormFieldsGroupConfig;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.application.R;

import android.app.Activity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FormConfigManager extends BaseConfigManager
{
    private ViewConfig rootMenuViewConfig;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public FormConfigManager(Activity activity, Configuration configurationContext, ViewGroup vRoot)
    {
        super(activity, configurationContext);
        rootMenuViewConfig = configurationContext.getApplicationConfig().getViewConfig(
                ConfigConstants.VIEW_NODE_PROPERTIES);
        this.vRoot = vRoot;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public void displayProperties(Node node)
    {
        vRoot.removeAllViews();
        generateProperties(rootMenuViewConfig.getForms().get(0), vRoot, LayoutInflater.from(getActivity()), node);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DRIVEN BY OBJECT
    // ///////////////////////////////////////////////////////////////////////////
    private void generateProperties(String formId, ViewGroup hookView, LayoutInflater li, Node node)
    {
        TextView header = null;
        ViewGroup groupview = hookView;

        // Retrieve form config by Id
        FormConfig config = configurationContext.getFormConfig(formId, node);

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
            for (FormFieldsGroupConfig group : config.getGroups())
            {
                createPropertiesView(group, groupview, li, node);
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
    private void createPropertiesView(FormFieldsGroupConfig group, ViewGroup hookView, LayoutInflater li,
            Node currentNode)
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
        for (FormFieldConfig fieldConfig : group.getFields())
        {
            // Add Line
            Property nodeProp = currentNode.getProperty(fieldConfig.getIdentifier());
            if (nodeProp.getValue() != null)
            {
                View v = createPropertyView(li, fieldConfig.getLabel(), nodeProp, currentNode);
                if (v != null)
                {
                    groupview.addView(v);
                }
            }
        }
    }

    protected View createPropertyView(LayoutInflater inflater, String propertyLabel, Property property, Node currentNode)
    {
        String value = null;
        if (PropertyType.DATETIME.equals(property.getType()))
        {
            value = DateFormat.getMediumDateFormat(getActivity()).format(
                    ((GregorianCalendar) property.getValue()).getTime())
                    + " "
                    + DateFormat.getTimeFormat(getActivity()).format(
                            ((GregorianCalendar) property.getValue()).getTime());
        }
        else if (property.getValue().toString() != null && !property.getValue().toString().isEmpty())
        {
            value = property.getValue().toString();
        }

        if (value == null) { return null; }

        View vr = inflater.inflate(R.layout.sdk_property_row, null);
        TextView tv = (TextView) vr.findViewWithTag("propertyName");
        tv.setText(propertyLabel);
        tv = (TextView) vr.findViewWithTag("propertyValue");
        tv.setText(value);
        tv.setClickable(true);
        tv.setFocusable(true);
        tv.setTag(currentNode);
        return vr;
    }
}
