/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.configuration;

import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.SearchLanguage;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.services.SearchService;
import org.alfresco.mobile.android.api.utils.JsonUtils;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.configuration.ConfigurationContext;
import org.alfresco.mobile.android.application.configuration.ConfigurationManager;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ConfigurationOperationThread extends AbstractBatchOperationThread<ConfigurationContext>
{
    private static final String TAG = ConfigurationOperationThread.class.getName();

    private ConfigurationContext configContext;

    private Node configurationDocument;

    private Folder dataDictionaryFolder;

    private String dataDictionaryIdentifier;

    private String configurationIdentifier;

    private long lastModificationTime = -1;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public ConfigurationOperationThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof ConfigurationOperationRequest)
        {
            this.dataDictionaryIdentifier = ((ConfigurationOperationRequest) request).getDataDictionaryIdentifier();
            this.configurationIdentifier = ((ConfigurationOperationRequest) request).getConfigurationIdentifier();
            this.lastModificationTime = ((ConfigurationOperationRequest) request).getLastModificationTime();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<ConfigurationContext> doInBackground()
    {
        LoaderResult<ConfigurationContext> result = new LoaderResult<ConfigurationContext>();

        try
        {
            super.doInBackground();

            if (listener != null)
            {
                listener.onPreExecute(this);
            }

            SearchService searchService = session.getServiceRegistry().getSearchService();
            DocumentFolderService docService = session.getServiceRegistry().getDocumentFolderService();
            
            try
            {
                // Retrieve By Identifier
                if (configurationIdentifier != null)
                {
                    configurationDocument = docService.getNodeByIdentifier(NodeRefUtils.getCleanIdentifier(configurationIdentifier));
                }
                // Retrieve Data Dictionary
                if (configurationDocument == null && dataDictionaryIdentifier != null)
                {
                    dataDictionaryFolder = (Folder) docService.getNodeByIdentifier(NodeRefUtils.getCleanIdentifier(dataDictionaryIdentifier));
                }
            }
            catch (Exception e)
            {
                // Nothing special
            }

            // If no shortcut available we search the datadictionary and next
            // the configuration file.
            if (configurationDocument == null && dataDictionaryFolder == null)
            {
                try
                {
                    List<Node> nodes = searchService.search(
                            "SELECT * FROM cmis:folder WHERE CONTAINS ('QNAME:\"app:company_home/app:dictionary\"')",
                            SearchLanguage.CMIS);
                    if (nodes != null && nodes.size() == 1)
                    {
                        dataDictionaryFolder = (Folder) nodes.get(0);
                        dataDictionaryIdentifier = dataDictionaryFolder.getIdentifier();
                    }
                }
                catch (Exception e)
                {
                    // If search doesnt work, we search in brute force
                    for (String dictionaryName : ConfigurationManager.DATA_DICTIONNARY_LIST)
                    {
                        dataDictionaryFolder = (Folder) docService.getChildByPath(dictionaryName);
                        if (dataDictionaryFolder != null)
                        {
                            dataDictionaryIdentifier = dataDictionaryFolder.getIdentifier();
                            break;
                        }
                    }
                }

                // Retrieve the configuration File
                configurationDocument = docService.getChildByPath((Folder) dataDictionaryFolder,
                        ConfigurationManager.DATA_DICTIONNARY_MOBILE_PATH);
                if (configurationDocument != null)
                {
                    configurationIdentifier = configurationDocument.getIdentifier();
                }
            }

            if (configurationDocument != null && configurationDocument.isDocument())
            {
                lastModificationTime = configurationDocument.getModifiedAt().getTimeInMillis();
                ContentStream stream = docService.getContentStream((Document) configurationDocument);
                Map<String, Object> json = JsonUtils.parseObject(stream.getInputStream(), "UTF-8");
                configContext = ConfigurationContext.parseJson(json);
                result.setData(configContext);
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_CONFIGURATION_COMPLETED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_CONFIGURATION, configContext);
        b.putLong(IntentIntegrator.EXTRA_ACCOUNT_ID, accountId);
        b.putString(IntentIntegrator.EXTRA_DATA_DICTIONARY_ID, dataDictionaryIdentifier);
        b.putString(IntentIntegrator.EXTRA_CONFIGURATION_ID, configurationIdentifier);
        b.putLong(IntentIntegrator.EXTRA_LASTMODIFICATION, lastModificationTime);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
