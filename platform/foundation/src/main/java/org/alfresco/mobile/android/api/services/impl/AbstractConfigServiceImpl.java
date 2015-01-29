/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.api.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.SearchLanguage;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.model.config.ConfigInfo;
import org.alfresco.mobile.android.api.model.config.ConfigScope;
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.model.config.CreationConfig;
import org.alfresco.mobile.android.api.model.config.FormConfig;
import org.alfresco.mobile.android.api.model.config.ProfileConfig;
import org.alfresco.mobile.android.api.model.config.RepositoryConfig;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.api.model.config.impl.ConfigInfoImpl;
import org.alfresco.mobile.android.api.model.config.impl.ConfigurationImpl;
import org.alfresco.mobile.android.api.model.config.impl.StringHelper;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.services.SearchService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.utils.JsonUtils;
import org.alfresco.mobile.android.platform.io.IOUtils;

import android.util.Log;

/**
 * @author Jean Marie Pascal
 */
public abstract class AbstractConfigServiceImpl extends AlfrescoService implements ConfigService
{
    private static final String TAG = AbstractConfigServiceImpl.class.getSimpleName();

    private ConfigurationImpl configuration;

    protected boolean hasConfig = true;

    /**
     * Default Constructor. Only used inside ServiceRegistry.
     * 
     * @param repositorySession : Repository Session.
     */
    public AbstractConfigServiceImpl(AlfrescoSession repositorySession)
    {
        super(repositorySession);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public ConfigService load()
    {
        this.configuration = retrieveConfiguration();
        return (configuration != null) ? this : null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    protected ConfigurationImpl retrieveConfiguration()
    {
        Node configurationDocument = null;
        Folder applicationConfigurationFolder = null;
        long lastModificationTime = -1;
        ConfigurationImpl configuration = null;
        StringHelper localHelper = null;
        try
        {
            // Retrieve the application configuration Folder
            DocumentFolderService docService = session.getServiceRegistry().getDocumentFolderService();
            Folder dataDictionaryFolder = getDataDictionaryFolder();
            if (dataDictionaryFolder == null) { throw new AlfrescoServiceException(
                    "Unable to retrieve Data Dictionary Folder"); }
            applicationConfigurationFolder = getApplicationConfigFolder(dataDictionaryFolder);

            // Retrieve configuration data
            if (applicationConfigurationFolder != null)
            {
                configurationDocument = getApplicationConfigFile(applicationConfigurationFolder);

                if (configurationDocument != null)
                {
                    // Retrieve localization Data
                    localHelper = createLocalizationHelper(docService, applicationConfigurationFolder);
                }
            }

            // If no configuration file there's no configuration object
            if (configurationDocument == null || !configurationDocument.isDocument())
            {
                // We need to remove previous cached configuration
                if (session.getParameter(CONFIGURATION_FOLDER) != null)
                {
                    File configFolder = new File((String) session.getParameter(CONFIGURATION_FOLDER));
                    File configFile = new File(configFolder, ConfigConstants.CONFIG_FILENAME);
                    File configMessages = new File(configFolder, ConfigConstants.CONFIG_LOCALIZATION_FOLDER_PATH);
                    if (configFile.exists())
                    {
                        configFile.delete();
                    }
                    if (configMessages.exists())
                    {
                        IOUtils.deleteContents(configMessages);
                        configMessages.delete();
                    }
                }
                return null;
            }

            // Prepare & Create Configuration Object
            hasConfig = true;

            // Retrieve Configuration Data
            lastModificationTime = configurationDocument.getModifiedAt().getTimeInMillis();
            ContentStream stream = docService.getContentStream((Document) configurationDocument);

            // Persist if defined by the session parameters
            InputStream inputStream = stream.getInputStream();
            if (session.getParameter(CONFIGURATION_FOLDER) != null)
            {
                File configFolder = new File((String) session.getParameter(CONFIGURATION_FOLDER));
                File configFile = new File(configFolder, configurationDocument.getName());
                org.alfresco.mobile.android.api.utils.IOUtils.copyFile(stream.getInputStream(), configFile);
                inputStream = new FileInputStream(configFile);
            }

            Map<String, Object> json = JsonUtils.parseObject(inputStream, "UTF-8");

            // Try to retrieve the configInfo if present
            ConfigInfo info = null;
            if (json.containsKey(ConfigTypeIds.INFO.value()))
            {
                info = ConfigInfoImpl.parseJson((Map<String, Object>) json.get(ConfigTypeIds.INFO.value()));
            }
            else if (json.containsKey(ConfigConstants.CATEGORY_ROOTMENU))
            {
                // If it's a format from v1.3
                info = ConfigInfoImpl.from(null, null, lastModificationTime);
            }

            // Finally create the Configuration Object
            configuration = ConfigurationImpl.parseJson(session, json, info, localHelper);
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return configuration;
    }

    protected StringHelper createLocalizationHelper(DocumentFolderService docService, Folder applicationFolder)
    {
        StringHelper config = null;
        InputStream inputStream = null;
        String filename = null;
        try
        {
            // Filename
            Node messagesDocument = null;
            if (!Locale.ENGLISH.getLanguage().equals(Locale.getDefault().getLanguage()))
            {
                messagesDocument = docService.getChildByPath(applicationFolder,
                        StringHelper.getRepositoryLocalizedFilePath());
                filename = StringHelper.getLocalizedFileName();
            }

            if (messagesDocument == null)
            {
                messagesDocument = docService.getChildByPath(applicationFolder,
                        StringHelper.getDefaultRepositoryLocalizedFilePath());
                filename = StringHelper.getDefaultLocalizedFileName();
            }

            if (messagesDocument != null && messagesDocument.isDocument())
            {
                ContentStream stream = docService.getContentStream((Document) messagesDocument);
                inputStream = stream.getInputStream();

                // Persist if defined by the session
                if (session.getParameter(CONFIGURATION_FOLDER) != null)
                {
                    File configFolder = new File((String) session.getParameter(CONFIGURATION_FOLDER));
                    File configFile = new File(configFolder, filename);
                    org.alfresco.mobile.android.api.utils.IOUtils.copyFile(stream.getInputStream(), configFile);
                    inputStream = new FileInputStream(configFile);
                }
                config = StringHelper.load(inputStream);
            }
        }
        catch (IOException e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        finally
        {
            org.alfresco.mobile.android.api.utils.IOUtils.closeStream(inputStream);
        }

        return config;
    }

    protected Folder getDataDictionaryFolder()
    {
        // We search the datadictionary and next the configuration file.
        Folder dataDictionaryFolder = null;
        SearchService searchService = session.getServiceRegistry().getSearchService();
        DocumentFolderService docService = session.getServiceRegistry().getDocumentFolderService();

        try
        {
            List<Node> nodes = searchService.search(
                    "SELECT * FROM cmis:folder WHERE CONTAINS ('QNAME:\"app:company_home/app:dictionary\"')",
                    SearchLanguage.CMIS);
            if (nodes != null && nodes.size() == 1)
            {
                dataDictionaryFolder = (Folder) nodes.get(0);
            }
        }
        catch (Exception e)
        {
            // If search doesn't work, we search in brute force
            for (String dictionaryName : ConfigConstants.DATA_DICTIONNARY_LIST)
            {
                dataDictionaryFolder = (Folder) docService.getChildByPath(dictionaryName);
                if (dataDictionaryFolder != null)
                {
                    break;
                }
            }
        }
        return dataDictionaryFolder;
    }

    protected Folder getApplicationConfigFolder(Folder dataDictionaryFolder)
    {
        DocumentFolderService docService = session.getServiceRegistry().getDocumentFolderService();
        return (Folder) docService.getChildByPath(dataDictionaryFolder, ConfigConstants.CONFIG_APPLICATION_FOLDER_PATH);
    }

    protected Document getApplicationConfigFile(Folder applicationConfigFolder)
    {
        DocumentFolderService docService = session.getServiceRegistry().getDocumentFolderService();
        return (Document) docService.getChildByPath(applicationConfigFolder, ConfigConstants.CONFIG_FILENAME);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INFO
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public ConfigInfo getConfigInfo()
    {
        if (configuration == null) { return null; }
        return configuration.getConfigInfo();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PROFILES
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public List<ProfileConfig> getProfiles()
    {
        if (configuration == null) { return new ArrayList<>(0); }
        return configuration.getProfiles();
    }

    @Override
    public ProfileConfig getDefaultProfile()
    {
        if (configuration == null) { return null; }
        return configuration.getDefaultProfile();
    }

    @Override
    public ProfileConfig getProfile(String profileId)
    {
        if (configuration == null) { return null; }
        return configuration.getProfile(profileId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REPOSITORY
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public RepositoryConfig getRepositoryConfig()
    {
        return (configuration == null) ? null : configuration.getRepositoryConfig();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEWS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public ViewConfig getViewConfig(String viewId, ConfigScope scope)
    {
        if (configuration == null) { return null; }
        return configuration.getViewConfig(viewId, scope);
    }

    @Override
    public ViewConfig getViewConfig(String viewId)
    {
        if (configuration == null) { return null; }
        return configuration.getViewConfig(viewId);
    }

    @Override
    public boolean hasViewConfig()
    {
        if (configuration == null) { return false; }
        return configuration.hasViewConfig();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FORMS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean hasFormConfig()
    {
        if (configuration == null) { return false; }
        return configuration.hasFormConfig();
    }

    @Override
    public FormConfig getFormConfig(String formId)
    {
        if (configuration == null) { return null; }
        return configuration.getFormConfig(formId, null);
    }

    @Override
    public FormConfig getFormConfig(String formId, ConfigScope scope)
    {
        if (configuration == null) { return null; }
        return configuration.getFormConfig(formId, scope);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATION
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean hasCreationConfig()
    {
        if (configuration == null) { return false; }
        return configuration.getCreationConfig() != null;
    }

    @Override
    public CreationConfig getCreationConfig(ConfigScope scope)
    {
        if (configuration == null) { return null; }
        return configuration.getCreationConfig(scope);
    }

    @Override
    public CreationConfig getCreationConfig()
    {
        if (configuration == null) { return null; }
        return configuration.getCreationConfig();
    }
}
