/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.account;

import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;

public class CreateAccountRequest extends AbstractBatchOperationRequestImpl
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
