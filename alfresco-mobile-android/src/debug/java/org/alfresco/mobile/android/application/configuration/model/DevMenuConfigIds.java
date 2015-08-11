package org.alfresco.mobile.android.application.configuration.model;

import java.util.ArrayList;

import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.fragments.config.DefaultMenuConfigIds;

/**
 * Created by jpascal on 09/08/2015.
 */
public interface DevMenuConfigIds extends DefaultMenuConfigIds, ConfigurationConstant, ConfigIconIds
{
    String VIEW_CONFIG_EDITOR = "view-config-menu-editor";

    String KEY_CONFIG_EDITOR = "org.alfresco.client.view.config.menu.editor";

    String ICON_CONFIG_EDITOR = PREFIX_ICON.concat(".settings");

    ArrayList<String> MENU_CONFIG_IDS = new ArrayList<String>(DefaultMenuConfigIds.MENU_TYPE_IDS)
    {
        {
            add(VIEW_CONFIG_EDITOR);
        }
    };

    ArrayList<String> TEMPLATE_VIEW_IDS = new ArrayList<String>(ConfigurationConstant.VIEW_TYPE_IDS)
    {
        {
            add(KEY_CONFIG_EDITOR);
        }
    };
}
