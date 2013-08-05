package org.alfresco.mobile.android.application.operations.batch.workflow.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;

import android.content.ContentValues;
import android.database.Cursor;

public class TaskOperationRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    protected String taskIdentifier;
    
    protected Map<String, Serializable> persistentProperties;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public TaskOperationRequest(String taskIdentifier)
    {
        this.taskIdentifier = taskIdentifier;
    }

    public TaskOperationRequest(Cursor cursor)
    {
        super(cursor);
        this.taskIdentifier = cursor.getString(BatchOperationSchema.COLUMN_NODE_ID_ID);
    }
    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getTaskIdentifier()
    {
        return taskIdentifier;
    }

    @Override
    public String getRequestIdentifier()
    {
        return taskIdentifier;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(BatchOperationSchema.COLUMN_NODE_ID, getTaskIdentifier());
        if (persistentProperties != null && !persistentProperties.isEmpty())
        {
            cValues.put(BatchOperationSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        }
        return cValues;
    }
    
    protected Map<String, String> retrievePropertiesMap(Cursor cursor)
    {
        // PROPERTIES
        String rawProperties = cursor.getString(BatchOperationSchema.COLUMN_PROPERTIES_ID);
        if (rawProperties != null)
        {
            return MapUtil.stringToMap(rawProperties);
        }
        else
        {
            return new HashMap<String, String>();
        }
    }
}
