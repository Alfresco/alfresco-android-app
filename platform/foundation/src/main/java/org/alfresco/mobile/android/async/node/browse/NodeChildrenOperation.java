/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.mobile.android.async.node.browse;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.SearchLanguage;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.model.impl.cloud.CloudFolderImpl;
import org.alfresco.mobile.android.api.network.NetworkHttpInvoker;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.api.utils.JsonUtils;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.async.utils.ISO9075;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;

import android.util.Log;

public class NodeChildrenOperation extends ListingOperation<PagingResult<Node>>
{
    private static final String TAG = NodeChildrenOperation.class.getName();

    protected String nodeIdentifier;

    protected Node node;

    protected String folderIdentifier;

    protected Folder parentFolder;

    protected String folderPath;

    protected String siteId;

    protected Site site;

    protected int folderTypeId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public NodeChildrenOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof NodeChildrenRequest)
        {
            this.nodeIdentifier = ((NodeChildrenRequest) request).getNodeIdentifier();
            this.folderIdentifier = ((NodeChildrenRequest) request).getParentFolderIdentifier();
            this.folderPath = ((NodeChildrenRequest) request).getFolderPath();
            this.parentFolder = ((NodeChildrenRequest) request).getParentFolder();
            this.siteId = ((NodeChildrenRequest) request).getSiteId();
            this.site = ((NodeChildrenRequest) request).getSite();
            this.folderTypeId = ((NodeChildrenRequest) request).getFolderTypeId();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<PagingResult<Node>> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<PagingResult<Node>> result = new LoaderResult<PagingResult<Node>>();
            PagingResult<Node> pagingResult = null;

            try
            {
                if (siteId != null && site == null)
                {
                    site = session.getServiceRegistry().getSiteService().getSite(siteId);
                }
                if (site != null)
                {
                    parentFolder = session.getServiceRegistry().getSiteService().getDocumentLibrary(site);
                }

                if (folderTypeId != -1)
                {
                    List<Node> nodes = null;

                    // Try for 5.X
                    if (folderTypeId == NodeChildrenRequest.FOLDER_USER_HOMES
                            && session.getRepositoryInfo().getMajorVersion() >= 5)
                    {
                        try
                        {
                            String statement = "SELECT cm:homeFolder FROM cm:person WHERE cm:userName = '%s'";
                            String nodeId = getUserHomeIdWithBrowserBinding(
                                    String.format(statement, ISO9075.encode(session.getPersonIdentifier())));
                            if (nodeId != null)
                            {
                                parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                                        .getNodeByIdentifier(nodeId);
                                nodes = new ArrayList<>(1);
                                nodes.add(parentFolder);
                                pagingResult = session.getServiceRegistry().getDocumentFolderService()
                                        .getChildren(parentFolder, listingContext);
                            }
                        }
                        catch (Exception e)
                        {
                            nodes = null;
                        }
                    }

                    // If not 5.X
                    if (nodes == null)
                    {
                        String query = null;
                        switch (folderTypeId)
                        {
                            case NodeChildrenRequest.FOLDER_SHARED:
                                query = "SELECT * FROM cmis:folder WHERE CONTAINS ('QNAME:\"app:company_home/app:shared\"')";
                                break;
                            case NodeChildrenRequest.FOLDER_USER_HOMES:
                                query = "SELECT * FROM cmis:folder WHERE CONTAINS ('QNAME:\"app:company_home/app:user_homes/cm:"
                                        + ISO9075.encode(session.getPersonIdentifier()) + "\"')";
                                break;
                            default:
                                break;
                        }

                        nodes = session.getServiceRegistry().getSearchService().search(query, SearchLanguage.CMIS);
                        if (nodes != null && nodes.size() == 1)
                        {
                            parentFolder = (Folder) nodes.get(0);
                            pagingResult = session.getServiceRegistry().getDocumentFolderService()
                                    .getChildren(parentFolder, listingContext);
                        }
                    }
                }
                else if (folderPath != null)
                {
                    Node n = session.getServiceRegistry().getDocumentFolderService().getChildByPath(folderPath);
                    if (n.isFolder())
                    {
                        pagingResult = session.getServiceRegistry().getDocumentFolderService().getChildren((Folder) n,
                                listingContext);
                        parentFolder = (Folder) n;
                    }
                    else
                    {
                        parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(n);
                    }
                }
                else if (parentFolder != null)
                {
                    if (parentFolder instanceof CloudFolderImpl)
                    {
                        parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                                .getNodeByIdentifier(parentFolder.getIdentifier());
                    }

                    pagingResult = session.getServiceRegistry().getDocumentFolderService().getChildren(parentFolder,
                            listingContext);
                }
                else if (folderIdentifier != null)
                {
                    Node n = session.getServiceRegistry().getDocumentFolderService()
                            .getNodeByIdentifier(folderIdentifier);
                    if (n.isFolder())
                    {
                        pagingResult = session.getServiceRegistry().getDocumentFolderService().getChildren((Folder) n,
                                listingContext);
                        parentFolder = (Folder) n;
                    }
                    else
                    {
                        parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(n);
                    }
                }

            }
            catch (AlfrescoServiceException e)
            {
                result.setException(e);
            }

            result.setData(pagingResult);

            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<PagingResult<Node>>();
    }

    private static final String BINDING_NETWORK_CMISATOM = "/api/-default-/public/cmis/versions/1.1/browser";

    private String getUserHomeIdWithBrowserBinding(String statement)
    {

        try
        {
            Map<String, List<String>> httpHeaders = ((AbstractAlfrescoSessionImpl) session).getAuthenticationProvider()
                    .getHTTPHeaders();

            UrlBuilder builder = new UrlBuilder(session.getBaseUrl().concat(BINDING_NETWORK_CMISATOM));
            builder.addParameter("cmisaction", "query");
            builder.addParameter("searchAllVersions", false);
            builder.addParameter("skipCount", 0);
            builder.addParameter("includeAllowableActions", false);
            builder.addParameter("maxItems", 1);
            builder.addParameter("includeRelationships", "none");
            builder.addParameter("succinct", true);
            builder.addParameter("statement", statement);

            Response resp = NetworkHttpInvoker.invokePOST(builder, null, null, httpHeaders);
            if (resp.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                // We got our info!
                Map<String, Object> json = JsonUtils.parseObject(resp.getStream(), resp.getCharset());
                Map<String, Object> entry = (Map<String, Object>) JSONConverter.getList(json.get("results")).get(0);
                List<Object> o = (List<Object>) ((Map<String, Object>) entry.get("succinctProperties"))
                        .get("cm:homeFolder");
                return JSONConverter.getJSONStringValue(o.get(0));
            }
        }
        catch (Exception e)
        {
            return null;
        }

        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<PagingResult<Node>> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new NodeChildrenEvent(getRequestId(), result, parentFolder, site));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getNodeIdentifier()
    {
        return nodeIdentifier;
    }

    public String getParentFolderIdentifier()
    {
        return folderIdentifier;
    }

    public Folder getParentFolder()
    {
        return parentFolder;
    }

    public String getSiteId()
    {
        return siteId;
    }

    public Site getSite()
    {
        return site;
    }

    public int getFolderTypeId()
    {
        return folderTypeId;
    }

    public String getFolderPath()
    {
        return folderPath;
    }
}
