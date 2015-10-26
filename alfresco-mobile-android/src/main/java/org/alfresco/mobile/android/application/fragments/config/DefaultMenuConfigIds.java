package org.alfresco.mobile.android.application.fragments.config;

import java.util.ArrayList;

/**
 * Created by jpascal on 09/08/2015.
 */
public interface DefaultMenuConfigIds
{
    String VIEW_ACTIVITIES = "view-activities-default";

    String VIEW_REPOSITORY = "view-repository-default";

    String VIEW_REPOSITORY_SHARED = "view-repository-shared-default";

    String VIEW_SITES = "view-sites-default";

    String VIEW_REPOSITORY_USERHOME = "view-repository-userhome-default";

    String VIEW_TASKS = "view-task-default";

    String VIEW_FAVORITES = "view-favorites-default";

    String VIEW_SYNC = "view-sync-default";

    String VIEW_SEARCH = "view-search-default";

    String VIEW_LOCAL_FILE = "view-local-default";

    ArrayList<String> MENU_TYPE_IDS = new ArrayList<String>(10)
    {
        {
            add(VIEW_ACTIVITIES);
            add(VIEW_REPOSITORY);
            add(VIEW_REPOSITORY_SHARED);
            add(VIEW_SITES);
            add(VIEW_REPOSITORY_USERHOME);
            add(VIEW_TASKS);
            add(VIEW_FAVORITES);
            add(VIEW_SYNC);
            add(VIEW_SEARCH);
            add(VIEW_LOCAL_FILE);
        }
    };

}
