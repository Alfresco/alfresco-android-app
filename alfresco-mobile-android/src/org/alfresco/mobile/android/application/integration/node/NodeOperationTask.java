/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.integration.node;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationTask;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.content.Context;

public abstract class NodeOperationTask<T> extends AbstractOperationTask<T>
{
    protected Node node;
    
    protected String nodeIdentifier;

    public NodeOperationTask(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof NodeOperationRequest)
        {
            this.accountId = ((AbstractOperationRequestImpl) request).getAccountId();
            this.nodeIdentifier = ((NodeOperationRequest) request).getNodeIdentifier();
        }
    }
    
    public Node getNode()
    {
        return node;
    }

    protected LoaderResult<T> doInBackground(Void... params)
    {
        try
        {
            session = requestSession();
            node = session.getServiceRegistry().getDocumentFolderService().getNodeByIdentifier(nodeIdentifier);
            if (listener != null)
            {
                listener.onPreExecute(this);
            }
        }
        catch (Exception e)
        {
        }
        return new LoaderResult<T>();
    }
}
