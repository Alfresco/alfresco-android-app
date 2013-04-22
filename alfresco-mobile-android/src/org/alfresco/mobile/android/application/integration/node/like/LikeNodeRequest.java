package org.alfresco.mobile.android.application.integration.node.like;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.integration.node.NodeOperationRequest;

import android.database.Cursor;

public class LikeNodeRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 50;

    private Boolean like;

    public LikeNodeRequest(String parentIdentifier, String documentIdentifier)
    {
        super(parentIdentifier, documentIdentifier);
        requestTypeId = TYPE_ID;
    }

    public LikeNodeRequest(Folder parent, Node node)
    {
        this(parent.getIdentifier(), node.getIdentifier());
        setNotificationTitle(node.getName());
        setMimeType(node.getName());
    }

    public LikeNodeRequest(String parentIdentifier, String documentIdentifier, boolean doLike)
    {
        this(parentIdentifier, documentIdentifier);
        this.like = doLike;
    }

    public LikeNodeRequest(Folder parent, Node node, boolean doLike)
    {
        this(parent.getIdentifier(), node.getIdentifier(), doLike);
        setNotificationTitle(node.getName());
        setMimeType(node.getName());
    }

    public LikeNodeRequest(Cursor cursor)
    {
        super(cursor);
    }

    public Boolean getLikeOperation()
    {
        return like;
    }
}
