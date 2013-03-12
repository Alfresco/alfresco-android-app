/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.intent;

import org.alfresco.mobile.android.intent.PublicIntent;

public interface IntentIntegrator extends PublicIntent
{
    String ALFRESCO_SCHEME_SHORT = "alfresco";

    String CLOUD_SIGNUP = "sign_up_cloud";

    String CLOUD_SIGNUP_I = "sign_up_cloud_i";

    String ACTION_REFRESH = "org.alfresco.mobile.android.intent.ACTION_REFRESH";

    String CATEGORY_REFRESH = "org.alfresco.mobile.android.intent.CATEGORY_REFRESH";

    String CATEGORY_REFRESH_ALL = "org.alfresco.mobile.android.intent.CATEGORY_REFRESH_ALL";

    String CATEGORY_REFRESH_DELETE = "org.alfresco.mobile.android.intent.CATEGORY_REFRESH_DELETE";

    String CATEGORY_REFRESH_OTHERS = "org.alfresco.mobile.android.intent.CATEGORY_REFRESH_OTHERS";

    String CATEGORY_REMOVE_AND_REFRESH = "org.alfresco.mobile.android.intent.CATEGORY_REMOVE_AND_REFRESH";

    String ACCOUNT_TYPE = "org.alfresco.mobile.android/object.account";

    String FILE_TYPE = "org.alfresco.mobile.android/object.file";

    String ACTION_LOAD_SESSION_FINISH = "org.alfresco.mobile.android.intent.ACTION_LOAD_SESSION_FINISH";

    String ACTION_CHECK_SIGNUP = "org.alfresco.mobile.android.intent.ACTION_CHECK_SIGNUP";

    String ACTION_DISPLAY_NODE = "org.alfresco.mobile.android.intent.ACTION_DISPLAY_NODE";

    String ACTION_USER_AUTHENTICATION = "org.alfresco.mobile.android.intent.ACTION_USER_AUTHENTICATION";

    String CATEGORY_OAUTH = "org.alfresco.mobile.android.intent.CATEGORY_OAUTH";

    String CATEGORY_OAUTH_REFRESH = "org.alfresco.mobile.android.intent.CATEGORY_OAUTH_REFRESH";

    String ACTION_DISPLAY_ERROR = "org.alfresco.mobile.android.intent.DISPLAY_ERROR";

    String ACTION_DISPLAY_ERROR_HOMESCREEN = "org.alfresco.mobile.android.intent.DISPLAY_ERROR_HOMESCREEN";

    String ACTION_DISPLAY_ERROR_IMPORT = "org.alfresco.mobile.android.intent.DISPLAY_ERROR_IMPORT";

    String DISPLAY_ERROR_DATA = "org.alfresco.mobile.android.intent.DISPLAY_ERROR_DATA";

    String ACTION_DISPLAY_DIALOG = "org.alfresco.mobile.android.intent.DISPLAY_DIALOG";

    String ACTION_DISPLAY_DIALOG_HOMESCREEN = "org.alfresco.mobile.android.intent.DISPLAY_DIALOG_HOMESCREEN";

    String ACTION_REMOVE_FRAGMENT = "org.alfresco.mobile.android.intent.REMOVE_FRAGMENT";

    String REMOVE_FRAGMENT_TAG = "org.alfresco.mobile.android.intent.REMOVE_FRAGMENT_TAG";

    String REMOVE_FRAGMENT_WAITING = "org.alfresco.mobile.android.intent.REMOVE_FRAGMENT_WAITING";

    String REMOVE_LOADER_ID = "org.alfresco.mobile.android.intent.REMOVE_LOADER_ID";

}
