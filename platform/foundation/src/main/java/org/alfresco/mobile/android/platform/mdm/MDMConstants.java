package org.alfresco.mobile.android.platform.mdm;

import org.alfresco.mobile.android.platform.intent.AlfrescoIntentAPI;

/**
 * Created by jpascal on 11/02/2015.
 * 
 * @since 1.5
 */
public interface MDMConstants
{
    String ALFRESCO_USERNAME = AlfrescoIntentAPI.EXTRA_ALFRESCO_USERNAME;

    String ALFRESCO_DISPLAY_NAME = AlfrescoIntentAPI.EXTRA_ALFRESCO_DISPLAY_NAME;

    String ALFRESCO_REPOSITORY_URL = AlfrescoIntentAPI.EXTRA_ALFRESCO_REPOSITORY_URL;

    String ALFRESCO_SHARE_URL = AlfrescoIntentAPI.EXTRA_ALFRESCO_SHARE_URL;

    String[] MANDATORUY_CONFIGURATION_KEYS = new String[] { ALFRESCO_REPOSITORY_URL, ALFRESCO_USERNAME };

}
