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
package org.alfresco.mobile.android.application.fragments.properties;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.AbstractBaseLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.fragment.AccountSettingsHelper;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.utils.MapUtil;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.operations.sync.utils.NodeSyncPlaceHolder;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides an asynchronous loader to retrieve a node (document and/or folder).
 * </br> Provides constructor for getting children from
 * <ul>
 * <li>an identifier</li>
 * <li>a Folder path</li>
 * </ul>
 * 
 * @author Jean Marie Pascal
 */
public class NodeLoader extends AbstractBaseLoader<LoaderResult<Node>>
{

    /** Unique NodeChildrenLoader identifier. */
    public static final int ID = NodeLoader.class.hashCode();

    private static final String MY_ALFRESCO_HOSTNAME = "my.alfresco.com";

    private static final String API_ALFRESCO_HOSTNAME = "api.alfresco.com";

    private static final String TAG = "NodeLoader";

    private AlfrescoSession session;

    private Folder parentFolder;

    private List<Account> accounts;

    private String uri;

    private Account selectAccount;

    private Account acc;

    public NodeLoader(Activity context, Account acc, AlfrescoSession session, String nodeIdentifier)
    {
        super(context);
        this.session = session;
        this.uri = nodeIdentifier;
        this.acc = acc;
    }

    @Override
    public LoaderResult<Node> loadInBackground()
    {
        LoaderResult<Node> result = new LoaderResult<Node>();
        Node n = null;
        String identifier = uri;
        try
        {
            if (!ConnectivityUtils.hasInternetAvailable(getContext())) { throw new AlfrescoServiceException(
                    "No Network"); }

            if (session == null)
            {
                // Detect url
                URL tmpurl = findUrl(uri);

                // Find Identifier
                identifier = findIdentifier(tmpurl);

                // Find if account can match url
                findAccount(tmpurl);

                // Create Session
                findSession(tmpurl);
            }

            // Retrieve Node
            n = session.getServiceRegistry().getDocumentFolderService().getNodeByIdentifier(identifier);

            try
            {
                parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(n);
            }
            catch (Exception e)
            {
                Log.w(TAG, Log.getStackTraceString(e));
            }
        }
        catch (Exception e)
        {
            SynchroManager syncManager = SynchroManager.getInstance(getContext());
            if (!ConnectivityUtils.hasInternetAvailable(getContext()) && syncManager.isSynced(acc, identifier))
            {
                // Retrieve Sync Cursor for the specified node
                Uri localUri = syncManager.getUri(acc, identifier);
                Cursor syncCursor = getContext().getContentResolver().query(localUri, SynchroSchema.COLUMN_ALL, null,
                        null, null);
                if (syncCursor.getCount() == 1 && syncCursor.moveToFirst())
                {
                    // syncCursor.getString(BatchOperationSchema.COLUMN_PROPERTIES_ID)
                    Map<String, String> properties = retrievePropertiesMap(syncCursor);
                    n = new NodeSyncPlaceHolder(properties);
                }
                else
                {
                    result.setException(e);
                }
                syncCursor.close();
            }
            else
            {
                result.setException(e);
            }
        }

        result.setData(n);

        return result;
    }

    // /////////////////////////////////////////////////////////////////////
    // UTILS
    // ////////////////////////////////////////////////////////////////////
    private String findIdentifier(URL tmpurl) throws UnsupportedEncodingException
    {
        String identifier = getIdentifier(URLDecoder.decode(tmpurl.toString(), "UTF-8"));
        if (session instanceof CloudSession)
        {
            identifier = NodeRefUtils.getVersionIdentifier(identifier);
        }
        if (identifier == null) { throw new AlfrescoServiceException("Unable to find a correct identifier : " + tmpurl); }
        return identifier;
    }

