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
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.text.TextUtils;
import android.util.Log;

public class NodeByPathOperation extends BaseOperation<Node>
{
    protected static final String TAG = NodeByPathOperation.class.getName();

    protected String nodeIdentifier;

    protected String parentFolderIdentifier;

    protected String path;

    protected Node node;

    protected Folder parentFolder;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public NodeByPathOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof NodeByPathRequest)
        {
            this.path = ((NodeByPathRequest) request).getPath();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<Node> doInBackground()
    {
        LoaderResult<Node> result = new LoaderResult<Node>();

        try
        {
            super.doInBackground();

            try
            {
                if (TextUtils.isEmpty(path)) { throw new AlfrescoServiceException("Path is empty!"); }

                node = session.getServiceRegistry().getDocumentFolderService().getChildByPath(path);
                nodeIdentifier = (node != null) ? node.getIdentifier() : null;
                result.setData(node);
            }
            catch (AlfrescoServiceException e)
            {
                result.setException(e);
            }

            parentFolder = retrieveParentFolder();

            if (listener != null)
            {
                listener.onPreExecute(this);
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected Folder retrieveParentFolder()
    {
        try
        {
            if (node != null)
            {
                parentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService().getParentFolder(node);
            }
        }
        catch (Exception e)
        {
            // Do Nothing
        }

        return parentFolder;
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
