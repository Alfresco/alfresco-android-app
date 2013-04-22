package org.alfresco.mobile.android.application.integration.node.delete;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.integration.node.NodeOperationRequest;

import android.database.Cursor;

public class DeleteNodeRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;
    
    public static final int TYPE_ID = 40;

    public DeleteNodeRequest(Folder parent, Node node)
    {
        super(parent.getIdentifier(), node.getIdentifier());
        requestTypeId = TYPE_ID;
        
        setNotificationTitle(node.getName());
        setMimeType(node.getName());
    }

    public DeleteNodeRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
    }
}
