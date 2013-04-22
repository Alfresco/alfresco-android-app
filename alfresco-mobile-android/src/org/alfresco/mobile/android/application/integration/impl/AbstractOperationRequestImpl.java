package org.alfresco.mobile.android.application.integration.impl;


import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.OperationSchema;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public abstract class AbstractOperationRequestImpl implements OperationRequest
{
    private static final long serialVersionUID = 1L;

    private long accountId;
    
    private String networkId;

    private int notificationVisibility = VISIBILITY_NOTIFICATIONS;

    private String title;

    private String description;

    private String mimeType;

    protected int requestTypeId;

    private Uri notificationUri;

    public AbstractOperationRequestImpl()
    {
    }
    
    public AbstractOperationRequestImpl(Cursor cursor){
        this.accountId = cursor.getLong(OperationSchema.COLUMN_ACCOUNT_ID_ID);
        this.notificationVisibility = cursor.getInt(OperationSchema.COLUMN_NOTIFICATION_VISIBILITY_ID);
        this.title = cursor.getString(OperationSchema.COLUMN_NOTIFICATION_TITLE_ID);
        this.mimeType = cursor.getString(OperationSchema.COLUMN_MIMETYPE_ID);
        this.requestTypeId = cursor.getInt(OperationSchema.COLUMN_REQUEST_TYPE_ID);
    }

    public abstract String getRequestIdentifier();

    public int getNotificationVisibility()
    {
        return notificationVisibility;
    }

    public String getNotificationTitle()
    {
        return title;
    }

    public String getNotificationDescription()
    {
        return description;
    }

    public OperationRequest setNotificationVisibility(int visibility)
    {
        this.notificationVisibility = visibility;
        return this;
    }

    public OperationRequest setNotificationTitle(String title)
    {
        this.title = title;
        return this;
    }

    public OperationRequest setNotificationDescription(String description)
    {
        this.description = description;
        return this;
    }

    public long getAccountId()
    {
        return accountId;
    }

    public OperationRequest setAccountId(long accountId)
    {
        this.accountId = accountId;
        return this;
    }
    
    public String getNetworkId()
    {
        return networkId;
    }

    public OperationRequest setNetworkId(String networkId)
    {
        this.networkId = networkId;
        return this;
    }


    public int getTypeId()
    {
        return requestTypeId;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    //TODO MimetypeManager Content Resolver !
    //We user filename for the moment instead of real mimetype
    public OperationRequest setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
        return this;
    }

    public Uri getNotificationUri()
    {
        return notificationUri;
    }

    public void setNotificationUri(Uri notificationUri)
    {
        this.notificationUri = notificationUri;
    }

    public ContentValues createContentValues(int status)
    {
        ContentValues cValues = OperationSchema.createDefaultContentValues();
        cValues.put(OperationSchema.COLUMN_NOTIFICATION_VISIBILITY, getNotificationVisibility());
        cValues.put(OperationSchema.COLUMN_ACCOUNT_ID, getAccountId());
        cValues.put(OperationSchema.COLUMN_NOTIFICATION_TITLE, getNotificationTitle());
        cValues.put(OperationSchema.COLUMN_MIMETYPE, getMimeType());
        cValues.put(OperationSchema.COLUMN_REQUEST_TYPE, getTypeId());
        cValues.put(OperationSchema.COLUMN_REASON, -1);
        cValues.put(OperationSchema.COLUMN_STATUS, status);
        return cValues;
    }

}
