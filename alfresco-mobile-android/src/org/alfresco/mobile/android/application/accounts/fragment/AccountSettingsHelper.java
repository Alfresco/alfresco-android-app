/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.accounts.fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.network.NetworkHttpInvoker;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuth2DataImpl;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.apache.chemistry.opencmis.commons.SessionParameter;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

public class AccountSettingsHelper
{

    public static final boolean ENABLE_CONFIG_FILE = true;

    public static final String OAUTH_URL = "oauth_url";

    public static final String OAUTH_API_KEY = "apikey";

    public static final String OAUTH_API_SECRET = "apisecret";

    private static final String BASE_URL = "org.alfresco.mobile.binding.internal.baseurl";

    private static final String USER = "org.alfresco.mobile.internal.credential.user";

    private static final String PASSWORD = "org.alfresco.mobile.internal.credential.password";

    private static final String CLOUD_BASIC_AUTH = "org.alfresco.mobile.binding.internal.cloud.basic";

    private static final String APP_CONFIG_PATH = Environment.getExternalStorageDirectory().getPath()
            + "/alfresco-mobile/app-config.properties";

    private static final String ONPREMISE_TRUSTMANAGER_CLASSNAME = "org.alfresco.mobile.binding.internal.https.trustmanager";

    private static final String ALFRESCO_CLOUD_URL = "http://my.alfresco.com";

    private Context context;

    private String baseUrl;

    private String username;

    private String password;

    private OAuthData data;

    private Account acc;

    private boolean isCloud = false;

    private boolean doesRequestNewToken = true;

    private String repositoryId;

    public AccountSettingsHelper(Context context, Account acc)
    {
        this.context = context;
        this.acc = acc;
        prepareData();
    }

    public AccountSettingsHelper(Context context, Account acc, OAuthData data)
    {
        this.context = context;
        this.acc = acc;
        prepareData();
        if (data != null)
        {
            this.data = data;
        }
    }

    public AccountSettingsHelper(Context context, String baseUrl, String username, String password, OAuthData data)
    {
        super();
        this.context = context;
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.data = data;
    }

    public static Bundle getOAuthSettings()
    {
        Bundle b = null;
        // Check Properties available inside the device
        if (ENABLE_CONFIG_FILE)
        {
            b = new Bundle();
            File f = new File(APP_CONFIG_PATH);
            if (f.exists())
            {
                Properties prop = new Properties();
                InputStream is = null;
                try
                {
                    is = new FileInputStream(f);
                    // load a properties file
                    prop.load(is);
                    b.putString(OAUTH_URL, prop.getProperty(OAUTH_URL));
                    b.putString(OAUTH_API_KEY, prop.getProperty(OAUTH_API_KEY));
                    b.putString(OAUTH_API_SECRET, prop.getProperty(OAUTH_API_SECRET));
                }
                catch (IOException ex)
                {
                    throw new AlfrescoServiceException(ErrorCodeRegistry.PARSING_GENERIC, ex);
                }
                finally
                {
                    IOUtils.closeStream(is);
                }
            }
        }
        return b;
    }

    public boolean isCloud()
    {
        if (acc != null)
        {
            return isCloud;
        }
        else
        {
            return (data != null || (baseUrl != null && baseUrl.startsWith(ALFRESCO_CLOUD_URL)));
        }
    }

    private void prepareData()
    {
        switch ((int) acc.getTypeId())
        {
            case Account.TYPE_ALFRESCO_TEST_OAUTH:
                isCloud = true;
                String apikey = null,
                apisecret = null;
                File f = new File(APP_CONFIG_PATH);
                if (f.exists() && ENABLE_CONFIG_FILE)
                {
                    Properties prop = new Properties();
                    InputStream is = null;
                    try
                    {
                        is = new FileInputStream(f);
                        // load a properties file
                        prop.load(is);
                        apikey = prop.getProperty("apikey");
                        apisecret = prop.getProperty("apisecret");
                    }
                    catch (IOException ex)
                    {
                        throw new AlfrescoServiceException(ErrorCodeRegistry.PARSING_GENERIC, ex);
                    }
                    finally
                    {
                        IOUtils.closeStream(is);
                    }
                }

                baseUrl = acc.getUrl();
                data = new OAuth2DataImpl(apikey, apisecret, acc.getAccessToken(), acc.getRefreshToken());
                break;
            case Account.TYPE_ALFRESCO_CLOUD:
                isCloud = true;
                baseUrl = acc.getUrl();
                data = new OAuth2DataImpl(getText(R.string.oauth_api_key), getText(R.string.oauth_api_secret),
                        acc.getAccessToken(), acc.getRefreshToken());
                repositoryId = acc.getRepositoryId();
                break;

            case Account.TYPE_ALFRESCO_TEST_BASIC:
            case Account.TYPE_ALFRESCO_CMIS:
                isCloud = false;
                baseUrl = acc.getUrl();
                username = acc.getUsername();
                password = acc.getPassword();
                break;
            default:
                break;
        }
    }