    private void findSession(URL tmpurl)
    {
        AccountSettingsHelper settingsHelper = new AccountSettingsHelper(getContext(), selectAccount);
        Map<String, Serializable> settings = settingsHelper.prepareCommonSettings();
        if (settingsHelper.isCloud())
        {
            settings.putAll(settingsHelper.prepareCloudSettings());
            session = CloudSession.connect(settingsHelper.getData(), settings);
        }
        else
        {
            session = RepositorySession.connect(settingsHelper.getBaseUrl(), settingsHelper.getUsername(),
                    settingsHelper.getPassword(), settings);
        }

        if (session == null) { throw new AlfrescoServiceException("Unable to connect to the appropriate server : "
                + tmpurl); }
    }

    private void findAccount(URL searchedURL) throws MalformedURLException
    {
        URL tmpurl = searchedURL;
        if (tmpurl.getHost().equals(MY_ALFRESCO_HOSTNAME))
        {
            tmpurl = new URL(tmpurl.toString().replace(MY_ALFRESCO_HOSTNAME, API_ALFRESCO_HOSTNAME));
        }

        List<Account> matchAccount = new ArrayList<Account>();
        boolean match = false;
        URL accountUrl = null;
        for (Account account : accounts)
        {
            accountUrl = new URL(account.getUrl());
            if (tmpurl.getHost().equals(accountUrl.getHost()))
            {
                matchAccount.add(account);
            }
        }

        if (matchAccount.size() == 1)
        {
            match = true;
            selectAccount = matchAccount.get(0);
        }
        else if (matchAccount.size() > 1)
        {
            match = true;
            selectAccount = matchAccount.get(0);
            // Cloud Account : check network
            for (Account account : matchAccount)
            {
                if (tmpurl.getPath().contains(account.getRepositoryId()))
                {
                    selectAccount = account;
                    break;
                }
            }
        }

        if (!match) { throw new AlfrescoServiceException("No account match this url : " + tmpurl); }
    }

    private URL findUrl(String text)
    {
        String[] parts = text.split("\\s");
        URL url = null;
        // Attempt to convert each item into an URL.
        for (String item : parts)
        {
            try
            {
                url = new URL(item);
                break;
            }
            catch (Exception e)
            {
            }
        }
        if (url == null) { throw new AlfrescoServiceException("This information is not a valid url"); }
        return url;
    }

    protected Map<String, String> retrievePropertiesMap(Cursor cursor)
    {
        // PROPERTIES
        String rawProperties = cursor.getString(BatchOperationSchema.COLUMN_PROPERTIES_ID);
        if (rawProperties != null)
        {
            return MapUtil.stringToMap(rawProperties);
        }
        else
        {
            return new HashMap<String, String>();
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // UTILS
    // ////////////////////////////////////////////////////////////////////
    private static final String NODEREF = "noderef=";

    private static final String NODE_ID = "id=";

    private static final List<String> PATTERNS = new ArrayList<String>(2);
    static
    {
        PATTERNS.add(NODEREF);
        PATTERNS.add(NODE_ID);
    }

    public static String getIdentifier(String url)
    {
        String identifier = null, tmp = null;
        tmp = url.toLowerCase();
        for (String pattern : PATTERNS)
        {
            if (tmp.contains(pattern.toLowerCase()))
            {
                identifier = TextUtils.substring(tmp, tmp.lastIndexOf(pattern) + pattern.length(), tmp.length());

                if (identifier.contains("&"))
                {
                    identifier = TextUtils.substring(identifier, 0, identifier.indexOf("&"));
                }

                if (NodeRefUtils.isNodeRef(identifier)) { return identifier; }
                if (NodeRefUtils.isVersionIdentifier(identifier)) { return identifier; }
                if (NodeRefUtils.isIdentifier(identifier)) { return identifier; }
            }
        }
        return null;
    }

    // /////////////////////////////////////////////////////////////////////
    // PUBLIC METHOD
    // ////////////////////////////////////////////////////////////////////
    public Folder getParentFolder()
    {
        return parentFolder;
    }

    public AlfrescoSession getSession()
    {
        return session;
    }

    public Account getAccount()
    {
        return selectAccount;
    }
}
