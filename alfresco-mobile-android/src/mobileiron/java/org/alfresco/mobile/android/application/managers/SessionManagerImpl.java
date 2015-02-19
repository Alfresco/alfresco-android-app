/*******************************************************************************
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.managers;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.services.impl.cloud.CloudServiceRegistry;
import org.alfresco.mobile.android.api.services.impl.onpremise.AlfrescoOnPremiseServiceRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuth2DataImpl;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoSessionSettings;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.network.MobileIronHttpInvoker;
import org.alfresco.mobile.android.platform.network.NetworkTrustManager;
import org.apache.chemistry.opencmis.commons.SessionParameter;

import android.content.Context;
import android.text.TextUtils;

import com.squareup.otto.Subscribe;

public class SessionManagerImpl extends SessionManager
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static SessionManager getInstance(Context context)
    {


        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new SessionManagerImpl(context);
            }

            return (SessionManager) mInstance;
        }
    }

    protected SessionManagerImpl(Context context)
    {
        super(context);
    }


    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onSessionRequested(RequestSessionEvent event)
    {
       super.onSessionRequested(event);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SETTINGS
    // ///////////////////////////////////////////////////////////////////////////
    public AlfrescoSessionSettings prepareSettings(AlfrescoAccount acc)
    {
        return new SessionSettingsBuilder(appContext).prepare(acc).build();
    }

    public AlfrescoSessionSettings prepareSettings(AlfrescoAccount acc, OAuthData data)
    {
        return new SessionSettingsBuilder(appContext).prepare(acc).build();
    }

    public AlfrescoSessionSettings prepareSettings(String baseUrl, String username, String password)
    {
        return new SessionSettingsBuilder(appContext).prepare(baseUrl, username, password).build();
    }

    public AlfrescoSessionSettings prepareSettings(OAuthData oauthData)
    {
        return new SessionSettingsBuilder(appContext).prepare(oauthData).build();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    protected static class SessionSettingsBuilder
    {
        protected static final String ONPREMISE_TRUSTMANAGER_CLASSNAME = "org.alfresco.mobile.binding.internal.https.trustmanager";

        protected WeakReference<Context> contextRef;

        protected Map<String, Serializable> extraSettings = new HashMap<String, Serializable>();

        protected String baseUrl;

        protected String username;

        protected String password;

        protected OAuthData oAuthData;

        protected boolean isCloud = false;

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTOR
        // ///////////////////////////////////////////////////////////////////////////
        public SessionSettingsBuilder(Context context)
        {
            this.contextRef = new WeakReference<Context>(context.getApplicationContext());
            prepareCommonSettings();
        }

        // ///////////////////////////////////////////////////////////////////////////
        // PREPARE
        // ///////////////////////////////////////////////////////////////////////////
        public AlfrescoSessionSettings build()
        {
            if (isCloud)
            {
                return new AlfrescoSessionSettings(oAuthData, extraSettings, true);
            }
            else
            {
                return new AlfrescoSessionSettings(baseUrl, username, password, extraSettings);
            }
        }

        // ///////////////////////////////////////////////////////////////////////////
        // OPERATION
        // ///////////////////////////////////////////////////////////////////////////
        public SessionSettingsBuilder prepare(AlfrescoAccount acc)
        {
            prepareData(acc);
            return this;
        }

        // TODO Implement it !
        public SessionSettingsBuilder prepare(OAuthData oauthData)
        {
            this.isCloud = true;
            this.oAuthData = oauthData;
            return this;
        }

        public SessionSettingsBuilder prepare(AlfrescoAccount acc, OAuthData oauthData)
        {
            this.isCloud = true;
            this.oAuthData = oauthData;
            prepareData(acc);
            return this;
        }

        public SessionSettingsBuilder prepare(String baseUrl, String username, String password)
        {
            this.isCloud = false;
            this.baseUrl = baseUrl;
            this.username = username;
            this.password = password;
            return this;
        }

        public SessionSettingsBuilder add(Map<String, Serializable> settings)
        {
            if (settings != null && !settings.isEmpty())
            {
                extraSettings.putAll(settings);
            }
            return this;
        }

        public SessionSettingsBuilder add(String key, Serializable value)
        {
            if (!TextUtils.isEmpty(key) && value != null)
            {
                extraSettings.put(key, value);
            }
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // GENERATE BUNDLE
        // ///////////////////////////////////////////////////////////////////////////
        protected void prepareData(AlfrescoAccount acc)
        {
            switch ((int) acc.getTypeId())
            {
                case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                    isCloud = true;
                    baseUrl = acc.getUrl();
                    oAuthData = new OAuth2DataImpl(getContext().getString(org.alfresco.mobile.android.foundation.R.string.oauth_api_key), getContext()
                            .getString(org.alfresco.mobile.android.foundation.R.string.oauth_api_secret), acc.getAccessToken(), acc.getRefreshToken());
                    prepareCloudSettings(acc.getRepositoryId());
                    break;
                case AlfrescoAccount.TYPE_ALFRESCO_TEST_BASIC:
                case AlfrescoAccount.TYPE_ALFRESCO_CMIS:
                    isCloud = false;
                    baseUrl = acc.getUrl();
                    username = acc.getUsername();
                    password = acc.getPassword();
                    prepareConfigurationSettings(acc);
                    prepareSSLSettings();
                    break;
                default:
                    break;
            }
        }

        protected void prepareConfigurationSettings(AlfrescoAccount acc)
        {
            extraSettings.put(ConfigService.CONFIGURATION_INIT, ConfigService.CONFIGURATION_INIT_DEFAULT);
            extraSettings.put(ConfigService.CONFIGURATION_FOLDER, AlfrescoStorageManager.getInstance(getContext())
                    .getConfigurationFolder(acc).getPath());
        }

        protected void prepareCommonSettings()
        {
            // Default settings for Alfresco Application
            extraSettings.put(AlfrescoSession.ONPREMISE_SERVICES_CLASSNAME,
                    AlfrescoOnPremiseServiceRegistry.class.getCanonicalName());
            extraSettings.put(SessionParameter.CONNECT_TIMEOUT, "10000");
            extraSettings.put(SessionParameter.READ_TIMEOUT, "60000");
            extraSettings.put(AlfrescoSession.EXTRACT_METADATA, true);
            extraSettings.put(AlfrescoSession.CREATE_THUMBNAIL, true);
            extraSettings.put(AlfrescoSession.HTTP_CHUNK_TRANSFERT, "true");
            //extraSettings.put(SessionParameter.CLIENT_COMPRESSION, "true");
            extraSettings.put(AlfrescoSession.HTTP_INVOKER_CLASSNAME, MobileIronHttpInvoker.class.getName());
            extraSettings.put(AlfrescoSession.CACHE_FOLDER, AlfrescoStorageManager.getInstance(getContext())
                    .getCacheDir("AlfrescoMobile").getPath());
        }

        protected void prepareCloudSettings(String repositoryId)
        {
            extraSettings.put(AlfrescoSession.CLOUD_SERVICES_CLASSNAME, CloudServiceRegistry.class.getCanonicalName());
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
                    AlfrescoNotificationManager.getInstance(getContext()).showToast(org.alfresco.mobile.android.foundation.R.string.security_ssl_disable);
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
