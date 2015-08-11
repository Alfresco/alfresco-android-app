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
import org.alfresco.mobile.android.ui.person.PeopleFragmentArgument;

/**
 * Created by jpascal on 10/08/2015.
 */
public class PeopleConfigModel extends ViewConfigModel implements PeopleFragmentArgument
{
    public static final String TYPE_ID = ConfigurationConstant.KEY_PEOPLE;

    public static final String ICON_ID = ConfigIconIds.ICON_GROUP;

    public static final int MENU_ICON_ID = R.drawable.ic_users_dark;

    public static final int MODEL_ICON_ID = R.drawable.ic_users_light;

    public static final int MENU_LABEL_ID = R.string.users;

    public static final int MENU_DESCRIPTION_ID = R.string.config_view_people;

    public PeopleConfigModel()
    {
        super();
        this.type = TYPE_ID;
        this.iconId = ICON_ID;
        this.iconResId = MENU_ICON_ID;
        this.iconModelResId = MODEL_ICON_ID;
        this.labelId = MENU_LABEL_ID;
        this.descriptionId = MENU_DESCRIPTION_ID;
    }

    public PeopleConfigModel(Map<String, Object> configuration)
    {
        this();
        refresh(configuration);
    }

    @Override
    protected void refresh(Map<String, Object> configuration)
    {
        if (configuration.containsKey(ARGUMENT_SITE_SHORTNAME))
        {
            this.labelId = R.string.members;
        }
    }

    protected List<ConfigParameterModel> createParameters()
    {
        List<ConfigParameterModel> params = new ArrayList<>(2);
        params.add(new ConfigParameterModel(ARGUMENT_KEYWORDS, R.string.config_view_people_keywords,
                PropertyType.STRING, true, true));
        params.add(new ConfigParameterModel(ARGUMENT_SITE_SHORTNAME, R.string.config_view_people_siteshortname,
                PropertyType.STRING, true, true));
        return params;
    }
}
