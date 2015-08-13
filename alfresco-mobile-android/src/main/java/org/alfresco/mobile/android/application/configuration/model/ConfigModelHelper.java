package org.alfresco.mobile.android.application.configuration.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.configuration.model.view.ActivitiesConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.AdvancedSearchConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.DocumentDetailsConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.FavoritesConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.FolderDetailsConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.LocalConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.LocalFilesConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.PeopleConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.RepositoryConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.RepositorySearchConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.SearchConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.SiteBrowserConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.SitesConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.SyncConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.TasksConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.UserProfileConfigModel;
import org.alfresco.mobile.android.application.fragments.config.DefaultMenuConfigIds;

import android.text.TextUtils;

/**
 * Created by jpascal on 10/08/2015.
 */
public class ConfigModelHelper implements ConfigIconIds
{
    /**
     * Type as defined by ConfigService Documentation.
     */
    public static final Map<String, String> ICON_TYPE_MAPPING = new HashMap<String, String>(16)
    {
        {
            put(ConfigurationConstant.KEY_ACTIVITIES, ICON_ACTIVITIES);
            put(ConfigurationConstant.KEY_REPOSITORY, ICON_REPOSITORY);
            put(ConfigurationConstant.KEY_REPOSITORY_SEARCH, ICON_SEARCH);
            put(ConfigurationConstant.KEY_DOC_DETAILS, ICON_DOCUMENT);
            put(ConfigurationConstant.KEY_FOLDER_DETAILS, ICON_FOLDER);
            put(ConfigurationConstant.KEY_SITES, ICON_SITES);
            put(ConfigurationConstant.KEY_SITE_BROWSER, ICON_SITES);
            put(ConfigurationConstant.KEY_FAVORITES, ICON_FAVORITES);
            put(ConfigurationConstant.KEY_LOCAL, ICON_LOCAL_FILES);
            put(ConfigurationConstant.KEY_LOCAL_FILES, ICON_LOCAL_FILES);
            put(ConfigurationConstant.KEY_PERSON_PROFILE, ICON_USER);
            put(ConfigurationConstant.KEY_PEOPLE, ICON_GROUP);
            put(ConfigurationConstant.KEY_SEARCH, ICON_SEARCH);
            put(ConfigurationConstant.KEY_SEARCH_ADVANCED, ICON_SEARCH);
            put(ConfigurationConstant.KEY_TASKS, ICON_TASKS);
            put(ConfigurationConstant.KEY_SYNC, ICON_SYNC);
        }
    };

    /**
     * Ids as defined in the embedded config file.
     */
    public static final Map<String, String> ICON_IDS_MAPPING = new HashMap<String, String>(10)
    {
        {
            put(DefaultMenuConfigIds.VIEW_ACTIVITIES, ICON_ACTIVITIES);
            put(DefaultMenuConfigIds.VIEW_REPOSITORY, ICON_REPOSITORY);
            put(DefaultMenuConfigIds.VIEW_REPOSITORY_SHARED, ICON_SHARED_FILES);
            put(DefaultMenuConfigIds.VIEW_SITES, ICON_SITES);
            put(DefaultMenuConfigIds.VIEW_REPOSITORY_USERHOME, ICON_MY_FILES);
            put(DefaultMenuConfigIds.VIEW_TASKS, ICON_TASKS);
            put(DefaultMenuConfigIds.VIEW_FAVORITES, ICON_FAVORITES);
            put(DefaultMenuConfigIds.VIEW_SYNC, ICON_SYNC);
            put(DefaultMenuConfigIds.VIEW_SEARCH, ICON_SEARCH);
            put(DefaultMenuConfigIds.VIEW_LOCAL_FILE, ICON_LOCAL_FILES);
        }
    };

    public static final Map<String, Integer> ICON_MAPPING_LIGHT = new HashMap<String, Integer>(14)
    {
        {
            put(ICON_ACTIVITIES, R.drawable.ic_activities_light);
            put(ICON_REPOSITORY, R.drawable.ic_companyhome_light);
            put(ICON_SITES, R.drawable.ic_site_light);
            put(ICON_FAVORITES, R.drawable.ic_favorite_light);
            put(ICON_LOCAL_FILES, R.drawable.ic_local_files_light);
            put(ICON_USER, R.drawable.ic_person_light);
            put(ICON_GROUP, R.drawable.ic_users_light);
            put(ICON_SEARCH, R.drawable.ic_search_light);
            put(ICON_TASKS, R.drawable.ic_task_light);
            put(ICON_SYNC, R.drawable.ic_sync_light);

            put(ICON_DOCUMENT, R.drawable.ic_doc_light);
            put(ICON_FOLDER, R.drawable.ic_repository_light);
            put(ICON_MY_FILES, R.drawable.ic_myfiles_light);
            put(ICON_SHARED_FILES, R.drawable.ic_shared_light);
            put(ICON_GALLERY, R.drawable.ic_gallery_light);
        }
    };

