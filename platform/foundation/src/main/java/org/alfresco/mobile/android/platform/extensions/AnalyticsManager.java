/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
package org.alfresco.mobile.android.platform.extensions;

import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;

public abstract class AnalyticsManager extends Manager
{

    ///////////////////////////////////////////////////////////
    // EVENTS : CATEGORIES
    ///////////////////////////////////////////////////////////
    public static final String CATEGORY_ACCOUNT = "Account";

    public static final String CATEGORY_SESSION = "Session";

    public static final String CATEGORY_DOCUMENT_MANAGEMENT = "DM";

    public static final String CATEGORY_SITE_MANAGEMENT = "Site";

    public static final String CATEGORY_BPM = "BPM";

    public static final String CATEGORY_SEARCH = "Search";

    public static final String CATEGORY_SYNC = "Sync";

    public static final String CATEGORY_SETTINGS = "Settings";

    public static final String CATEGORY_USER = "User";

    public static final String CATEGORY_WIDGET = "Widget";

    public static final String CATEGORY_DOC_PROVIDER = "Document Provider";

    ///////////////////////////////////////////////////////////
    // EVENTS : ACTIONS
    ///////////////////////////////////////////////////////////
    public static final String ACTION_QUICK_ACTIONS = "Quick Actions";

    public static final String ACTION_TOOLBAR = "Toolbar";

    public static final String ACTION_SHORTCUT = "Shortcut";

    public static final String ACTION_INFO = "Info";

    public static final String ACTION_SEARCH = "Search";

    public static final String ACTION_SWITCH = "Switch";

    public static final String ACTION_CREATE = "Create";

    public static final String ACTION_COMMENT = "Comment";

    public static final String ACTION_VIEW = "View";

    public static final String ACTION_OPEN = "Open";

    public static final String ACTION_OPEN_OFFLINE = "Open Offline";

    public static final String ACTION_DOWNLOAD = "Download";

    public static final String ACTION_EDIT = "Edit";

    public static final String ACTION_UPDATE = "Update";

    public static final String ACTION_UPDATE_MENU = "Update Menu";

    public static final String ACTION_DELETE = "Delete";

    public static final String ACTION_RUN = "Run Simple";

    public static final String ACTION_RUN_ADVANCED = "Run Advanced";

    public static final String ACTION_RUN_HISTORY = "Run Hisotry";

    public static final String ACTION_ANALYTICS = "Analytics";

    public static final String ACTION_SYNC_CELLULAR = "Sync Cellular";

    public static final String ACTION_DATA_PROTECTION = "Data Protection";

    public static final String ACTION_PASSCODE = "Passcode";

    public static final String ACTION_FAVORITE = "Favorite";

    public static final String ACTION_UNFAVORITE = "UnFavorite";

    public static final String ACTION_LIKE = "Like";

    public static final String ACTION_UNLIKE = "UnLike";

    public static final String ACTION_SYNC = "Sync";

    public static final String ACTION_UNSYNC = "UnSync";

    public static final String ACTION_SHARE = "Share";

    public static final String ACTION_SHARE_AS_LINK = "Share Link";

    public static final String ACTION_MEMBERSHIP = "Membership";

    public static final String ACTION_CALL = "Call";

    public static final String ACTION_SKYPE = "Skype";

    public static final String ACTION_EMAIL = "Email";

    public static final String ACTION_ADD_CONTACT = "Add Contact";

    public static final String ACTION_TAKE_PHOTO = "Take Photo";

    public static final String ACTION_RECORD_VIDEO = "Record Video";

    public static final String ACTION_RECORD_AUDIO = "Record Audio";

    public static final String ACTION_SCAN = "Scan";

    public static final String ACTION_MULTI_SELECT = "Multiselect";

    public static final String ACTION_REASSIGN = "Reassign";

    public static final String ACTION_START_REVIEW = "Send for Review";

    public static final String ACTION_COMPLETE_TASK = "Complete";

    public static final String ACTION_GRANT_PERMISSION = "Grant Permission";

    public static final String ACTION_DENY_PERMISSION = "Deny Permission";

    public static final String ACTION_CHANGE_AUTHENTICATION = "Change Authentication";

    ///////////////////////////////////////////////////////////
    // EVENTS : LABELS
    ///////////////////////////////////////////////////////////
    public static final String LABEL_CREATE_TEXT = "Text";

    public static final String LABEL_SPEECH_2_TEXT = "Speech 2 Text";

    public static final String LABEL_TAKE_PHOTO = "Take Photo";

    public static final String LABEL_FAILED = "Failed";

    public static final String LABEL_UNAUTHORIZED = "Unauthorized";

