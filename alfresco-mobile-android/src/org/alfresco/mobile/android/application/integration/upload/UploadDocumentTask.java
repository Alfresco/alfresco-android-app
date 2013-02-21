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
package org.alfresco.mobile.android.application.integration.upload;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.utils.ProgressNotification;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

/**
 * Responsible to upload a document from the device to the repository.
 * 
 * @author Jean Marie Pascal
 */
public class UploadDocumentTask extends AsyncTask<Void, Integer, LoaderResult<Document>>
{

    private static final String TAG = "DocumentCreateTask";

    /** Parent Folder object of the new folder. */
    private Folder parentFolder;

    /** Name of the future document. */
    private String documentName;

    /** list of properties. */
    private Map<String, Serializable> properties;

    /** Binary Content of the future document. */
    private ContentFile contentFile;

    protected AlfrescoSession session;

    private Context context;

    /**
     * Create an empty (with no content) document object.
     * 
     * @param context : Android Context
     * @param session : Repository Session
     * @param parentFolder : Future parent folder of a new document
     * @param documentName : Name of the document
     */
    public UploadDocumentTask(Context ctx, AlfrescoSession session, Folder parentFolder, String documentName)
    {
        this(ctx, session, parentFolder, documentName, null);
    }

    /**
     * Create an empty (with no content) document object.
     * 
     * @param context : Android Context
     * @param session : Repository Session
     * @param parentFolder : Future parent folder of a new document
     * @param documentName : Name of the document
     * @param properties : (Optional) list of property values that must be
     *            applied
     */
    public UploadDocumentTask(Context ctx, AlfrescoSession session, Folder parentFolder, String documentName,
            Map<String, Serializable> properties)
    {
        this(ctx, session, parentFolder, documentName, properties, null);
    }

    /**
     * Create a document object.
     * 
     * @param context : Android Context
     * @param session : Repository Session
     * @param parentFolder : Future parent folder of a new document
     * @param properties : (Optional) list of property values that must be
     *            applied
     * @param contentFile : (Optional) ContentFile that contains data stream or
     *            file
     */
    public UploadDocumentTask(Context ctx, AlfrescoSession session, Folder parentFolder, String documentName,
            Map<String, Serializable> properties, ContentFile contentFile)
    {
        this.session = session;
        this.documentName = documentName;
        this.parentFolder = parentFolder;
        this.properties = properties;
        this.contentFile = contentFile;
        this.context = ctx;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected LoaderResult<Document> doInBackground(Void... params)
    {
        LoaderResult<Document> result = new LoaderResult<Document>();
        Document doc = null;

        try
        {
            if (parentFolder != null)
            {
                // TAGS
                List<String> tags = null;
                if (properties.containsKey(ContentModel.PROP_TAGS) && properties.get(ContentModel.PROP_TAGS) != null)
                {
                    tags = (ArrayList<String>) properties.get(ContentModel.PROP_TAGS);
                    properties.remove(ContentModel.PROP_TAGS);
                }

                // CREATE CONTENT
                doc = session.getServiceRegistry().getDocumentFolderService()
                        .createDocument(parentFolder, documentName, properties, contentFile);

                if (tags != null && !tags.isEmpty())
                {
                    session.getServiceRegistry().getTaggingService().addTags(doc, tags);
                }
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(doc);

        return result;
    }

    @Override
    protected void onPostExecute(LoaderResult<Document> results)
    {
        if (results.hasException())
        {
            Log.e(TAG, Log.getStackTraceString(results.getException()));
            ContentFile contentFile = getContentFile();
            if (contentFile != null)
            {
                // An error occurs, notify the user.
                ProgressNotification.updateProgress(getDocumentName(), ProgressNotification.FLAG_UPLOAD_IMPORT_ERROR);
            }
        }
        else
        {
            if (getContentFile() != null)
            {
                // Notify the upload is complete.
                ProgressNotification.updateProgress(getDocumentName(), ProgressNotification.FLAG_UPLOAD_COMPLETED);
            }
        }
        
        // The upload is done. Update service information.
        Bundle params = new Bundle();
        params.putString(UploadService.ARGUMENT_TASK_ID, getId());
        UploadService.updateImportService(context, params);
    }

    public Folder getParentFolder()
    {
        return parentFolder;
    }

    public void setParentFolder(Folder parentFolder)
    {
        this.parentFolder = parentFolder;
    }

    public String getDocumentName()
    {
        return documentName;
    }

    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

    public ContentFile getContentFile()
    {
        return contentFile;
    }

    public String getId()
    {
        return parentFolder.getProperty(PropertyIds.PATH) + contentFile.getFile().getPath();
    }
}
