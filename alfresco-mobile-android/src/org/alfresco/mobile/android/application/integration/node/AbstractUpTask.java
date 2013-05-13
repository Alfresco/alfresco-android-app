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
package org.alfresco.mobile.android.application.integration.node;

import java.io.File;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationTask;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl.ReaderListener;

import android.content.Context;

public abstract class AbstractUpTask extends AbstractOperationTask<Document> implements ReaderListener
{
    /** Parent Folder object of the new folder. */
    protected Folder parentFolder;

    protected String parentFolderIdentifier;

    /** Name of the future document. */
    protected String documentName;

    /** Binary Content of the future document. */
    protected ContentFile contentFile;

    private int segment = 0;

    private long totalLength = 0;

    public AbstractUpTask(Context context, AbstractOperationRequestImpl request)
    {
        super(context, request);

        if (request instanceof AbstractUpRequest)
        {
            this.parentFolderIdentifier = ((AbstractUpRequest) request).getParentFolderIdentifier();
            this.documentName = ((AbstractUpRequest) request).getDocumentName();

            File f = new File(((AbstractUpRequest) request).getLocalFilePath());
            this.contentFile = new ContentFileProgressImpl(f);
            this.totalLength = f.length();
            this.segment = initSegment();
        }
    }

    protected LoaderResult<Document> doInBackground(Void... params)
    {
        LoaderResult<Document> result = new LoaderResult<Document>();
        session = requestSession();
        parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                .getNodeByIdentifier(parentFolderIdentifier);

        if (contentFile instanceof ContentFileProgressImpl)
        {
            ((ContentFileProgressImpl) contentFile).setReaderListener(this);
        }
        
        if (listener != null)
        {
            listener.onPreExecute(this);
        }

        return result;
    }

    @Override
    public void onRead(ContentFileProgressImpl contentFile, Long amountCopied)
    {
        // We limit progress notification to one per 10%
        if (listener != null)
        {
            saveProgress(amountCopied);
            listener.onProgressUpdate(this, amountCopied);
        }
        super.onProgressUpdate(amountCopied);
    }

    protected void saveProgress(long progress)
    {
        if (request.getNotificationUri() != null && request instanceof AbstractUpRequest)
        {
            context.getContentResolver().update(request.getNotificationUri(),
                    ((AbstractUpRequest) request).createContentValues(progress), null, null);
        }
    }

    private int initSegment()
    {
        int segment = 1;

        // 100kb
        if (totalLength < 102400)
        {
            segment = 2;
        }
        else
        // 500kb
        if (totalLength < 512000)
        {
            segment = 3;
        }
        else if (totalLength < 1048576)
        {
            // 1MB
            segment = 4;
        }
        else if (totalLength < 5242880)
        {
            // 5MB
            segment = 10;
        }
        else if (totalLength < 10485760)
        {
            // 10MB
            segment = 15;
        }
        else if (totalLength < 20971520)
        {
            // 20MB
            segment = 20;
        }
        else if (totalLength < 52428800)
        {
            // 50MB
            segment = 25;
        }
        else
        {
            segment = Math.round(totalLength / 1048576);
        }

        return segment;
    }

    public int getSegment()
    {
        return segment;
    }

    public ContentFile getContentFile()
    {
        return contentFile;
    }

    public String getDocumentName()
    {
        return documentName;
    }

    public Folder getParentFolder()
    {
        return parentFolder;
    }
}
