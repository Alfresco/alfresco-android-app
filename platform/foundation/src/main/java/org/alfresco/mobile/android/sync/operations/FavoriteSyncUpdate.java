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
package org.alfresco.mobile.android.sync.operations;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.security.EncryptionUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncSchema;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.util.Log;

public class FavoriteSyncUpdate extends FavoriteSync
{
    private static final String TAG = FavoriteSyncUpdate.class.getName();

    public static final int TYPE_ID = 30;

    private Document updatedNode = null;

    private final ContentFile contentFile;

    private final Document document;

    private final boolean doRemove;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public FavoriteSyncUpdate(Context context, AlfrescoAccount acc, AlfrescoSession session, SyncResult syncResult,
            String parentIdentifier, Document document, ContentFile contentFile, Uri localUri, boolean doRemove)
    {
        super(context, acc, session, syncResult, localUri);
        this.document = document;
        this.contentFile = contentFile;
        this.doRemove = doRemove;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void execute()
    {
        Log.d("Alfresco", "update for doc[" + document.getName() + "]");
        super.execute();

        try
        {
            if (contentFile != null)
            {
                // Disable data protection if necessary
                if (DataProtectionManager.getInstance(context).isEncrypted(contentFile.getFile().getPath()))
                {
                    // Decrypt now !
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

                // Enable data protection if necessary
                if (DataProtectionManager.getInstance(context).isEncryptionEnable()
                        && !DataProtectionManager.getInstance(context).isEncrypted(contentFile.getFile().getPath()))
                {
                    EncryptionUtils.encryptFile(context, contentFile.getFile().getPath(), true);
                }
            }

            onPostExecute(doRemove, syncUri);

            syncResult.stats.numUpdates++;
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            syncResult.stats.numIoExceptions++;
            saveStatus(FavoriteSyncStatus.STATUS_FAILED);
        }

    }

    protected void onPostExecute(Boolean doRemove, Uri localUri)
    {
        if (doRemove)
        {
            context.getContentResolver().delete(localUri, null, null);
        }
        else
        {
            // Update Sync Info
            ContentValues cValues = new ContentValues();
            cValues.put(OperationSchema.COLUMN_STATUS, FavoriteSyncStatus.STATUS_SUCCESSFUL);
            cValues.put(FavoritesSyncSchema.COLUMN_NODE_ID, updatedNode.getIdentifier());
            cValues.put(FavoritesSyncSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, updatedNode.getModifiedAt()
                    .getTimeInMillis());
            cValues.put(FavoritesSyncSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, contentFile.getFile().lastModified());
            cValues.put(FavoritesSyncSchema.COLUMN_CONTENT_URI,
                    (String) updatedNode.getProperty(PropertyIds.CONTENT_STREAM_ID).getValue());
            context.getContentResolver().update(localUri, cValues, null, null);
        }
    }
}
