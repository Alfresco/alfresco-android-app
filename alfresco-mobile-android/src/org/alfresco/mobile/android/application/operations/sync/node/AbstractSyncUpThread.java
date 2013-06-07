package org.alfresco.mobile.android.application.operations.sync.node;

import java.io.File;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.operations.sync.impl.AbstractSyncOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.sync.impl.AbstractSyncOperationThread;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl.ReaderListener;

import android.content.Context;
import android.util.Log;

public abstract class AbstractSyncUpThread extends AbstractSyncOperationThread<Document> implements ReaderListener
{
    private static final String TAG = AbstractSyncUpThread.class.getName();

    /** Parent Folder object of the new folder. */
    protected Folder parentFolder;

    protected String parentFolderIdentifier;

    /** Name of the future document. */
    protected String documentName;

    /** Binary Content of the future document. */
    protected ContentFile contentFile;

    private int segment = 0;

    private long totalLength = 0;

    protected String nodeIdentifier;

    protected Document document;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AbstractSyncUpThread(Context context, AbstractSyncOperationRequestImpl request)
    {
        super(context, request);

        if (request instanceof AbstractSyncUpRequest)
        {
            this.parentFolderIdentifier = ((AbstractSyncUpRequest) request).getParentFolderIdentifier();
            this.nodeIdentifier = ((AbstractSyncUpRequest) request).getNodeIdentifier();
            this.documentName = ((AbstractSyncUpRequest) request).getDocumentName();

            File f = new File(((AbstractSyncUpRequest) request).getLocalFilePath());
            this.contentFile = new ContentFileProgressImpl(f);
            this.totalLength = f.length();
            this.segment = initSegment();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<Document> doInBackground()
    {
        LoaderResult<Document> result = new LoaderResult<Document>();
        try
        {
            super.doInBackground();

            if (nodeIdentifier != null)
            {
                document = (Document) session.getServiceRegistry().getDocumentFolderService()
                        .getNodeByIdentifier(nodeIdentifier);
            }

            parentFolder = retrieveParentFolder();

            if (contentFile instanceof ContentFileProgressImpl)
            {
                ((ContentFileProgressImpl) contentFile).setReaderListener(this);
            }

            if (listener != null)
            {
                listener.onPreExecute(this);
            }
        }
        catch (Exception e)
        {
            result.setException(e);
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onRead(ContentFileProgressImpl contentFile, Long amountCopied)
    {
        // We limit progress notification to one per 10%
        if (listener != null)
        {
            saveProgress(amountCopied);
            listener.onProgressUpdate(this, amountCopied);
        }
        // super.onProgressUpdate(amountCopied);
    }

    protected void saveProgress(long progress)
    {
        if (request.getNotificationUri() != null && request instanceof AbstractSyncUpRequest)
        {
            context.getContentResolver().update(request.getNotificationUri(),
                    ((AbstractSyncUpRequest) request).createContentValues(progress), null, null);
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

    protected Folder retrieveParentFolder()
    {
        if (parentFolder == null && parentFolderIdentifier != null)
        {
            parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                    .getNodeByIdentifier(parentFolderIdentifier);
        }
        return parentFolder;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
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
    
    public Document getDocument()
    {
        return document;
    }
       
}
