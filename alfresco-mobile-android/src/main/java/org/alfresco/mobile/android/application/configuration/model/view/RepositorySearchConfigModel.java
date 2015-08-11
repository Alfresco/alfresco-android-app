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
import org.alfresco.mobile.android.ui.node.search.SearchNodesTemplate;

/**
 * Created by jpascal on 10/08/2015.
 */
public class RepositorySearchConfigModel extends ViewConfigModel implements SearchNodesTemplate
{
    public static final String TYPE_ID = ConfigurationConstant.KEY_REPOSITORY_SEARCH;

    public static final String ICON_ID = ConfigIconIds.ICON_SEARCH;

    public static final int MENU_ICON_ID = R.drawable.ic_search_dark;

    public static final int MODEL_ICON_ID = R.drawable.ic_search_light;

    public static final int MENU_LABEL_ID = R.string.menu_search;

    public static final int MENU_DESCRIPTION_ID = R.string.config_view_repository_search;

    public RepositorySearchConfigModel()
    {
        super();
        this.type = TYPE_ID;
        this.iconId = ICON_ID;
        this.iconResId = MENU_ICON_ID;
        this.iconModelResId = MODEL_ICON_ID;
        this.labelId = MENU_LABEL_ID;
        this.descriptionId = MENU_DESCRIPTION_ID;
    }

    public RepositorySearchConfigModel(Map<String, Object> configuration)
    {
        this();
    }

    protected List<ConfigParameterModel> createParameters()
    {
        List<ConfigParameterModel> params = new ArrayList<>(1);
        params.add(new ConfigParameterModel(ARGUMENT_KEYWORDS, R.string.config_view_repository_search_keywords,
                PropertyType.STRING, false, true, true));
        params.add(new ConfigParameterModel(ARGUMENT_EXACTMATCH, R.string.config_view_repository_search_isExact,
                PropertyType.BOOLEAN, false, false, true));
        params.add(new ConfigParameterModel(ARGUMENT_FULLTEXT, R.string.config_view_repository_search_fullText,
                PropertyType.BOOLEAN, false, false, true));
        params.add(new ConfigParameterModel(ARGUMENT_SEARCH_FOLDER,
                R.string.config_view_repository_search_searchFolderOnly, PropertyType.BOOLEAN, false, false, true));
        params.add(new ConfigParameterModel(ARGUMENT_STATEMENT, R.string.config_view_repository_search_statement,
                PropertyType.STRING, false, true, true));
        return params;
    }

}
