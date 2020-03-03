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

package org.alfresco.mobile.android.application.fragments.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.ViewGroupConfig;
import org.alfresco.mobile.android.api.model.config.impl.ViewConfigImpl;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.model.DevConfigModelHelper;
import org.alfresco.mobile.android.application.configuration.model.DevMenuConfigIds;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionButton;

/**
 * Created by jpascal on 21/01/2015.
 */
public class ConfigMenuEditorFragment extends AlfrescoFragment implements DevMenuConfigIds
{
    private static final String ARGUMENT_ACCOUNT_ID = "accountId";

    public static final String TAG = ConfigMenuEditorFragment.class.getName();

    // //////////////////////////////////////////////////////////////////////
    // VARIABLES
    // //////////////////////////////////////////////////////////////////////
    private LinkedHashMap<String, ViewConfig> defaultMenuItems;

    private ArrayList<ViewConfig> menuConfigItems;

    private ConfigMenuItemAdapter adapter;

    private ConfigService customConfiguration;

    private ConfigManager configManager;

    private AlfrescoAccount account;

    private DynamicListView listView;

    private Long accountId = null;

    private int selectedPosition = -1;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public ConfigMenuEditorFragment()
    {
        requiredSession = false;
        checkSession = false;
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    protected static ConfigMenuEditorFragment newInstanceByTemplate(Bundle b)
    {
        ConfigMenuEditorFragment cbf = new ConfigMenuEditorFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(inflater.inflate(R.layout.fr_config_menu_editor, container, false));

        account = null;
        if (getArguments() != null)
        {
            accountId = BundleUtils.getLong(getArguments(), ARGUMENT_ACCOUNT_ID);
            if (accountId != null)
            {
                account = AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(accountId);
            }
            else
            {
                account = getAccount();
                accountId = account.getId();
            }
        }

        listView = (DynamicListView) viewById(R.id.listview);
        configManager = ConfigManager.getInstance(getActivity());
        customConfiguration = ConfigManager.getInstance(getActivity()).getCustomConfig(accountId);

        if (adapter == null)
        {
            if (customConfiguration != null)
            {
                updateMenu();
            }
            else
            {
                defaultMenuItems = DevConfigModelHelper.createDefaultMenu(getActivity());
            }
            if (defaultMenuItems != null)
            {
                menuConfigItems = new ArrayList<>(defaultMenuItems.values());
            }
            else
            {
                menuConfigItems = new ArrayList<>(0);
            }
        }

        // List Items
        adapter = new ConfigMenuItemAdapter(this, R.layout.row_two_lines, menuConfigItems);
        listView.setItemList(menuConfigItems != null ? menuConfigItems : new ArrayList<ViewConfig>(0));
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                selectedPosition = position;
                CreateConfigMenuItemFragment.with(getActivity())
                        .viewConfig((ViewConfigImpl) parent.getItemAtPosition(position)).display();
            }
        });

        // Add Items
        FloatingActionButton button = (FloatingActionButton) viewById(R.id.fab_create_view);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CreateConfigMenuItemFragment.with(getActivity()).display();
            }
        });

        FloatingActionButton button2 = (FloatingActionButton) viewById(R.id.fab_create_viewgroup);
        button2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CreateConfigMenuItemFragment.with(getActivity()).display();
            }
        });

        return getRootView();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // HELPER
    // ///////////////////////////////////////////////////////////////////////////
    public void addMenuConfigItem(ViewConfig viewConfig)
    {
        adapter.add(viewConfig);
        adapter.notifyDataSetInvalidated();
    }

    private void updateMenu()
    {
        defaultMenuItems = new LinkedHashMap<>();

        String profileId = configManager.getCurrentProfileId();
        if (profileId == null)
        {
            profileId = customConfiguration.getDefaultProfile().getIdentifier();
        }
        ViewConfig rootMenuViewConfig = customConfiguration.getViewConfig(
                customConfiguration.getProfile(profileId).getRootViewId(), configManager.getCurrentScope());

        for (ViewConfig viewConfig : ((ViewGroupConfig) rootMenuViewConfig).getItems())
        {
            defaultMenuItems.put(viewConfig.getIdentifier(), viewConfig);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        menu.add(Menu.NONE, R.id.config_menu_save, Menu.FIRST, "Save");
        menu.add(Menu.NONE, R.id.config_menu_send, Menu.FIRST, "Send");
        menu.add(Menu.NONE, R.id.config_menu_reset, Menu.FIRST, "Reset");
        menu.add(Menu.NONE, R.id.config_menu_clear, Menu.FIRST, "Clear");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.config_menu_send:
                send();
                return true;
            case R.id.config_menu_save:
                saveConfiguration();
                return true;
            case R.id.config_menu_reset:
                reset();
                return true;
            case R.id.config_menu_clear:
                clear();
                return true;
            default:
                return false;
        }
    }

    private void reset()
    {
        menuConfigItems.clear();
        defaultMenuItems = DevConfigModelHelper.createDefaultMenu(getActivity());

        menuConfigItems = new ArrayList<>(defaultMenuItems.values());
        adapter = new ConfigMenuItemAdapter(this, R.layout.row_two_lines, menuConfigItems);
        listView.setItemList(menuConfigItems != null ? menuConfigItems : new ArrayList<ViewConfig>(0));
        listView.setAdapter(adapter);

        saveConfiguration();
        updateMenu();
    }

    private void send()
    {
        File configFolder = AlfrescoStorageManager.getInstance(getActivity()).getCustomFolder(account);
        File configFile = new File(configFolder, ConfigConstants.CONFIG_FILENAME);
        ActionUtils.actionSend(getActivity(), configFile, "text/plain");
    }

    private void clear()
    {
        menuConfigItems.clear();
        adapter = new ConfigMenuItemAdapter(this, R.layout.row_two_lines, menuConfigItems);
        listView.setItemList(menuConfigItems != null ? menuConfigItems : new ArrayList<ViewConfig>(0));
        listView.setAdapter(adapter);
        saveConfiguration();
    }

    private JSONObject saveConfiguration()
    {
        JSONObject configuration = new JSONObject();
        try
        {
            // INFO
            JSONObject info = new JSONObject();
            info.put(ConfigConstants.SCHEMA_VERSION_VALUE, 0.2);
            info.putOpt(ConfigConstants.CONFIG_VERSION_VALUE, 0.1);
            configuration.put(ConfigTypeIds.INFO.value(), info);

            // PROFILES
            JSONObject profiles = new JSONObject();
            JSONObject defaultProfile = new JSONObject();
            defaultProfile.put(ConfigConstants.DEFAULT_VALUE, true);
            defaultProfile.putOpt(ConfigConstants.LABEL_ID_VALUE, "Custom Default");
            defaultProfile.putOpt(ConfigConstants.ROOTVIEW_ID_VALUE, "views-menu-default");
            profiles.put("default", defaultProfile);
            configuration.put(ConfigTypeIds.PROFILES.value(), profiles);

            // VIEW GROUPS
            JSONArray viewGroupsArray = new JSONArray();
            JSONObject defaultMenu = new JSONObject();
            defaultMenu.putOpt(ConfigConstants.ID_VALUE, "views-menu-default");
            defaultMenu.putOpt(ConfigConstants.LABEL_ID_VALUE, getString(R.string.menu_view));

            // Items
            JSONArray items = new JSONArray();
            for (int i = 0; i < adapter.getCount(); i++)
            {
                items.put(((ViewConfigImpl) menuConfigItems.get(i)).toJson());
            }

            defaultMenu.putOpt(ConfigConstants.ITEMS_VALUE, items);
            viewGroupsArray.put(defaultMenu);

            configuration.put(ConfigTypeIds.VIEW_GROUPS.value(), viewGroupsArray);

            // SAVE TO DEVICE
            OutputStream sourceFile = null;
            try
            {
                File configFolder = AlfrescoStorageManager.getInstance(getActivity()).getCustomFolder(account);
                File configFile = new File(configFolder, ConfigConstants.CONFIG_FILENAME);

                sourceFile = new FileOutputStream(configFile);
                sourceFile.write(configuration.toString().getBytes("UTF-8"));
                sourceFile.close();

                // Send Event
                ConfigManager.getInstance(getActivity()).loadAndUseCustom(account);
                EventBusManager.getInstance().post(new ConfigManager.ConfigurationMenuEvent(accountId));
            }
            catch (Exception e)
            {
                Log.w(TAG, Log.getStackTraceString(e));
            }
            finally
            {
                org.alfresco.mobile.android.api.utils.IOUtils.closeStream(sourceFile);
            }
        }
        catch (JSONException e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return configuration;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void delete(ViewConfig item)
    {
        adapter.remove(item);
    }

    public void edit(ViewConfigImpl item)
    {
        selectedPosition = adapter.getPosition(item);
        CreateConfigMenuItemFragment.with(getActivity()).viewConfig(item).display();
    }

    public void update(ViewConfig item)
    {
        adapter.remove(adapter.getItem(selectedPosition));
        adapter.add(item);
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
        public static final int LABEL_ID = R.string.settings;

        public static final int ICON_ID = R.drawable.ic_settings_dark;

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
            menuIconId = ICON_ID;
            menuTitleId = LABEL_ID;
            templateArguments = new String[] { ARGUMENT_ACCOUNT_ID };
        }

        public Builder accountId(long accountId)
        {
            extraConfiguration.putLong(ARGUMENT_ACCOUNT_ID, accountId);
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

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class MenuItemConfig
    {
        final ViewConfig config;

        final Integer iconId;

        private boolean isEnable = true;

        MenuItemConfig(ViewConfig config, Integer iconId)
        {
            this.config = config;
            if (iconId != null)
            {
                this.iconId = iconId;
            }
            else
            {
                this.iconId = DevConfigModelHelper.getLightIconId(config.getType(), config.getIconIdentifier(),
                        config.getIdentifier());
            }
        }

        public void setEnable(boolean enable)
        {
            this.isEnable = enable;
        }

        public boolean isEnable()
        {
            return isEnable;
        }
    }

}
