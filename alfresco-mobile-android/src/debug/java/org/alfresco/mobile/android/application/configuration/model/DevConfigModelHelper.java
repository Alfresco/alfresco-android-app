package org.alfresco.mobile.android.application.configuration.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.model.view.ActivitiesConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.FavoritesConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.LocalConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.MenuEditorConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.RepositoryConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.SearchConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.SiteBrowserConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.SyncConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.TasksConfigModel;
import org.alfresco.mobile.android.ui.node.browse.NodeBrowserTemplate;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by jpascal on 10/08/2015.
 */
public class DevConfigModelHelper extends ConfigModelHelper implements DevMenuConfigIds
{
    public static final String ICON_SETTINGS = PREFIX_ICON.concat(".settings");

    public static final Map<String, String> TYPE_MAPPING = new HashMap<String, String>(
            ConfigModelHelper.ICON_TYPE_MAPPING)
    {
        {
            put(DevMenuConfigIds.KEY_CONFIG_EDITOR, ICON_SETTINGS);
        }
    };

    public static final Map<String, String> IDS_MAPPING = new HashMap<String, String>(10)
    {
        {
            put(DevMenuConfigIds.VIEW_CONFIG_EDITOR, ICON_SETTINGS);
        }
    };

    public static final Map<String, Integer> ICON_MAPPING_LIGHT = new HashMap<String, Integer>(
            ConfigModelHelper.ICON_MAPPING_LIGHT)
    {
        {
            put(ICON_SETTINGS, R.drawable.ic_settings_light);
        }
    };

    public static final Map<String, Integer> ICON_MAPPING_DARK = new HashMap<String, Integer>(10)
    {
        {
            put(ICON_SETTINGS, R.drawable.ic_settings_dark);
        }
    };

    public static int getDarkIconId(ViewConfig config)
    {
        int id = getDarkIconId(config.getType(), config.getIconIdentifier(), config.getIdentifier());
        if (id == -1) { return ConfigModelHelper.getDarkIconId(config.getType(), config.getIconIdentifier(),
                config.getIdentifier()); }
        return id;
    }

    public static int getLightIconId(ViewConfig config)
    {
        int id = getLightIconId(config.getType(), config.getIconIdentifier(), config.getIdentifier());
        if (id == -1) { return ConfigModelHelper.getLightIconId(config.getType(), config.getIconIdentifier(),
                config.getIdentifier()); }
        return id;
    }

    public static int getLightIconId(String type, String icon, String id)
    {
        // Defined by the configuration
        if (!TextUtils.isEmpty(icon) && ICON_MAPPING_LIGHT.containsKey(icon)) { return ICON_MAPPING_LIGHT.get(icon); }

        // Defined by its ids (relevant only for embedded and default)
        if (!TextUtils.isEmpty(id)
                && IDS_MAPPING.containsKey(id)) { return ICON_MAPPING_LIGHT.get(IDS_MAPPING.get(id)); }

        // Defined by its type
        if (!TextUtils.isEmpty(type)
                && TYPE_MAPPING.containsKey(type)) { return ICON_MAPPING_LIGHT.get(TYPE_MAPPING.get(type)); }

        return -1;
    }

    public static int getDarkIconId(String type, String icon, String id)
    {
        // Defined by the configuration
        if (!TextUtils.isEmpty(icon) && ICON_MAPPING_DARK.containsKey(icon)) { return ICON_MAPPING_DARK.get(icon); }

        // Defined by its ids (relevant only for embedded and default)
        if (!TextUtils.isEmpty(id)
                && IDS_MAPPING.containsKey(id)) { return ICON_MAPPING_DARK.get(IDS_MAPPING.get(id)); }

        // Defined by its type
        if (!TextUtils.isEmpty(type)
                && TYPE_MAPPING.containsKey(type)) { return ICON_MAPPING_DARK.get(TYPE_MAPPING.get(type)); }

        return -1;
    }

    public static LinkedHashMap createDefaultMenu(Context context)
    {
        LinkedHashMap defaultMenuItems = new LinkedHashMap<>();
        // Activities
        defaultMenuItems.put(VIEW_ACTIVITIES, new ActivitiesConfigModel().createViewConfig(VIEW_ACTIVITIES, context));

        // Company Home - Repository
        defaultMenuItems.put(VIEW_REPOSITORY, new RepositoryConfigModel().createViewConfig(VIEW_REPOSITORY, context));

        // Shared Files
        HashMap<String, Object> sharedProperties = new HashMap<String, Object>();
        sharedProperties.put(NodeBrowserTemplate.ARGUMENT_FOLDER_TYPE_ID, RepositoryConfigModel.FOLDER_TYPE_SHARED);
        defaultMenuItems.put(VIEW_REPOSITORY_SHARED,
                new RepositoryConfigModel(sharedProperties).createViewConfig(VIEW_REPOSITORY_SHARED, context));

        // Sites
        defaultMenuItems.put(VIEW_SITES, new SiteBrowserConfigModel().createViewConfig(VIEW_SITES, context));

        // Userhome
        HashMap<String, Object> userHomeProperties = new HashMap<String, Object>();
        userHomeProperties.put(NodeBrowserTemplate.ARGUMENT_FOLDER_TYPE_ID, RepositoryConfigModel.FOLDER_TYPE_USERHOME);
        defaultMenuItems.put(VIEW_REPOSITORY_USERHOME,
                new RepositoryConfigModel(userHomeProperties).createViewConfig(VIEW_REPOSITORY_USERHOME, context));

        // Tasks & Workflow
        defaultMenuItems.put(VIEW_TASKS, new TasksConfigModel().createViewConfig(VIEW_TASKS, context));

        // Favorites
        defaultMenuItems.put(VIEW_FAVORITES, new FavoritesConfigModel().createViewConfig(VIEW_FAVORITES, context));

        // Sync
        defaultMenuItems.put(VIEW_SYNC, new SyncConfigModel().createViewConfig(VIEW_SYNC, context));

        // Search
        defaultMenuItems.put(VIEW_SEARCH, new SearchConfigModel().createViewConfig(VIEW_SEARCH, context));

        // Local Files
        defaultMenuItems.put(VIEW_LOCAL_FILE, new LocalConfigModel().createViewConfig(VIEW_LOCAL_FILE, context));

        // Local Files
        defaultMenuItems.put(VIEW_CONFIG_EDITOR,
                new MenuEditorConfigModel().createViewConfig(VIEW_CONFIG_EDITOR, context));

        return defaultMenuItems;
    }
}
