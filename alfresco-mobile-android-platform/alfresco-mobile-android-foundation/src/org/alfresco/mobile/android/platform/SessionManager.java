/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.alfresco.mobile.android.api.constants.OAuthConstant;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuth2DataImpl;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountCompletedEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountErrorEvent;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadInactiveAccountEvent;
import org.alfresco.mobile.android.async.session.LoadSessionRequest;
import org.alfresco.mobile.android.async.session.LoadSessionRequest.Builder;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoSessionSettings;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.network.NetworkHttpInvoker;
import org.alfresco.mobile.android.platform.network.NetworkTrustManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.platform.utils.MessengerUtils;
import org.apache.chemistry.opencmis.commons.SessionParameter;

import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.squareup.otto.Subscribe;

/**
 * Responsible to manage sessions across activities.
 * 
 * @author Jean Marie Pascal
 */
public abstract class SessionManager extends Manager
{
    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    protected Map<Long, AlfrescoSession> sessionIndex = new HashMap<Long, AlfrescoSession>();

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
        if (event.data != null)
        {
            loadSession(event.accountToLoad, event.data);
        }
        else
        {
            loadSession(event.accountToLoad);
        }
    }

    @Subscribe
    public void onReloadSessionRequested(RequestSessionEvent event)
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
        Builder request = new LoadSessionRequest.Builder(data);
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
            EventBusManager.getInstance().post(new LoadAccountCompletedEvent(null, accountToLoad));
        }
        else if (getCurrentAccount() == null || accountToLoad.getId() != getCurrentAccount().getId())
        {
            // Create the session for the specific account
            createSession(accountToLoad);
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
            EventBusManager.getInstance().post(
                    new LoadAccountErrorEvent(null, currentAccount, new NetworkErrorException("Not Online"), -1));
            return;
        }

        if (hasSession(currentAccount.getId()))
        {
            removeAccount(currentAccount.getId());
        }

        Builder request = new LoadSessionRequest.Builder();
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
        return sessionIndex.containsKey(accountId);
    }

    public AlfrescoSession getSession(Long accountId)
    {
        return sessionIndex.get(accountId);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SETTINGS
    // ///////////////////////////////////////////////////////////////////////////
    public String getSignUpHostname()
    {
        return OAuthConstant.PUBLIC_API_HOSTNAME;
    }
    
    public Bundle getOAuthSettings(){
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

    public AlfrescoSessionSettings prepareSettings(String baseUrl, String username, String password)
    {
        return new SettingsBuilder(appContext).prepare(baseUrl, username, password).build();
    }

    public AlfrescoSessionSettings prepareSettings(OAuthData oauthData)
    {
        return new SettingsBuilder(appContext).prepare(oauthData).build();
    }
    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class SettingsBuilder
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

        //TODO Implement it  !
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
            switch ((int) acc.getTypeId())
            {
                case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                    isCloud = true;
                    baseUrl = acc.getUrl();
                    oAuthData = new OAuth2DataImpl(getContext().getString(R.string.oauth_api_key), getContext()
                            .getString(R.string.oauth_api_secret), acc.getAccessToken(), acc.getRefreshToken());
                    prepareCloudSettings(acc.getRepositoryId());
                    break;
                case AlfrescoAccount.TYPE_ALFRESCO_TEST_BASIC:
                case AlfrescoAccount.TYPE_ALFRESCO_CMIS:
                    isCloud = false;
                    baseUrl = acc.getUrl();
                    username = acc.getUsername();
                    password = acc.getPassword();
                    prepareSSLSettings();
                    break;
                default:
                    break;
            }
        }

        protected void prepareCommonSettings()
        {
            // Default settings for Alfresco Application
            extraSettings.put(SessionParameter.CONNECT_TIMEOUT, "10000");
            extraSettings.put(SessionParameter.READ_TIMEOUT, "60000");
            extraSettings.put(AlfrescoSession.EXTRACT_METADATA, true);
            extraSettings.put(AlfrescoSession.CREATE_THUMBNAIL, true);
            extraSettings.put(SessionParameter.CLIENT_COMPRESSION, "true");
            extraSettings.put(AlfrescoSession.HTTP_INVOKER_CLASSNAME, NetworkHttpInvoker.class.getName());
            extraSettings.put(AlfrescoSession.CACHE_FOLDER, AlfrescoStorageManager.getInstance(getContext())
                    .getCacheDir("AlfrescoMobile").getPath());
        }

        protected void prepareCloudSettings(String repositoryId)
        {
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
                    MessengerUtils.showToast(getContext(), R.string.security_ssl_disable);
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
