package org.alfresco.mobile.android.application.integration.account;

import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;

public class LoadSessionRequest extends AbstractOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 100;
    
    public static final String SESSION_MIME = "AlfrescoSession";
    
    private OAuthData data;
    
    public LoadSessionRequest()
    {
        super();
        requestTypeId = TYPE_ID;
        
        setMimeType(SESSION_MIME);
    }
    
    public LoadSessionRequest(OAuthData data)
    {
        this();
        this.data = data;
    }
    
    public OAuthData getData()
    {
        return data;
    }
    
    @Override
    public String getRequestIdentifier()
    {
        return getAccountId() + "";
    }
}
