package org.alfresco.mobile.android.application.integration.node;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationTask;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.content.Context;

public abstract class NodeOperationTask<T> extends AbstractOperationTask<T>
{
    protected Node node;
    
    protected String nodeIdentifier;

    public NodeOperationTask(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof NodeOperationRequest)
        {
            this.accountId = ((AbstractOperationRequestImpl) request).getAccountId();
            this.nodeIdentifier = ((NodeOperationRequest) request).getNodeIdentifier();
        }
    }
    
    public Node getNode()
    {
        return node;
    }

    protected LoaderResult<T> doInBackground(Void... params)
    {
        try
        {
            session = requestSession();
            node = session.getServiceRegistry().getDocumentFolderService().getNodeByIdentifier(nodeIdentifier);
            if (listener != null)
            {
                listener.onPreExecute(this);
            }
        }
        catch (Exception e)
        {
        }
        return new LoaderResult<T>();
    }
}