    public static final String LABEL_OFFLINE = "Offline";

    public static final String LABEL_CONTENT_ALREADY_EXIST = "Content Already Exist";

    public static final String LABEL_UNKNOWN_SERVER = "Unknown Server";

    public static final String LABEL_NETWORK = "Network";

    public static final String LABEL_PROFILE = "Profile";

    public static final String LABEL_PHONE = "Phone";

    public static final String LABEL_MOBILE = "Mobile";

    public static final String LABEL_COMPANY = "Company";

    public static final String LABEL_CHAT = "Chat";

    public static final String LABEL_CALL = "Call";

    public static final String LABEL_VIDEOCALL = "Video";

    public static final String LABEL_USER = "User";

    public static final String LABEL_CONTACT = "Contact";

    public static final String LABEL_ENABLE = "Enable";

    public static final String LABEL_DISABLE = "Disable";

    public static final String LABEL_ENABLE_BY_CONFIG = "Enable By Config";

    public static final String LABEL_DISABLE_BY_CONFIG = "Disable By Config";

    public static final String LABEL_JOIN = "Join";

    public static final String LABEL_LEAVE = "Leave";

    public static final String LABEL_CANCEL = "Cancel";

    public static final String LABEL_NAME = "Name";

    public static final String LABEL_CREDENTIALS = "Credentials";

    public static final String LABEL_DOCUMENTS = "Documents";

    public static final String LABEL_FOLDERS = "Folders";

    public static final String LABEL_SITES = "Sites";

    public static final String LABEL_PEOPLE = "People";

    public static final String LABEL_STORAGE = "Storage";

    public static final String LABEL_BASIC_AUTH = "Basic";

    public static final String LABEL_SAML_AUTH = "SAML";

    public static final String LABEL_ADD = "Add";

    // Default
    public static final String LABEL_SYNC_SYSTEM = "System";

    // After network reconnection if there's pending sync
    public static final String LABEL_SYNC_NETWORK = "Network";

    // After session creation (start of the app)
    public static final String LABEL_SYNC_SESSION_LOADED = "Session";

    // After 60 min if the session is still active
    public static final String LABEL_SYNC_CRON = "Cron";

    // After a node has been synced
    public static final String LABEL_SYNC_ACTION = "Sync Action";

    // After a server side config changed the scheduler
    public static final String LABEL_SYNC_SCHEDULER_CHANGED = "Sync Period";

    // After a sync node has been edited
    public static final String LABEL_SYNC_SAVE_BACK = "Save Back";

    // Pull to refresh on sync screen
    public static final String LABEL_SYNC_REFRESH = "Refresh";

    // From the SAF
    public static final String LABEL_SYNC_DOC_PROVIDER = "Doc Provider";

    ///////////////////////////////////////////////////////////
    // CUSTOM DIMENSIONS
    ///////////////////////////////////////////////////////////
    // Beware to have the same index as defined in GAnalytics
    public static final int INDEX_SERVER_TYPE = 1;

    public static final String SERVER_TYPE_ONPREMISE = "OnPremise";

    public static final String SERVER_TYPE_ONPREMISE_SAML = "OnPremise SAML";

    public static final String SERVER_TYPE_CLOUD = "Cloud";

    public static final int INDEX_SERVER_VERSION = 2;

    public static final int INDEX_SERVER_EDITION = 3;

    public static final int INDEX_SYNC_FILE_COUNT = 4;

    public static final String INDEX_SYNC_FILE_COUNT_0 = "0";

    public static final String INDEX_SYNC_FILE_COUNT_1 = "1";

    public static final String INDEX_SYNC_FILE_COUNT_5 = "2 - 5";

    public static final String INDEX_SYNC_FILE_COUNT_10 = "5 - 10";

    public static final String INDEX_SYNC_FILE_COUNT_20 = "11 - 20";

    public static final String INDEX_SYNC_FILE_COUNT_50 = "21 - 50";

    public static final String INDEX_SYNC_FILE_COUNT_100 = "51 - 100";

    public static final String INDEX_SYNC_FILE_COUNT_250 = "101 - 250";

    public static final String INDEX_SYNC_FILE_COUNT_500 = "251 - 500";

    public static final String INDEX_SYNC_FILE_COUNT_1000 = "501 - 1000";

    public static final String INDEX_SYNC_FILE_COUNT_1001 = "1000+";

    public static final int INDEX_ACCOUNT_COUNT = 5;

    public static final String INDEX_ACCOUNT_COUNT_1 = "1";

    public static final String INDEX_ACCOUNT_COUNT_2 = "2";

    public static final String INDEX_ACCOUNT_COUNT_3 = "3";

