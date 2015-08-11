package org.alfresco.mobile.android.application.configuration.model.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.configuration.model.ConfigIconIds;
import org.alfresco.mobile.android.application.configuration.model.ConfigParameterModel;
import org.alfresco.mobile.android.application.configuration.model.ViewConfigModel;
import org.alfresco.mobile.android.application.fragments.search.AdvancedSearchFragmentTemplate;

/**
 * Created by jpascal on 10/08/2015.
 */
public class AdvancedSearchConfigModel extends ViewConfigModel implements AdvancedSearchFragmentTemplate
{
    public static final String TYPE_ID = ConfigurationConstant.KEY_SEARCH_ADVANCED;

    public static final String ICON_ID = ConfigIconIds.ICON_SEARCH;

    public static final int MENU_ICON_ID = R.drawable.ic_search_dark;

    public static final int MODEL_ICON_ID = R.drawable.ic_search_light;

    public static final int MENU_LABEL_ID = R.string.search_advanced;

    public static final int MENU_DESCRIPTION_ID = R.string.config_view_search_advanced;

    public AdvancedSearchConfigModel()
    {
        super();
        this.type = TYPE_ID;
        this.iconId = ICON_ID;
        this.iconResId = MENU_ICON_ID;
        this.iconModelResId = MODEL_ICON_ID;
        this.labelId = MENU_LABEL_ID;
        this.descriptionId = MENU_DESCRIPTION_ID;
    }

    public AdvancedSearchConfigModel(Map<String, Object> configuration)
    {
        this();
    }

    protected List<ConfigParameterModel> createParameters()
    {
        List<ConfigParameterModel> params = new ArrayList<>(1);
        params.add(new ConfigParameterModel(ARGUMENT_SEARCH_TYPE, R.string.config_view_advanced_search_type));
        return params;
    }
}
