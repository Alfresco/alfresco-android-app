package org.alfresco.mobile.android.application.integration.node.create;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.integration.node.NodeOperationRequest;
import org.alfresco.mobile.android.application.integration.utils.MapUtil;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.ContentValues;
import android.database.Cursor;

public class CreateFolderRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;
    
    protected String folderName;
    
    private Map<String, Serializable> persistentProperties = new HashMap<String, Serializable>(1);

    private Map<String, Serializable> properties;
    
    public static final int TYPE_ID = 70;

    
    public CreateFolderRequest(Folder parentFolder, String folderName)
    {
        super(parentFolder.getIdentifier(), null);
        this.folderName = folderName;
        requestTypeId = TYPE_ID;

        properties = new HashMap<String, Serializable>(2);
        properties.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.FOLDER_BASETYPE_ID);
        
        persistentProperties.put(ContentModel.PROP_NAME, folderName);
        persistentProperties.putAll(properties);
        
        setNotificationTitle(folderName);
        setMimeType(ContentModel.TYPE_FOLDER);
    }
    
    public CreateFolderRequest(Cursor cursor)
    {
        super(cursor);
        
        // PROPERTIES
        String rawProperties = cursor.getString(OperationSchema.COLUMN_PROPERTIES_ID);
        Map<String, String> tmpProperties = MapUtil.stringToMap(rawProperties);

        this.folderName = "";
        if (tmpProperties.containsKey(ContentModel.PROP_NAME))
        {
            this.folderName = tmpProperties.remove(ContentModel.PROP_NAME);
        }
        Map<String, Serializable> finalProperties = new HashMap<String, Serializable>(tmpProperties);
        this.properties = finalProperties;
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_PROPERTIES, persistentProperties.toString());
        return cValues;
    }
    
    public String getFolderName()
    {
        return folderName;
    }
    
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }
}
