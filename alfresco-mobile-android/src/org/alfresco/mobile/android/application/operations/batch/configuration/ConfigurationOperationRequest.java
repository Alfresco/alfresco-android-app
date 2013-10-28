package org.alfresco.mobile.android.application.operations.batch.configuration;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.mobile.android.application.configuration.ConfigurationManager;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;

import android.content.ContentValues;
import android.database.Cursor;

public class ConfigurationOperationRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 654;

    protected String dataDictionaryIdentifier;
    
    protected String configurationIdentifier;
    
    protected long lastModificationTime;

    private HashMap<String, Serializable> persistentProperties;

    public static final String MIMETYPE = "Configuration";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public ConfigurationOperationRequest()
    {
        super();
        requestTypeId = TYPE_ID;
        setNotificationTitle(getRequestIdentifier());
        setMimeType(MIMETYPE);
    }

    public ConfigurationOperationRequest(String dataDictionaryIdentifier, String configurationIdentifier, long lastModificationTime)
    {
        super();
        requestTypeId = TYPE_ID;
        this.dataDictionaryIdentifier = dataDictionaryIdentifier;
        this.configurationIdentifier = configurationIdentifier;
        this.lastModificationTime = lastModificationTime;
        setNotificationTitle(getRequestIdentifier());
        setMimeType(MIMETYPE);
        save();
    }

    public ConfigurationOperationRequest(Cursor cursor)
    {
        super(cursor);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getRequestIdentifier()
    {
        return ConfigurationManager.DATA_DICTIONNARY_MOBILE_PATH;
    }

    public String getDataDictionaryIdentifier()
    {
        return dataDictionaryIdentifier;
    }
    
    public String getConfigurationIdentifier()
    {
        return configurationIdentifier;
    }
    
    public long getLastModificationTime()
    {
        return lastModificationTime;
    }

    private void save()
    {
        persistentProperties = new HashMap<String, Serializable>();
        persistentProperties.put(IntentIntegrator.EXTRA_CONFIGURATION_ID, configurationIdentifier);
        persistentProperties.put(IntentIntegrator.EXTRA_DATA_DICTIONARY_ID, dataDictionaryIdentifier);
        persistentProperties.put(IntentIntegrator.EXTRA_LASTMODIFICATION, lastModificationTime);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = super.createContentValues(status);
        if (persistentProperties != null && !persistentProperties.isEmpty())
        {
            cValues.put(BatchOperationSchema.COLUMN_PROPERTIES, MapUtil.mapToString(persistentProperties));
        }
        return cValues;
    }
}
