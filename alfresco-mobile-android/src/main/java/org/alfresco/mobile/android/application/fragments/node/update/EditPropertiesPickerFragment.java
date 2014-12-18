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
package org.alfresco.mobile.android.application.fragments.node.update;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.form.BaseField;
import org.alfresco.mobile.android.application.ui.form.TextField;
import org.alfresco.mobile.android.application.ui.form.views.AlfrescoFieldView;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class EditPropertiesPickerFragment extends AlfrescoFragment
{
    public static final String TAG = EditPropertiesPickerFragment.class.getName();

    private static final String ARGUMENT_FIELD_ID = "fieldId";

    private EditPropertiesFragment editProperties;

    private String fieldId;

    private BaseField field;

    private ListView lv;

    private AlfrescoFieldView fieldView;

    private String titleId;

    protected View pb;

    protected View ev;

    protected BaseListAdapter adapter;

    protected List<Object> objectsRemoved = new ArrayList<Object>();

    protected List<Object> objectsAdded = new ArrayList<Object>();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public EditPropertiesPickerFragment()
    {
    }

    public static EditPropertiesPickerFragment newInstance(String fieldId)
    {
        EditPropertiesPickerFragment bf = new EditPropertiesPickerFragment();
        Bundle b = new Bundle();
        b.putString(ARGUMENT_FIELD_ID, fieldId);
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getArguments() == null || !getArguments().containsKey(ARGUMENT_FIELD_ID)) { return null; }
        fieldId = getArguments().getString(ARGUMENT_FIELD_ID);

        // Retrieve parameters
        editProperties = ((EditPropertiesFragment) getFragmentManager().findFragmentByTag(
                EditPropertiesFragment.TAG));
        field = editProperties.getField(fieldId);
        titleId = field.getLabel();

        if (getDialog() != null)
        {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        setRetainInstance(true);
        setSession(SessionUtils.getSession(getActivity()));
        SessionUtils.checkSession(getActivity(), getSession());
        setRootView(inflater.inflate(R.layout.form_picker_list, container, false));
        if (getSession() == null) { return getRootView(); }

        lv = ((ListView) viewById(R.id.listView));
        pb = (View) viewById(R.id.progressbar);
        ev = viewById(R.id.empty);

        TextView evt = (TextView) viewById(R.id.empty_text);
        evt.setText(R.string.empty);

        if (field.getListAdapter(this) != null)
        {
            adapter = (BaseListAdapter) field.getListAdapter(this);
            lv.setAdapter(adapter);
        }

        // Title Selection
        TextView titleSelection = (TextView) viewById(R.id.title);
        titleSelection.setText(String.format(getString(R.string.picker_selected_value), field.getLabel()));
        refresh();

        // Button Selection
        titleId = String.format(getString(R.string.picker_select_value), field.getLabel());

        Button b = (Button) viewById(R.id.action_select);
        if (field.isMultiValued() && field instanceof TextField)
        {
            // Field
            ViewGroup hookView = (ViewGroup) viewById(R.id.picker_group);
            fieldView = (AlfrescoFieldView) field.getEditView(null, hookView, true);
            hookView.addView(fieldView, 0);

            // Add button action
            b.setText(R.string.add);
            b.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // field.add(fieldView.getValue());
                    adapter.add(fieldView.getValue());
                    objectsAdded.add(fieldView.getValue());
                    fieldView.clear();
                    refresh();
                }
            });
        }
        else
        {
            b.setText(titleId);
            b.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    field.startPicker(EditPropertiesPickerFragment.this, EditPropertiesFragment.TAG);
                    dismiss();
                }
            });
        }

        // BUTTONS
        b = (Button) viewById(R.id.cancel);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });

        b = (Button) viewById(R.id.validate_action);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                for (Object object : objectsAdded)
                {
                    field.add(object);
                }
                for (Object object : objectsRemoved)
                {
                    field.remove(object);
                }
                dismiss();
            }
        });

        return getRootView();
    }

    public void removeValue(Object object)
    {
        adapter.remove(object);
        objectsRemoved.add(object);
        refresh();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    private void refresh()
    {
        lv.setAdapter(adapter);
        if (lv.getAdapter().isEmpty())
        {
            displayEmptyView();
        }
        else
        {
            display(true);
        }
    }

    private void display(Boolean shown)
    {
        if (ev == null || lv == null || pb == null) { return; }

        if (shown)
        {
            ev.setVisibility(View.GONE);
            lv.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }
        else
        {
            ev.setVisibility(View.GONE);
            lv.setVisibility(View.GONE);
            pb.setVisibility(View.VISIBLE);
        }
    }

    private void displayEmptyView()
    {
        if (ev == null || lv == null || pb == null) { return; }

        ev.setVisibility(View.VISIBLE);
        lv.setVisibility(View.GONE);
        pb.setVisibility(View.GONE);
    }
}
