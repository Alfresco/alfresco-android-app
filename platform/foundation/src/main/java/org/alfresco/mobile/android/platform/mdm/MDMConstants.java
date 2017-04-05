/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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

    String ALFRESCO_USER_PROFILE = AlfrescoIntentAPI.EXTRA_ALFRESCO_USER_PROFILE;

    String[] MANDATORUY_CONFIGURATION_KEYS = new String[] { ALFRESCO_REPOSITORY_URL, ALFRESCO_USERNAME };

}