    public static final String INDEX_ACCOUNT_COUNT_4 = "4";

    public static final String INDEX_ACCOUNT_COUNT_5 = "5+";

    public static final int INDEX_PROFILE_COUNT = 4;

    public static final String INDEX_PROFILE_COUNT_0 = "0";

    public static final String INDEX_PROFILE_COUNT_1 = "1";

    public static final String INDEX_PROFILE_COUNT_2 = "2";

    public static final String INDEX_PROFILE_COUNT_3 = "3";

    public static final String INDEX_PROFILE_COUNT_4 = "4";

    public static final String INDEX_PROFILE_COUNT_5 = "5+";

    ///////////////////////////////////////////////////////////
    // CUSTOM METRICS
    ///////////////////////////////////////////////////////////
    // Beware to have the same index as defined in GAnalytics
    public static final int INDEX_ACCOUNT_NUMBER = 1;

    public static final int INDEX_DATA_PROTECTION = 2;

    public static final int INDEX_PASSCODE = 3;

    public static final int INDEX_LOCAL_FILES = 4;

    public static final int INDEX_SYNCED_FILES = 6;

    public static final int INDEX_SYNCED_FOLDERS = 5;

    public static final int INDEX_SYNCED_SIZE = 7;

    public static final int INDEX_SESSION_CREATION = 8;

    public static final int INDEX_SYNC_CREATION = 9;

    public static final int INDEX_FILE_SIZE = 10;

    public static final int INDEX_PROFILES = 11;

    public static final int INDEX_SYNC_CELLULAR = 12;

    public static final SparseArray<String> CUSTOM_METRIC_LABEL = new SparseArray<String>()
    {
        {
            put(INDEX_ACCOUNT_NUMBER, "Account Number");
            put(INDEX_DATA_PROTECTION, "Data Protection");
            put(INDEX_PASSCODE, "Passcode");
            put(INDEX_LOCAL_FILES, "Local Files");
            put(INDEX_SESSION_CREATION, "Session Creation");
            put(INDEX_SYNC_CREATION, "Account Number");
        }
    };

    public static final SparseArray<String> CUSTOM_DIMENSION_LABEL = new SparseArray<String>()
    {
        {
            put(INDEX_SERVER_TYPE, "Server Type");
            put(INDEX_SERVER_VERSION, "Server Version");
            put(INDEX_SERVER_EDITION, "Server Edition");
        }
    };

    ///////////////////////////////////////////////////////////
    // TYPE :
    ///////////////////////////////////////////////////////////
    public static final String SYNCED_FILES = "Synced Files";

    public static final String TYPE_FOLDER = "Folder";

    public static final String SYNCED_FOLDERS = "Synced Folders";

    ///////////////////////////////////////////////////////////
    // SCREEN NAME
    ///////////////////////////////////////////////////////////
    public static final String ROOT_APPLICATION = "Application";

    public static final String PREFIX_ACCOUNT = "Account - ";

    public static final String SCREEN_ACCOUNT_EDIT = PREFIX_ACCOUNT + "Edit";

    public static final String SCREEN_ACCOUNT_TYPE = PREFIX_ACCOUNT + "Create - Type Picker";

    public static final String SCREEN_ACCOUNT_SERVER = PREFIX_ACCOUNT + "Create - Server";

    public static final String SCREEN_ACCOUNT_USER = PREFIX_ACCOUNT + "Create - Credentials";

    public static final String SCREEN_ACCOUNT_NAME = PREFIX_ACCOUNT + "Create - Validation";

    public static final String SCREEN_ACCOUNT_SIGNIN = PREFIX_ACCOUNT + "Sign In";

    public static final String SCREEN_ACCOUNT_OAUTH = PREFIX_ACCOUNT + "OAuth";

    public static final String SCREEN_ACCOUNT_SAML = PREFIX_ACCOUNT + "Saml";

    public static final String SCREEN_ACCOUNT_NETWORK = PREFIX_ACCOUNT + "Networks";

    public static final String PREFIX_MENU = "Menu - ";

    public static final String SCREEN_ACTIVITIES = "Activities";

    public static final String SCREEN_REPOSITORY = "Repository";

    public static final String SCREEN_REPOSITORY_SHARED = "Shared Files";

    public static final String SCREEN_REPOSITORY_USERHOME = "My Files";

    public static final String PREFIX_DOCUMENT = "Document - ";

    public static final String SCREEN_NODE_GALLERY = PREFIX_DOCUMENT + "Gallery";

    public static final String SCREEN_NODE_LISTING = PREFIX_DOCUMENT + "Listing";

