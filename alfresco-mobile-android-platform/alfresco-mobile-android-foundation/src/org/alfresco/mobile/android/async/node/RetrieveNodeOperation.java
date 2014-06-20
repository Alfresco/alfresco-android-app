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
package org.alfresco.mobile.android.async.node;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoSessionSettings;
import org.alfresco.mobile.android.platform.provider.MapUtil;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.sync.FavoritesSyncSchema;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;

import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

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
public class RetrieveNodeOperation extends NodeOperation<Node>
{

    /** Unique NodeChildrenLoader identifier. */
    public static final int ID = RetrieveNodeOperation.class.hashCode();

    private static final String MY_ALFRESCO_HOSTNAME = "my.alfresco.com";

    private static final String API_ALFRESCO_HOSTNAME = "api.alfresco.com";

    private static final String TAG = "NodeLoader";

    private AlfrescoSession session;

    private Folder parentFolder;

    private List<AlfrescoAccount> accounts;

    private String uri;

    private AlfrescoAccount selectAccount;

    private AlfrescoAccount acc;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public RetrieveNodeOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Node> doInBackground()
    {
        LoaderResult<Node> result = new LoaderResult<Node>();
        String identifier = nodeIdentifier;

        try
        {
            result = super.doInBackground();
            if (!ConnectivityUtils.hasInternetAvailable(context)) { throw new AlfrescoServiceException("No Network"); }

            if (node == null)
            {
                // Detect url
                URL tmpurl = findUrl(uri);

                // Find Identifier
                identifier = findIdentifier(tmpurl);

                // Find if AlfrescoAccount can match url
                findAccount(tmpurl);

                // Create Session
                findSession(tmpurl);
            }
        }
        catch (Exception e)
        {
            FavoritesSyncManager syncManager = FavoritesSyncManager.getInstance(context);
            if (!ConnectivityUtils.hasInternetAvailable(context) && syncManager.isSynced(acc, identifier))
            {
                // Retrieve Sync Cursor for the specified node
                Uri localUri = syncManager.getUri(acc, identifier);
                Cursor syncCursor = context.getContentResolver().query(localUri, FavoritesSyncSchema.COLUMN_ALL, null,
                        null, null);
                if (syncCursor.getCount() == 1 && syncCursor.moveToFirst())
                {
                    // syncCursor.getString(OperationSchema.COLUMN_PROPERTIES_ID)
                    Map<String, Serializable> properties = retrievePropertiesMap(syncCursor);
                    node = new NodeSyncPlaceHolder(properties);
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

        result.setData(node);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Node> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new RetrieveNodeEvent(getRequestId(), result, parentFolder));
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
        AlfrescoSessionSettings sessionSettings = SessionManager.getInstance(context).prepareSettings(selectAccount);
        if (sessionSettings.isCloud)
        {
            session = CloudSession.connect(sessionSettings.oAuthData, sessionSettings.extraSettings);
        }
        else
        {
            session = RepositorySession.connect(sessionSettings.baseUrl, sessionSettings.username,
                    sessionSettings.password, sessionSettings.extraSettings);
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

        List<AlfrescoAccount> matchAccount = new ArrayList<AlfrescoAccount>();
        boolean match = false;
        URL accountUrl = null;
        for (AlfrescoAccount account : accounts)
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
            // Cloud AlfrescoAccount : check network
            for (AlfrescoAccount account : matchAccount)
            {
                if (tmpurl.getPath().contains(account.getRepositoryId()))
                {
                    selectAccount = account;
                    break;
                }
            }
        }

        if (!match) { throw new AlfrescoServiceException("No AlfrescoAccount match this url : " + tmpurl); }
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

    protected Map<String, Serializable> retrievePropertiesMap(Cursor cursor)
    {
        // PROPERTIES
        String rawProperties = cursor.getString(OperationSchema.COLUMN_PROPERTIES_ID);
        if (rawProperties != null)
        {
            return MapUtil.stringToMap(rawProperties);
        }
        else
        {
            return new HashMap<String, Serializable>();
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

    public AlfrescoAccount getAccount()
    {
        return selectAccount;
    }
}
