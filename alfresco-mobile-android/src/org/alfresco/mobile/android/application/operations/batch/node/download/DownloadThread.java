/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.node.download;

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
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.node.NodeOperationThread;
import org.alfresco.mobile.android.application.utils.IOUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class DownloadThread extends NodeOperationThread<ContentFile>
{
    private static final String TAG = DownloadThread.class.getName();

    private static final int MAX_BUFFER_SIZE = 1024;

    private static final int SEGMENT = 10;

    private int downloaded;

    private long totalDownloaded;

    private File destFile;

    private int segment;

    private int currentSegment = 0;

    private long totalLength;

    private boolean overwrite = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public DownloadThread(Context ctx, AbstractBatchOperationRequestImpl request)
    {
        super(ctx, request);
        if (request instanceof DownloadRequest)
        {
            overwrite = ((DownloadRequest) request).isOverwrite();
        }
        downloaded = 0;
        totalDownloaded = 0;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<ContentFile> doInBackground()
    {
        LoaderResult<ContentFile> result = null;
        ContentFile contentFileResult = null;
        try
        {
            result = super.doInBackground();

            destFile = getDownloadFile();

            ContentStream contentStream = session.getServiceRegistry().getDocumentFolderService()
                    .getContentStream((Document) node);
            totalLength = contentStream.getLength();
            segment = (int) (contentStream.getLength() / SEGMENT);
            copyFile(contentStream.getInputStream(), contentStream.getLength(), destFile);
            contentFileResult = new ContentFileImpl(destFile);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }

        result.setData(contentFileResult);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    protected void publishProgress(Long... values)
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
                publishProgress(totalDownloaded);
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
            org.alfresco.mobile.android.api.utils.IOUtils.closeStream(src);
            org.alfresco.mobile.android.api.utils.IOUtils.closeStream(os);
        }
        return copied;
    }

    private File getDownloadFile()
    {
        File newLocalFile = null;
        if (context != null && node != null && session != null)
        {
            File folder = StorageManager.getDownloadFolder(context, acc);
            if (folder != null)
            {
                newLocalFile = new File(folder, node.getName());
                if (!overwrite)
                {
                    newLocalFile = IOUtils.createFile(newLocalFile);
                }
            }
        }

        return newLocalFile;
    }

    private void saveProgress(long progress)
    {
        if (request.getNotificationUri() != null && request instanceof DownloadRequest)
        {
            context.getContentResolver().update(request.getNotificationUri(),
                    ((DownloadRequest) request).createContentValues(progress), null, null);
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
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, getDocument());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

}
