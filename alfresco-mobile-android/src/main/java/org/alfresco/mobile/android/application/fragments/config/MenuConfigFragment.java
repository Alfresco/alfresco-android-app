package org.alfresco.mobile.android.application.fragments.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.ViewGroupConfig;
import org.alfresco.mobile.android.api.model.config.impl.ViewConfigImpl;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.fragments.activitystream.ActivityFeedFragment;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.favorite.FavoritesFragment;
import org.alfresco.mobile.android.application.fragments.search.SearchFragment;
import org.alfresco.mobile.android.application.fragments.site.browser.BrowserSitesFragment;
import org.alfresco.mobile.android.application.fragments.sync.SyncFragment;
import org.alfresco.mobile.android.application.fragments.workflow.task.TasksFragment;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.node.browse.NodeBrowserTemplate;
import org.alfresco.mobile.android.ui.utils.UIUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by jpascal on 21/01/2015.
 */
public class MenuConfigFragment extends AlfrescoFragment
{
    public static final String TAG = MenuConfigFragment.class.getSimpleName();

    public static final String VIEW_ACTIVITIES = "view-activities-default";

    public static final String VIEW_REPOSITORY = "view-repository-default";

    public static final String VIEW_REPOSITORY_SHARED = "view-repository-shared-default";

    public static final String VIEW_SITES = "view-sites-default";

    public static final String VIEW_REPOSITORY_USERHOME = "view-repository-userhome-default";

    public static final String VIEW_TASKS = "view-task-default";

    public static final String VIEW_FAVORITES = "view-favorites-default";

    public static final String VIEW_SYNC = "view-sync-default";

    public static final String VIEW_SEARCH = "view-search-default";

    public static final String VIEW_LOCAL_FILE = "view-local-default";

    // //////////////////////////////////////////////////////////////////////
    // VARIABLES
    // //////////////////////////////////////////////////////////////////////
    private LinkedHashMap<String, MenuItemConfig> defaultMenuItems;

    private ArrayList<MenuItemConfig> menuConfigItems;

    private MenuItemConfigAdapter adapter;

    private ConfigService customConfiguration;

    private ConfigManager configManager;

    private Button save;

    private boolean originalSyncState;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public MenuConfigFragment()
    {
        requiredSession = false;
        checkSession = false;
        setHasOptionsMenu(true);
    }

