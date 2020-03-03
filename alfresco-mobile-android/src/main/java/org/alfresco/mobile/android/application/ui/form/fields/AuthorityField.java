
package org.alfresco.mobile.android.application.ui.form.fields;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.node.update.EditPropertiesPickerFragment;
import org.alfresco.mobile.android.application.fragments.user.UserSearchFragment;
import org.alfresco.mobile.android.application.ui.form.FormManager;
import org.alfresco.mobile.android.application.ui.form.adapter.AuthorityAdapter;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;

import android.content.Context;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.widget.ListAdapter;

import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by jpascal on 28/03/2015.
 */
public class AuthorityField extends BaseField
{
    public static final String OUTPUT_OBJECT = "object";

    public static final String OUTPUT_ID = "type";

    public static final String OUTPUT_FULLNAME = "fullName";

    private String authorityType;

    private Map<String, Person> persons = new HashMap<String, Person>(0);

    private Boolean allowMultipleSelection = false;

    private String outputValue = OUTPUT_ID;

    protected MaterialEditText pickerEditText;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public AuthorityField(Context context, FormManager manager, Property property, FieldConfig configuration,
            PropertyDefinition propertyDefinition, boolean isReadMode)
    {
        super(context, manager, property, configuration, propertyDefinition, isReadMode);
        if (configuration.getParameter(ConfigConstants.AUTHORITY_VALUE) != null)
        {
            authorityType = (String) configuration.getParameter(ConfigConstants.AUTHORITY_VALUE);
        }
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
            persons = (Map<String, Person>) object;
            String values = "";

            Object outputVal = getOutputValue();
            if (outputVal instanceof List)
            {
                values = String.format(
                        MessageFormat.format(getContext().getString(R.string.picker_multi_value_selected),
                                ((List) outputVal).size()),
                        Integer.toString(((List) outputVal).size()), fieldConfig.getLabel());
            }
            else if (outputVal instanceof Person)
            {
                values = getEditValue((Person) outputVal);
            }
            else if (outputVal instanceof String)
            {
                values = (String) outputVal;
            }

            pickerEditText.setText(values);
        }
    }

    private String getEditValue(Person person)
    {
        if (person == null) { return ""; }
        return person.getFullName();
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
        if (persons.values() == null || persons.values().isEmpty()) { return null; }

        switch (outputValue)
        {
            case OUTPUT_FULLNAME:
                List<String> names = new ArrayList<String>(persons.size());
                for (Person person : persons.values())
                {
                    names.add(person.getIdentifier());
                }
                return (allowMultipleSelection) ? (Serializable) names : names.get(0);

            case OUTPUT_OBJECT:
                return (allowMultipleSelection) ? new ArrayList<Person>(persons.values())
                        : new ArrayList<Person>(persons.values()).get(0);

            default:
                List<String> ids = new ArrayList<String>(persons.size());
                for (Person person : persons.values())
                {
                    ids.add(person.getIdentifier());
                }
                return (allowMultipleSelection) ? (Serializable) ids : ids.get(0);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTING MODE
    // ///////////////////////////////////////////////////////////////////////////
    public ListAdapter getListAdapter(AlfrescoFragment fr)
    {
        return new AuthorityAdapter(fr, R.layout.row_two_lines_caption_divider, new ArrayList<Person>(persons.values()),
                outputValue);
    }

    public void remove(Object object)
    {
        if (object instanceof Person)
        {
            persons.remove(((Person) object).getIdentifier());
        }
        setEditionValue(persons);
    }

    public void startPicker(AlfrescoFragment fr, String tag)
    {
        super.setFragment(fr);
        DialogFragment df = (DialogFragment) UserSearchFragment.with(getFragment().getActivity())
                .fieldId(fieldConfig.getModelIdentifier()).fragmentTag(tag).singleChoice(!allowMultipleSelection)
                .mode(ListingModeFragment.MODE_PICK).createFragment();
        df.show(getFragment().getFragmentManager(), UserSearchFragment.TAG);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICKER MODE
    // ///////////////////////////////////////////////////////////////////////////
    public Object getValuePicked()
    {
        return persons;
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
                    EditPropertiesPickerFragment.newInstance(fieldConfig.getModelIdentifier())
                            .show(getFragment().getFragmentManager(), EditPropertiesPickerFragment.TAG);
                }
            });
        }
    }

}
