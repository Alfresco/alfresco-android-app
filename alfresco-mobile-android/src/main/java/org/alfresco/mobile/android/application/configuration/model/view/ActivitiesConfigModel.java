package org.alfresco.mobile.android.application.configuration.model.view;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.configuration.model.ConfigIconIds;
import org.alfresco.mobile.android.application.configuration.model.ConfigParameterModel;
import org.alfresco.mobile.android.application.configuration.model.ViewConfigModel;
import org.alfresco.mobile.android.ui.activitystream.ActivityStreamTemplate;

/**
 * Created by jpascal on 10/08/2015.
 */
public class ActivitiesConfigModel extends ViewConfigModel implements ActivityStreamTemplate
{
    public static final String TYPE_ID = ConfigurationConstant.KEY_ACTIVITIES;

    public static final String ICON_ID = ConfigIconIds.ICON_ACTIVITIES;

    public static final int MENU_ICON_ID = R.drawable.ic_activities_dark;

    public static final int MODEL_ICON_ID = R.drawable.ic_activities_light;

    public static final int LABEL_ID = R.string.menu_browse_activities;

    public static final int MENU_DESCRIPTION_ID = R.string.config_view_activities;

    public ActivitiesConfigModel()
    {
        super();
        this.type = TYPE_ID;
        this.iconId = ICON_ID;
        this.iconResId = MENU_ICON_ID;
        this.iconModelResId = MODEL_ICON_ID;
        this.labelId = LABEL_ID;
        this.descriptionId = MENU_DESCRIPTION_ID;
    }

    protected List<ConfigParameterModel> createParameters()
    {
        List<ConfigParameterModel> params = new ArrayList<>(2);
        params.add(new ConfigParameterModel(ARGUMENT_USERNAME, R.string.config_view_activities_username));
        params.add(new ConfigParameterModel(ARGUMENT_SITE_SHORTNAME, R.string.config_view_activities_siteshortname));
        return params;
    }

}
