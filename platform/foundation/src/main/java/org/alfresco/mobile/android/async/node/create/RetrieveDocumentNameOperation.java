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
package org.alfresco.mobile.android.async.node.create;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.io.IOUtils;

import android.util.Log;

public class RetrieveDocumentNameOperation extends BaseOperation<String>
{
    private static final String TAG = RetrieveDocumentNameOperation.class.getName();

    /** Parent Folder object of the new folder. */
    protected Folder parentFolder;

    protected String parentFolderIdentifier;

    /** Name of the future document. */
    protected String documentName;

    protected String finalDocumentName;

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public RetrieveDocumentNameOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof RetrieveDocumentNameRequest)
        {
            this.parentFolderIdentifier = ((RetrieveDocumentNameRequest) request).getParentFolderIdentifier();
            this.documentName = ((RetrieveDocumentNameRequest) request).documentName;
        }
    }

    // ////////////////////////////////////////////////////
    // LIFE CYCLE
    // ////////////////////////////////////////////////////
    @Override
    protected LoaderResult<String> doInBackground()
    {
        LoaderResult<String> result = new LoaderResult<String>();
        try
        {
            result = super.doInBackground();

            parentFolder = retrieveParentFolder();

            finalDocumentName = createUniqueName();
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }

        result.setData(finalDocumentName);

        return result;
    }

    private String createUniqueName()
    {
        String fileNameWithoutExtension = documentName.replaceFirst("[.][^.]+$", "");
        String fileExtension = getFileExtension(documentName);

        int index = 1;

        String tmpName = documentName;

        while (doesExist(tmpName))
        {
            tmpName = fileNameWithoutExtension + "-" + index + fileExtension;
            index++;
        }
        return tmpName;
    }

    private static String getFileExtension(String fileName)
    {
        return "." + IOUtils.extractFileExtension(fileName);
    }

    private boolean doesExist(String documentPath)
    {
        try
        {
            Document tmpDoc = (Document) session.getServiceRegistry().getDocumentFolderService()
                    .getChildByPath(parentFolder, documentPath);
            if (tmpDoc != null) { return true; }
        }
        catch (Exception e)
        {
            return false;
        }
        return false;
    }

    private Folder retrieveParentFolder()
    {
        if (parentFolder == null && parentFolderIdentifier != null)
        {
            parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                    .getNodeByIdentifier(parentFolderIdentifier);
        }
        return parentFolder;
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
    protected void onPostExecute(LoaderResult<String> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new RetrieveDocumentNameEvent(getRequestId(), result, parentFolder));
    }
}
