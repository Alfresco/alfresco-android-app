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


public interface IntentIntegrator extends PublicIntent
{
    
    // ///////////////////////////////////////////////////////////////////////////
    // SIGNUP PROCESS
    // ///////////////////////////////////////////////////////////////////////////
    String ALFRESCO_SCHEME_SHORT = "alfresco";

    String CLOUD_SIGNUP = "sign_up_cloud";

    String CLOUD_SIGNUP_I = "sign_up_cloud_i";

    String ACTION_CHECK_SIGNUP = "org.alfresco.mobile.android.intent.ACTION_CHECK_SIGNUP";

    String ACTION_USER_AUTHENTICATION = "org.alfresco.mobile.android.intent.ACTION_USER_AUTHENTICATION";

    String CATEGORY_OAUTH = "org.alfresco.mobile.android.intent.CATEGORY_OAUTH";

    String CATEGORY_OAUTH_REFRESH = "org.alfresco.mobile.android.intent.CATEGORY_OAUTH_REFRESH";

    String ACTION_DISPLAY_ERROR = "org.alfresco.mobile.android.intent.DISPLAY_ERROR";

    String ACTION_DISPLAY_ERROR_HOMESCREEN = "org.alfresco.mobile.android.intent.DISPLAY_ERROR_HOMESCREEN";

    String ACTION_DISPLAY_ERROR_IMPORT = "org.alfresco.mobile.android.intent.DISPLAY_ERROR_IMPORT";

    String DISPLAY_ERROR_DATA = "org.alfresco.mobile.android.intent.DISPLAY_ERROR_DATA";

    String ACTION_DISPLAY_DIALOG_HOMESCREEN = "org.alfresco.mobile.android.intent.DISPLAY_DIALOG_HOMESCREEN";

    String ACTION_REMOVE_FRAGMENT = "org.alfresco.mobile.android.intent.REMOVE_FRAGMENT";

    String REMOVE_FRAGMENT_TAG = "org.alfresco.mobile.android.intent.REMOVE_FRAGMENT_TAG";

    String REMOVE_FRAGMENT_WAITING = "org.alfresco.mobile.android.intent.REMOVE_FRAGMENT_WAITING";

    String REMOVE_LOADER_ID = "org.alfresco.mobile.android.intent.REMOVE_LOADER_ID";

    String ACTION_DOWNLOAD_COMPLETE = "org.alfresco.mobile.android.intent.ACTION_DOWNLOAD_COMPLETE";

    String ACTION_DELETE_COMPLETE = "org.alfresco.mobile.android.intent.ACTION_DELETE_COMPLETE";

    String ACTION_UPLOAD_START = "org.alfresco.mobile.android.intent.ACTION_UPLOAD_START";

    String EXTRA_DOCUMENT_NAME = "org.alfresco.mobile.android.intent.EXTRA_DOCUMENT_NAME";

    String ACTION_UPLOAD_COMPLETE = "org.alfresco.mobile.android.intent.ACTION_UPLOAD_COMPLETE";

    String ACTION_CREATE_FOLDER_START = "org.alfresco.mobile.android.intent.ACTION_CREATE_FOLDER_START";

    String ACTION_CREATE_FOLDER_COMPLETE = "org.alfresco.mobile.android.intent.ACTION_CREATE_FOLDER_COMPLETE";

    String EXTRA_CREATED_FOLDER = "org.alfresco.mobile.android.intent.EXTRA_CREATED_FOLDER";

    String ACTION_UPDATE_START = "org.alfresco.mobile.android.intent.ACTION_UPDATE_START";

    String ACTION_UPDATE_COMPLETE = "org.alfresco.mobile.android.intent.ACTION_UPDATE_COMPLETE";

    String EXTRA_UPDATED_DOCUMENT = "org.alfresco.mobile.android.intent.EXTRA_UPDATED_DOCUMENT";

    String ACTION_LIKE_COMPLETE = "org.alfresco.mobile.android.intent.ACTION_LIKE_COMPLETE";

    String EXTRA_LIKE = "org.alfresco.mobile.android.intent.EXTRA_LIKE";

    String ACTION_FAVORITE_COMPLETE = "org.alfresco.mobile.android.intent.ACTION_FAVORITE_COMPLETE";

    String EXTRA_FAVORITE = "org.alfresco.mobile.android.intent.EXTRA_FAVORITE";

    String ACTION_DISPLAY_OPERATIONS = "org.alfresco.mobile.android.intent.ACTION_DISPLAY_OPERATIONS";
    
