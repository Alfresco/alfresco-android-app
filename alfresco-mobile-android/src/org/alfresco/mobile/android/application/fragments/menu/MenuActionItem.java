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

    // ///////////////////////////////////////////
    // SYNC
    // ///////////////////////////////////////////
    int MENU_RESOLVE_CONFLICT = 500;

    // ///////////////////////////////////////////
    // GENERAL
    // ///////////////////////////////////////////

    int ACCOUNT_ID = 1000;

    int PARAMETER_ID = 2000;

    int PARAMETER_HIDE_SHOW_TAB = 3000;

    int ABOUT_ID = 4000;
}