    public static final Map<String, Integer> ICON_MAPPING_DARK = new HashMap<String, Integer>(10)
    {
        {
            put(ICON_ACTIVITIES, R.drawable.ic_activities_dark);
            put(ICON_REPOSITORY, R.drawable.ic_companyhome_dark);
            put(ICON_SITES, R.drawable.ic_site_dark);
            put(ICON_FAVORITES, R.drawable.ic_favorite_dark);
            put(ICON_LOCAL_FILES, R.drawable.ic_local_files_dark);
            put(ICON_USER, R.drawable.ic_person_dark);
            put(ICON_GROUP, R.drawable.ic_users_dark);
            put(ICON_SEARCH, R.drawable.ic_search_dark);
            put(ICON_TASKS, R.drawable.ic_task_dark);
            put(ICON_SYNC, R.drawable.ic_sync_dark);

            put(ICON_DOCUMENT, R.drawable.ic_doc_dark);
            put(ICON_FOLDER, R.drawable.ic_repository_dark);
            put(ICON_MY_FILES, R.drawable.ic_myfiles_dark);
            put(ICON_SHARED_FILES, R.drawable.ic_shared_dark);
            put(ICON_GALLERY, R.drawable.ic_gallery_dark);
        }
    };

    public static int getLightIconId(ViewConfig config)
    {
        return getLightIconId(config.getType(), config.getIconIdentifier(), config.getIdentifier());
    }

    public static int getLightIconId(String type)
    {
        return getLightIconId(type, null, null);
    }

    public static int getLightIconId(String type, String icon, String id)
    {
        // Defined by the configuration
        if (!TextUtils.isEmpty(icon) && ICON_MAPPING_LIGHT.containsKey(icon)) { return ICON_MAPPING_LIGHT.get(icon); }

        // Defined by its ids (relevant only for embedded and default)
        if (!TextUtils.isEmpty(id)
                && ICON_IDS_MAPPING.containsKey(id)) { return ICON_MAPPING_LIGHT.get(ICON_IDS_MAPPING.get(id)); }

        // Defined by its type
        if (!TextUtils.isEmpty(type)
                && ICON_TYPE_MAPPING.containsKey(type)) { return ICON_MAPPING_LIGHT.get(ICON_TYPE_MAPPING.get(type)); }

        return -1;
    }

    public static int getDarkIconId(ViewConfig config)
    {
        return getDarkIconId(config.getType(), config.getIconIdentifier(), config.getIdentifier());
    }

    public static int getDarkIconId(String type, String icon, String id)
    {
        // Defined by the configuration
        if (!TextUtils.isEmpty(icon) && ICON_MAPPING_DARK.containsKey(icon)) { return ICON_MAPPING_DARK.get(icon); }

        // Defined by its ids (relevant only for embedded and default)
        if (!TextUtils.isEmpty(id)
                && ICON_IDS_MAPPING.containsKey(id)) { return ICON_MAPPING_DARK.get(ICON_IDS_MAPPING.get(id)); }

        // Defined by its type
        if (!TextUtils.isEmpty(type)
                && ICON_TYPE_MAPPING.containsKey(type)) { return ICON_MAPPING_DARK.get(ICON_TYPE_MAPPING.get(type)); }

        return -1;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PARAMETERS
    // ///////////////////////////////////////////////////////////////////////////
    public static final Map<String, ViewConfigModel> CONFIG_MODELS = new LinkedHashMap<String, ViewConfigModel>(16)
    {
        {
            put(ActivitiesConfigModel.TYPE_ID, new ActivitiesConfigModel());
            put(RepositoryConfigModel.TYPE_ID, new RepositoryConfigModel());
            put(RepositorySearchConfigModel.TYPE_ID, new RepositorySearchConfigModel());
            put(DocumentDetailsConfigModel.TYPE_ID, new DocumentDetailsConfigModel());
            put(FolderDetailsConfigModel.TYPE_ID, new FolderDetailsConfigModel());
            put(SitesConfigModel.TYPE_ID, new SitesConfigModel());
            put(SiteBrowserConfigModel.TYPE_ID, new SiteBrowserConfigModel());
            put(FavoritesConfigModel.TYPE_ID, new FavoritesConfigModel());
            put(LocalConfigModel.TYPE_ID, new LocalConfigModel());
            put(LocalFilesConfigModel.TYPE_ID, new LocalFilesConfigModel());
            put(UserProfileConfigModel.TYPE_ID, new UserProfileConfigModel());
            put(PeopleConfigModel.TYPE_ID, new PeopleConfigModel());
            put(SearchConfigModel.TYPE_ID, new SearchConfigModel());
            put(AdvancedSearchConfigModel.TYPE_ID, new AdvancedSearchConfigModel());
            put(TasksConfigModel.TYPE_ID, new TasksConfigModel());
            put(SyncConfigModel.TYPE_ID, new SyncConfigModel());
        }
    };

}