    public static final String PREFIX_NODE_DETAILS = PREFIX_DOCUMENT + "Details - ";

    public static final String SCREEN_NODE_SUMMARY = PREFIX_NODE_DETAILS + "Summary";

    public static final String SCREEN_NODE_PROPERTIES = PREFIX_NODE_DETAILS + "Properties";

    public static final String SCREEN_NODE_PREVIEW = PREFIX_NODE_DETAILS + "Preview";

    public static final String SCREEN_NODE_COMMENTS = PREFIX_NODE_DETAILS + "Comments";

    public static final String SCREEN_NODE_VERSIONS = PREFIX_NODE_DETAILS + "Versions";

    public static final String SCREEN_NODE_CREATE = PREFIX_DOCUMENT + "Create - ";

    public static final String SCREEN_NODE_CREATE_TYPE = SCREEN_NODE_CREATE + "Type Picker";

    public static final String SCREEN_NODE_CREATE_EDITOR = SCREEN_NODE_CREATE + "Editor Picker";

    public static final String SCREEN_NODE_CREATE_NAME = SCREEN_NODE_CREATE + "Name";

    public static final String SCREEN_NODE_CREATE_FORM = SCREEN_NODE_CREATE + "Form";

    public static final String SCREEN_NODE_CREATE_FOLDER_FORM = SCREEN_NODE_CREATE + "Folder Form";

    public static final String SCREEN_NODE_EDIT_PROPERTIES = PREFIX_DOCUMENT + "Edit Properties";

    public static final String PREFIX_SITES = "Sites - ";

    public static final String SCREEN_SITES_MY = PREFIX_SITES + "My";

    public static final String SCREEN_SITES_ALL = PREFIX_SITES + "All";

    public static final String SCREEN_SITES_FAVORITES = PREFIX_SITES + "Favorites";

    public static final String SCREEN_SITES_SEARCH = PREFIX_SITES + "Search";

    public static final String SCREEN_SITES_PENDING_REQUEST = PREFIX_SITES + "Pending Requests";

    public static final String SCREEN_SITES_MEMBERS = PREFIX_SITES + "Members";

    public static final String SCREEN_FAVORITES = "Favorites";

    public static final String SCREEN_SYNCED_CONTENT = "Synced Content";

    public static final String PREFIX_SEARCH = "Search - ";

    public static final String SCREEN_SEARCH_FILES = PREFIX_SEARCH + "Files";

    public static final String SCREEN_SEARCH_FOLDERS = PREFIX_SEARCH + "Folders";

    public static final String SCREEN_SEARCH_USERS = PREFIX_SEARCH + "Users";

    public static final String SCREEN_SEARCH_SITES = PREFIX_SEARCH + "Sites";

    public static final String PREFIX_SEARCH_RESULT = PREFIX_SEARCH + "Result - ";

    public static final String SCREEN_SEARCH_RESULT_FILES = PREFIX_SEARCH_RESULT + "Files";

    public static final String SCREEN_SEARCH_RESULT_FOLDERS = PREFIX_SEARCH_RESULT + "Folders";

    public static final String SCREEN_SEARCH_RESULT_USERS = PREFIX_SEARCH_RESULT + "People";

    public static final String SCREEN_SEARCH_ADVANCED = PREFIX_SEARCH + "Advanced - ";

    public static final String SCREEN_SEARCH_ADVANCED_FILES = SCREEN_SEARCH_ADVANCED + "Files";

    public static final String SCREEN_SEARCH_ADVANCED_FOLDERS = SCREEN_SEARCH_ADVANCED + "Folders";

    public static final String SCREEN_SEARCH_ADVANCED_USERS = SCREEN_SEARCH_ADVANCED + "People";

    public static final String PREFIX_TASKS = "Tasks - ";

    public static final String SCREEN_TASKS_LISTING = PREFIX_TASKS + "Listing";

    public static final String SCREEN_TASKS_LISTING_ASSIGNED = SCREEN_TASKS_LISTING + " - Tasks Assigned to Me";

    public static final String SCREEN_TASKS_LISTING_STARTED = SCREEN_TASKS_LISTING + " - Tasks I've started";

    public static final String SCREEN_TASKS_LISTING_COMPLETED = SCREEN_TASKS_LISTING + " - Completed Tasks";

    public static final String SCREEN_TASKS_LISTING_HIGH = SCREEN_TASKS_LISTING + " - High Priority Tasks";

    public static final String SCREEN_TASKS_LISTING_DUE = SCREEN_TASKS_LISTING + " - Tasks Due Today";

    public static final String SCREEN_TASKS_LISTING_OVERDUE = SCREEN_TASKS_LISTING + " - Overdue Tasks";

