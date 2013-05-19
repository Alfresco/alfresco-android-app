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
package org.alfresco.mobile.android.application.integration.node.delete;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationRequestImpl;
import org.alfresco.mobile.android.application.integration.node.NodeOperationThread;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class DeleteNodeThread extends NodeOperationThread<Void>
{
    private static final String TAG = DeleteNodeTask.class.getName();

    private Folder parent;
    
    public DeleteNodeThread(Context ctx, AbstractOperationRequestImpl request)
    {
        super(ctx, request);
    }
    
    @Override
    public LoaderResult<Void> doInBackground()
    {
        Log.d(TAG, "Start Delete");
        
        LoaderResult<Void> result = null;
        try
        {
            result = super.doInBackground();

            parent = session.getServiceRegistry().getDocumentFolderService().getParentFolder(node);
            if (parent == null) { return result; }
            
            session.getServiceRegistry().getDocumentFolderService().deleteNode(node);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            if (result == null)
            {
                result = new LoaderResult<Void>();
            }
            result.setException(e);
        }
        
        return result;
    }
    
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_DELETE_COMPLETE);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, parent);
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, node);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
    
}
