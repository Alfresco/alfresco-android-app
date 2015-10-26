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

import java.io.File;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.impl.ContentFileImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.security.EncryptionUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SyncContentCreate extends SyncContent
{
    private static final String TAG = SyncContentCreate.class.getName();

    public static final int TYPE_ID = 30;

    private Document updatedNode = null;

    private final Long itemId;

    private Folder parentFolder;

    private File localFile;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncContentCreate(Context context, AlfrescoAccount acc, AlfrescoSession session, SyncResult syncResult,
            Long itemId, Uri localUri)
    {
        super(context, acc, session, syncResult, localUri);
        this.itemId = itemId;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void execute()
    {
        Log.d("Alfresco", "Create for doc[" + itemId + "]");
        super.execute();

        try
        {

            Cursor itemCursor = context.getContentResolver().query(SyncContentManager.getUri(itemId),
                    SyncContentSchema.COLUMN_ALL, null, null, null);
            if (itemCursor.getCount() == 1 && itemCursor.moveToNext())
            {
                String parentIdentifier = itemCursor.getString(SyncContentSchema.COLUMN_PARENT_ID_ID);

                // Retrieve parent
                try
                {
                    parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                            .getNodeByIdentifier(parentIdentifier);
                }
                catch (Exception e)
                {
                    parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                            .getNodeByIdentifier(NodeRefUtils.createNodeRefByIdentifier(parentIdentifier));
                }

                if (!session.getServiceRegistry().getDocumentFolderService().getPermissions(parentFolder)
                        .canAddChildren())
                {
                    // User has no right to create ==> Display error
                    ContentValues cValues = new ContentValues();
                    cValues.put(OperationSchema.COLUMN_STATUS, SyncContentStatus.STATUS_REQUEST_USER);
                    cValues.put(OperationSchema.COLUMN_REASON, SyncContentStatus.REASON_NO_PERMISSION);
                    context.getContentResolver().update(SyncContentManager.getUri(itemId), cValues, null, null);
                    return;
                }

                String name = itemCursor.getString(SyncContentSchema.COLUMN_TITLE_ID);
                Uri localFileUri = Uri.parse(itemCursor.getString(SyncContentSchema.COLUMN_LOCAL_URI_ID));
                if (localFileUri != null && !localFileUri.getPath().isEmpty())
                {
                    localFile = new File(localFileUri.getPath());
                    updatedNode = session.getServiceRegistry().getDocumentFolderService().createDocument(parentFolder,
                            name, null, new ContentFileImpl(localFile));

                    // Time to move the file to the correct location
                    File destFile = SyncContentManager.getInstance(context).getSynchroFile(acc, updatedNode);
                    localFile.renameTo(destFile);

                    // Enable data protection if necessary
                    if (DataProtectionManager.getInstance(context).isEncryptionEnable()
                            && !DataProtectionManager.getInstance(context).isEncrypted(localFile.getPath()))
                    {
                        EncryptionUtils.encryptFile(context, localFile.getPath(), true);

                        // Update statut of the sync reference
                        ContentValues cValues = new ContentValues();
                        cValues.put(SyncContentSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, localFile.lastModified());
                        context.getContentResolver().update(SyncContentManager.getInstance(context).getUri(
                                SessionUtils.getAccount(context), updatedNode.getIdentifier()), cValues, null, null);
                    }
                }
            }

            onPostExecute(SyncContentManager.getUri(itemId));

            syncResult.stats.numInserts++;
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            syncResult.stats.numIoExceptions++;
            saveStatus(SyncContentStatus.STATUS_FAILED);
        }

    }

    protected void onPostExecute(Uri localUri)
    {
        // Update Sync Info
        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_STATUS, SyncContentStatus.STATUS_SUCCESSFUL);
        cValues.put(SyncContentSchema.COLUMN_NODE_ID, updatedNode.getIdentifier());
        cValues.put(SyncContentSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP,
                updatedNode.getModifiedAt().getTimeInMillis());
        cValues.put(SyncContentSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, localFile.lastModified());
        cValues.put(SyncContentSchema.COLUMN_CONTENT_URI,
                (String) updatedNode.getProperty(PropertyIds.CONTENT_STREAM_ID).getValue());
        cValues.put(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES, updatedNode.getContentStreamLength());
        cValues.put(SyncContentSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, updatedNode.getContentStreamLength());
        cValues.put(SyncContentSchema.COLUMN_DOC_SIZE_BYTES, 0);
        cValues.put(SyncContentSchema.COLUMN_PROPERTIES, SyncContentManager.serializeProperties(updatedNode));
        cValues.put(SyncContentSchema.COLUMN_LOCAL_URI, Uri.fromFile(localFile).toString());

        context.getContentResolver().update(localUri, cValues, null, null);
    }
}
