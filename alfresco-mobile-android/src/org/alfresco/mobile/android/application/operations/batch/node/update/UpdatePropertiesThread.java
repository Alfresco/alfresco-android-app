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
package org.alfresco.mobile.android.application.operations.batch.node.update;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.node.NodeOperationThread;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class UpdatePropertiesThread extends NodeOperationThread<Node>
{
    private static final String TAG = UpdatePropertiesThread.class.getName();

    private Node updatedNode = null;

    private Map<String, Serializable> properties;

    private List<String> tags;
    
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public UpdatePropertiesThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof UpdatePropertiesRequest)
        {
            this.properties = ((UpdatePropertiesRequest) request).getProperties();
            this.tags = ((UpdatePropertiesRequest) request).getTags();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Node> doInBackground()
    {
        LoaderResult<Node> result = new LoaderResult<Node>();
        try
        {
            result = super.doInBackground();

            if (properties != null)
            {
                updatedNode = session.getServiceRegistry().getDocumentFolderService().updateProperties(node, properties);
            }

            if (tags != null && !tags.isEmpty())
            {
                session.getServiceRegistry().getTaggingService().addTags(updatedNode, tags);
            }
        }
        catch (Exception e)
        {
            result.setException(e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        result.setData(updatedNode);

        return result;
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }
    
    public Node getUpdatedNode()
    {
        return updatedNode;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_STARTED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_NODE, getNode());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_NODE, getNode());
        b.putParcelable(IntentIntegrator.EXTRA_UPDATED_NODE, getUpdatedNode());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
