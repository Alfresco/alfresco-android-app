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
package org.alfresco.mobile.android.application.ui.form.picker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.user.UserPickerCallback;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

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
            String pickFragmentTag = getArguments().getString(ARGUMENT_FRAGMENT_TAG);
            fragmentPick = getFragmentManager().findFragmentByTag(pickFragmentTag);
            if (fragmentPick != null && fragmentPick instanceof UserPickerCallback)
            {
                selectedItems = (Map<String, Object>) ((onPickAllowableValuesFragment) fragmentPick)
                        .getAllowableValuesSelected(fieldId);
            }
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).title(title)
                .positiveText(android.R.string.ok).negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog)
                    {
                        isCancelled = true;
                        dialog.dismiss();
                    }
                });

        final Set<String> valueSet = allowableValues.keySet();
        final String[] valueArray = valueSet.toArray(new String[valueSet.size()]);
        if (singleSelection)
        {
            String[] selectedArray = selectedItems.keySet().toArray(new String[selectedItems.keySet().size()]);
            int index = selectedArray.length == 0 ? -1 : Arrays.asList(valueArray).indexOf(selectedArray[0]);
            builder.items(valueArray).itemsCallbackSingleChoice(index, new MaterialDialog.ListCallbackSingleChoice()
            {
                @Override
                public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence)
                {
                    selectedItems.clear();
                    selectedItems.put(valueArray[i], allowableValues.get(valueArray[i]));
                    onValueSet();
                    return false;
                }
            });
        }
        else
        {
            final Integer[] index = new Integer[selectedItems.size()];
            int i = 0;
            for (String selected : selectedItems.keySet())
            {
                index[i] = Arrays.asList(valueArray).indexOf(selected);
            }

            builder.items(valueArray).itemsCallbackMultiChoice(index, new MaterialDialog.ListCallbackMultiChoice()
            {
                @Override
                public boolean onSelection(MaterialDialog materialDialog, Integer[] integers,
                        CharSequence[] charSequences)
                {
                    for (Integer index : integers)
                    {
                        selectedItems.put(valueArray[index], allowableValues.get(valueArray[index]));
                    }
                    onValueSet();
                    return false;
                }
            });
        }

        return builder.show();
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
            // Object value = property.getValue();

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