    public Map<String, Serializable> prepareCommonSettings()
    {
        // Default settings for Alfresco Application
        HashMap<String, Serializable> settings = new HashMap<String, Serializable>();
        settings.put(SessionParameter.CONNECT_TIMEOUT, "10000");
        settings.put(SessionParameter.READ_TIMEOUT, "60000");
        settings.put(AlfrescoSession.EXTRACT_METADATA, true);
        settings.put(AlfrescoSession.CREATE_THUMBNAIL, true);
        settings.put(SessionParameter.CLIENT_COMPRESSION, "true");
        settings.put(AlfrescoSession.HTTP_INVOKER_CLASSNAME, NetworkHttpInvoker.class.getName());
        settings.put(AlfrescoSession.CACHE_FOLDER, StorageManager.getCacheDir(context, "AlfrescoMobile").getPath());
        return settings;
    }

    public Map<String, Serializable> prepareCloudSettings()
    {
        HashMap<String, Serializable> settings = new HashMap<String, Serializable>();
        // Specific for Test Instance server
        if (isCloud())
        {
            String tmpurl = null, oauthUrl = null;
            // Check Properties available inside the device
            if (ENABLE_CONFIG_FILE)
            {
                File f = new File(APP_CONFIG_PATH);
                if (f.exists() && ENABLE_CONFIG_FILE)
                {
                    Properties prop = new Properties();
                    InputStream is = null;
                    try
                    {
                        is = new FileInputStream(f);
                        // load a properties file
                        prop.load(is);
                        tmpurl = prop.getProperty("url");
                        oauthUrl = prop.getProperty("oauth_url");
                    }
                    catch (IOException ex)
                    {
                        throw new AlfrescoServiceException(ErrorCodeRegistry.PARSING_GENERIC, ex);
                    }
                    finally
                    {
                        IOUtils.closeStream(is);
                    }
                }
            }

            if (tmpurl != null && (baseUrl != null && baseUrl.startsWith(ALFRESCO_CLOUD_URL)))
            {
                settings.put(CLOUD_BASIC_AUTH, true);
                settings.put(USER, username);
                settings.put(PASSWORD, password);
                settings.put(BASE_URL, tmpurl);
                doesRequestNewToken = false;
            }
            else if (oauthUrl != null && !oauthUrl.isEmpty())
            {
                settings.put(BASE_URL, ((baseUrl == null) ? oauthUrl : baseUrl));
            }

            if (repositoryId != null && !repositoryId.isEmpty())
            {
                settings.put(CloudSession.CLOUD_NETWORK_ID, repositoryId);
            }
            
            settings.put(AlfrescoSession.HTTP_ACCEPT_ENCODING, "false");
            settings.put(AlfrescoSession.HTTP_CHUNK_TRANSFERT, "true");
        }
        return settings;
    }

    private String getText(int resId)
    {
        return context.getText(resId).toString();
    }

    public Context getContext()
    {
        return context;
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

    public OAuthData getData()
    {
        return data;
    }

    public Account getAcc()
    {
        return acc;
    }

    public boolean getNewToken()
    {
        return doesRequestNewToken;
    }

    public Map<String, Serializable> prepareSSLSettings()
    {
        Map<String, Serializable> settings = new HashMap<String, Serializable>(1);
        // ssl certificate
        try
        {
            URI url = new URI(baseUrl);
            File f = StorageManager.getFileInPrivateFolder(context, url.getHost() + ".properties");
            if (f.exists() && f.isFile())
            {
                MessengerManager.showToast(context, R.string.security_ssl_disable);
                settings.put(ONPREMISE_TRUSTMANAGER_CLASSNAME,
                        "org.alfresco.mobile.android.application.security.AlfrescoTrustManager");
            }
            settings.put(ONPREMISE_TRUSTMANAGER_CLASSNAME,
                    "org.alfresco.mobile.android.application.security.AlfrescoTrustManager");
        }
        catch (Exception e)
        {
            // Nothing special
        }

        return settings;
    }
}
