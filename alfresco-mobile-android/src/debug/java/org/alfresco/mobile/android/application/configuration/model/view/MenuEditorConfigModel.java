package org.alfresco.mobile.android.application.configuration.model.view;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.model.DevMenuConfigIds;
import org.alfresco.mobile.android.application.configuration.model.ViewConfigModel;
import org.alfresco.mobile.android.ui.activitystream.ActivityStreamTemplate;

/**
 * Created by jpascal on 10/08/2015.
 */
public class MenuEditorConfigModel extends ViewConfigModel implements ActivityStreamTemplate
{
    public static final String TYPE_ID = DevMenuConfigIds.KEY_CONFIG_EDITOR;

    public static final String ICON_ID = DevMenuConfigIds.ICON_CONFIG_EDITOR;

    public static final int MENU_ICON_ID = R.drawable.ic_settings_dark;

    public static final int MODEL_ICON_ID = R.drawable.ic_settings_light;

    public static final int LABEL_ID = R.string.settings;

    public static final int MENU_DESCRIPTION_ID = R.string.config_view_config_editor;

    public MenuEditorConfigModel()
    {
        super();
        this.type = TYPE_ID;
        this.iconId = ICON_ID;
        this.iconResId = MENU_ICON_ID;
        this.iconModelResId = MODEL_ICON_ID;
        this.labelId = LABEL_ID;
        this.descriptionId = MENU_DESCRIPTION_ID;
    }
}