    String ACTION_PICK_FILE = "org.alfresco.mobile.android.intent.ACTION_PICK_FILE";

    // ///////////////////////////////////////////////////////////////////////////
    // OPERATIONS MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    
    // ACTION
    String ACTION_OPERATION_STOP = "org.alfresco.mobile.android.intent.ACTION_OPERATION_STOP";

    String ACTION_OPERATIONS_STOP = "org.alfresco.mobile.android.intent.ACTION_OPERATIONS_STOP";
    
    String ACTION_OPERATIONS_CANCEL = "org.alfresco.mobile.android.intent.ACTION_OPERATIONS_CANCEL";

    
    // BROADCAST
    String ACTION_OPERATION_COMPLETED = "org.alfresco.mobile.android.intent.ACTION_OPERATION_COMPLETE";

    String ACTION_OPERATIONS_COMPLETED = "org.alfresco.mobile.android.intent.ACTION_OPERATIONS_COMPLETE";
    // ///////////////////////////////////////////////////////////////////////////
    // ACCOUNT MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////

    // ACTION
    /** Load or reuse a session for the specified account. */
    String ACTION_LOAD_ACCOUNT = "org.alfresco.mobile.android.intent.ACTION_LOAD_ACCOUNT";
    
    /** Create a new session for the specified account. */
    String ACTION_RELOAD_ACCOUNT = "org.alfresco.mobile.android.intent.ACTION_RELOAD_ACCOUNT";

    String ACTION_CREATE_ACCOUNT = "org.alfresco.mobile.android.intent.ACTION_CREATE_ACCOUNT";

    // BROADCAST
    /** The specified account is inactive. */
    String ACTION_ACCOUNT_INACTIVE = "org.alfresco.mobile.android.intent.ACTION_ACCOUNT_INACTIVE";
    
    String ACTION_LOAD_ACCOUNT_ERROR = "org.alfresco.mobile.android.intent.ACTION_LOAD_ACCOUNT_ERROR";
    
    String ACTION_LOAD_ACCOUNT_STARTED = "org.alfresco.mobile.android.intent.ACTION_LOAD_ACCOUNT_STARTED";

    String ACTION_LOAD_ACCOUNT_COMPLETED = "org.alfresco.mobile.android.intent.ACTION_LOAD_ACCOUNT_COMPLETED";

    String ACTION_CREATE_ACCOUNT_STARTED = "org.alfresco.mobile.android.intent.ACTION_CREATE_ACCOUNT_STARTED";

    String ACTION_CREATE_ACCOUNT_COMPLETED = "org.alfresco.mobile.android.intent.ACTION_CREATE_ACCOUNT_COMPLETED";
    
    String ACTION_DELETE_ACCOUNT_STARTED = "org.alfresco.mobile.android.intent.ACTION_DELETE_ACCOUNT_STARTED";

    String ACTION_DELETE_ACCOUNT_COMPLETED = "org.alfresco.mobile.android.intent.ACTION_DELETE_ACCOUNT_COMPLETED";

    // EXTRA
    String EXTRA_ACCOUNT_ID = "org.alfresco.mobile.android.intent.EXTRA_ACCOUNT_ID";
    
    String EXTRA_OAUTH_DATA = "org.alfresco.mobile.android.intent.EXTRA_OAUTH_DATA";
    
    String EXTRA_CREATE_REQUEST = "org.alfresco.mobile.android.intent.EXTRA_CREATE_REQUEST";
    
    String EXTRA_NETWORK_ID = "org.alfresco.mobile.android.intent.EXTRA_NETWORK_ID";
    // ///////////////////////////////////////////////////////////////////////////
    // DISPLAY DIALOG
    // ///////////////////////////////////////////////////////////////////////////
    /** Display dialog with extra bundle */
    String ACTION_DISPLAY_DIALOG = "org.alfresco.mobile.android.intent.ACTION_DISPLAY_DIALOG";

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    String ACTION_UPDATE_STARTED = "org.alfresco.mobile.android.intent.ACTION_UPDATE_STARTED";
    
    String ACTION_UPDATE_COMPLETED = "org.alfresco.mobile.android.intent.ACTION_UPDATE_COMPLETED";

    String EXTRA_UPDATED_NODE = "org.alfresco.mobile.android.intent.EXTRA_UPDATED_NODE";

    String EXTRA_UPDATED_FILE = "org.alfresco.mobile.android.intent.EXTRA_UPDATED_FILE";

    String EXTRA_LIBRARY = "org.alfresco.mobile.android.intent.EXTRA_LIBRARY";

}
