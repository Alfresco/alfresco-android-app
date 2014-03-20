/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.operations.sync.node.download;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.impl.ContentFileImpl;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.operations.sync.impl.AbstractSyncOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.sync.node.SyncNodeOperationThread;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class SyncDownloadThread extends SyncNodeOperationThread<ContentFile>
{
    private static final String TAG = SyncDownloadThread.class.getName();

    private static final int MAX_BUFFER_SIZE = 1024;

    private static final int SEGMENT = 10;

    private int downloaded;

    private long totalDownloaded;

    private int segment;

    private int currentSegment = 0;

    private long totalLength;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncDownloadThread(Context ctx, AbstractSyncOperationRequestImpl request)
    {
        super(ctx, request);
        downloaded = 0;
        totalDownloaded = 0;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<ContentFile> doInBackground()
    {
        LoaderResult<ContentFile> result = new LoaderResult<ContentFile>();
        ContentFile contentFileResult = null;
        try
        {
            result = super.doInBackground();

            File destFile = StorageManager.getSynchroFile(context, acc, (Document) node);

            // Download content
            ContentStream contentStream = session.getServiceRegistry().getDocumentFolderService()
                    .getContentStream((Document) node);
            totalLength = contentStream.getLength();
            segment = (int) (contentStream.getLength() / SEGMENT) + 1;
            copyFile(contentStream.getInputStream(), contentStream.getLength(), destFile);
            contentFileResult = new ContentFileImpl(destFile);

            // Delete previous versioned file (name.txt, new.txt)
            cursor = context.getContentResolver().query(request.getNotificationUri(), SynchroSchema.COLUMN_ALL, null,
                    null, null);
            if (cursor != null && cursor.moveToFirst())
            {
                Uri localFileUri = Uri.parse(cursor.getString(SynchroSchema.COLUMN_LOCAL_URI_ID));
                if (localFileUri != null && !localFileUri.getPath().isEmpty())
                {
                    File localFile = new File(localFileUri.getPath());
                    if (localFile != null && !destFile.getPath().equals(localFile.getPath()))
                    {
                        localFile.delete();
                    }
                }
            }

            if (DataProtectionManager.getInstance(context).isEncryptionEnable())
            {
                DataProtectionManager.getInstance(context).encrypt(acc, destFile);
            }


            // Update Sync Info
            ContentValues cValues = new ContentValues();
            cValues.put(SynchroSchema.COLUMN_LOCAL_URI, Uri.fromFile(destFile).toString());
            if (parentFolder != null)
            {
                cValues.put(SynchroSchema.COLUMN_PARENT_ID, parentFolder.getIdentifier());
            }
            cValues.put(SynchroSchema.COLUMN_CONTENT_URI, (String) node.getPropertyValue(PropertyIds.CONTENT_STREAM_ID));
            cValues.put(SynchroSchema.COLUMN_PROPERTIES, SynchroManager.serializeProperties(node));
            cValues.put(SynchroSchema.COLUMN_TOTAL_SIZE_BYTES, ((Document) node).getContentStreamLength());
            cValues.put(SynchroSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, ((Document) node).getContentStreamLength());
            cValues.put(SynchroSchema.COLUMN_DOC_SIZE_BYTES, 0);
            context.getContentResolver().update(request.getNotificationUri(), cValues, null, null);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }

        result.setData(contentFileResult);

        return result;
    }

    @Override
    protected void onPostExecute(LoaderResult<ContentFile> result)
    {
        super.onPostExecute(result);

        if (!result.hasException())
        {
            // Update Sync Info
            ContentValues cValues = new ContentValues();
            cValues.put(SynchroSchema.COLUMN_NODE_ID, getDocument().getIdentifier());
            cValues.put(SynchroSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP, getDocument().getModifiedAt()
                    .getTimeInMillis());
            cValues.put(SynchroSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, result.getData().getFile().lastModified());
            context.getContentResolver().update(request.getNotificationUri(), cValues, null, null);

            // Update Parent Folder if present
            if (parentFolder != null)
            {
                SynchroManager.getInstance(context).updateParentFolder(acc, parentFolder.getIdentifier());
            }
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private void publishProgress()
    {
        if (listener != null && (totalDownloaded / segment > currentSegment) || totalDownloaded == totalLength)
        {
            ++currentSegment;
            saveProgress(totalDownloaded);
            listener.onProgressUpdate(this, totalDownloaded);
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
        catch (FileNotFoundException e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            copied = false;
        }
        catch (IOException e)
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

    private void saveProgress(long progress)
    {
        if (request.getNotificationUri() != null && request instanceof SyncDownloadRequest)
        {
            context.getContentResolver().update(request.getNotificationUri(),
                    ((SyncDownloadRequest) request).createContentValues(progress), null, null);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Document getDocument()
    {
        return (Document) node;
    }

    public long getTotalLength()
    {
        return totalLength;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_DOWNLOAD_COMPLETED);
        return broadcastIntent;
    }
}
