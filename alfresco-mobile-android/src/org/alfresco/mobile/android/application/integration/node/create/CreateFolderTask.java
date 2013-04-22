package org.alfresco.mobile.android.application.integration.node.create;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationTask;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CreateFolderTask extends AbstractOperationTask<Folder>
{
    /** Parent Folder object of the new folder. */
    protected Folder parentFolder;

    protected String parentFolderIdentifier;

    protected String folderName;

    protected Map<String, Serializable> properties;

    protected Folder folder = null;

    public CreateFolderTask(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof CreateFolderRequest)
        {
            this.parentFolderIdentifier = ((CreateFolderRequest) request).getParentFolderIdentifier();
            this.properties = ((CreateFolderRequest) request).getProperties();
            this.folderName = ((CreateFolderRequest) request).getFolderName();
        }
    }

    @Override
    protected LoaderResult<Folder> doInBackground(Void... params)
    {
        LoaderResult<Folder> result = new LoaderResult<Folder>();

        try
        {
            session = SessionUtils.getSession(context, accountId);
            parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                    .getNodeByIdentifier(parentFolderIdentifier);
            
            if (listener != null)
            {
                listener.onPreExecute(this);
            }

            if (parentFolder != null)
            {
                folder = session.getServiceRegistry().getDocumentFolderService()
                        .createFolder(parentFolder, folderName, properties);
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(folder);

        return result;
    }

    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_CREATE_FOLDER_START);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_CREATE_FOLDER_COMPLETE);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_CREATED_FOLDER, folder);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Folder getParentFolder()
    {
        return parentFolder;
    }

    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

}
