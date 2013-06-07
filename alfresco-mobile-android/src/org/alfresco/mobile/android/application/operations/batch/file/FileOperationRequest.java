package org.alfresco.mobile.android.application.operations.batch.file;

import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;

import android.content.ContentValues;
import android.database.Cursor;

public class FileOperationRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    protected String filePath;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public FileOperationRequest(String filePath)
    {
        this.filePath = filePath;
    }

    public FileOperationRequest(Cursor cursor)
    {
        super(cursor);
    }
    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getFilePath()
    {
        return filePath;
    }

    @Override
    public String getRequestIdentifier()
    {
        return filePath;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(BatchOperationSchema.COLUMN_NODE_ID, getFilePath());
        return cValues;
    }
}
