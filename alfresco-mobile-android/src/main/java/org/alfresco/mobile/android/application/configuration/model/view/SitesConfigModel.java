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
import org.alfresco.mobile.android.ui.site.SitesTemplate;

/**
 * Created by jpascal on 10/08/2015.
 */
public class SitesConfigModel extends ViewConfigModel implements SitesTemplate
{
    public static final String TYPE_ID = ConfigurationConstant.KEY_SITES;

    public static final String ICON_ID = ConfigIconIds.ICON_SITES;

    public static final int MENU_ICON_ID = R.drawable.ic_site_dark;

    public static final int MODEL_ICON_ID = R.drawable.ic_site_light;

    public static final int MENU_LABEL_ID = R.string.menu_browse_sites;

    public static final int MENU_DESCRIPTION_ID = R.string.config_view_sites;

    public SitesConfigModel()
    {
        super();
        this.type = TYPE_ID;
        this.iconId = ICON_ID;
        this.iconResId = MENU_ICON_ID;
        this.iconModelResId = MODEL_ICON_ID;
        this.labelId = MENU_LABEL_ID;
        this.descriptionId = MENU_DESCRIPTION_ID;
    }

    public SitesConfigModel(Map<String, Object> configuration)
    {
        this();
    }

    protected List<ConfigParameterModel> createParameters()
    {
        List<ConfigParameterModel> params = new ArrayList<>(1);
        params.add(new ConfigParameterModel(ARGUMENT_SHOW, R.string.config_view_sites_show));
        params.add(new ConfigParameterModel(ARGUMENT_KEYWORDS, R.string.config_view_advanced_search_type,
                PropertyType.STRING, false, true, true));
        return params;
    }
}
