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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.impl.ContentFileImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.security.EncryptionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SyncContentDownload extends SyncContent
{
    private static final String TAG = SyncContentDownload.class.getName();

    public static final int TYPE_ID = 10;

    private static final int MAX_BUFFER_SIZE = 1024;

    private static final int SEGMENT = 10;

    private int downloaded;

    private long totalDownloaded;

    private int segment = 1;

    private int currentSegment = 0;

    private long totalLength = 0;

    private Cursor cursor;

    private Folder parentFolder;

    private Document doc;

    private ContentFile contentFileResult;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncContentDownload(Context context, AlfrescoAccount acc, AlfrescoSession session, SyncResult syncResult,
            Document doc, Uri localUri)
    {
        super(context, acc, session, syncResult, localUri);
        this.doc = doc;
        downloaded = 0;
        totalDownloaded = 0;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void execute()
    {
        super.execute();
        contentFileResult = null;
        try
        {
            Log.d("Alfresco", "Download for doc[" + doc.getName() + "]");

            // Retrieve parent
            parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(doc);

            File destFile = SyncContentManager.getInstance(context).getSynchroFile(acc, doc);

            // Download content
            persistDocument(destFile);

            if (!destFile.exists())
            {
                persistDocument(destFile);
            }

            contentFileResult = new ContentFileImpl(destFile);

            // Delete previous versioned file (name.txt, new.txt)
            cursor = context.getContentResolver().query(syncUri, SyncContentSchema.COLUMN_ALL, null, null, null);
            if (cursor != null && cursor.moveToFirst())
            {
                Uri localFileUri = Uri.parse(cursor.getString(SyncContentSchema.COLUMN_LOCAL_URI_ID));
                if (localFileUri != null && !localFileUri.getPath().isEmpty())
                {
                    File localFile = new File(localFileUri.getPath());
                    if (localFile != null && !destFile.getPath().equals(localFile.getPath()))
                    {
                        localFile.delete();
                    }
                }
            }

            if (DataProtectionManager.getInstance(context).isEncryptionEnable()
                    && !DataProtectionManager.getInstance(context).isEncrypted(destFile.getPath()))
            {
                EncryptionUtils.encryptFile(context, destFile.getPath(), true);
            }

            // Update Sync Info
            ContentValues cValues = new ContentValues();
            cValues.put(SyncContentSchema.COLUMN_LOCAL_URI, Uri.fromFile(destFile).toString());
            if (parentFolder != null)
            {
                cValues.put(SyncContentSchema.COLUMN_PARENT_ID, parentFolder.getIdentifier());
            }
            cValues.put(SyncContentSchema.COLUMN_CONTENT_URI,
                    (String) doc.getPropertyValue(PropertyIds.CONTENT_STREAM_ID));
            cValues.put(SyncContentSchema.COLUMN_PROPERTIES, SyncContentManager.serializeProperties(doc));
            cValues.put(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES, doc.getContentStreamLength());
            cValues.put(SyncContentSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, doc.getContentStreamLength());
            cValues.put(SyncContentSchema.COLUMN_DOC_SIZE_BYTES, 0);
            context.getContentResolver().update(syncUri, cValues, null, null);

            onPostExecute();

            syncResult.stats.numInserts++;
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            syncResult.stats.numIoExceptions++;
            saveStatus(SyncContentStatus.STATUS_FAILED);
        }
        finally
        {
            CursorUtils.closeCursor(cursor);
        }

    }

    protected void onPostExecute()
    {
        // Update Sync Info
        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_STATUS, SyncContentStatus.STATUS_SUCCESSFUL);
        cValues.put(SyncContentSchema.COLUMN_NODE_ID, doc.getIdentifier());
        cValues.put(SyncContentSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, doc.getModifiedAt().getTimeInMillis());
        cValues.put(SyncContentSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, contentFileResult.getFile().lastModified());
        context.getContentResolver().update(syncUri, cValues, null, null);

        // Update Parent Folder if present
        if (parentFolder != null)
        {
            SyncContentManager.getInstance(context).updateParentFolder(acc, parentFolder.getIdentifier());
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private void persistDocument(File destFile)
    {
        ContentStream contentStream = session.getServiceRegistry().getDocumentFolderService().getContentStream(doc);
        if (contentStream != null)
        {
            totalLength = contentStream.getLength();
            segment = (int) (contentStream.getLength() / SEGMENT) + 1;
            copyFile(contentStream.getInputStream(), contentStream.getLength(), destFile);
        }
    }

    private void publishProgress()
    {
        if ((totalDownloaded / segment > currentSegment) || totalDownloaded == totalLength)
        {
            ++currentSegment;
            saveProgress();
        }
    }

    private boolean copyFile(InputStream src, long size, File dest)
    {
        IOUtils.ensureOrCreatePathAndFile(dest);
        OutputStream os = null;
        boolean copied = true;

        try
        {
            os = new BufferedOutputStream(new FileOutputStream(dest));

            byte[] buffer = new byte[MAX_BUFFER_SIZE];

            while (size - downloaded > 0)
            {
                if (size - downloaded < MAX_BUFFER_SIZE)
                {
                    buffer = new byte[(int) (size - downloaded)];
                }

                int read = src.read(buffer);
                if (read == -1)
                {
                    break;
                }

                os.write(buffer, 0, read);
                downloaded += read;
                totalDownloaded += read;
                publishProgress();
            }

        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            copied = false;
        }
        finally
        {
            IOUtils.closeStream(src);
            IOUtils.closeStream(os);
        }
        return copied;
    }

    private void saveProgress()
    {
        if (syncUri != null)
        {
            ContentValues cValues = new ContentValues();
            cValues.put(OperationSchema.COLUMN_TOTAL_SIZE_BYTES, totalLength);
            cValues.put(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, totalDownloaded);
            context.getContentResolver().update(syncUri, cValues, null, null);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public long getTotalLength()
    {
        return totalLength;
    }
}
