/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.intent;

import org.alfresco.mobile.android.intent.PublicIntent;

public class IntentIntegrator extends PublicIntent
{
    public static final String ACTION_REFRESH = "org.alfresco.mobile.android.intent.ACTION_REFRESH";

    public static final String CATEGORY_REFRESH = "org.alfresco.mobile.android.intent.CATEGORY_REFRESH";
    public static final String CATEGORY_REFRESH_ALL = "org.alfresco.mobile.android.intent.CATEGORY_REFRESH_ALL";
    public static final String CATEGORY_REFRESH_DELETE = "org.alfresco.mobile.android.intent.CATEGORY_REFRESH_DELETE";
    public static final String CATEGORY_REFRESH_OTHERS = "org.alfresco.mobile.android.intent.CATEGORY_REFRESH_OTHERS";
    public static final String CATEGORY_REMOVE_AND_REFRESH = "org.alfresco.mobile.android.intent.CATEGORY_REMOVE_AND_REFRESH";

    public static final String ACCOUNT_TYPE = "org.alfresco.mobile.android/object.account";
    public static final String FILE_TYPE = "org.alfresco.mobile.android/object.file";

    public static final String ACTION_LOAD_SESSION_FINISH = "org.alfresco.mobile.android.intent.ACTION_LOAD_SESSION_FINISH";
    public static final String ACTION_CHECK_SIGNUP = "org.alfresco.mobile.android.intent.ACTION_CHECK_SIGNUP";
    public static final String ACTION_DISPLAY_NODE = "org.alfresco.mobile.android.intent.ACTION_DISPLAY_NODE";
    
    public static final String ACTION_USER_AUTHENTICATION = "org.alfresco.mobile.android.intent.ACTION_USER_AUTHENTICATION";
    public static final String CATEGORY_OAUTH = "org.alfresco.mobile.android.intent.CATEGORY_OAUTH";


}
