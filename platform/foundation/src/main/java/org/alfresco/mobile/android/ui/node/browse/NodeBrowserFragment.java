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
package org.alfresco.mobile.android.ui.node.browse;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.node.browse.NodeChildrenEvent;
import org.alfresco.mobile.android.async.node.browse.NodeChildrenRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.ui.RefreshFragment;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;

import com.squareup.otto.Subscribe;

import android.os.Bundle;

/**
 * Displays a fragment list of document and folders.
 * 
 * @author Jean Marie Pascal
 */
public class NodeBrowserFragment extends BaseGridFragment implements RefreshFragment, NodeBrowserTemplate
{
    private static final String TAG = NodeBrowserFragment.class.getName();

    // Browser Parameters
    protected Folder parentFolder;

    protected Site site = null;

    protected String siteId = null;

    protected String pathParameter = null;

    protected Folder folderParameter = null;

    protected int folderTypeId = -1;

    protected String folderIdentifier;

    private Boolean activateThumbnail = Boolean.FALSE;

    protected List<Node> selectedItems = new ArrayList<Node>(1);

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public NodeBrowserFragment()
    {
        emptyListMessageId = R.string.empty_child;
    }

    protected static Bundle createBundleArgs(Folder parentFolder, String pathFolder, Site site)
    {
        Bundle args = new Bundle();
        BundleUtils.addIfNotNull(args, ARGUMENT_FOLDER, parentFolder);
        BundleUtils.addIfNotNull(args, ARGUMENT_SITE, site);
        BundleUtils.addIfNotEmpty(args, ARGUMENT_PATH, pathFolder);
        return args;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    protected void onRetrieveParameters(Bundle bundle)
    {
        folderParameter = (Folder) bundle.getSerializable(ARGUMENT_FOLDER);
        folderTypeId = bundle.containsKey(ARGUMENT_FOLDER_TYPE_ID) ? bundle.getInt(ARGUMENT_FOLDER_TYPE_ID) : -1;
        site = (Site) bundle.getSerializable(ARGUMENT_SITE);
        folderIdentifier = (String) bundle.getSerializable(ARGUMENT_FOLDER_NODEREF);
        pathParameter = bundle.getString(ARGUMENT_PATH);
        siteId = bundle.getString(ARGUMENT_SITE_SHORTNAME);

    }

    // //////////////////////////////////////////////////////////////////////
    // REQUEST
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        NodeChildrenRequest.Builder request = null;

        if (pathParameter != null)
        {
            title = (pathParameter.equals("/") ? "/"
                    : pathParameter.substring(pathParameter.lastIndexOf("/") + 1,
                    pathParameter.length()));
            request = new NodeChildrenRequest.Builder(ARGUMENT_PATH, pathParameter);
        }
        else if (siteId != null && site == null)
        {
            request = new NodeChildrenRequest.Builder(ARGUMENT_SITE_SHORTNAME, siteId);
        }
        else if (site != null && folderParameter == null)
        {
            title = site.getTitle();
            request = new NodeChildrenRequest.Builder(site);
        }
        else if (folderParameter != null)
        {
            title = folderParameter.getName();
            request = new NodeChildrenRequest.Builder(folderParameter);
        }
        else if (folderTypeId != -1)
        {
            switch (folderTypeId)
            {
                case NodeChildrenRequest.FOLDER_SHARED:
                    title = getString(R.string.menu_browse_shared);
                    break;
                case NodeChildrenRequest.FOLDER_USER_HOMES:
                    title = getString(R.string.menu_browse_userhome);
                    break;
                default:
                    title = "";
                    break;
            }
            request = new NodeChildrenRequest.Builder(folderTypeId);
        }
        else if (folderIdentifier != null)
        {
            request = new NodeChildrenRequest.Builder(folderIdentifier);
        }
        else
        {
            if (getSession() == null)
            {
                title = "/";
                request = new NodeChildrenRequest.Builder("/");
            }
            else
            {
                title = getSession().getRootFolder().getName();
                request = new NodeChildrenRequest.Builder(getSession().getRootFolder());
            }
        }

        // Uncomment to display document & folder links
        /*
         * if(listingContext == null){ listingContext = new ListingContext(); }
         * ListingFilter filter = new ListingFilter();
         * filter.addFilter(DocumentFolderService.FILTER_INCLUDE_LINKS, true);
         * listingContext.setFilter(filter);
         */

        return request.setListingContext(listingContext);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTENER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(NodeChildrenEvent event)
    {
        displayData(event);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public Boolean hasActivateThumbnail()
    {
        return activateThumbnail;
    }

    public void setActivateThumbnail(Boolean activateThumbnail)
    {
        this.activateThumbnail = activateThumbnail;
    }

    public Folder getParent()
    {
        return parentFolder;
    }
}
