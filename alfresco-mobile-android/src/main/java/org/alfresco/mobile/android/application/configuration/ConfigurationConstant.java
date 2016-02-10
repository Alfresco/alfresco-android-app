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
package org.alfresco.mobile.android.application.configuration;

import java.util.ArrayList;

public interface ConfigurationConstant
{

    // ///////////////////////////////////////////////////////////////////////////
    // APPLICATION
    // ///////////////////////////////////////////////////////////////////////////
    String PAGINATION = "pagination";

    String ON_ITEM_SELECTED = "onItemSelected";

    String VIEW = "view";

    // ///////////////////////////////////////////////////////////////////////////
    // DEFAULT TEMPLATE LISTING VIEWS
    // ///////////////////////////////////////////////////////////////////////////
    String PREFIX = "org.alfresco.client.view";

    String KEY_ACTIVITIES = PREFIX.concat(".activities");

    String KEY_REPOSITORY = PREFIX.concat(".repository");

    String KEY_REPOSITORY_SEARCH = PREFIX.concat(".repository-search");

    String KEY_DOC_DETAILS = PREFIX.concat(".document-details");

    String KEY_FOLDER_DETAILS = PREFIX.concat(".folder-details");

    String KEY_SITES = PREFIX.concat(".sites");

    String KEY_SITE_BROWSER = PREFIX.concat(".site-browser");

    String KEY_TASKS = PREFIX.concat(".tasks");

    String KEY_FAVORITES = PREFIX.concat(".favorites");

    String KEY_SYNC = PREFIX.concat(".sync");

    String KEY_SEARCH = PREFIX.concat(".search");

    String KEY_SEARCH_ADVANCED = PREFIX.concat(".search-advanced");

    String KEY_PEOPLE = PREFIX.concat(".people");

    String KEY_PERSON_PROFILE = PREFIX.concat(".person-profile");

    String KEY_LOCAL = PREFIX.concat(".local");

    String KEY_LOCAL_FILES = PREFIX.concat(".local-files");

    ArrayList<String> VIEW_TYPE_IDS = new ArrayList<String>(10)
    {
        {
            add(KEY_ACTIVITIES);
            add(KEY_REPOSITORY);
            add(KEY_REPOSITORY_SEARCH);
            add(KEY_DOC_DETAILS);
            add(KEY_FOLDER_DETAILS);
            add(KEY_SITES);
            add(KEY_SITE_BROWSER);
            add(KEY_TASKS);
            add(KEY_FAVORITES);
            add(KEY_SYNC);
            add(KEY_SEARCH);
            add(KEY_SEARCH_ADVANCED);
            add(KEY_PEOPLE);
            add(KEY_PERSON_PROFILE);
            add(KEY_LOCAL);
            add(KEY_LOCAL_FILES);
        }
    };
}
