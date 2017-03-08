/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
package org.alfresco.mobile.android.platform;

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
import org.alfresco.mobile.android.api.session.authentication.SamlData;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuth2DataImpl;
import org.alfresco.mobile.android.api.session.authentication.impl.Saml2TicketImpl;
import org.alfresco.mobile.android.api.session.authentication.impl.SamlDataImpl;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountCompletedEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountErrorEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadInactiveAccountEvent;
import org.alfresco.mobile.android.async.session.LoadSessionRequest.Builder;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoSessionSettings;
import org.alfresco.mobile.android.platform.exception.AlfrescoExceptionHelper;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.network.NetworkHttpInvoker;
import org.alfresco.mobile.android.platform.network.NetworkTrustManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.apache.chemistry.opencmis.commons.SessionParameter;

import com.squareup.otto.Subscribe;

import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

/**
 * Responsible to manage sessions across activities.
 * 
 * @author Jean Marie Pascal
 */
public abstract class SessionManager extends Manager
{
    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    protected LongSparseArray<AlfrescoSession> sessionIndex = new LongSparseArray<>();

    protected AlfrescoAccount currentAccount;

    protected AlfrescoAccountManager accountManager;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    protected SessionManager(Context applicationContext)
    {
        super(applicationContext);
        accountManager = AlfrescoAccountManager.getInstance(applicationContext);
        EventBusManager.getInstance().register(this);
    }

