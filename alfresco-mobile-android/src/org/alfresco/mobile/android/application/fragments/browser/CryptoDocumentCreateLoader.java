/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.browser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.DocumentCreateLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.CipherUtils;

import android.content.Context;

/**
 * Provides an asynchronous Loader to create a Document object.</br>
 * 
 * @author Jean Marie Pascal
 */
public class CryptoDocumentCreateLoader extends DocumentCreateLoader
{
    private Context context;
    
    /**
     * Create an empty (with no content) document object.
     * 
     * @param context : Android Context
     * @param session : Repository Session
     * @param parentFolder : Future parent folder of a new document
     * @param documentName : Name of the document
     */
    public CryptoDocumentCreateLoader(Context context, AlfrescoSession session, Folder parentFolder, String documentName)
    {
        super(context, session, parentFolder, documentName, null);
        this.context = context;
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
    public CryptoDocumentCreateLoader(Context context, AlfrescoSession session, Folder parentFolder, String documentName,
            Map<String, Serializable> properties)
    {
        super(context, session, parentFolder, documentName, properties, null);
        this.context = context;
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
    public CryptoDocumentCreateLoader(Context context, AlfrescoSession session, Folder parentFolder, String documentName,
            Map<String, Serializable> properties, ContentFile contentFile)
    {
        super(context, session, parentFolder, documentName, properties, contentFile);
        this.context = context;
    }

    @Override
    public LoaderResult<Document> loadInBackground()
    {
        LoaderResult<Document> result = new LoaderResult<Document>();

        try
        {
            String filename = getContentFile().getFile().getPath();
            boolean encdec = StorageManager.shouldEncryptDecrypt(context, filename);
            
            if (encdec)
            {
                CipherUtils.decryptFile(context, filename);
            }
            
            result = super.loadInBackground();
            
            if (encdec)
            {
                CipherUtils.encryptFile(context, filename, true);
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        return result;
    }
}
