package org.alfresco.mobile.android.application.integration.node.update;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.node.NodeOperationTask;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class UpdatePropertiesTask extends NodeOperationTask<Node>
{
    private static final String TAG = UpdatePropertiesTask.class.getName();

    /** Parent Folder object of the new folder. */
    protected Folder parentFolder;

    protected String parentFolderIdentifier;

    private Node resultNode = null;

    private Map<String, Serializable> properties;

    private List<String> tags;

    public UpdatePropertiesTask(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof UpdatePropertiesRequest)
        {
            this.parentFolderIdentifier = ((UpdatePropertiesRequest) request).getParentFolderIdentifier();
            this.properties = ((UpdatePropertiesRequest) request).getProperties();
            this.tags = ((UpdatePropertiesRequest) request).getTags();
        }
    }

    @Override
    protected LoaderResult<Node> doInBackground(Void... params)
    {
        LoaderResult<Node> result = new LoaderResult<Node>();

        try
        {
            session = requestSession();
            node = session.getServiceRegistry().getDocumentFolderService().getNodeByIdentifier(nodeIdentifier);
            parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                    .getNodeByIdentifier(parentFolderIdentifier);
            if (listener != null)
            {
                listener.onPreExecute(this);
            }

            if (properties != null)
            {
                resultNode = session.getServiceRegistry().getDocumentFolderService().updateProperties(node, properties);
            }

            if (tags != null && !tags.isEmpty())
            {
                session.getServiceRegistry().getTaggingService().addTags(resultNode, tags);
            }
        }
        catch (Exception e)
        {
            result.setException(e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        result.setData(resultNode);

        return result;
    }

    public Folder getParentFolder()
    {
        return parentFolder;
    }

    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_STARTED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_NODE, getNode());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_NODE, node);
        b.putParcelable(IntentIntegrator.EXTRA_UPDATED_NODE, resultNode);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
