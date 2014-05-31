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
package org.alfresco.mobile.android.async.node.download;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.impl.ContentFileImpl;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.NodeOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.io.IOUtils;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;

import android.util.Log;

public class DownloadOperation extends NodeOperation<ContentFile>
{
    private static final String TAG = DownloadOperation.class.getName();

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
    public DownloadOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof DownloadRequest)
        {
            overwrite = ((DownloadRequest) request).overwrite;
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
        LoaderResult<ContentFile> result = new LoaderResult<ContentFile>();
        ContentFile contentFileResult = null;
        try
        {
            result = super.doInBackground();

            destFile = getDownloadFile();

            ContentStream contentStream = session.getServiceRegistry().getDocumentFolderService()
                    .getContentStream((Document) node);
            totalLength = contentStream.getLength();
            segment = (int) (contentStream.getLength() / SEGMENT) + 1;
            copyFile(contentStream.getInputStream(), contentStream.getLength(), destFile);
            contentFileResult = new ContentFileImpl(destFile);

            // Encryption if necessary
            DataProtectionManager.getInstance(context).checkEncrypt(acc, destFile);
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
        if ((totalDownloaded / segment > currentSegment) || totalDownloaded == totalLength)
        {
            Log.d("Progress", request.notificationUri + " " + totalDownloaded);
            ++currentSegment;
            saveProgress(totalDownloaded);
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
                if (isInterrupted())
                {
                    hasCancelled = true;
                    throw new IOException(EXCEPTION_OPERATION_CANCEL);
                }

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
            File folder = AlfrescoStorageManager.getInstance(context).getDownloadFolder(acc);
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
        if (request.notificationUri != null && request instanceof DownloadRequest)
        {
            Log.d("Progress", request.notificationUri + " " + progress);
            context.getContentResolver().update(request.notificationUri,
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
    @Override
    protected void onPostExecute(LoaderResult<ContentFile> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new DownloadEvent(getRequestId(), result, (Document) node, parentFolder));
    }

}
