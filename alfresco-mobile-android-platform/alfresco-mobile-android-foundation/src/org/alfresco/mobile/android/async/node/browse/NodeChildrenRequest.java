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
package org.alfresco.mobile.android.async.node.browse;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.ListingOperationRequest;
import org.alfresco.mobile.android.ui.node.browse.NodeBrowserTemplate;

import android.content.Context;

public class NodeChildrenRequest extends ListingOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_NODE_BROWSE;

    public static final int FOLDER_USER_HOMES = 0;

    public static final int FOLDER_SHARED = 1;

    final String nodeIdentifier;

    final String parentFolderIdentifier;

    final Folder parentFolder;

    final String parentFolderPath;

    final String siteId;

    final Site site;

    final int folderTypeId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected NodeChildrenRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, ListingContext listingContext, String nodeIdentifier,
            String parentFolderIdentifier, Folder parentFolder, String parentFolderPath, String siteId, Site site,
            int folderTypeId)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, listingContext);
        this.nodeIdentifier = nodeIdentifier;
        this.parentFolderIdentifier = parentFolderIdentifier;
        this.parentFolder = parentFolder;
        this.parentFolderPath = parentFolderPath;
        this.siteId = siteId;
        this.site = site;
        this.folderTypeId = folderTypeId;
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
        return parentFolderIdentifier;
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
        return parentFolderPath;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return NodeChildrenRequest.class.getName();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends ListingOperationRequest.Builder
    {
        protected String nodeIdentifier;

        protected String parentFolderIdentifier;

        protected Folder parentFolder;

        protected String parentFolderPath;

        protected String siteId;

        protected Site site;

        protected int folderTypeId = -1;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(Folder parentFolder)
        {
            this();
            this.parentFolder = parentFolder;
        }

        public Builder(Site site)
        {
            this();
            this.site = site;
        }

        public Builder(String type, String value)
        {
            this();
            if (NodeBrowserTemplate.ARGUMENT_FOLDER_NODEREF.equalsIgnoreCase(type))
            {
                this.parentFolderIdentifier = value;
            }
            else if (NodeBrowserTemplate.ARGUMENT_PATH.equalsIgnoreCase(type))
            {
                this.parentFolderPath = value;
            }
            else if (NodeBrowserTemplate.ARGUMENT_SITE_SHORTNAME.equalsIgnoreCase(type))
            {
                this.siteId = value;
            }
        }

        /**
         * Get all children from a specified folder defined by its ID. </br>
         * 
         * @param parentFolderIdentifier : Parent Folder Id
         */
        public Builder(String parentFolderIdentifier)
        {
            this();
            this.parentFolderIdentifier = parentFolderIdentifier;
        }

        /**
         * Get all children from a specified folder defined by CONSTANT. </br>
         * 
         * @param folderTypeId : Constant
         */
        public Builder(int folderTypeId)
        {
            this();
            this.folderTypeId = folderTypeId;
        }

        public NodeChildrenRequest build(Context context)
        {
            return new NodeChildrenRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, listingContext, nodeIdentifier, parentFolderIdentifier, parentFolder,
                    parentFolderPath, siteId, site, folderTypeId);
        }
    }
}
