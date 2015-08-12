package org.alfresco.mobile.android.application.configuration.model.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.PropertyType;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.configuration.model.ConfigIconIds;
import org.alfresco.mobile.android.application.configuration.model.ConfigParameterModel;
import org.alfresco.mobile.android.application.configuration.model.ViewConfigModel;
import org.alfresco.mobile.android.ui.node.browse.NodeBrowserTemplate;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

/**
 * Created by jpascal on 10/08/2015.
 */
public class RepositoryConfigModel extends ViewConfigModel implements NodeBrowserTemplate
{
    public static final String ICON_REPOSITORY_ID = ConfigIconIds.ICON_REPOSITORY;

    public static final String ICON_SHARED_ID = ConfigIconIds.ICON_SHARED_FILES;

    public static final String ICON_USERHOME_ID = ConfigIconIds.ICON_MY_FILES;

    public static final int MENU_ICON_ID_REPOSITORY = R.drawable.ic_companyhome_dark;

    public static final int MODEL_ICON_ID_REPOSITORY = R.drawable.ic_companyhome_light;

    public static final int LABEL_ID_REPOSITORY = R.string.menu_browse_root;

    public static final int ICON_ID_SHARED = R.drawable.ic_shared_dark;

    public static final int MODEL_ICON_ID_SHARED = R.drawable.ic_shared_light;

    public static final int LABEL_ID_SHARED = R.string.menu_browse_shared;

    public static final int ICON_ID_USERHOME = R.drawable.ic_myfiles_dark;

    public static final int MODEL_ICON_ID_USERHOME = R.drawable.ic_myfiles_light;

    public static final int LABEL_ID_USERHOME = R.string.menu_browse_userhome;

    public static final String TYPE_ID = ConfigurationConstant.KEY_REPOSITORY;

    public static final int MENU_ICON_ID = R.drawable.ic_repository_dark;

    public static final int MODEL_ICON_ID = R.drawable.ic_repository_light;

    public static final int MENU_LABEL_ID = R.string.details;

    public static final int MENU_DESCRIPTION_ID = R.string.config_view_repository;

    public RepositoryConfigModel()
    {
        super();
        this.type = TYPE_ID;
        this.iconId = ICON_REPOSITORY_ID;
        this.iconResId = MENU_ICON_ID;
        this.iconModelResId = MODEL_ICON_ID;
        this.labelId = MENU_LABEL_ID;
        this.descriptionId = MENU_DESCRIPTION_ID;
    }

    public RepositoryConfigModel(Map<String, Object> configuration)
    {
        this();
        refresh(configuration);
    }

    protected void refresh(Map<String, Object> configuration)
    {
        iconResId = MENU_ICON_ID_REPOSITORY;
        iconId = ICON_REPOSITORY_ID;
        labelId = LABEL_ID_REPOSITORY;
        if (configuration != null && configuration.containsKey(ARGUMENT_SITE_SHORTNAME))
        {
            iconResId = R.drawable.ic_site_dark;
            iconId = ConfigIconIds.ICON_SITES;
        }

        if (configuration != null && configuration.containsKey(NodeBrowserTemplate.ARGUMENT_FOLDER_TYPE_ID))
        {
            String folderTypeValue = JSONConverter.getString(configuration,
                    NodeBrowserTemplate.ARGUMENT_FOLDER_TYPE_ID);
            if (NodeBrowserTemplate.FOLDER_TYPE_SHARED.equalsIgnoreCase(folderTypeValue))
            {
                iconResId = ICON_ID_SHARED;
                iconId = ICON_SHARED_ID;
                labelId = LABEL_ID_SHARED;
            }
            else if (NodeBrowserTemplate.FOLDER_TYPE_USERHOME.equalsIgnoreCase(folderTypeValue))
            {
                iconId = ICON_USERHOME_ID;
                iconResId = ICON_ID_USERHOME;
                labelId = LABEL_ID_USERHOME;
            }
        }

        if (configuration != null && (configuration.containsKey(NodeBrowserTemplate.ARGUMENT_FOLDER_NODEREF)
                || configuration.containsKey(NodeBrowserTemplate.ARGUMENT_PATH)))
        {
            iconResId = MENU_ICON_ID;
            iconId = ConfigIconIds.ICON_FOLDER;
        }
    }

    protected List<ConfigParameterModel> createParameters()
    {
        List<ConfigParameterModel> params = new ArrayList<>(1);
        params.add(new ConfigParameterModel(ARGUMENT_PATH, R.string.config_view_repository_path, PropertyType.STRING,
                false, true, true));
        params.add(new ConfigParameterModel(ARGUMENT_FOLDER_NODEREF, R.string.config_view_repository_noderef,
                PropertyType.STRING, false, true, true));
        params.add(new ConfigParameterModel(ARGUMENT_SITE_SHORTNAME, R.string.config_view_repository_siteShortName,
                PropertyType.STRING, false, true, true));
        params.add(new ConfigParameterModel(ARGUMENT_FOLDER_TYPE_ID, R.string.config_view_repository_folderTypeId,
                PropertyType.STRING, false, true, true));
        return params;
    }
}
