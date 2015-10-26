package org.alfresco.mobile.android.application.configuration.model.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.PropertyType;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.configuration.model.ConfigIconIds;
import org.alfresco.mobile.android.application.configuration.model.ConfigParameterModel;

/**
 * Created by jpascal on 10/08/2015.
 */
public class FolderDetailsConfigModel extends NodeDetailsConfigModel
{
    public static final String TYPE_ID = ConfigurationConstant.KEY_FOLDER_DETAILS;

    public static final String ICON_ID = ConfigIconIds.ICON_FOLDER;

    public static final int MENU_ICON_ID = R.drawable.ic_repository_dark;

    public static final int MODEL_ICON_ID = R.drawable.ic_repository_light;

    public static final int MENU_LABEL_ID = R.string.details;

    public static final int MENU_DESCRIPTION_ID = R.string.config_view_folder_details;

    public FolderDetailsConfigModel()
    {
        super();
        this.type = TYPE_ID;
        this.iconId = ICON_ID;
        this.iconResId = MENU_ICON_ID;
        this.iconModelResId = MODEL_ICON_ID;
        this.labelId = MENU_LABEL_ID;
        this.descriptionId = MENU_DESCRIPTION_ID;
    }

    public FolderDetailsConfigModel(Map<String, Object> configuration)
    {
        this();
    }

    protected List<ConfigParameterModel> createParameters()
    {
        List<ConfigParameterModel> params = new ArrayList<>(1);
        params.add(new ConfigParameterModel(ARGUMENT_NODE_ID, R.string.config_view_folder_details_noderef,
                PropertyType.STRING, false, true, true));
        params.add(new ConfigParameterModel(ARGUMENT_PATH, R.string.config_view_folder_details_path,
                PropertyType.STRING, false, true, true));
        return params;
    }
}
