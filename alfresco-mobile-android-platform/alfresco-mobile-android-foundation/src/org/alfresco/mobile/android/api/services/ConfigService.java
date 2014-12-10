/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.api.services;

import java.util.List;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.config.ActionConfig;
import org.alfresco.mobile.android.api.model.config.ConfigInfo;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.CreationConfig;
import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.alfresco.mobile.android.api.model.config.FormConfig;
import org.alfresco.mobile.android.api.model.config.MenuConfig;
import org.alfresco.mobile.android.api.model.config.ProcessConfig;
import org.alfresco.mobile.android.api.model.config.ProfileConfig;
import org.alfresco.mobile.android.api.model.config.RepositoryConfig;
import org.alfresco.mobile.android.api.model.config.SearchConfig;
import org.alfresco.mobile.android.api.model.config.TaskConfig;
import org.alfresco.mobile.android.api.model.config.ThemeConfig;
import org.alfresco.mobile.android.api.model.config.ViewConfig;

/**
 * @author Jean Marie Pascal
 */
public interface ConfigService extends Service
{
    // ///////////////////////////////////////////////
    // CONFIGURATION CONSTANTS
    // ///////////////////////////////////////////////
    /**
     * Define the applicationId to preload inside the configuration object.<br/>
     * Value must be String value that represents a valid application Identifier<br/>
     * Default : empty string.
     */
    String CONFIGURATION_APPLICATION_ID = "org.alfresco.mobile.api.configuration.application.id";

    /**
     * Define the profileId to preload inside the configuration object.<br/>
     * Can’t be used without CONFIGURATION_APPLICATION_ID <br/>
     * Value must be String value that represents a valid profile Identifier
     * Default : empty string.
     */
    String CONFIGURATION_PROFILE_ID = "org.alfresco.mobile.api.configuration.profile.id";

    /**
     * Define the path to the configuration folder. The configuration folder is
     * used to store configuration file and localization file.<br/>
     * Value must be String value that represents a valid path inside the
     * device.<br/>
     * Default :
     * "/sdcard/Android/data/org.alfresco.mobile.android.sdk/configuration"
     */
    String CONFIGURATION_FOLDER = "org.alfresco.mobile.api.configuration.folder";

    /**
     * During the session creation configuration information is loaded if
     * available. <br/>
     * Value must be a String. Default : null
     */
    String CONFIGURATION_INIT = "org.alfresco.mobile.api.configuration.init";
    
    String CONFIGURATION_INIT_IF_UPDATED  = "org.alfresco.mobile.api.configuration.init.newer";
    String CONFIGURATION_INIT_DEFAULT = "org.alfresco.mobile.api.configuration.init.default";
    String CONFIGURATION_INIT_NONE = "org.alfresco.mobile.api.configuration.init.none";

    String CONFIGURATION_VERSION_ID = "org.alfresco.mobile.api.configuration.version.id";
     
    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // INFO
    /**
     * Returns configuration information like schema, version
     */
    ConfigInfo getConfigInfo();

    // ///////////////////////////////////////////////////////////////////////////
    // PROFILES
    /**
     * Returns a list of profiles available on the server the client application
     * can select from.
     */
    List<ProfileConfig> getProfiles();

    /**
     * Returns the default profile.
     */
    ProfileConfig getDefaultProfile();

    /**
     * Returns the Profile for the given identifier
     */
    ProfileConfig getProfile(String identifier);

    // ///////////////////////////////////////////////////////////////////////////
    // REPOSITORY
    /**
     * Returns configuration information about the repository, for example, the
     * Share host and port.
     */
    RepositoryConfig getRepositoryConfig();

    // ///////////////////////////////////////////////////////////////////////////
    // FEATURE
    /** Returns the feature configuration for the current application. */
    List<FeatureConfig> getFeatureConfig();

    /** Returns the feature configuration for the current application. */
    List<FeatureConfig> getFeatureConfig(ConfigScope scope);

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    /** Returns the configuration for the menu with the given identifier. */
    List<MenuConfig> getMenuConfig(String menuId);

    // ///////////////////////////////////////////////////////////////////////////
    // VIEWS
    /**
     * Returns the configuration for the view with the given identifier and
     * optionally for the given node.
     */
    boolean hasViewConfig();

    ViewConfig getViewConfig(String viewId);

    ViewConfig getViewConfig(String viewId, ConfigScope scope);

    // ///////////////////////////////////////////////////////////////////////////
    // FORMS
    /** Returns the configuration for the form with the given identifier.*/
    FormConfig getFormConfig(String formId);
    
    /**
     * Returns the configuration for the form with the given identifier and
     * optionally for the given node.
     */
    FormConfig getFormConfig(String formId, ConfigScope scope);

    // ///////////////////////////////////////////////////////////////////////////
    // WORKFLOW
    /** Returns the configuration for workflow processes. */
    List<ProcessConfig> getProcessConfig();

    /** Returns the configuration for workflow tasks. */
    List<TaskConfig> getTaskConfig();

    // ///////////////////////////////////////////////////////////////////////////
    // CREATION
    /** Returns the configuration for creation related features. */
    CreationConfig getCreationConfig();

    /** Returns the configuration for creation related features. */
    CreationConfig getCreationConfig(ConfigScope scope);

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION
    /**
     * Returns the list of ActionConfig object representing the actions
     * available in the given group id and optionally for the given node.
     */
    List<ActionConfig> getActionConfig(String groupId, Node node);

    // ///////////////////////////////////////////////////////////////////////////
    // SEARCH
    /** Returns the configuration for search related features. */
    SearchConfig getSearchConfig(Node node);

    // ///////////////////////////////////////////////////////////////////////////
    // THEME
    /** Returns branding configuration. */
    ThemeConfig getThemeConfig();

}
