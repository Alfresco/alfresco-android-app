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
package org.alfresco.mobile.android.async.node.delete;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.NodeOperation;
import org.alfresco.mobile.android.async.node.sync.SyncNodeRequest;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.sync.SyncContentManager;

import android.util.Log;

public class DeleteNodeOperation extends NodeOperation<Void>
{
    private static final String TAG = DeleteNodeOperation.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public DeleteNodeOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    @Override
    public LoaderResult<Void> doInBackground()
    {
        LoaderResult<Void> result = new LoaderResult<Void>();
        try
        {
            result = super.doInBackground();

            if (parentFolder == null) { return result; }

            session.getServiceRegistry().getDocumentFolderService().deleteNode(node);

            // UNSync if necessary
            if (SyncContentManager.getInstance(context).isRootSynced(getAccount(), parentFolder))
            {
                Operator.with(context, getAccount())
                        .load(new SyncNodeRequest.Builder(node.getIdentifier(), false, false));
            }

        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Void> result)
    {
        super.onPostExecute(result);

        // Analytics
        AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                AnalyticsManager.ACTION_DELETE,
                node.isDocument() ? ((Document) node).getContentStreamMimeType() : AnalyticsManager.TYPE_FOLDER, 1,
                result.hasException());

        EventBusManager.getInstance().post(new DeleteNodeEvent(getRequestId(), node));
    }
}
