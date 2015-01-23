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

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.ui.form.BaseField;
import org.alfresco.mobile.android.application.ui.form.picker.AllowablePickerFragment.onPickAllowableValuesFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DatePickerFragment.onPickDateFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DocumentPickerFragment;
import org.alfresco.mobile.android.application.ui.form.picker.DocumentPickerFragment.onPickDocumentFragment;
import org.alfresco.mobile.android.application.ui.form.picker.PersonPickerFragment.onPickAuthorityFragment;
import org.alfresco.mobile.android.application.ui.form.picker.TimePickerFragment.onPickTimeFragment;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.definition.TypeDefinitionEvent;
import org.alfresco.mobile.android.async.node.update.UpdateNodeRequest;
import org.alfresco.mobile.android.platform.utils.BundleUtils;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

public class EditPropertiesFragment extends EditNodePropertiesFragment implements onPickDateFragment,
        onPickTimeFragment, onPickAllowableValuesFragment, onPickAuthorityFragment, onPickDocumentFragment
{
    public static final String TAG = EditPropertiesFragment.class.getName();

    protected static final String ARGUMENT_FOLDER = "folder";

    private Folder folder;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public static EditPropertiesFragment newInstanceByTemplate(Bundle b)
    {
        EditPropertiesFragment adf = new EditPropertiesFragment();
        adf.setArguments(b);
        //adf.setRetainInstance(true);
        return adf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        folder = (Folder) getArguments().getSerializable(ARGUMENT_FOLDER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        bcreate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                updateNode();
            }
        });

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        HashMap<String, Serializable> props = formManager.prepareProperties();
        outState.putSerializable("props", props);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        else
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    protected void updateNode()
    {
        bcreate.setEnabled(false);
        Map<String, Serializable> props = formManager.prepareProperties();
        Operator.with(getActivity()).load(new UpdateNodeRequest.Builder(folder, node, props));

        if (getDialog() != null)
        {
            dismiss();
        }
        else
        {
            getActivity().finish();
        }
    }

    public Object getPickedValues(String fieldId)
    {
        return formManager.getValuePicked(fieldId);
    }

    public BaseField getField(String fieldId)
    {
        return formManager.getField(fieldId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DATE/TIME PICKER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onDatePicked(String fieldId, GregorianCalendar gregorianCalendar)
    {
        formManager.setPropertyValue(fieldId, gregorianCalendar);
    }

    @Override
    public void onDateClear(String fieldId)
    {
        formManager.setPropertyValue(fieldId, null);
    }

    @Override
    public void onTimePicked(String fieldId, int hourOfDay, int minute)
    {
        // TODO
        Log.d(TAG, "Time :" + hourOfDay + ":" + minute);
    }

    @Override
    public void onTimeClear(String fieldId)
    {
        formManager.setPropertyValue(fieldId, null);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ALLOWABLE PICKER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onValuesPicked(String fieldId, Map<String, Object> values)
    {
        formManager.setPropertyValue(fieldId, values);
    }

    @Override
    public void onValuesClear(String fieldId)
    {
        formManager.setPropertyValue(fieldId, null);
    }

    @Override
    public Object getAllowableValuesSelected(String fieldId)
    {
        return formManager.getValuePicked(fieldId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PERSON PICKER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onPersonSelected(String fieldId, Map<String, Person> p)
    {
        formManager.setPropertyValue(fieldId, p);
    }

    @Override
    public void onPersonClear(String fieldId)
    {
        formManager.setPropertyValue(fieldId, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Person> getPersonSelected(String fieldId)
    {
        return (Map<String, Person>) formManager.getValuePicked(fieldId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DOCUMENT/FOLDER PICKER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onNodeSelected(String fieldId, Map<String, Node> items)
    {
        formManager.setPropertyValue(fieldId, items);
        getActivity().getFragmentManager().popBackStackImmediate(DocumentPickerFragment.TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onNodeClear(String fieldId)
    {
        formManager.setPropertyValue(fieldId, null);
    }

    @Override
    public Map<String, Node> getNodeSelected(String fieldId)
    {
        return (Map<String, Node>) formManager.getValuePicked(fieldId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(TypeDefinitionEvent event)
    {
        if (event.hasException)
        {
            displayEmptyView();
            ((TextView) viewById(R.id.empty_text)).setText(R.string.empty_child);
        }
        else if (getActivity() != null)
        {
            displayData();
            modelDefinition = event.data;
            configService = configurationManager.getConfig(getAccount().getId());
            configure(LayoutInflater.from(getActivity()));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS & HELPERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            templateArguments = new String[] {};
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder parentFolder(Folder folder)
        {
            BundleUtils.addIfNotNull(extraConfiguration, ARGUMENT_FOLDER, folder);
            return this;
        }

        public Builder node(Node node)
        {
            BundleUtils.addIfNotNull(extraConfiguration, ARGUMENT_NODE, node);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }
}
