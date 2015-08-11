package org.alfresco.mobile.android.application.configuration.model.view;

import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.configuration.model.ConfigIconIds;
import org.alfresco.mobile.android.application.configuration.model.ViewConfigModel;

/**
 * Created by jpascal on 10/08/2015.
 */
public class LocalConfigModel extends ViewConfigModel
{
    public static final String TYPE_ID = ConfigurationConstant.KEY_LOCAL;

    public static final String ICON_ID = ConfigIconIds.ICON_LOCAL_FILES;

    public static final int MENU_ICON_ID = R.drawable.ic_local_files_dark;

    public static final int MODEL_ICON_ID = R.drawable.ic_local_files_light;

    public static final int MENU_LABEL_ID = R.string.menu_local_files;

    public static final int MENU_DESCRIPTION_ID = R.string.config_view_local;

    public LocalConfigModel()
    {
        super();
        this.type = TYPE_ID;
        this.iconId = ICON_ID;
        this.iconResId = MENU_ICON_ID;
        this.iconModelResId = MODEL_ICON_ID;
        this.labelId = MENU_LABEL_ID;
        this.descriptionId = MENU_DESCRIPTION_ID;
    }

    public LocalConfigModel(Map<String, Object> configuration)
    {
        this();
    }
}
