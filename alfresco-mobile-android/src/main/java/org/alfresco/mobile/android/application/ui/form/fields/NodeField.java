
package org.alfresco.mobile.android.application.ui.form.fields;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesPickerFragment;
import org.alfresco.mobile.android.application.ui.form.FormManager;
import org.alfresco.mobile.android.application.ui.form.adapter.NodeFieldAdapter;
import org.alfresco.mobile.android.application.ui.form.picker.DocumentPickerFragment;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;

import android.content.Context;
import android.view.View;
import android.widget.ListAdapter;

import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by jpascal on 28/03/2015.
 */
public class NodeField extends BaseField
{
    public static final String EXTRA_PARENT_FOLDER = "extraParentFolder";

    public static final String OUTPUT_OBJECT = "object";

    public static final String OUTPUT_ID = "type";

    private Map<String, Node> nodes = new HashMap<>(0);

    private Boolean allowMultipleSelection = false;

    private String outputValue = OUTPUT_ID;

    protected MaterialEditText pickerEditText;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public NodeField(Context context, FormManager manager, Property property, FieldConfig configuration,
            PropertyDefinition propertyDefinition, boolean isReadMode)
    {
        super(context, manager, property, configuration, propertyDefinition, isReadMode);
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
    // VALUES
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Object getEditionValue()
    {
        return null;
    }

    @Override
    public void setEditionValue(Object object)
    {
        if (object instanceof Map || object == null)
        {
            nodes = (Map<String, Node>) object;
            String values = "";

            Object outputVal = getOutputValue();
            if (outputVal instanceof List)
            {
                values = String.format(
                        MessageFormat.format(getContext().getString(R.string.picker_multi_value_selected),
                                ((List) outputVal).size()),
                        Integer.toString(((List) outputVal).size()), fieldConfig.getLabel());
            }
            else if (outputVal instanceof Node)
            {
                values = getEditValue((Node) outputVal);
            }
            else if (outputVal instanceof String)
            {
                values = (String) outputVal;
            }

            pickerEditText.setText(values);
        }
    }

    private String getEditValue(Node node)
    {
        if (node == null) { return ""; }
        return node.getName();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEW GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
    public View setupEditionView(Object value)
    {
        View vr = inflater.inflate(R.layout.form_picker, null);
        pickerEditText = (MaterialEditText) vr.findViewById(R.id.picker);
        pickerEditText.setText(getHumanReadableEditionValue());

        // Asterix if required
        pickerEditText.setFloatingLabelText(getLabelText(fieldConfig.getLabel()));

        pickerEditText.setHint(getLabelText(fieldConfig.getLabel()));
        pickerEditText.setFloatingLabelAlwaysShown(true);
        pickerEditText.setFocusable(false);

        pickerEditText.setIconRight(R.drawable.ic_menu_expand);

        editionView = vr;

        checkReadOnly(editionView);

        return vr;
    }

    @Override
    public Serializable getOutputValue()
    {
        if (nodes.values() == null || nodes.values().isEmpty()) { return null; }

        switch (outputValue)
        {
            case OUTPUT_ID:
                List<String> names = new ArrayList<String>(nodes.size());
                for (Node node : nodes.values())
                {
                    names.add(node.getIdentifier());
                }
                return (allowMultipleSelection) ? (Serializable) names : names.get(0);
            case OUTPUT_OBJECT:
                return (allowMultipleSelection) ? new ArrayList<Node>(nodes.values())
                        : new ArrayList<Node>(nodes.values()).get(0);
            default:
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
                return (allowMultipleSelection) ? (Serializable) values : values.get(0);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTING MODE
    // ///////////////////////////////////////////////////////////////////////////
    public ListAdapter getListAdapter(AlfrescoFragment fr)
    {
        return new NodeFieldAdapter(fr, R.layout.row_two_lines_progress, new ArrayList<>(nodes.values()), outputValue);
    }

    public void remove(Object object)
    {
        if (object instanceof Node)
        {
            nodes.remove(((Node) object).getIdentifier());
        }
        setEditionValue(nodes);
    }

    public void startPicker(AlfrescoFragment fr, String tag)
    {
        super.setFragment(fr);
        if (fr.getActivity() instanceof PrivateDialogActivity)
        {
            ((PrivateDialogActivity) fr.getActivity()).setFieldId(fieldConfig.getModelIdentifier());
        }
        DocumentPickerFragment.with(fr.getActivity()).display();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKER MODE
    // ///////////////////////////////////////////////////////////////////////////
    public Object getValuePicked()
    {
        return nodes;
    }

    public boolean isPickerRequired()
    {
        return true;
    }

    @Override
    public void setFragment(AlfrescoFragment fr)
    {
        super.setFragment(fr);
        if (getFragment() != null && editionView != null && !isReadOnly)
        {
            editionView.findViewById(R.id.picker_container).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    EditPropertiesPickerFragment.newInstance(fieldConfig.getModelIdentifier()).show(
                            getFragment().getActivity().getSupportFragmentManager(), EditPropertiesPickerFragment.TAG);
                }
            });
        }
    }

}
