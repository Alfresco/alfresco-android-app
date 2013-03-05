/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.properties;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.alfresco.mobile.android.api.asynchronous.AbstractBaseLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import android.content.Context;
import android.util.Log;

/**
 * Provides an asynchronous loader to update a Node object. Could be an update
 * of content document or an update of properties
 * 
 * @author Jean Marie Pascal
 */
public class UpdateContentLoader extends AbstractBaseLoader<LoaderResult<Document>>
{

    /** Unique NodeUpdateLoader identifier. */
    public static final int ID = UpdateContentLoader.class.hashCode();

    private static final String TAG = "UpdateContentLoader";

    /** Binary Content of the future document. */
    private ContentFile contentFile;

    /** Node object to update. */
    private Document node;

    private Context context;

    /**
     * Update an existing document with current parameters (Content and/or
     * properties)
     * 
     * @param context : Android Context
     * @param session : Repository Session
     * @param document : Document object to update
     * @param properties : (Optional) list of property values that must be
     *            applied
     * @param contentFile : (Optional) ContentFile that contains data stream or
     *            file
     */
    public UpdateContentLoader(Context context, AlfrescoSession session, Document document, ContentFile contentFile)
    {
        super(context);
        this.session = session;
        this.node = document;
        this.contentFile = contentFile;
        this.context = context; //Only used for non UI interaction.
    }

    @Override
    public LoaderResult<Document> loadInBackground()
    {
        LoaderResult<Document> result = new LoaderResult<Document>();
        Document resultNode = null;

        try
        {
            if (contentFile != null)
            {
                if (StorageManager.shouldEncryptDecrypt(context, contentFile.getFile().getPath()))
                {
                    CipherUtils.decryptFile(context, contentFile.getFile().getPath());
                }
                
                Session cmisSession = ((AbstractAlfrescoSessionImpl) session).getCmisSession();
                AlfrescoDocument cmisDoc = (AlfrescoDocument) cmisSession.getObject(node.getIdentifier());

                String idpwc = cmisDoc.getVersionSeriesCheckedOutId();

                try
                {
                    if (idpwc == null)
                    {
                        idpwc = cmisDoc.checkOut().getId();
                    }
                }
                catch (Exception e)
                {
                    Log.e(TAG, Log.getStackTraceString(e));
                    if (idpwc == null)
                    {
                        try
                        {
                            idpwc = cmisDoc.checkOut().getId();
                        }
                        catch (Exception ee)
                        {
                            Log.e(TAG, Log.getStackTraceString(ee));
                            throw ee;
                        }
                    }
                }

                org.apache.chemistry.opencmis.client.api.Document cmisDocpwc = null;
                try
                {
                    cmisDocpwc = (org.apache.chemistry.opencmis.client.api.Document) cmisSession.getObject(idpwc);
                }
                catch (Exception e)
                {
                    Log.e(TAG, Log.getStackTraceString(e));
                    cmisDocpwc = (org.apache.chemistry.opencmis.client.api.Document) cmisSession.getObject(idpwc);
                }

                ContentStream c = cmisSession.getObjectFactory().createContentStream(contentFile.getFileName(),
                        contentFile.getLength(), contentFile.getMimeType(),
                        IOUtils.getContentFileInputStream(contentFile));

                ObjectId iddoc = cmisDocpwc.checkIn(false, null, c, "");
                cmisDoc = (AlfrescoDocument) cmisSession.getObject(iddoc);
                cmisDoc = (AlfrescoDocument) cmisDoc.getObjectOfLatestVersion(false);

                resultNode = (Document) session.getServiceRegistry().getDocumentFolderService()
                        .getNodeByIdentifier(cmisDoc.getId());
                
                if (StorageManager.shouldEncryptDecrypt(context, contentFile.getFile().getPath()))
                {
                    CipherUtils.encryptFile(context, contentFile.getFile().getPath(), true);
                }
            }
        }
        catch (Exception e)
        {
            result.setException(e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        result.setData(resultNode);

        return result;
    }
    
    public ContentFile getContentFile()
    {
        return contentFile;
    }
    
    public Document getDocument()
    {
        return node;
    }
    
}
