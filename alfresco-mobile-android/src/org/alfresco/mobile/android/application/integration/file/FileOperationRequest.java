package org.alfresco.mobile.android.application.integration.file;

import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;

import android.content.ContentValues;
import android.database.Cursor;

public class FileOperationRequest extends AbstractOperationRequestImpl
{

    private static final long serialVersionUID = 1L;

    protected String filePath;

    public FileOperationRequest(String filePath)
    {
        this.filePath = filePath;
    }

    public String getFilePath()
    {
        return filePath;
    }

    @Override
    public String getRequestIdentifier()
    {
        return filePath;
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_NODE_ID, getFilePath());
        return cValues;
    }

    public FileOperationRequest(Cursor cursor)
    {
        super(cursor);
        // Parent
        this.filePath = cursor.getString(OperationSchema.COLUMN_NODE_ID_ID);
    }
}
