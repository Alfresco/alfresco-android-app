/**
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
package org.alfresco.mobile.android.application.operations.batch.node.update;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.node.AbstractUpThread;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.security.EncryptionUtils;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class UpdateContentThread extends AbstractUpThread
{
    private static final String TAG = UpdateContentThread.class.getName();

    private Document originalDocument;

    private String originalIdentifier;

    private Document updatedDocument = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public UpdateContentThread(Context ctx, AbstractBatchOperationRequestImpl request)
    {
        super(ctx, request);
        if (request instanceof UpdateContentRequest)
        {
            this.originalIdentifier = ((UpdateContentRequest) request).getNodeIdentifier();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Document> doInBackground()
    {
        LoaderResult<Document> result = new LoaderResult<Document>();

        try
        {
            result = super.doInBackground();

            originalDocument = (Document) session.getServiceRegistry().getDocumentFolderService()
                    .getNodeByIdentifier(originalIdentifier);

            if (contentFile != null)
            {
                if (!StorageManager.isTempFile(context, contentFile.getFile())
                        && DataProtectionManager.getInstance(context).isEncrypted(contentFile.getFile().getPath()))
                {
                    //Decrypt now !
                    EncryptionUtils.decryptFile(context, contentFile.getFile().getPath());
                }

                Session cmisSession = ((AbstractAlfrescoSessionImpl) session).getCmisSession();
                AlfrescoDocument cmisDoc = (AlfrescoDocument) cmisSession.getObject(originalDocument.getIdentifier());

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

                updatedDocument = (Document) session.getServiceRegistry().getDocumentFolderService()
                        .getNodeByIdentifier(cmisDoc.getId());

                // Encrypt if necessary / Delete otherwise
                StorageManager.manageFile(context, contentFile.getFile());
            }
        }
        catch (Exception e)
        {
            if (result == null)
            {
                result = new LoaderResult<Document>();
            }
            result.setException(e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        result.setData(updatedDocument);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Document getDocument()
    {
        return originalDocument;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_STARTED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, originalDocument);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, originalDocument);
        b.putParcelable(IntentIntegrator.EXTRA_UPDATED_NODE, updatedDocument);
        b.putString(IntentIntegrator.EXTRA_FILE_PATH, contentFile.getFile().getPath());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
