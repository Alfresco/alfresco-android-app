/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.loaders;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.AbstractBaseLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.api.constants.CloudConstant;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.utils.CloudUrlRegistry;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.LoginLoaderCallback;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.utils.UrlFinder;

import android.content.Context;
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

    private AlfrescoSession session;

    private Folder parentFolder;

    private List<Account> accounts;

    private String uri;

    private Map<String, Serializable> settings;

    private Account selectAccount;

    public NodeLoader(Context context, List<Account> accounts, String url)
    {
        super(context);
        this.accounts = accounts;
        this.uri = url;
    }
    
    public NodeLoader(Context context, AlfrescoSession session, String url)
    {
        super(context);
        this.session = session;
        this.uri = url;
    }

    @Override
    public LoaderResult<Node> loadInBackground()
    {
        LoaderResult<Node> result = new LoaderResult<Node>();
        Node n = null;
        String identifier = uri;
        try
        {
            if (session == null){
                // Detect url
                URL  tmpurl = findUrl(uri);

                // Find if account can match url
                findAccount(tmpurl);

                // Create Session
                findSession(tmpurl);

                // Find Identifier
                identifier = findIdentifier(tmpurl);
            }
           
            // Retrieve Node
            n = session.getServiceRegistry().getDocumentFolderService().getNodeByIdentifier(identifier);

            if (n.isDocument()) try
            {
                parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(n);
            }
            catch (Exception e)
            {
                Log.d("NodeLoader", e.toString());
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(n);

        return result;
    }

    // /////////////////////////////////////////////////////////////////////
    // UTILS
    // ////////////////////////////////////////////////////////////////////
    private String findIdentifier(URL tmpurl) throws UnsupportedEncodingException
    {
        String identifier = UrlFinder.getIdentifier(URLDecoder.decode(tmpurl.toString(), "UTF-8"));
        if (session instanceof CloudSession) identifier = NodeRefUtils.getVersionIdentifier(identifier);
        Log.d("Identifier", identifier);
        if (identifier == null) throw new AlfrescoServiceException("Unable to find a correct identifier : " + tmpurl);
        return identifier;
    }

    //TODO Better!
    private static final String BASE_URL = "org.alfresco.mobile.binding.baseurl";
    
    private void findSession(URL tmpurl)
    {
        String url = selectAccount.getUrl();
        if (url.startsWith(LoginLoaderCallback.ALFRESCO_CLOUD_URL))
        {
            if (settings == null) settings = new HashMap<String, Serializable>();
            if (!settings.containsKey(BASE_URL)) settings.put(BASE_URL, url);
            session = CloudSession.connect(selectAccount.getUsername(), selectAccount.getPassword(), null, settings);
        }
        else
        {
            session = RepositorySession.connect(selectAccount.getUrl(), selectAccount.getUsername(),
                    selectAccount.getPassword(), settings);
        }

        if (session == null)
            throw new AlfrescoServiceException("Unable to connect to the appropriate server : " + tmpurl);
    }

    private void findAccount(URL tmpurl) throws MalformedURLException
    {
        boolean match = false;
        URL accountUrl = null;
        for (Account account : accounts)
        {
            accountUrl = new URL(account.getUrl());
            if (tmpurl.getHost().equals(accountUrl.getHost()))
            {
                selectAccount = account;
                match = true;
            }
        }
        if (!match) throw new AlfrescoServiceException("No account match this url : " + tmpurl);
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
        if (url == null) throw new AlfrescoServiceException("This information is not a valid url");
        return url;
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
