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
import java.util.Map;

import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.SamlData;

public class AlfrescoSessionSettings
{
    public final boolean isCloud;

    public final boolean requestNewOAuthToken;

    public final Map<String, Serializable> extraSettings;

    public final String baseUrl;

    public final String username;

    public final String password;

    public final OAuthData oAuthData;

    public final SamlData samlData;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AlfrescoSessionSettings(OAuthData oAuthData, Map<String, Serializable> extraSettings,
            boolean requestNewOAuthToken)
    {
        super();
        this.baseUrl = null;
        this.isCloud = true;
        this.extraSettings = extraSettings;
        this.username = null;
        this.password = null;
        this.oAuthData = oAuthData;
        this.requestNewOAuthToken = requestNewOAuthToken;
        this.samlData = null;
    }

    public AlfrescoSessionSettings(String baseUrl, OAuthData oAuthData, Map<String, Serializable> extraSettings,
            boolean requestNewOAuthToken)
    {
        super();
        this.baseUrl = baseUrl;
        this.isCloud = true;
        this.extraSettings = extraSettings;
        this.username = null;
        this.password = null;
        this.oAuthData = oAuthData;
        this.requestNewOAuthToken = requestNewOAuthToken;
        this.samlData = null;
    }

    public AlfrescoSessionSettings(String baseUrl, String username, String password,
            Map<String, Serializable> extraSettings)
    {
        super();
        this.baseUrl = baseUrl;
        this.isCloud = false;
        this.extraSettings = extraSettings;
        this.username = username;
        this.password = password;
        this.oAuthData = null;
        this.requestNewOAuthToken = false;
        this.samlData = null;
    }

    public AlfrescoSessionSettings(String baseUrl, SamlData data, Map<String, Serializable> extraSettings)
    {
        super();
        this.baseUrl = baseUrl;
        this.isCloud = false;
        this.extraSettings = extraSettings;
        this.username = data.getUserId();
        this.password = data.getTicket();
        this.oAuthData = null;
        this.requestNewOAuthToken = false;
        this.samlData = data;
    }
}
