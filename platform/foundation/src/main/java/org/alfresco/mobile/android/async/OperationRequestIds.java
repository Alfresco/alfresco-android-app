/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.mobile.android.async;

/**
 * Contains all request unique identifier</br>
 * <p>
 * Pattern : [Family ID][Operation ID]
 * </p>
 * <ul>
 * <li>Create 1</li>
 * <li>Read 2</li>
 * <li>Update 3</li>
 * <li>Delete 4</li>
 * </ul>
 * 
 * @author jpascal
 */
public interface OperationRequestIds
{
    int OPERATION_CREATE = 1;

    int OPERATION_READ = 2;

    int OPERATION_LIST = 3;

    int OPERATION_RENAME = 4;

    int OPERATION_UPDATE = 5;

    int OPERATION_DELETE = 6;

    int OPERATION_COMPLETE = 7;

    int OPERATION_SEARCH = 8;

    // ///////////////////////////////////////////////////////////////////////////
    // ACCOUNTS
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_ACCOUNT = 0;

    int ID_ACCOUNT_CREATE = FAMILY_ACCOUNT + OPERATION_CREATE;

    int ID_ACCOUNT_CHECK = FAMILY_ACCOUNT + OPERATION_READ;

    // ///////////////////////////////////////////////////////////////////////////
    // SESSION
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_SESSION = 50;

    int ID_SESSION_CREATE = FAMILY_SESSION + OPERATION_CREATE;

    int ID_NETWORK_LIST = FAMILY_SESSION + 10;

    int ID_OAUTH = FAMILY_SESSION + 20;

    // ///////////////////////////////////////////////////////////////////////////
    // NODE
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_NODE = 100;

    int ID_NODE_BROWSE = FAMILY_NODE + OPERATION_LIST;

    int ID_NODE_CREATE = FAMILY_NODE + OPERATION_CREATE;

    int ID_NODE_DOWNLOAD = FAMILY_NODE + OPERATION_READ;

    int ID_NODE_CREATE_FOLDER = FAMILY_NODE + 10 + OPERATION_CREATE;

    int ID_NODE_DELETE = FAMILY_NODE + OPERATION_DELETE;

    int ID_NODE_UPDATE = FAMILY_NODE + OPERATION_UPDATE;

    int ID_NODE_UPDATE_CONTENT = FAMILY_NODE + 20;

    // ///////////////////////////////////////////////////////////////////////////
    // FAVORITE
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_FAVORITE = 200;

    int ID_FAVORITE_NODE_BROWSE = FAMILY_FAVORITE + OPERATION_LIST;

    int ID_FAVORITE_NODE_READ = FAMILY_FAVORITE + OPERATION_READ;

    int ID_FAVORITE_CREATE = FAMILY_FAVORITE + OPERATION_CREATE;

    // ///////////////////////////////////////////////////////////////////////////
    // FAVORITE
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_SYNC = 210;

    int ID_SYNC_NODE_BROWSE = FAMILY_SYNC + OPERATION_LIST;

    int ID_SYNC_NODE_READ = FAMILY_SYNC + OPERATION_READ;

    int ID_SYNC_CREATE = FAMILY_SYNC + OPERATION_CREATE;

    // ///////////////////////////////////////////////////////////////////////////
    // LIKE
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_LIKE = 250;

    int ID_LIKE_LIST = FAMILY_LIKE + OPERATION_LIST;

    // ///////////////////////////////////////////////////////////////////////////
    // VERSION
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_VERSION = 260;

    int ID_VERSION_LIST = FAMILY_VERSION + OPERATION_LIST;

    // ///////////////////////////////////////////////////////////////////////////
    // WORKFLOW
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_WORKFLOW = 300;

    int ID_WORKFLOW_TASK_LIST = FAMILY_WORKFLOW + OPERATION_LIST;

    int ID_WORKFLOW_TASK_COMPLETE = FAMILY_WORKFLOW + OPERATION_COMPLETE;

