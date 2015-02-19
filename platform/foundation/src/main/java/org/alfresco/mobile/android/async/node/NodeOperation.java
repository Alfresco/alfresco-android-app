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

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;

import android.util.Log;

public abstract class NodeOperation<T> extends BaseOperation<T>
{
    protected static final String TAG = NodeOperation.class.getName();

    protected String nodeIdentifier;

    protected String parentFolderIdentifier;

    protected Node node;

    protected Folder parentFolder;

    protected boolean ignoreParentFolder = false;

    protected boolean ignoreNode = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public NodeOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof NodeRequest)
        {
            this.nodeIdentifier = ((NodeRequest) request).getNodeIdentifier();
            this.parentFolderIdentifier = ((NodeRequest) request).getParentFolderIdentifier();
            this.node = ((NodeRequest) request).getNode();
            this.parentFolder = ((NodeRequest) request).getParentFolder();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<T> doInBackground()
    {
        try
        {
            super.doInBackground();

            try
            {
                if (!ignoreNode && nodeIdentifier != null && node == null)
                {
                    node = session.getServiceRegistry().getDocumentFolderService().getNodeByIdentifier(nodeIdentifier);
                }
            }
            catch (AlfrescoServiceException e)
            {
                try
                {
                    if (node == null)
                    {
                        node = session.getServiceRegistry().getDocumentFolderService()
                                .getNodeByIdentifier(NodeRefUtils.getCleanIdentifier(nodeIdentifier));
                    }
                }
                catch (AlfrescoServiceException er)
                {
                    // DO NOTHING
                }
            }

            if (!ignoreParentFolder)
            {
                parentFolder = retrieveParentFolder();
            }

            if (listener != null)
            {
                listener.onPreExecute(this);
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<T>();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected Folder retrieveParentFolder()
    {
        try
        {
            if (parentFolder == null && parentFolderIdentifier != null)
            {
                parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                        .getNodeByIdentifier(parentFolderIdentifier);
            }

            if (parentFolder == null && node != null)
            {
                parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(node);
            }
        }
        catch (Exception e)
        {
            // Do Nothing
        }

        return parentFolder;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getNodeIdentifier()
    {
        return nodeIdentifier;
    }

    public Node getNode()
    {
        return node;
    }

    public Folder getParentFolder()
    {
        return parentFolder;
    }
}
