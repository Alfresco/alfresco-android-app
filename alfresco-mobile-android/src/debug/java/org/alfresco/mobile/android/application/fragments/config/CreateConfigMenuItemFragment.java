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

package org.alfresco.mobile.android.application.fragments.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.impl.ViewConfigImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.model.ConfigModelHelper;
import org.alfresco.mobile.android.application.configuration.model.ConfigParameterModel;
import org.alfresco.mobile.android.application.configuration.model.ViewConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.ActivitiesConfigModel;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by jpascal on 09/08/2015.
 */
public class CreateConfigMenuItemFragment extends AlfrescoFragment
{
    private static final String ARGUMENT_VIEWCONFIG = "viewConfig";

    private TwoLinesViewHolder typeField;

    private MaterialEditText idField;

    private MaterialEditText nameField;

    private ViewConfigModel selectedConfigType;

    private ViewConfig viewConfig;

    private ViewConfigTypeItemAdapter adapter;

    private List<ViewConfigModel> typeItems;

    private HashMap<ConfigParameterModel, MaterialEditText> fieldIndex;

    private boolean override = false;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public CreateConfigMenuItemFragment()
    {
        requiredSession = false;
        checkSession = false;
    }

    protected static CreateConfigMenuItemFragment newInstanceByTemplate(Bundle b)
    {
        CreateConfigMenuItemFragment cbf = new CreateConfigMenuItemFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getArguments() != null)
        {
            viewConfig = (ViewConfig) getArguments().get(ARGUMENT_VIEWCONFIG);
        }

        setRootView(inflater.inflate(R.layout.fr_menuitem_create, container, false));

        idField = (MaterialEditText) viewById(R.id.config_menu_id);
        nameField = (MaterialEditText) viewById(R.id.config_menu_name);

        // Prepare infos
        typeItems = new ArrayList<>(ConfigModelHelper.CONFIG_MODELS.values());

