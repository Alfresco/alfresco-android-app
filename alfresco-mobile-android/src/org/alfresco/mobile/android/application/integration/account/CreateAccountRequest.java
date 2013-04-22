package org.alfresco.mobile.android.application.integration.account;

import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;

public class CreateAccountRequest extends AbstractOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 105;

    public static final String SESSION_MIME = "AlfrescoSession";

    protected String baseUrl;

    protected String username;

    protected String password;

    protected String description;

    protected OAuthData data;

    public CreateAccountRequest()
    {
        super();
        requestTypeId = TYPE_ID;

        setMimeType(SESSION_MIME);
    }

    public CreateAccountRequest(String url, String username, String password, String description)
    {
        this();
        this.baseUrl = url;
        this.username = username;
        this.password = password;
        this.description = description;
        setNotificationTitle(description);
    }

    public CreateAccountRequest(OAuthData data)
    {
        this();
        this.data = data;
        setNotificationTitle("Cloud");
    }

    @Override
    public String getRequestIdentifier()
    {
        return baseUrl + "@" + username;
    }
    

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getDescription()
    {
        return description;
    }

    public OAuthData getData()
    {
        return data;
    }

}
