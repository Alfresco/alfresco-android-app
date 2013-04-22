package org.alfresco.mobile.android.application.integration.node;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationThread;

import android.content.Context;

public abstract class NodeOperationThread<T> extends AbstractOperationThread<T>
{
    protected Node node;
    
    protected String nodeIdentifier;
    
    public NodeOperationThread(Context context, OperationRequest request)
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
    
    protected LoaderResult<T> doInBackground()
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
