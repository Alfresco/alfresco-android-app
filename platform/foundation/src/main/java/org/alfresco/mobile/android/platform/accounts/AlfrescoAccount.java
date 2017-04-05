/*******************************************************************************
 * Copyright (C) 2005-2017 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.platform.accounts;

import java.io.Serializable;

import org.alfresco.mobile.android.foundation.BuildConfig;

import android.accounts.AccountManager;
import android.text.TextUtils;

public class AlfrescoAccount implements Serializable
{
    private static final long serialVersionUID = 1L;

    // ///////////////////////////////////////////////////////////////////////////
    // REGISTRY
    // ///////////////////////////////////////////////////////////////////////////
    public static final int TYPE_ALFRESCO_CMIS = 2;

    public static final String REPOSITORY_TYPE_ALFRESCO_CMIS = String.valueOf(TYPE_ALFRESCO_CMIS);

    public static final int TYPE_ALFRESCO_CMIS_SAML = 3;

    public static final String REPOSITORY_TYPE_ALFRESCO_CMIS_SAML = String.valueOf(TYPE_ALFRESCO_CMIS_SAML);

    public static final int TYPE_ALFRESCO_CLOUD = 4;

    public static final String REPOSITORY_TYPE_ALFRESCO_CLOUD = String.valueOf(TYPE_ALFRESCO_CLOUD);

    public static final int TYPE_ALFRESCO_TEST_BASIC = 10;

    public static final String REPOSITORY_TYPE_ALFRESCO_TEST_BASIC = String.valueOf(TYPE_ALFRESCO_TEST_BASIC);

    public static final int TYPE_ALFRESCO_TEST_OAUTH = 11;

    public static final String REPOSITORY_TYPE_ALFRESCO_TEST_OAUTH = String.valueOf(TYPE_ALFRESCO_TEST_OAUTH);

    // ///////////////////////////////////////////////////////////////////////////
    // ACCOUNT MANAGER
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Account Manager : Account type
     */
    public static final String ACCOUNT_TYPE = BuildConfig.ACCOUNT_ID;

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Name/Label (Description) of the account
     */
    public static final String ACCOUNT_ID = ACCOUNT_TYPE.concat(".id");

    /**
     * Name/Label (Description) of the account
     */
    public static final String ACCOUNT_NAME = ACCOUNT_TYPE.concat(".name");

    /**
     * Full URL to the CMIS endpoint.
     */
    public static final String ACCOUNT_URL = ACCOUNT_TYPE.concat(".url");

    /**
     * Authentication : Username.
     */
    public static final String ACCOUNT_USERNAME = ACCOUNT_TYPE.concat(".username");

    /**
     * Repository Identifier.
     */
    public static final String ACCOUNT_REPOSITORY_ID = ACCOUNT_TYPE.concat(".repositoryId");

    /**
     * Repository Type Identifier.
     */
    public static final String ACCOUNT_REPOSITORY_TYPE_ID = ACCOUNT_TYPE.concat(".repositoryTypeId");

    /**
     * Activation Info during signup process.
     */
    public static final String ACCOUNT_ACTIVATION = ACCOUNT_TYPE.concat(".activation");

    /**
     * OAuth2 refresh token.
     */
    public static final String ACCOUNT_REFRESH_TOKEN = ACCOUNT_TYPE.concat(".refreshToken");

    /**
     * OAuth2 access token.
     */
    public static final String ACCOUNT_ACCESS_TOKEN = ACCOUNT_TYPE.concat(".accessToken");

    /**
     * Floag to indicate if it's a paid account or not.
     */
    public static final String ACCOUNT_IS_PAID_ACCOUNT = ACCOUNT_TYPE.concat(".isPaidAccount");

    // ///////////////////////////////////////////////////////////////////////////
    // MEMBERS
    // ///////////////////////////////////////////////////////////////////////////
    private long id;

    private String title;

    private String url;

    private String username;

    private String password;

    private String repositoryId;

    private int typeId;

    private String activation;

    private String accessToken;

    private String refreshToken;

    private boolean isPaidAccount;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AlfrescoAccount()
    {
    }

    public static AlfrescoAccount parse(AccountManager mAccountManager, android.accounts.Account account)
    {
        AlfrescoAccount acc = new AlfrescoAccount();
        acc.id = Long.parseLong(mAccountManager.getUserData(account, ACCOUNT_ID));
        acc.title = mAccountManager.getUserData(account, ACCOUNT_NAME);
        acc.url = mAccountManager.getUserData(account, ACCOUNT_URL);
        acc.username = mAccountManager.getUserData(account, ACCOUNT_USERNAME);
        acc.password = mAccountManager.getPassword(account);
        acc.repositoryId = mAccountManager.getUserData(account, ACCOUNT_REPOSITORY_ID);
        acc.typeId = Integer.parseInt(mAccountManager.getUserData(account, ACCOUNT_REPOSITORY_TYPE_ID));
        acc.activation = mAccountManager.getUserData(account, ACCOUNT_ACTIVATION);
        acc.accessToken = mAccountManager.getUserData(account, ACCOUNT_ACCESS_TOKEN);
        acc.refreshToken = mAccountManager.getUserData(account, ACCOUNT_REFRESH_TOKEN);
        acc.isPaidAccount = Boolean.parseBoolean(mAccountManager.getUserData(account, ACCOUNT_IS_PAID_ACCOUNT));
        return acc;
    }

    public AlfrescoAccount(String url, String username)
    {
        super();
        this.url = url;
        this.username = username;
    }

    /** Create a Alfresco account. */
    public AlfrescoAccount(long id, String accountLabel, String url, String username, String password,
            String repositoryId, String typeId, String activation, String accessToken, String refreshToken,
            String isPaidAccount)
    {
        super();
        this.id = id;
        this.title = accountLabel;
        this.url = url;
        this.username = username;
        this.password = password;
        this.repositoryId = repositoryId;
        if (!TextUtils.isEmpty(typeId))
        {
            this.typeId = Integer.parseInt(typeId);
        }
        this.activation = activation;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        if (!TextUtils.isEmpty(isPaidAccount))
        {
            this.isPaidAccount = Boolean.parseBoolean(isPaidAccount);
        }
    }

    /** Create an OnPremise Alfresco account. */
    public AlfrescoAccount(long id, String accountLabel, String url, String username, String password,
            String repositoryId, String typeId, String activation, String isPaidAccount)
    {
        this(id, accountLabel, url, username, password, repositoryId, typeId, activation, typeId, activation,
                isPaidAccount);
    }

    /** Create a Cloud Alfresco account. */
    public AlfrescoAccount(long id, String accountLabel, String url, String username, String repositoryId,
            String typeId, String activation, String accessToken, String refreshToken, String isPaidAccount)
    {
        this(id, accountLabel, url, username, null, repositoryId, typeId, activation, typeId, activation, isPaidAccount);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
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

    public String getTitle()
    {
        return title;
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
        return isPaidAccount;
    }
}
