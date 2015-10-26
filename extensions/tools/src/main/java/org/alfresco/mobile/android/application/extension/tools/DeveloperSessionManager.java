/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.extension.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuth2DataImpl;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoSessionSettings;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.network.NetworkTrustManager;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

public class DeveloperSessionManager extends SessionManager
{
    public static final String OAUTH_URL = "oauth_url";

    public static final String OAUTH_API_KEY = "apikey";

    public static final String OAUTH_API_SECRET = "apisecret";

    private static final String APP_CONFIG_PATH = Environment.getExternalStorageDirectory().getPath()
            + "/alfresco-mobile/app-config.properties";

    private static final String BASE_URL = "org.alfresco.mobile.binding.internal.baseurl";

    private static final String USER = "org.alfresco.mobile.internal.credential.user";

    private static final String PASSWORD = "org.alfresco.mobile.internal.credential.password";

    private static final String CLOUD_BASIC_AUTH = "org.alfresco.mobile.binding.internal.cloud.basic";

    private static final String ALFRESCO_CLOUD_URL = "http://my.alfresco.com";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public static DeveloperSessionManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new DeveloperSessionManager(context);
            }

            return (DeveloperSessionManager) mInstance;
        }
    }

    protected DeveloperSessionManager(Context context)
    {
        super(context);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SETTINGS
    // ///////////////////////////////////////////////////////////////////////////
    public AlfrescoSessionSettings prepareSettings(AlfrescoAccount acc)
    {
        return new SettingsBuilder(appContext).prepare(acc).build();
    }

    public AlfrescoSessionSettings prepareSettings(AlfrescoAccount acc, OAuthData data)
    {
        return new SettingsBuilder(appContext).prepare(acc).build();
    }

    public AlfrescoSessionSettings prepareSettings(String baseUrl, String username, String password)
    {
        return new SettingsBuilder(appContext).prepare(baseUrl, username, password).build();
    }

    public AlfrescoSessionSettings prepareSettings(OAuthData oauthData)
    {
        return new SettingsBuilder(appContext).prepare(oauthData).build();
    }

    @Override
    public Bundle getOAuthSettings()
    {
        Bundle b;
        // Check Properties available inside the device
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
        return b;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class DevSettingsBuilder extends SettingsBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTOR
        // ///////////////////////////////////////////////////////////////////////////
        public DevSettingsBuilder(Context context)
        {
            super(context);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // OPERATION
        // ///////////////////////////////////////////////////////////////////////////
        public SettingsBuilder prepare(AlfrescoAccount acc)
        {
            prepareData(acc);
            return this;
        }

        public SettingsBuilder prepare(OAuthData oauthData)
        {
            this.isCloud = true;
            this.oAuthData = oauthData;
            return this;
        }

        public SettingsBuilder prepare(AlfrescoAccount acc, OAuthData oauthData)
        {
            this.isCloud = true;
            this.oAuthData = oauthData;
            prepareData(acc);
            return this;
        }

        public SettingsBuilder prepare(String baseUrl, String username, String password)
        {
            this.isCloud = false;
            this.baseUrl = baseUrl;
            this.username = username;
            this.password = password;
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // GENERATE BUNDLE
        // ///////////////////////////////////////////////////////////////////////////
        protected void prepareData(AlfrescoAccount acc)
        {
            switch (acc.getTypeId())
            {
                case AlfrescoAccount.TYPE_ALFRESCO_TEST_OAUTH:
                    isCloud = true;
                    String apikey = null,
                    apisecret = null;
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
                            apikey = prop.getProperty(OAUTH_API_KEY);
                            apisecret = prop.getProperty(OAUTH_API_SECRET);
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
                    oAuthData = new OAuth2DataImpl(apikey, apisecret, acc.getAccessToken(), acc.getRefreshToken());
                    break;
                default:
                    super.prepareData(acc);
                    break;
            }
        }

        protected void prepareCloudSettings(String repositoryId)
        {
            String tmpurl = null;
            // Check Properties available inside the device
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
                    tmpurl = prop.getProperty("url");
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

            if (tmpurl != null && (baseUrl != null && baseUrl.startsWith(ALFRESCO_CLOUD_URL)))
            {
                extraSettings.put(CLOUD_BASIC_AUTH, true);
                extraSettings.put(USER, username);
                extraSettings.put(PASSWORD, password);
                extraSettings.put(BASE_URL, tmpurl);
            }

            if (repositoryId != null && !repositoryId.isEmpty())
            {
                extraSettings.put(CloudSession.CLOUD_NETWORK_ID, repositoryId);
            }
            extraSettings.put(AlfrescoSession.HTTP_ACCEPT_ENCODING, "false");
            extraSettings.put(AlfrescoSession.HTTP_CHUNK_TRANSFERT, "true");
        }

        protected void prepareSSLSettings()
        {
            // ssl certificate
            try
            {
                URI url = new URI(baseUrl);
                File f = AlfrescoStorageManager.getInstance(getContext()).getFileInPrivateFolder(
                        url.getHost() + ".properties");
                if (f.exists() && f.isFile())
                {
                    AlfrescoNotificationManager.getInstance(getContext()).showToast(R.string.security_ssl_disable);
                    extraSettings.put(ONPREMISE_TRUSTMANAGER_CLASSNAME, NetworkTrustManager.class.getName());
                }
            }
            catch (Exception e)
            {
                // Nothing special
            }
        }

        private Context getContext()
        {
            return contextRef.get();
        }
    }

}
