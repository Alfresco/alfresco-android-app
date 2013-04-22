package org.alfresco.mobile.android.application.integration.node;


import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;

import android.content.ContentValues;
import android.database.Cursor;

public class NodeOperationRequest extends AbstractOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    protected String nodeIdentifier;
    protected String parentFolderIdentifier;

    public NodeOperationRequest(String parentFolderIdentifier, String documentIdentifier)
    {
        this.nodeIdentifier = documentIdentifier;
        this.parentFolderIdentifier = parentFolderIdentifier;
    }

    public String getNodeIdentifier()
    {
        return nodeIdentifier;
    }
    
    public String getParentFolderIdentifier()
    {
        return parentFolderIdentifier;
    }

    @Override
    public String getRequestIdentifier()
    {
        return nodeIdentifier;
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_NODE_ID, getNodeIdentifier());
        cValues.put(OperationSchema.COLUMN_PARENT_ID, getParentFolderIdentifier());
        return cValues;
    }
    
    public NodeOperationRequest(Cursor cursor){
        super(cursor);
        // Parent
        this.parentFolderIdentifier = cursor.getString(OperationSchema.COLUMN_PARENT_ID_ID);
        this.nodeIdentifier = cursor.getString(OperationSchema.COLUMN_NODE_ID_ID);
    }

}