        // Account Name
        typeField = new TwoLinesViewHolder(viewById(R.id.config_menu_type));
        typeField.topText.setText("Type");
        typeField.bottomText.setText("Select Type");
        typeField.choose.setVisibility(View.VISIBLE);
        typeField.choose.setImageResource(R.drawable.expander_open_holo_light);
        typeField.icon.setVisibility(View.GONE);
        viewById(R.id.config_menu_type_container).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayTypes();
            }
        });

        if (selectedConfigType != null)
        {
            displayInformations();
            if (selectedConfigType.hasParameters())
            {
                displayParameters();
            }
        }
        else
        {
            hide(R.id.validation_panel);
            hide(R.id.config_menu_info_container);
            hide(R.id.config_menu_parameters_container);
        }

        Button add = (Button) viewById(R.id.config_menu_add);
        add.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                validate();
            }
        });

        if (viewConfig != null)
        {
            displayInformations();
        }

        return getRootView();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void displayTypes()
    {
        if (adapter == null)
        {
            adapter = new ViewConfigTypeItemAdapter(this, R.layout.row_two_lines, typeItems);
        }
        new MaterialDialog.Builder(getActivity()).iconRes(R.drawable.ic_application_logo).title("Select Type")
                .adapter(adapter, new MaterialDialog.ListCallback()
                {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text)
                    {
                        ViewConfigModel tmpConfigTypeItem = typeItems.get(which);
                        typeField.topText.setText(getString(tmpConfigTypeItem.getLabelId()));
                        typeField.bottomText.setText(getString(tmpConfigTypeItem.getDescriptionId()));
                        typeField.icon.setImageResource(tmpConfigTypeItem.getIconModelResId());
                        typeField.icon.setVisibility(View.VISIBLE);
                        selectedConfigType = tmpConfigTypeItem;
                        displayInformations();
                        if (selectedConfigType.hasParameters())
                        {
                            displayParameters();
                        }
                        show(R.id.validation_panel);

                        if (viewConfig != null)
                        {
                            override = true;
                        }

                        dialog.dismiss();
                    }
                }).show();
    }

    private void displayInformations()
    {
        show(R.id.config_menu_info_container);

        if (viewConfig != null && !override)
        {
            selectedConfigType = ConfigModelHelper.CONFIG_MODELS.get(viewConfig.getType());
            typeField.topText.setText(getString(selectedConfigType.getLabelId()));
            typeField.bottomText.setText(getString(selectedConfigType.getDescriptionId()));
            typeField.icon.setImageResource(selectedConfigType.getIconModelResId());
            typeField.icon.setVisibility(View.VISIBLE);

            idField.setText(viewConfig.getIdentifier());
            idField.setEnabled(false);
            nameField.setText(viewConfig.getLabel());

            if (selectedConfigType.hasParameters())
            {
                displayParameters();
            }
            show(R.id.validation_panel);
        }

        if (idField.getText().length() == 0)
        {
            idField.setText(selectedConfigType.getType() + "-" + UIUtils.generateViewId());
            idField.setShowClearButton(true);
        }

        if (nameField.getText().length() == 0)
        {
            nameField.setText(selectedConfigType.getLabel(getActivity()));
            nameField.setShowClearButton(true);
        }
    }

    private void displayParameters()
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        fieldIndex = new HashMap<>(selectedConfigType.getParameters().size());
        show(R.id.config_menu_parameters_container);
        ViewGroup anchorView = (ViewGroup) viewById(R.id.config_menu_parameters);
        anchorView.removeAllViews();

        MaterialEditText vr = null;
        String title;
        for (ConfigParameterModel param : selectedConfigType.getParameters())
        {
            vr = (MaterialEditText) inflater.inflate(R.layout.form_material_edittext, null);
            title = param.name;
            title = (param.isMandatory) ? title.concat("*") : title;
            title = (param.isExclusive) ? title.concat(" (Exclusive)") : title;
            if (!param.isSupported)
            {
                vr.setPrimaryColor(R.color.alfresco_dbp_orange);
                vr.setBaseColor(R.color.alfresco_dbp_orange);
                vr.setMetTextColor(R.color.alfresco_dbp_orange);
                vr.setUnderlineColor(R.color.alfresco_dbp_orange);
            }
            vr.setFloatingLabelText(title);
            vr.setHint(getString(param.descriptionId));
            vr.setFloatingLabelAlwaysShown(true);
            anchorView.addView(vr);
            fieldIndex.put(param, vr);
        }
    }

    private HashMap<String, Object> getParameters()
    {
        if (fieldIndex == null) { return null; }
        HashMap<String, Object> properties = new HashMap<>(fieldIndex.size());
        for (Map.Entry<ConfigParameterModel, MaterialEditText> entry : fieldIndex.entrySet())
        {
            if (entry.getValue().getText().length() > 0)
            {
                switch (entry.getKey().type)
                {
                    case BOOLEAN:
                        properties.put(entry.getKey().name,
                                Boolean.parseBoolean(entry.getValue().getText().toString()));
                        break;
                    case STRING:
                        properties.put(entry.getKey().name, entry.getValue().getText().toString());
                        break;
                }
            }
        }

        return properties.isEmpty() ? null : properties;
    }

    private void validate()
    {
        String id = idField.getText().toString().trim();
        if (TextUtils.isEmpty(id))
        {
            id = selectedConfigType.getType() + "-" + UIUtils.generateViewId();
        }

        String name = nameField.getText().toString().trim();
        if (TextUtils.isEmpty(name))
        {
            name = selectedConfigType.getLabel(getActivity());
        }

        HashMap<String, Object> properties = getParameters();

        ViewConfig menuItem = selectedConfigType.createViewConfig(id, name, properties);

        if (viewConfig != null)
        {
            ((ConfigMenuEditorFragment) getFragmentByTag(ConfigMenuEditorFragment.TAG)).update(menuItem);
        }
        else
        {
            ((ConfigMenuEditorFragment) getFragmentByTag(ConfigMenuEditorFragment.TAG)).addMenuConfigItem(menuItem);
        }

        getActivity().onBackPressed();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            viewConfigModel = new ActivitiesConfigModel();
            menuIconId = R.drawable.ic_settings_dark;
            menuTitleId = R.string.settings;
        }

        public Builder viewConfig(ViewConfigImpl viewConfig)
        {
            extraConfiguration.putSerializable(ARGUMENT_VIEWCONFIG, viewConfig);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

    }
}
