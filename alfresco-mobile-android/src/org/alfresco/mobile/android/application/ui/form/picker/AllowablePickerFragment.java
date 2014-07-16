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
package org.alfresco.mobile.android.application.ui.form.picker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.person.UserPickerCallback;
import org.alfresco.mobile.android.application.ui.form.adapter.AllowableAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @author jpascal
 */
public class AllowablePickerFragment extends DialogFragment
{
    public static final String TAG = AllowablePickerFragment.class.getName();

    private static final String ARGUMENT_FRAGMENT_TAG = "fragmentTag";

    private static final String ARGUMENT_TITLE = "title";

    private static final String ARGUMENT_FIELD_ID = "fielId";

    private static final String ARGUMENT_SINGLE_SELECTION = "singleSelection";

    private ListView lv;

    private Map<String, Object> allowableValues;

    private Map<String, Object> selectedItems = new HashMap<String, Object>(1);

    private boolean isCancelled = false;

    private boolean singleSelection = false;

    private String pickFragmentTag;

    private Fragment fragmentPick;

    private String fieldId;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // //////////////////////////////////////////////////////////////////////
    public static AllowablePickerFragment newInstance(String fieldId, String fragmentTag, boolean singleSelection,
            String pickerTitle)
    {
        AllowablePickerFragment bf = new AllowablePickerFragment();
        Bundle b = new Bundle();
        b.putString(ARGUMENT_FIELD_ID, fieldId);
        b.putString(ARGUMENT_FRAGMENT_TAG, fragmentTag);
        b.putString(ARGUMENT_TITLE, pickerTitle);
        b.putBoolean(ARGUMENT_SINGLE_SELECTION, singleSelection);
        bf.setArguments(b);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        setRetainInstance(true);

        String title = getString(R.string.create_document_editor_title);
        if (getArguments() != null)

        {
            title = getArguments().getString(ARGUMENT_TITLE);
            fieldId = getArguments().getString(ARGUMENT_FIELD_ID);
            singleSelection = getArguments().getBoolean(ARGUMENT_SINGLE_SELECTION);
            pickFragmentTag = getArguments().getString(ARGUMENT_FRAGMENT_TAG);
            fragmentPick = getFragmentManager().findFragmentByTag(pickFragmentTag);
            if (fragmentPick != null && fragmentPick instanceof UserPickerCallback)
            {
                selectedItems = (Map<String, Object>) ((onPickAllowableValuesFragment) fragmentPick)
                        .getAllowableValuesSelected(fieldId);
            }
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.sdk_list, null);
        lv = (ListView) v.findViewById(R.id.listView);
        if (allowableValues != null)
        {
            lv.setAdapter(new AllowableAdapter(getActivity(), R.layout.app_list_checkbox_row, allowableValues,
                    selectedItems));
        }

        lv.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String selected = (String) parent.getItemAtPosition(position);
                if (singleSelection)
                {
                    selectedItems.clear();
                }
                else if (selectedItems.containsKey(selected))
                {
                    selectedItems.remove(selected);
                }
                selectedItems.put(selected, allowableValues.get(selected));
                ((AllowableAdapter) lv.getAdapter()).notifyDataSetChanged();
            }
        });

        return new AlertDialog.Builder(getActivity()).setTitle(title).setView(v)
                .setPositiveButton(android.R.string.ok, new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        onValueSet();
                    }
                }).setNegativeButton(android.R.string.cancel, new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        isCancelled = true;
                        dismiss();
                    }
                }).create();
    }

    public void setPropertyDefinition(Object value, PropertyDefinition definition)
    {
        if (definition.getAllowableValues() == null) { return; }
        if (definition.isMultiValued())
        {
            // TODO
        }
        else
        {
            // Retrieve selected value
            //Object value = property.getValue();

            // Let's construct a map of single value
            this.allowableValues = new HashMap<String, Object>(definition.getAllowableValues().size());
            for (Map<String, Object> map : definition.getAllowableValues())
            {
                for (Entry<String, Object> entry : map.entrySet())
                {
                    Object entryValue = entry.getValue();
                    if (entry.getValue() instanceof List)
                    {
                        entryValue = ((List) entry.getValue()).get(0);
                    }

                    if (value != null && value.equals(entryValue))
                    {
                        selectedItems.put(entry.getKey(), entryValue);
                    }
                    allowableValues.put(entry.getKey(), entryValue);
                }
            }

        }
        if (lv != null && getActivity() != null)
        {
            lv.setAdapter(new AllowableAdapter(getActivity(), R.layout.sdk_list_row, allowableValues, selectedItems));
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    public void onValueSet()
    {
        if (getArguments() == null || !getArguments().containsKey(ARGUMENT_FRAGMENT_TAG)) { return; }

        if (fragmentPick == null) { return; }

        if (isCancelled)
        {
            ((onPickAllowableValuesFragment) fragmentPick).onValuesClear(fieldId);
        }
        else
        {
            ((onPickAllowableValuesFragment) fragmentPick).onValuesPicked(fieldId, selectedItems);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERFACE
    // //////////////////////////////////////////////////////////////////////
    public interface onPickAllowableValuesFragment
    {
        void onValuesPicked(String fieldId, Map<String, Object> values);

        void onValuesClear(String fieldId);

        Object getAllowableValuesSelected(String fieldId);
    }
}