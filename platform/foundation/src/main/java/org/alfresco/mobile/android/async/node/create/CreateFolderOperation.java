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
package org.alfresco.mobile.android.async.node.create;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.NodeOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

import android.util.Log;

public class CreateFolderOperation extends NodeOperation<Folder>
{
    /** Parent Folder object of the new folder. */
    protected String folderName;

    protected Folder folder = null;

    protected HashMap<String, Serializable> properties;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateFolderOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof CreateFolderRequest)
        {
            this.folderName = ((CreateFolderRequest) request).folderName;
        }
        properties = new HashMap<String, Serializable>(2);
        properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
        ignoreNode = true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Folder> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<Folder> result = new LoaderResult<Folder>();
            Folder folder = null;

            try
            {
                folder = session.getServiceRegistry().getDocumentFolderService().createFolder(parentFolder, folderName,
                        properties);
            }
            catch (Exception e)
            {
                result.setException(e);
            }

            result.setData(folder);

            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }

        return new LoaderResult<Folder>();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Folder getParentFolder()
    {
        return parentFolder;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Folder> result)
    {
        // Analytics
        // Define exactly what's wrong
        String label = AnalyticsManager.TYPE_FOLDER;
        boolean hasException = false;
        try
        {
            if (result.hasException())
            {
                if (result.getException() instanceof AlfrescoServiceException
                        && result.getException().getCause() != null)
                {
                    if (result.getException().getCause() instanceof CmisUnauthorizedException)
                    {
                        hasException = false;
                        label = AnalyticsManager.LABEL_UNAUTHORIZED;
                    }
                    else if (result.getException().getCause() instanceof CmisConnectionException)
                    {
                        if (ConnectivityUtils.hasInternetAvailable(context))
                        {
                            hasException = false;
                            label = AnalyticsManager.LABEL_UNKNOWN_SERVER;
                        }
                        else
                        {
                            hasException = false;
                            label = AnalyticsManager.LABEL_OFFLINE;
                        }
                    }
                    else if (result.getException().getCause() instanceof CmisContentAlreadyExistsException)
                    {
                        hasException = false;
                        label = AnalyticsManager.LABEL_CONTENT_ALREADY_EXIST;
                    }
                }
            }
        }
        catch (Exception e)
        {
            hasException = true;
            label = AnalyticsManager.LABEL_FAILED;
        }

        AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                AnalyticsManager.ACTION_CREATE, label, 1, hasException);

        super.onPostExecute(result);
        EventBusManager.getInstance().post(new CreateFolderEvent(getRequestId(), result, parentFolder));
    }
}
