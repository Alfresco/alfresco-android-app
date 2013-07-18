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
package org.alfresco.mobile.android.application.operations.sync.node.update;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.operations.sync.impl.AbstractSyncOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.sync.node.AbstractSyncUpThread;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.security.EncryptionUtils;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SyncUpdateThread extends AbstractSyncUpThread
{
    private static final String TAG = SyncUpdateThread.class.getName();

    private Document updatedNode = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncUpdateThread(Context ctx, AbstractSyncOperationRequestImpl request)
    {
        super(ctx, request);
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

            if (contentFile != null)
            {
                if (DataProtectionManager.getInstance(context).isEncrypted(contentFile.getFile().getPath()))
                {
                    //Decrypt now !
                    EncryptionUtils.decryptFile(context, contentFile.getFile().getPath());
                }

                Session cmisSession = ((AbstractAlfrescoSessionImpl) session).getCmisSession();
                AlfrescoDocument cmisDoc = (AlfrescoDocument) cmisSession.getObject(document.getIdentifier());

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

                updatedNode = (Document) session.getServiceRegistry().getDocumentFolderService()
                        .getNodeByIdentifier(cmisDoc.getId());

                DataProtectionManager.getInstance(context).checkEncrypt(acc, contentFile.getFile());
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

        result.setData(updatedNode);

        return result;
    }

    @Override
    protected void onPostExecute(LoaderResult<Document> result)
    {
        super.onPostExecute(result);

        if (!result.hasException() && request instanceof SyncUpdateRequest)
        {

            if (((SyncUpdateRequest) request).doRemove())
            {
                context.getContentResolver().delete(request.getNotificationUri(), null, null);
            }
            else
            {
                // Update Sync Info
                ContentValues cValues = new ContentValues();
                cValues.put(SynchroSchema.COLUMN_NODE_ID, result.getData().getIdentifier());
                cValues.put(SynchroSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, result.getData().getModifiedAt()
                        .getTimeInMillis());
                cValues.put(SynchroSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, contentFile.getFile().lastModified());
                cValues.put(SynchroSchema.COLUMN_CONTENT_URI,
                        (String) result.getData().getProperty(PropertyIds.CONTENT_STREAM_ID).getValue());
                context.getContentResolver().update(request.getNotificationUri(), cValues, null, null);
            }
        }
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
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, document);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, document);
        b.putParcelable(IntentIntegrator.EXTRA_UPDATED_NODE, updatedNode);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
