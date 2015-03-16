package org.alfresco.mobile.android.platform.mdm;

/**
 * Created by jpascal on 11/02/2015.
 * 
 * @since 1.5
 */
public interface MDMConstants
{
    String ALFRESCO_USERNAME = "AlfrescoUserName";

    String ALFRESCO_DISPLAY_NAME = "AlfrescoDisplayName";

    String ALFRESCO_REPOSITORY_URL = "AlfrescoRepositoryURL";

    String ALFRESCO_SHARE_URL = "AlfrescoShareURL";

    String[] MANDATORUY_CONFIGURATION_KEYS = new String[] { ALFRESCO_REPOSITORY_URL, ALFRESCO_USERNAME };

}
