package org.alfresco.mobile.android.application.integration.node.favorite;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.integration.node.NodeOperationRequest;

import android.database.Cursor;

public class FavoriteNodeRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;
    
    public static final int TYPE_ID = 60;
    
    private Boolean markFavorite;

    public FavoriteNodeRequest(String parentIdentifier, String documentIdentifier)
    {
        super(parentIdentifier, documentIdentifier);
        requestTypeId = TYPE_ID;
    }
    
    
    public FavoriteNodeRequest(Folder parent, Node node)
    {
        this(parent.getIdentifier(), node.getIdentifier());
        setNotificationTitle(node.getName());
        setMimeType(node.getName());
    }
    
    public FavoriteNodeRequest(String parentIdentifier, String documentIdentifier, boolean markFavorite)
    {
        this(parentIdentifier, documentIdentifier);
        this.markFavorite = markFavorite;
    }
    
    public FavoriteNodeRequest(Folder parent, Node node, boolean markFavorite)
    {
        this(parent.getIdentifier(), node.getIdentifier(), markFavorite);
        setNotificationTitle(node.getName());
        setMimeType(node.getName());
    }

    public FavoriteNodeRequest(Cursor cursor)
    {
        super(cursor);
    }


    public Boolean markAsFavorite()
    {
        return markFavorite;
    }
}
