package org.alfresco.mobile.android.application.integration.node.update;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.integration.node.AbstractUpRequest;

import android.database.Cursor;

public class UpdateContentRequest extends AbstractUpRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 30;

    public UpdateContentRequest(Folder parentFolder, Document document, ContentFile contentFile)
    {
        super(parentFolder.getIdentifier(), document.getIdentifier(), document.getName(), contentFile.getFile()
                .getPath(), contentFile.getMimeType(), contentFile.getLength());
        requestTypeId = TYPE_ID;
    }

    public UpdateContentRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
    }

    @Override
    public String getRequestIdentifier()
    {
        return nodeIdentifier;
    }
}
