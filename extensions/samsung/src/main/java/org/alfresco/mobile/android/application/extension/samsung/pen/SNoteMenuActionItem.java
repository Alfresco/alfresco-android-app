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
package org.alfresco.mobile.android.application.extension.samsung.pen;

/**
 * List of all Action Item menu available inside the application.
 * 
 * @author Jean Marie Pascal
 */
public interface SNoteMenuActionItem
{
    // ///////////////////////////////////////////
    // EDITOR
    // ///////////////////////////////////////////
    int MENU_EDITOR_TOOLS = 10;

    int MENU_EDITOR_PEN = 11;

    int MENU_EDITOR_TEXT = 12;

    int MENU_EDITOR_SELECTION = 13;

    int MENU_EDITOR_ERASER = 14;

    // ///////////////////////////////////////////
    // SETTINGS
    // ///////////////////////////////////////////
    int MENU_EDITOR_SETTINGS = 20;

    int MENU_EDITOR_SETTINGS_PEN = 21;

    int MENU_EDITOR_SETTINGS_TEXT = 22;

    int MENU_EDITOR_SETTINGS_SELECTION = 23;

    int MENU_EDITOR_SETTINGS_ERASER = 24;

    // ///////////////////////////////////////////
    // ADD
    // ///////////////////////////////////////////
    int MENU_EDITOR_ADD = 30;

    int MENU_EDITOR_ADD_IMAGE = 31;

    // ///////////////////////////////////////////
    // PAGES
    // ///////////////////////////////////////////
    int MENU_EDITOR_PAGE = 40;

    int MENU_EDITOR_PAGE_MOVE = 41;

    int MENU_EDITOR_PAGE_ADD = 42;

    int MENU_EDITOR_PAGE_DELETE = 43;

    // ///////////////////////////////////////////
    // EDITOR CONTEXT MENU
    // ///////////////////////////////////////////
    int CONTEXT_MENU_GROUP_ID = 20;

    int CONTEXT_MENU_UNGROUP_ID = 21;

    int CONTEXT_MENU_MOVE_TO_BOTTOM_ID = 30;

    int CONTEXT_MENU_MOVE_TO_BACKWARD_ID = 31;

    int CONTEXT_MENU_MOVE_TO_FORWARD_ID = 33;

    int CONTEXT_MENU_MOVE_TO_TOP_ID = 32;

    int CONTEXT_MENU_DELETE_ID = 40;

    // ///////////////////////////////////////////
    // EDITOR EXTRA MENU
    // ///////////////////////////////////////////
    int MENU_EDITOR_SAVE = 100;

}
