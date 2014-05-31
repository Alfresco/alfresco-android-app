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
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.utils.IOUtils;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Provides an asynchronous task to download the content of a document
 * object.</br> onProgressUpdate returns the progress of dthe download in
 * percentage.
 * 
 * @author Jean Marie Pascal
 */
public class DownloadTask extends AsyncTask<Void, Integer, ContentFile>
{

    private static final String TAG = "DownloadTask";

    private static final int MAX_BUFFER_SIZE = 1024;

    private int downloaded;

    private int totalDownloaded;

    private AlfrescoSession session;

    private Document doc;

    private File destFile;

    private DownloadTaskListener dl;

    public DownloadTask(AlfrescoSession session, Document document, File destFile)
    {
        this.session = session;
        this.destFile = destFile;
        this.doc = document;
    }

    public interface DownloadTaskListener
    {
        void onPreExecute();

        void onPostExecute(ContentFile f);

        void onProgressUpdate(Integer... values);
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        if (dl != null)
        {
            dl.onPreExecute();
        }
        downloaded = 0;
        totalDownloaded = 0;
    }

    @Override
    protected ContentFile doInBackground(Void... params)
    {
        try
        {
            ContentStream contentStream = session.getServiceRegistry().getDocumentFolderService().getContentStream(doc);
            copyFile(contentStream.getInputStream(), contentStream.getLength(), destFile);
            return new ContentFileImpl(destFile);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    @Override
    protected void onPostExecute(ContentFile f)
    {
        super.onPostExecute(f);
        if (dl != null)
        {
            dl.onPostExecute(f);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
        if (dl != null)
        {
            dl.onProgressUpdate(values);
        }
    }

    public boolean copyFile(InputStream src, long size, File dest)
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
            IOUtils.closeStream(src);
            IOUtils.closeStream(os);
        }
        return copied;
    }

    public void setDl(DownloadTaskListener dl)
    {
        this.dl = dl;
    }

}