    public static SessionManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = Manager.getInstance(context, SessionManager.class.getSimpleName());
            }
            return (SessionManager) mInstance;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void shutdown()
    {
        sessionIndex.clear();
        mInstance = null;
        currentAccount = null;
        EventBusManager.getInstance().unregister(this);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onSessionRequested(RequestSessionEvent event)
    {
        if (event.requestReload)
        {
            if (event.networkId != null)
            {
                android.accounts.Account acc = accountManager.getAndroidAccount(event.accountToLoad.getId());
                AccountManager manager = AccountManager.get(appContext);
                manager.setUserData(acc, AlfrescoAccount.ACCOUNT_REPOSITORY_ID, event.networkId);
            }
            createSession(event.accountToLoad);
            return;
        }

        if (event.data != null)
        {
            loadSession(event.accountToLoad, event.data);
        }
        else
        {
            loadSession(event.accountToLoad);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACCOUNT / SESSION MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public void loadSession()
    {
        loadSession(null);
    }

    public void loadSession(AlfrescoAccount acc, OAuthData data)
    {
        if (hasSession(acc.getId()))
        {
            removeAccount(acc.getId());
        }
        Builder request = new Builder(data);
        Operator.with(appContext, acc).load(request);
    }

    public AlfrescoSession loadSession(AlfrescoAccount account)
    {
        AlfrescoSession session = null;
        AlfrescoAccount accountToLoad = getCurrentAccount();

        // First Session Loading
        if (account == null && accountToLoad == null)
        {
            accountToLoad = accountManager.getDefaultAccount();
            SessionManager.getInstance(appContext).saveAccount(accountToLoad);
            if (accountToLoad == null)
            {
                EventBusManager.getInstance().post(new LoadInactiveAccountEvent(null, accountToLoad));
            }
        }
        else if (account != null)
        {
            // User has choose a specific account to load
            accountToLoad = account;
        }

        if (accountToLoad == null) { return null; }

        if (accountToLoad.getActivation() != null)
        {
            // SEND broadcast : account is not active !
            EventBusManager.getInstance().post(new LoadInactiveAccountEvent(null, accountToLoad));
        }

        // Check if Session available for this specific account
        if (hasSession(accountToLoad.getId()))
        {
            session = getSession(accountToLoad.getId());
            EventBusManager.getInstance()
                    .post(new LoadAccountCompletedEvent(LoadAccountCompletedEvent.SWAP, accountToLoad));
        }
        else if (getCurrentAccount() == null || accountToLoad.getId() != getCurrentAccount().getId())
        {
            // Create the session for the specific account
            createSession(accountToLoad);
        }
        else if (getCurrentAccount() != null && !hasSession(getCurrentAccount().getId()))
        {
            // Create the session for the specific account
            createSession(getCurrentAccount());
        }

        // Mark accountId for the specific activity.
        // Help to retrieve session associated to a specific activity
        saveAccount(accountToLoad);

        return session;
    }

    public void createSession(AlfrescoAccount currentAccount)
    {
        // Check Connectivity
        if (!ConnectivityUtils.hasInternetAvailable(appContext))
        {
            EventBusManager.getInstance()
                    .post(new LoadAccountErrorEvent(null, currentAccount, new NetworkErrorException("Not Online"),
                            AlfrescoExceptionHelper.getMessageId(appContext, new NetworkErrorException("Not Online"))));
            return;
        }

        if (hasSession(currentAccount.getId()))
        {
            removeAccount(currentAccount.getId());
            saveAccount(currentAccount);
        }

        Builder request = new Builder();
        Operator.with(appContext, currentAccount).load(request);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACCOUNT / SESSION MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public AlfrescoAccount getCurrentAccount()
    {
        return currentAccount;
    }

    public void saveAccount(AlfrescoAccount currentAccount)
    {
        this.currentAccount = currentAccount;
    }

    public AlfrescoSession getCurrentSession()
    {
        if (currentAccount == null) { return null; }
        return sessionIndex.get(currentAccount.getId());
    }

    public void saveSession(AlfrescoAccount account, AlfrescoSession session)
    {
        sessionIndex.put(account.getId(), session);
    }

    public void saveSession(AlfrescoSession session)
    {
        saveSession(currentAccount, session);
    }

    public void removeAccount(long accountId)
    {
        if (currentAccount != null && currentAccount.getId() == accountId)
        {
            currentAccount = null;
        }
        sessionIndex.remove(accountId);
    }

    public boolean hasSession(Long accountId)
    {
        return sessionIndex.get(accountId) != null;
    }

    public AlfrescoSession getSession(Long accountId)
    {
        return sessionIndex.get(accountId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SETTINGS
    // ///////////////////////////////////////////////////////////////////////////
    public Bundle getOAuthSettings()
    {
        return null;
    }

    public AlfrescoSessionSettings prepareSettings(AlfrescoAccount acc)
    {
        return new SettingsBuilder(appContext).prepare(acc).build();
    }

    public AlfrescoSessionSettings prepareSettings(AlfrescoAccount acc, OAuthData data)
    {
        return new SettingsBuilder(appContext).prepare(acc).build();
    }

    public AlfrescoSessionSettings prepareSettings(AlfrescoAccount acc, SamlData data)
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

    public AlfrescoSessionSettings prepareSettings(String baseUrl, SamlData samlData)
    {
        return new SettingsBuilder(appContext).prepare(baseUrl, samlData).build();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    protected static class SettingsBuilder
    {
        protected static final String ONPREMISE_TRUSTMANAGER_CLASSNAME = "org.alfresco.mobile.binding.internal.https.trustmanager";

        protected WeakReference<Context> contextRef;

        protected Map<String, Serializable> extraSettings = new HashMap<String, Serializable>();

        protected String baseUrl;

        protected String username;

        protected String password;

        protected OAuthData oAuthData;

        protected SamlData samlData;

        protected boolean isCloud = false;

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTOR
        // ///////////////////////////////////////////////////////////////////////////
        public SettingsBuilder(Context context)
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
            else if (samlData != null)
            {
                return new AlfrescoSessionSettings(baseUrl, samlData, extraSettings);
            }
            else
            {
                return new AlfrescoSessionSettings(baseUrl, username, password, extraSettings);
            }
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

        public SettingsBuilder prepare(String baseUrl, SamlData samlData)
        {
            this.isCloud = false;
            this.baseUrl = baseUrl;
            this.samlData = samlData;
            this.username = samlData.getUserId();
            this.password = null;
            prepareConfigurationSettings(new AlfrescoAccount(baseUrl, username));
            prepareSSLSettings();
            return this;
        }

        public SettingsBuilder prepare(String baseUrl, String username, String password)
        {
            this.isCloud = false;
            this.baseUrl = baseUrl;
            this.username = username;
            this.password = password;
            prepareConfigurationSettings(new AlfrescoAccount(baseUrl, username));
            prepareSSLSettings();
            return this;
        }

        public SettingsBuilder add(Map<String, Serializable> settings)
        {
            if (settings != null && !settings.isEmpty())
            {
                extraSettings.putAll(settings);
            }
            return this;
        }

        public SettingsBuilder add(String key, Serializable value)
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
            switch (acc.getTypeId())
            {
                case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                    isCloud = true;
                    baseUrl = acc.getUrl();
                    oAuthData = new OAuth2DataImpl(getContext().getString(R.string.oauth_api_key),
                            getContext().getString(R.string.oauth_api_secret), acc.getAccessToken(),
                            acc.getRefreshToken());
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
                case AlfrescoAccount.TYPE_ALFRESCO_CMIS_SAML:
                    // TODO Improve SAML Data handling
                    isCloud = false;
                    baseUrl = acc.getUrl();
                    username = acc.getUsername();
                    samlData = new SamlDataImpl(new Saml2TicketImpl(acc.getPassword(), username), null);
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
            extraSettings.put(ConfigService.CONFIGURATION_FOLDER,
                    AlfrescoStorageManager.getInstance(getContext()).getConfigurationFolder(acc).getPath());
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
            // extraSettings.put(SessionParameter.CLIENT_COMPRESSION, "true");
            extraSettings.put(AlfrescoSession.HTTP_INVOKER_CLASSNAME, NetworkHttpInvoker.class.getName());
            extraSettings.put(AlfrescoSession.CACHE_FOLDER,
                    AlfrescoStorageManager.getInstance(getContext()).getCacheDir("AlfrescoMobile").getPath());
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
                File f = AlfrescoStorageManager.getInstance(getContext())
                        .getFileInPrivateFolder(url.getHost() + ".properties");
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
