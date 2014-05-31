/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.application.configuration.manager;

public interface ConfigurationConstant
{

    // ///////////////////////////////////////////////////////////////////////////
    // APPLICATION
    // ///////////////////////////////////////////////////////////////////////////
    String APPLICATION = "application";

    String DEFAULT = "default";

    String ROOTMENU = "rootMenu";

    String MERGE = "alf:merge";

    String LABEL_ID = "label-id";

    String HEADER = "header";

    String TYPE = "type";

    String EVALUATORS = "evaluators";

    String PAGINATION = "pagination";

    String FILTER = "filter";

    String PARAMS = "params";

    String NEGATE = "negate";

    String DETAILS = "details";

    String CREATION = "creation";

    String ASPECT = "aspect";

    String VISIBILITY = "visibility";

    String ON_ITEM_SELECTED = "onItemSelected";

    String VIEW = "view";

    String[] PATH_DEFAULT_ROOT_MENU = new String[] { APPLICATION, DEFAULT, ROOTMENU };

    String[] PATH_DEFAULT_DETAILS = new String[] { APPLICATION, DEFAULT, DETAILS };

    String[] PATH_DEFAULT_CREATION = new String[] { APPLICATION, DEFAULT, CREATION };

    // ///////////////////////////////////////////////////////////////////////////
    // TEMPLATE LISTING VIEWS
    // ///////////////////////////////////////////////////////////////////////////
    String KEY_ACTIVITIES = "com.alfresco.type.activities";

    String KEY_REPOSITORY = "com.alfresco.type.repository";

    String KEY_SITES = "com.alfresco.type.sites";

    String KEY_TASKS = "com.alfresco.type.tasks";

    String KEY_FAVORITES = "com.alfresco.type.favorites";

    String KEY_SEARCH = "com.alfresco.type.search";

    String KEY_PERSONS = "com.alfresco.type.persons";

    String KEY_PERSON_PROFILE = "com.alfresco.type.person.profile";

    String KEY_LOCALFILES = "com.alfresco.type.local";

    // ///////////////////////////////////////////////////////////////////////////
    // EVALUATORS
    // ///////////////////////////////////////////////////////////////////////////
    String EVALUATOR_REPOSITORY_INFO = "com.alfresco.evaluator.repositoryInfo";

    String EVALUATOR_NODE_TYPE = "com.alfresco.evaluator.nodeType";

    String EVALUATOR_HAS_ASPECT = "com.alfresco.evaluator.hasAspect";

    String EVALUATOR_ARGUMENT_NODE = "node";

    // ///////////////////////////////////////////////////////////////////////////
    // REPOSITORY INFO EVALUATOR
    // ///////////////////////////////////////////////////////////////////////////
    String REPOSITORY_INFO_TYPE_ONPREMISE = "OnPremise";

    String REPOSITORY_INFO_TYPE_CLOUD = "Cloud";

}