    int ID_WORKFLOW_TASK_REASSIGN = FAMILY_WORKFLOW + 9;

    int ID_WORKFLOW_PROCESS_START = FAMILY_WORKFLOW + 20 + OPERATION_CREATE;

    int ID_WORKFLOW_PROCESS_LIST = FAMILY_WORKFLOW + 20 + OPERATION_LIST;

    int ID_WORKFLOW_PROCESS_DEFINITION_LIST = FAMILY_WORKFLOW + 20 + OPERATION_LIST;

    int ID_WORKFLOW_ATTACHMENT_LIST = FAMILY_WORKFLOW + 50 + OPERATION_LIST;

    // ///////////////////////////////////////////////////////////////////////////
    // SITE
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_SITE = 400;

    int ID_SITE_LIST = FAMILY_SITE + OPERATION_LIST;

    int ID_SITE_SEARCH = FAMILY_SITE + OPERATION_SEARCH;

    int ID_SITE_FAVORITE_UPDATE = FAMILY_SITE + OPERATION_UPDATE;

    int ID_SITE_MEMBERSHIP_CREATE = FAMILY_SITE + 10 + OPERATION_CREATE;

    int ID_SITE_MEMBERSHIP_LIST = FAMILY_SITE + 10 + OPERATION_LIST;

    int ID_SITE_MEMBERSHIP_CANCEL = FAMILY_SITE + 10 + OPERATION_DELETE;

    // ///////////////////////////////////////////////////////////////////////////
    // PERSON
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_PERSON = 500;

    int ID_PERSON_READ = FAMILY_PERSON + OPERATION_READ;

    int ID_PERSON_LIST = FAMILY_PERSON + OPERATION_LIST;

    int ID_AVATAR_READ = FAMILY_PERSON + 10 + OPERATION_READ;

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIVITY STREAM
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_ACTIVITY_STREAM = 600;

    int ID_ACTIVITY_STREAM_READ = FAMILY_ACTIVITY_STREAM + OPERATION_LIST;

    // ///////////////////////////////////////////////////////////////////////////
    // SEARCH
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_SEARCH = 700;

    int ID_SEARCH_LIST = FAMILY_SEARCH + OPERATION_LIST;

    // ///////////////////////////////////////////////////////////////////////////
    // TAG
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_TAG = 800;

    int ID_TAG_LIST = FAMILY_TAG + OPERATION_LIST;

    // ///////////////////////////////////////////////////////////////////////////
    // COMMENT
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_COMMENT = 900;

    int ID_COMMENT_LIST = FAMILY_COMMENT + OPERATION_LIST;

    int ID_COMMENT_CREATE = FAMILY_COMMENT + OPERATION_CREATE;

    // ///////////////////////////////////////////////////////////////////////////
    // FILE
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_FILE = 2000;

    int ID_FILE_LIST = FAMILY_FILE + OPERATION_LIST;

    int ID_FILE_CREATE_DIRECTORY = FAMILY_FILE + OPERATION_CREATE;

    int ID_FILE_DELETE = FAMILY_FILE + OPERATION_DELETE;

    int ID_FILE_READ = FAMILY_FILE + OPERATION_READ;

    int ID_FILE_RENAME = FAMILY_FILE + OPERATION_RENAME;

    // ///////////////////////////////////////////////////////////////////////////
    // CONFIGURATION
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_CONFIGURATION = 3000;

    int ID_CONFIGURATION_READ = FAMILY_CONFIGURATION + OPERATION_READ;
    
    // ///////////////////////////////////////////////////////////////////////////
    // TYPE DEFINITION
    // ///////////////////////////////////////////////////////////////////////////
    int FAMILY_TYPE_DEFINITION = 3100;

    int ID_TYPE_DEFINITION_READ = FAMILY_TYPE_DEFINITION + OPERATION_READ;
}
