/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.node.like;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.node.NodeOperationThread;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LikeNodeThread extends NodeOperationThread<Boolean>
{

    private static final String TAG = LikeNodeThread.class.getName();

    private Boolean value;

    private Boolean isLiked = Boolean.FALSE;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public LikeNodeThread(Context ctx, OperationRequest request)
    {
        super(ctx, request);
        if (request instanceof LikeNodeRequest)
        {
            this.value = ((LikeNodeRequest) request).getLikeOperation();
        }
    }

    
    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Boolean> doInBackground()
    {
        LoaderResult<Boolean> result = new LoaderResult<Boolean>();

        try
        {
            result = super.doInBackground();

            isLiked = session.getServiceRegistry().getRatingService().isLiked(node);

            if ((value == null && isLiked) || (value != null && !value && isLiked))
            {
                session.getServiceRegistry().getRatingService().unlike(node);
                isLiked = false;
            }
            else if ((value == null && !isLiked) || (value != null && value && !isLiked))
            {
                session.getServiceRegistry().getRatingService().like(node);
                isLiked = true;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }

        result.setData(isLiked);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_LIKE_COMPLETED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, getNode());
        b.putString(IntentIntegrator.EXTRA_LIKE, isLiked.toString());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
