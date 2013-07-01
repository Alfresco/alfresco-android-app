/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.accounts;

import java.io.Serializable;

public class Account implements Serializable
{

    public static final int TYPE_ALFRESCO_CMIS = 2;

    public static final int TYPE_ALFRESCO_CLOUD = 4;

    public static final int TYPE_ALFRESCO_TEST_BASIC = 10;

    public static final int TYPE_ALFRESCO_TEST_OAUTH = 11;

    private static final long serialVersionUID = 1L;

    private long id;

    private String description;

    private String url;

    private String username;

    private String password;

    private String repositoryId;

    private int typeId;

    private String activation;

    private String accessToken;

    private String refreshToken;

    private int isPaidAccount;
    
    public Account(long id, String description, String url, String username, String password, String repositoryId,
            int typeId, String activation, String accessToken, String refreshToken, int isPaidAccount)
    {
        super();
        this.id = id;
        this.description = description;
        this.url = url;
        this.username = username;
        this.password = password;
        this.repositoryId = repositoryId;
        this.typeId = typeId;
        this.activation = activation;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.isPaidAccount = isPaidAccount;
    }
    
    public String getAccessToken()
    {
        return accessToken;
    }

    public String getRefreshToken()
    {
        return refreshToken;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getUrl()
    {
        return url;
    }

    public long getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public int getTypeId()
    {
        return typeId;
    }

    public String getActivation()
    {
        return activation;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }
    
    public boolean getIsPaidAccount()
    {
        return (isPaidAccount != 0);
    }
}
