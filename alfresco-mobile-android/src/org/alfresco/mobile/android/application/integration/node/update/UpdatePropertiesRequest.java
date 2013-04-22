package org.alfresco.mobile.android.application.integration.node.update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.integration.OperationSchema;
import org.alfresco.mobile.android.application.integration.node.NodeOperationRequest;
import org.alfresco.mobile.android.application.integration.utils.MapUtil;

import android.content.ContentValues;
import android.database.Cursor;

public class UpdatePropertiesRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 35;

    private Map<String, Serializable> properties;

    private List<String> tags;
    
    private Map<String, Serializable> persistentProperties;
    
    private static final String PROP_TAG = "tag";

    
    public UpdatePropertiesRequest(Folder parent, Node node,  Map<String, Serializable> properties)
    {
        this(parent.getIdentifier(), node.getIdentifier(), properties, null);
    }
    
    public UpdatePropertiesRequest(String parentFolderIdentifier, String documentIdentifier,  Map<String, Serializable> properties, List<String> tags)
    {
        super(parentFolderIdentifier, documentIdentifier);
        requestTypeId = TYPE_ID;
        
        this.properties = properties;
        this.tags = tags;

        persistentProperties = new HashMap<String, Serializable>();
        if (properties != null)
        {
            persistentProperties.putAll(properties);
        }
        if (tags == null) { return; }
        int i = 0;
        for (String tagValue : tags)
        {
            persistentProperties.put(PROP_TAG + i, tagValue);
            i++;
        }
    }
    
    public List<String> getTags()
    {
        return tags;
    }
    
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }
    
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        cValues.put(OperationSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        return cValues;
    }

    public UpdatePropertiesRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
        
        // PROPERTIES
        String rawProperties = cursor.getString(OperationSchema.COLUMN_PROPERTIES_ID);
        Map<String, String> tmpProperties = MapUtil.stringToMap(rawProperties);

        List<String> tags = new ArrayList<String>();
        List<String> keys = new ArrayList<String>();
        for (Entry<String, String> entry : tmpProperties.entrySet())
        {
            if (entry.getKey().startsWith(PROP_TAG))
            {
                tags.add(entry.getValue());
                keys.add(entry.getKey());
            }
        }

        for (String key : keys)
        {
            tmpProperties.remove(key);
        }

        Map<String, Serializable> finalProperties = new HashMap<String, Serializable>(tmpProperties);
        this.properties = finalProperties;
        this.tags = tags;
    }

}
