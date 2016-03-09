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
package org.alfresco.mobile.android.async.node.like;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.NodeOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;

import android.util.Log;

public class LikeNodeOperation extends NodeOperation<Boolean>
{

    private static final String TAG = LikeNodeOperation.class.getName();

    private Boolean readOnly;

    private Boolean isLiked = Boolean.FALSE;

    private Boolean performLike;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public LikeNodeOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof LikeNodeRequest)
        {
            this.readOnly = ((LikeNodeRequest) request).isReadOnly;
            this.performLike = ((LikeNodeRequest) request).isLike;
        }
        ignoreParentFolder = true;
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
            checkCancel();
            result = super.doInBackground();

            isLiked = session.getServiceRegistry().getRatingService().isLiked(node);

            if (!readOnly)
            {
                if (performLike != null)
                {
                    if (performLike && performLike != isLiked)
                    {
                        session.getServiceRegistry().getRatingService().like(node);
                        isLiked = true;
                    }
                    else if (!performLike && performLike != isLiked)
                    {
                        session.getServiceRegistry().getRatingService().unlike(node);
                        isLiked = false;
                    }
                }
                else
                {
                    if (isLiked)
                    {
                        session.getServiceRegistry().getRatingService().unlike(node);
                        isLiked = false;
                    }
                    else
                    {
                        session.getServiceRegistry().getRatingService().like(node);
                        isLiked = true;
                    }
                }
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
    @Override
    protected void onPostExecute(LoaderResult<Boolean> result)
    {
        super.onPostExecute(result);

        // Analytics
        if (!readOnly)
        {
            AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                    isLiked ? AnalyticsManager.ACTION_LIKE : AnalyticsManager.ACTION_UNLIKE,
                    node.isDocument() ? ((Document) node).getContentStreamMimeType() : AnalyticsManager.TYPE_FOLDER, 1,
                    result.hasException());
        }

        EventBusManager.getInstance().post(new LikeNodeEvent(getRequestId(), readOnly, result));
    }
}