    protected static MenuConfigFragment newInstanceByTemplate(Bundle b)
    {
        MenuConfigFragment cbf = new MenuConfigFragment();
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
        setRootView(inflater.inflate(R.layout.config_default_menu, container, false));

        configManager = ConfigManager.getInstance(getActivity());
        customConfiguration = ConfigManager.getInstance(getActivity()).getCustomConfig(getAccount().getId());

        createDefaultMenu();
        if (customConfiguration != null)
        {
            updateMenu();
        }

        menuConfigItems = new ArrayList<>(defaultMenuItems.values());

        adapter = new MenuItemConfigAdapter(this, R.layout.config_menu_row_dynamic, menuConfigItems);
        DynamicListView listView = (DynamicListView) viewById(R.id.listview);
        listView.setCheeseList(menuConfigItems);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final Button cancel = UIUtils.initCancel(getRootView(), R.string.discardbutton);
        cancel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                getActivity().onBackPressed();
            }
        });

        save = UIUtils.initValidation(getRootView(), R.string.confirm);
        save.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!defaultMenuItems.get(VIEW_SYNC).isEnable() && originalSyncState
                        || (defaultMenuItems.get(VIEW_SYNC).isEnable() && !originalSyncState))
                {
                    manageSyncSetting();
                }
                else
                {
                    saveConfiguration();
                    getActivity().onBackPressed();
                }
            }
        });

        return getRootView();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // HELPER
    // ///////////////////////////////////////////////////////////////////////////
    public void manageSyncSetting()
    {
        if (!defaultMenuItems.get(VIEW_SYNC).isEnable() && originalSyncState)
        {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).cancelable(false)
                    .title(R.string.favorites_deactivate).callback(new MaterialDialog.ButtonCallback()
                    {
                        @Override
                        public void onPositive(MaterialDialog dialog)
                        {
                            FavoritesSyncManager.getInstance(getActivity()).setActivateSync(getAccount(), false);
                            FavoritesSyncManager.getInstance(getActivity()).unsync(getAccount());
                            saveConfiguration();
                            getActivity().onBackPressed();
                        }
                    }).content(Html.fromHtml(getString(R.string.favorites_deactivate_description)))
                    .positiveText(R.string.ok).negativeText(R.string.cancel);
            builder.show();
        }
        else if (defaultMenuItems.get(VIEW_SYNC).isEnable() && !originalSyncState)
        {
            FavoritesSyncManager.getInstance(getActivity()).setActivateSync(getAccount(), true);
            saveConfiguration();
            getActivity().onBackPressed();
        }
    }

    public void updateCounter(int counter)
    {
        int selectedCounter = counter;
        save.setEnabled(selectedCounter != 0);
    }

    private JSONObject saveConfiguration()
    {
        JSONObject configuration = new JSONObject();
        boolean hasItems = false;
        try
        {
            // INFO
            JSONObject info = new JSONObject();
            info.put(ConfigConstants.SCHEMA_VERSION_VALUE, 0.1);
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
            JSONObject item = null;
            MenuItemConfig itemConfig = null;
            for (int i = 0; i < adapter.getCount(); i++)
            {
                itemConfig = menuConfigItems.get(i);
                if (itemConfig.isEnable())
                {
                    items.put(((ViewConfigImpl) itemConfig.config).toJson());
                    hasItems = true;
                }
            }

            defaultMenu.putOpt(ConfigConstants.ITEMS_VALUE, items);
            viewGroupsArray.put(defaultMenu);

            configuration.put(ConfigTypeIds.VIEW_GROUPS.value(), viewGroupsArray);

            // SAVE TO DEVICE
            OutputStream sourceFile = null;
            try
            {
                File configFolder = AlfrescoStorageManager.getInstance(getActivity()).getCustomFolder(getAccount());
                File configFile = new File(configFolder, ConfigConstants.CONFIG_FILENAME);

                sourceFile = new FileOutputStream(configFile);
                sourceFile.write(configuration.toString().getBytes("UTF-8"));
                sourceFile.close();

                // Send Event
                ConfigManager.getInstance(getActivity()).loadAndUseCustom(getAccount());
                EventBusManager.getInstance().post(new ConfigManager.ConfigurationMenuEvent(getAccount().getId()));
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

    private void addMenuConfigItem(String id, int labelId, String type, int iconId, HashMap<String, Object> properties)
    {
        if (defaultMenuItems == null)
        {
            defaultMenuItems = new LinkedHashMap<String, MenuItemConfig>();
        }

        defaultMenuItems.put(id, new MenuItemConfig(new ViewConfigImpl(id, getString(labelId), type, properties),
                iconId));
    }

    private void updateMenu()
    {
        LinkedHashMap<String, MenuItemConfig> sortedItems = new LinkedHashMap<String, MenuItemConfig>();

        String profileId = configManager.getCurrentProfileId();
        if (profileId == null)
        {
            profileId = customConfiguration.getDefaultProfile().getIdentifier();
        }
        ViewConfig rootMenuViewConfig = customConfiguration.getViewConfig(customConfiguration.getProfile(profileId)
                .getRootViewId(), configManager.getCurrentScope());

        for (ViewConfig viewConfig : ((ViewGroupConfig) rootMenuViewConfig).getItems())
        {
            sortedItems.put(viewConfig.getIdentifier(), defaultMenuItems.remove(viewConfig.getIdentifier()));
        }
        for (Map.Entry<String, MenuItemConfig> item : defaultMenuItems.entrySet())
        {
            item.getValue().setEnable(false);
            sortedItems.put(item.getKey(), item.getValue());
        }
        defaultMenuItems.clear();
        defaultMenuItems = sortedItems;

        originalSyncState = defaultMenuItems.get(VIEW_SYNC).isEnable();
    }

    private void createDefaultMenu()
    {
        defaultMenuItems = new LinkedHashMap<String, MenuItemConfig>();

        // Activities
        addMenuConfigItem(VIEW_ACTIVITIES, ActivityFeedFragment.Builder.LABEL_ID, ConfigurationConstant.KEY_ACTIVITIES,
                R.drawable.ic_activities_light, null);

        // Repository
        addMenuConfigItem(VIEW_REPOSITORY, DocumentFolderBrowserFragment.Builder.LABEL_ID_REPOSITORY,
                ConfigurationConstant.KEY_REPOSITORY, R.drawable.ic_repository_light, null);

        // Shared Files
        HashMap<String, Object> sharedProperties = new HashMap<String, Object>();
        sharedProperties.put(NodeBrowserTemplate.ARGUMENT_FOLDER_TYPE_ID, NodeBrowserTemplate.FOLDER_TYPE_SHARED);
        addMenuConfigItem(VIEW_REPOSITORY_SHARED, DocumentFolderBrowserFragment.Builder.LABEL_ID_SHARED,
                ConfigurationConstant.KEY_REPOSITORY, R.drawable.ic_shared_light, sharedProperties);

        // Sites
        addMenuConfigItem(VIEW_SITES, BrowserSitesFragment.Builder.LABEL_ID, ConfigurationConstant.KEY_SITES,
                R.drawable.ic_site_light, null);

        // Userhome
        HashMap<String, Object> userHomeProperties = new HashMap<String, Object>();
        userHomeProperties.put(NodeBrowserTemplate.ARGUMENT_FOLDER_TYPE_ID, NodeBrowserTemplate.FOLDER_TYPE_USERHOME);
        addMenuConfigItem(VIEW_REPOSITORY_USERHOME, DocumentFolderBrowserFragment.Builder.LABEL_ID_USERHOME,
                ConfigurationConstant.KEY_REPOSITORY, R.drawable.ic_myfiles_light, userHomeProperties);

        // Tasks & Workflow
        addMenuConfigItem(VIEW_TASKS, TasksFragment.Builder.LABEL_ID, ConfigurationConstant.KEY_TASKS,
                R.drawable.ic_task_light, null);

        // Favorites
        addMenuConfigItem(VIEW_FAVORITES, FavoritesFragment.Builder.LABEL_ID, ConfigurationConstant.KEY_FAVORITES,
                R.drawable.ic_favorite_light, null);

        // Sync
        addMenuConfigItem(VIEW_SYNC, SyncFragment.Builder.LABEL_ID, ConfigurationConstant.KEY_SYNC,
                R.drawable.ic_sync_light, null);

        // Search
        addMenuConfigItem(VIEW_SEARCH, SearchFragment.Builder.LABEL_ID, ConfigurationConstant.KEY_SEARCH,
                R.drawable.ic_search_light, null);

        // Local Files
        addMenuConfigItem(VIEW_LOCAL_FILE, FileExplorerFragment.Builder.LABEL_ID, ConfigurationConstant.KEY_LOCALFILES,
                R.drawable.ic_download_light, null);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
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

        final int iconId;

        private boolean isEnable = true;

        MenuItemConfig(ViewConfig config, int iconId)
        {
            this.config = config;
            this.iconId = iconId;
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
