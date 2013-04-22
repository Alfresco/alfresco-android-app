package org.alfresco.mobile.android.application.integration.node.download;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;
import org.alfresco.mobile.android.application.integration.node.NodeOperationRequest;
import org.alfresco.mobile.android.application.integration.utils.MapUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class DownloadRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 10;

    private long contentStreamLength;

    public DownloadRequest(String parentIdentifier, String documentIdentifier)
    {
        super(parentIdentifier, documentIdentifier);
        requestTypeId = TYPE_ID;
    }

    public DownloadRequest(Folder folder, Document document)
    {
        this(folder.getIdentifier(), document.getIdentifier());
        
        this.contentStreamLength = document.getContentStreamLength();
        
        setNotificationTitle(document.getName());
        setMimeType(document.getName());
    }

    public long getContentStreamLength()
    {
        return contentStreamLength;
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_TOTAL_SIZE_BYTES, getContentStreamLength());
        if (status != Operation.STATUS_RUNNING)
        {
            cValues.put(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, -1);
        }
        return cValues;
    }

    public ContentValues createContentValues(long downloaded)
    {
        ContentValues cValues = super.createContentValues(Operation.STATUS_RUNNING);
        cValues.put(OperationSchema.COLUMN_TOTAL_SIZE_BYTES, getContentStreamLength());
        cValues.put(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR, downloaded);
        return cValues;
    }

    public static AbstractOperationRequestImpl fromCursor(Context mAppContext, Cursor cursor)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public DownloadRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
        contentStreamLength = cursor.getLong(OperationSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
    }
}