    public static final String SCREEN_TASKS_LISTING_ACTIVE = SCREEN_TASKS_LISTING + " - Active Tasks";

    public static final String SCREEN_TASKS_HISTORY = PREFIX_TASKS + "History";

    public static final String SCREEN_TASKS_FILTER = PREFIX_TASKS + "Filter";

    public static final String SCREEN_TASKS_FILTER_LISTING = PREFIX_TASKS + "Filter - Listing";

    public static final String SCREEN_TASK_DETAILS = PREFIX_TASKS + "Details";

    public static final String SCREEN_TASK_CREATE_TYPE = PREFIX_TASKS + "Create - Type";

    public static final String SCREEN_TASK_CREATE_FORM = PREFIX_TASKS + "Create - Form";

    public static final String SCREEN_LOCAL_FILES_BROWSER = "Local Files - Listing";

    public static final String SCREEN_LOCAL_FILES_MENU = "Local Files - Menu";

    public static final String SCREEN_SETTINGS = "Settings - ";

    public static final String SCREEN_SETTINGS_DETAILS = SCREEN_SETTINGS + "Details";

    public static final String SCREEN_SETTINGS_ACCOUNT = SCREEN_SETTINGS + "Account";

    public static final String SCREEN_SETTINGS_PASSCODE = SCREEN_SETTINGS + "Passcode";

    public static final String SCREEN_SETTINGS_CUSTOM_MENU = SCREEN_SETTINGS + "Custom Menu";

    public static final String SCREEN_SETTINGS_ABOUT = SCREEN_SETTINGS + "About";

    public static final String SCREEN_HELP = "Help";

    public static final String PREFIX_USER = "User - ";

    public static final String SCREEN_USERS = PREFIX_USER + "Listing";

    public static final String SCREEN_USER_DETAILS = PREFIX_USER + "Details";

    public static final String SCREEN_SAMSUNG_SNOTE_EDITOR = "Samsung - SNote Editor";

    public static final String SCREEN_TEXT_EDITOR = "Text Editor";

    public static final String SCREEN_TEXT_EDITOR_ENCODING = "Text Editor - Encoding";

    public static final String SCREEN_TEXT_EDITOR_TEXT_SIZE = "Text Editor - Text Size";

    public static final String PREFIX_ACCOUNTS = "Accounts - ";

    public static final String SCREEN_ACCOUNTS_LISTING = PREFIX_ACCOUNTS + "Listing";

    // ////////////////////////////////////////////////////
    // SETTINGS
    // ////////////////////////////////////////////////////
    protected static final String ANALYTICS_PREFIX = "Analytics-";

    public static final int STATUS_BLOCKED = -1;

    public static final int STATUS_DISABLE = 0;

    public static final int STATUS_ENABLE = 1;

    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static AnalyticsManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = Manager.getInstance(context, AnalyticsManager.class.getSimpleName());
            }

            return (AnalyticsManager) mInstance;
        }
    }

    protected AnalyticsManager(Context applicationContext)
    {
        super(applicationContext);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public abstract void cleanOptInfo(Context context, AlfrescoAccount account);

    public abstract void optOutByConfig(Context context, AlfrescoAccount account);

    public abstract void optInByConfig(Context context, AlfrescoAccount account);

    /**
     * OptIn check must be done prior to call this method.
     * 
     * @param activity
     */
    public abstract void optIn(Activity activity);

    /**
     * optOut check must be done prior to call this method.
     * 
     * @param activity
     */
    public abstract void optOut(Activity activity);

    public abstract boolean isEnable();

    public abstract boolean isEnable(AlfrescoAccount account);

    public abstract boolean isBlocked();

    public abstract boolean isBlocked(AlfrescoAccount account);

    // ///////////////////////////////////////////////////////////////////////////
    // ABSTRACT METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public abstract void startReport(Activity activity);

    public abstract void reportScreen(String name);

    public abstract void reportEvent(String category, String action, String label, int value);

    public abstract void reportEvent(String category, String action, String label, int value, int customMetricId,
            Long customMetricValue);

    public abstract void reportEvent(String category, String action, String label, int eventValue,
            SparseArray<String> dimensions, SparseArray<Long> metrics);

    public abstract void reportInfo(String label, SparseArray<String> dimensions, SparseArray<Long> metrics);

    public abstract void reportError(boolean isFatal, String description);

    // ///////////////////////////////////////////////////////////////////////////
    // Fragment Interface
    // ///////////////////////////////////////////////////////////////////////////
    public interface FragmentAnalyzed
    {
        String getScreenName();

        boolean reportAtCreationEnable();
    }

}
