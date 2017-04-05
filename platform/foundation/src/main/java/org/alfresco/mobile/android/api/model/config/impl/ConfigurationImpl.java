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
package org.alfresco.mobile.android.api.model.config.impl;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.config.ActionConfig;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigInfo;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.model.config.CreationConfig;
import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.alfresco.mobile.android.api.model.config.FormConfig;
import org.alfresco.mobile.android.api.model.config.ProfileConfig;
import org.alfresco.mobile.android.api.model.config.RepositoryConfig;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.utils.JsonUtils;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.text.TextUtils;
import android.util.Log;

/**
 * @author Jean Marie Pascal
 */
public class ConfigurationImpl
{
    private static final String TAG = ConfigurationImpl.class.getSimpleName();

    private ConfigInfo info;

    private CreationHelper creationHelper;

    private FeatureHelper featureHelper;

    private ViewHelper viewHelper;

    private FormHelper formHelper;

    private EvaluatorHelper evaluatorHelper;

    private ValidationHelper validationHelper;

    private StringHelper stringHelper;

    private ProfileHelper profileHelper;

    private ActionHelper actionHelper;

    private RepositoryConfig repositoryConfig;

    private WeakReference<AlfrescoSession> session;

    private String personId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public static ConfigurationImpl load(File sourceFile, String sourceAsString, File configFolder)
    {
        ConfigurationImpl config = null;
        File configFile;
        try
        {
            if (sourceFile != null && sourceFile.exists())
            {
                configFile = sourceFile;
            }
            else
            {
                configFile = new File(configFolder, ConfigConstants.CONFIG_FILENAME);
            }

            if (!configFile.exists()) { return null; }

            // Try to find localization
            String filename = ConfigConstants.CONFIG_LOCALIZATION_FILENAME;
            if (!Locale.ENGLISH.getLanguage().equals(Locale.getDefault().getLanguage()))
            {
                filename = String.format(ConfigConstants.CONFIG_LOCALIZATION_FILENAME_PATTERN,
                        Locale.getDefault().getLanguage());
            }
            File localizedFile = new File(configFolder, filename);
            StringHelper stringConfig = StringHelper.load(localizedFile);

            // Try to retrieve configuration data
            Map<String, Object> json;
            if (sourceAsString != null)
            {
                json = JsonUtils.parseObject(sourceAsString);
            }
            else
            {
                FileInputStream inputStream = new FileInputStream(configFile);
                json = JsonUtils.parseObject(inputStream, "UTF-8");
            }

            // Try to retrieve the configInfo if present
            ConfigInfo info = null;
            if (json.containsKey(ConfigTypeIds.INFO.value()))
            {
                info = ConfigInfoImpl.parseJson(JSONConverter.getMap(json.get(ConfigTypeIds.INFO.value())));
            }

            // Finally create the configuration
            config = parseJson(null, json, info, stringConfig);
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return config;
    }

    public ConfigurationImpl(AlfrescoSession session)
    {
        this.session = new WeakReference<AlfrescoSession>(session);
    }

    public static ConfigurationImpl parseJson(AlfrescoSession session, Map<String, Object> json, ConfigInfo info,
            StringHelper stringHelper)
    {
        ConfigurationImpl configuration = new ConfigurationImpl(session);
        configuration.stringHelper = stringHelper;
        configuration.info = info;

        // Check if it's a beta
        if (json.containsKey(ConfigConstants.CATEGORY_ROOTMENU) && !json.containsKey(ConfigTypeIds.VIEWS.value()))
        {
            // It's the beta version of configuration file
            configuration.info = info;
            configuration.viewHelper = new ViewHelper(configuration, stringHelper,
                    prepareBetaViews(configuration, JSONConverter.getMap(json.get(ConfigConstants.CATEGORY_ROOTMENU))));
            return configuration;
        }

        // We need to load each configuration category by dependencies
        // EVALUATORS
        if (json.containsKey(ConfigTypeIds.EVALUATORS.value()))
        {
            if (configuration.evaluatorHelper == null)
            {
                configuration.evaluatorHelper = new EvaluatorHelper(configuration, stringHelper);
            }
            configuration.evaluatorHelper
                    .addEvaluators(JSONConverter.getMap(json.get(ConfigTypeIds.EVALUATORS.value())));
        }

        // VALIDATION
        if (json.containsKey(ConfigTypeIds.VALIDATION_RULES.value()))
        {
            if (configuration.validationHelper == null)
            {
                configuration.validationHelper = new ValidationHelper(configuration, stringHelper);
            }
            configuration.validationHelper
                    .addValidation(JSONConverter.getMap(json.get(ConfigTypeIds.VALIDATION_RULES.value())));
        }

        // FIELDS
        if (json.containsKey(ConfigTypeIds.FIELDS.value()))
        {
            if (configuration.formHelper == null)
            {
                configuration.formHelper = new FormHelper(configuration, stringHelper);
            }
            configuration.formHelper.addFields(JSONConverter.getMap(json.get(ConfigTypeIds.FIELDS.value())));
        }

        // FIELDS GROUP
        if (json.containsKey(ConfigTypeIds.FIELD_GROUPS.value()))
        {
            if (configuration.formHelper == null)
            {
                configuration.formHelper = new FormHelper(configuration, stringHelper);
            }
            configuration.formHelper.addFieldGroups(JSONConverter.getMap(json.get(ConfigTypeIds.FIELD_GROUPS.value())));
        }

        // FORMS
        if (json.containsKey(ConfigTypeIds.FORMS.value()))
        {
            if (configuration.formHelper == null)
            {
                configuration.formHelper = new FormHelper(configuration, stringHelper);
            }
            configuration.formHelper.addForms(JSONConverter.getList(json.get(ConfigTypeIds.FORMS.value())));
        }

        // VIEWS
        if (json.containsKey(ConfigTypeIds.VIEWS.value()))
        {
            if (configuration.viewHelper == null)
            {
                configuration.viewHelper = new ViewHelper(configuration, stringHelper);
            }
            configuration.viewHelper.addViews(JSONConverter.getMap(json.get(ConfigTypeIds.VIEWS.value())));
        }

        // VIEWS GROUP
        if (json.containsKey(ConfigTypeIds.VIEW_GROUPS.value()))
        {
            if (configuration.viewHelper == null)
            {
                configuration.viewHelper = new ViewHelper(configuration, stringHelper);
            }
            configuration.viewHelper.addViewGroups(JSONConverter.getList(json.get(ConfigTypeIds.VIEW_GROUPS.value())));
        }

        // ACTIONS
        if (json.containsKey(ConfigTypeIds.ACTIONS.value()))
        {
            if (configuration.actionHelper == null)
            {
                configuration.actionHelper = new ActionHelper(configuration, stringHelper);
            }
            configuration.actionHelper.addActions(JSONConverter.getMap(json.get(ConfigTypeIds.ACTIONS.value())));
        }

        // ACTIONS GROUP
        if (json.containsKey(ConfigTypeIds.ACTION_GROUPS.value()))
        {
            if (configuration.actionHelper == null)
            {
                configuration.actionHelper = new ActionHelper(configuration, stringHelper);
            }
            configuration.actionHelper
                    .addActionGroups(JSONConverter.getList(json.get(ConfigTypeIds.ACTION_GROUPS.value())));
        }

        // CREATION
        if (json.containsKey(ConfigTypeIds.CREATION.value()))
        {
            configuration.creationHelper = new CreationHelper(configuration, stringHelper);
            if (!configuration.creationHelper
                    .addCreationConfig(JSONConverter.getMap(json.get(ConfigTypeIds.CREATION.value()))))
            {
                configuration.creationHelper = null;
            }
        }

        // FEATURE
        if (json.containsKey(ConfigTypeIds.FEATURES.value()))
        {
            configuration.featureHelper = new FeatureHelper(configuration, stringHelper);
            if (configuration.featureHelper
                    .addFeatureConfig(JSONConverter.getList(json.get(ConfigTypeIds.FEATURES.value()))))
            {

            }
            else
            {
                configuration.featureHelper = null;

            }
        }

        // CONFIGURATION
        if (json.containsKey(ConfigTypeIds.REPOSITORY.value()))
        {
            configuration.repositoryConfig = RepositoryConfigImpl
                    .parseJson(JSONConverter.getMap(json.get(ConfigTypeIds.REPOSITORY.value())));
        }

        // PROFILES
        if (json.containsKey(ConfigTypeIds.PROFILES.value()))
        {
            configuration.profileHelper = new ProfileHelper(configuration, stringHelper);
            configuration.profileHelper.addProfiles(JSONConverter.getMap(json.get(ConfigTypeIds.PROFILES.value())));
        }

        return configuration;
    }

    private static LinkedHashMap<String, ViewConfig> prepareBetaViews(ConfigurationImpl context,
            Map<String, Object> json)
    {
        LinkedHashMap<String, ViewConfig> viewConfigIndex = new LinkedHashMap<String, ViewConfig>(json.size());
        ViewConfig viewConfig;
        for (Entry<String, Object> objectEntry : json.entrySet())
        {
            viewConfig = ViewHelper.parseBeta(objectEntry.getKey(), JSONConverter.getMap(objectEntry.getValue()));
            if (viewConfig == null)
            {
                continue;
            }
            viewConfigIndex.put(viewConfig.getIdentifier(), viewConfig);
        }
        return viewConfigIndex;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public ConfigInfo getConfigInfo()
    {
        return info;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PROFILES
    // ///////////////////////////////////////////////////////////////////////////
    public List<ProfileConfig> getProfiles()
    {
        if (profileHelper == null) { return new ArrayList<ProfileConfig>(0); }
        return profileHelper.getProfiles();
    }

    public ProfileConfig getDefaultProfile()
    {
        if (profileHelper == null) { return null; }
        return profileHelper.getDefaultProfile();
    }

    public ProfileConfig getProfile(String profileId)
    {
        if (profileHelper == null) { return null; }
        return (TextUtils.isEmpty(profileId)) ? null : profileHelper.getProfileById(profileId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REPOSITORY
    // ///////////////////////////////////////////////////////////////////////////
    public RepositoryConfig getRepositoryConfig()
    {
        if (repositoryConfig == null) { return null; }
        return repositoryConfig;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEWS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasViewConfig()
    {
        return viewHelper != null && viewHelper.hasViewConfig();
    }

    public ViewConfig getViewConfig(String viewId, ConfigScope scope)
    {
        if (viewHelper == null) { return null; }
        return viewHelper.getViewById(viewId, scope);
    }

    public ViewConfig getViewConfig(String viewId)
    {
        if (viewHelper == null) { return null; }
        return viewHelper.getViewById(viewId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasActionConfig()
    {
        return actionHelper != null && actionHelper.hasActionConfig();
    }

    public ActionConfig getActionConfig(String viewId, ConfigScope scope)
    {
        if (actionHelper == null) { return null; }
        return actionHelper.getActionByType(viewId, scope);
    }

    public ActionConfig getActionConfig(String viewId)
    {
        if (actionHelper == null) { return null; }
        return actionHelper.getActionByType(viewId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FORMS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasFormConfig()
    {
        return formHelper != null && formHelper.hasFormConfig();
    }

    public FormConfig getFormConfig(String formId, ConfigScope scope)
    {
        if (formHelper == null) { return null; }
        Node node = null;
        if (scope != null && scope.getContextValue(ConfigScope.NODE) != null)
        {
            node = (Node) scope.getContextValue(ConfigScope.NODE);
        }
        return formHelper.getFormById(formId, node);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATION
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasCreationConfig()
    {
        return creationHelper != null && creationHelper.hasCreationConfig();
    }

    public CreationConfig getCreationConfig()
    {
        if (creationHelper == null) { return null; }
        return creationHelper.getCreationConfig();
    }

    public CreationConfig getCreationConfig(ConfigScope scope)
    {
        if (creationHelper == null) { return null; }
        return creationHelper.getCreationConfig(scope);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FEATURE
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasFeatureConfig()
    {
        return featureHelper != null && featureHelper.getFeatures() != null;
    }

    public List<FeatureConfig> getFeatureConfig()
    {
        if (featureHelper == null) { return new ArrayList<>(0); }
        return featureHelper.getFeatures();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    ViewHelper getViewHelper()
    {
        if (viewHelper == null) { return null; }
        return viewHelper;
    }

    ProfileHelper getProfileHelper()
    {
        return profileHelper;
    }

    EvaluatorHelper getEvaluatorHelper()
    {
        if (evaluatorHelper == null) { return null; }
        return evaluatorHelper;
    }

    ValidationHelper getValidationHelper()
    {
        if (validationHelper == null) { return null; }
        return validationHelper;
    }

    public AlfrescoSession getSession()
    {
        return session.get();
    }

    public String getPersonId()
    {
        if (personId == null && getSession() != null)
        {
            personId = getSession().getPersonIdentifier();
        }
        return personId;
    }

    public void setPersonId(String personId)
    {
        this.personId = personId;
    }

    public void setSession(AlfrescoSession session)
    {
        if (this.session != null)
        {
            this.session.clear();
        }
        this.session = new WeakReference<AlfrescoSession>(session);
    }

    public String getString(String id)
    {
        if (id == null) { return id; }
        if (stringHelper == null) { return id; }
        return stringHelper.getString(id);
    }

}
