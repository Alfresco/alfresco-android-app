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
package org.alfresco.mobile.android.application.fragments.menu;

/**
 * List of all Action Item menu available inside the application.
 * 
 * @author Jean Marie Pascal
 */
public interface MenuActionItem
{
    // ///////////////////////////////////////////
    // PROFILE
    // ///////////////////////////////////////////
    int MENU_PROFILE = 1;

    // ///////////////////////////////////////////
    // BROWSER
    // ///////////////////////////////////////////
    int MENU_SEARCH = 10;

    int MENU_SEARCH_FOLDER = 11;

    int MENU_SEARCH_OPTION = 11;

    int MENU_CREATE_DOCUMENT = 19;

    int MENU_CREATE_FOLDER = 20;

    int MENU_UPLOAD = 30;

    int MENU_DEVICE_CAPTURE = 31;

    int MENU_REFRESH = 40;

    int MENU_DETAILS = 50;
    
    int MENU_DISPLAY_ITEMS = 12;
    
    int MENU_DISPLAY_GALLERY = 13;

    // ///////////////////////////////////////////
    // FILE EXPLORER
    // ///////////////////////////////////////////
    int MENU_SHORTCUT = 5;

    int MENU_SEND = 60;

    int MENU_SAVE = 65;

    int MENU_SPEECH = 66;

    int MENU_ENCODING = 67;

    int MENU_FONT_SIZE = 68;

    int MENU_ENCRYPT = 70;

    int MENU_DECRYPT = 71;

    // ///////////////////////////////////////////
    // DETAILS
    // ///////////////////////////////////////////
    int MENU_SHARE = 100;

    int MENU_OPEN_IN = 110;

    int MENU_DOWNLOAD = 120;

    int MENU_DOWNLOAD_ALL = 121;

    int MENU_UPDATE = 130;

    int MENU_FAVORITE = 135;

    int MENU_FAVORITE_GROUP = 136;

    int MENU_FAVORITE_GROUP_FAVORITE = 137;

    int MENU_FAVORITE_GROUP_UNFAVORITE = 138;

    int MENU_COMMENT = 140;

    int MENU_LIKE = 150;

    int MENU_LIKE_GROUP = 151;

    int MENU_LIKE_GROUP_LIKE = 152;

    int MENU_LIKE_GROUP_UNLIKE = 153;

    int MENU_EDIT = 160;

    int MENU_VERSION_HISTORY = 170;

    int MENU_TAGS = 180;

    int MENU_DELETE = 190;

    int MENU_DELETE_FOLDER = 191;

    // ///////////////////////////////////////////
    // SELECTION
    // ///////////////////////////////////////////
    int MENU_SELECT_ALL = 198;

    int MENU_OPERATIONS = 199;

    // ///////////////////////////////////////////
    // Account
    // ///////////////////////////////////////////

    int MENU_ACCOUNT_ADD = 200;

    int MENU_ACCOUNT_EDIT = 210;

    int MENU_ACCOUNT_DELETE = 220;

    // ///////////////////////////////////////////
    // DEVICE CAPTURE SUB-MENU
    // ///////////////////////////////////////////
    int MENU_DEVICE_CAPTURE_CAMERA_PHOTO = 300;

    int MENU_DEVICE_CAPTURE_CAMERA_VIDEO = 310;

    int MENU_DEVICE_CAPTURE_MIC_AUDIO = 320;

    // ///////////////////////////////////////////
    // SITES
    // ///////////////////////////////////////////
    int MENU_SITE_JOIN = 401;

    int MENU_SITE_LEAVE = 402;

    int MENU_SITE_CANCEL = 403;

    int MENU_SITE_FAVORITE = 404;

    int MENU_SITE_UNFAVORITE = 405;

    int MENU_SITE_LIST_REQUEST = 406;

    int MENU_SITE_MEMBERS = 400;

    // ///////////////////////////////////////////
    // SYNC
    // ///////////////////////////////////////////
    int MENU_RESOLVE_CONFLICT = 500;
    
    // ///////////////////////////////////////////
    // WORKFLOW
    // ///////////////////////////////////////////
    int MENU_PROCESS_DETAILS = 600;

    int MENU_WORKFLOW_ADD = 603;
    
    int MENU_PROCESS_REVIEW_ATTACHMENTS = 601;
    
    int MENU_TASK_REASSIGN= 602;

    int MENU_TASK_CLAIM= 604;
    
    int MENU_TASK_UNCLAIM= 605;
    
    int MENU_PROCESS_HISTORY = 606;

    // ///////////////////////////////////////////
    // ACTIVITY DETAILS
    // ///////////////////////////////////////////
    int MENU_ACTIVITY_SITE = 550;

    int MENU_ACTIVITY_PROFILE = 551;

    int MENU_ACTIVITY_PROFILE_2ND = 552;

    // ///////////////////////////////////////////
    // USER PROFILE
    // ///////////////////////////////////////////
    int MENU_CHAT = 650;

    int MENU_CALL = 651;

    int MENU_VIDEOCALL = 652;

    int MENU_EMAIL = 653;

    int MENU_COMPANY_EMAIL = 654;

    int MENU_COMPANY_TEL = 655;

    int MENU_TEL = 656;

    int MENU_MOBILE = 657;
    
    // ///////////////////////////////////////////
    // SYNC
    // ///////////////////////////////////////////
    int MENU_SYNC_WARNING = 700;

    // ///////////////////////////////////////////
    // GENERAL
    // ///////////////////////////////////////////

    int ACCOUNT_ID = 1000;
    
    int ACCOUNT_RELOAD = 1001;

    int PARAMETER_ID = 2000;

    int PARAMETER_HIDE_SHOW_TAB = 3000;

    // ///////////////////////////////////////////
    // OVERFLOW GENERAL MENU
    // ///////////////////////////////////////////
    int MENU_SETTINGS_ID = 4000;
    int MENU_HELP_ID = 4002;
    int MENU_ABOUT_ID = 4003;

}
