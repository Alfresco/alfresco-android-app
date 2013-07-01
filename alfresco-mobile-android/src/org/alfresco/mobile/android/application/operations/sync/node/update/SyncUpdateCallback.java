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
package org.alfresco.mobile.android.application.operations.sync.node.update;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.NotificationHelper;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.sync.impl.AbstractSyncOperationCallback;
import org.alfresco.mobile.android.application.operations.sync.node.AbstractSyncUpRequest;
import org.alfresco.mobile.android.application.operations.sync.node.AbstractSyncUpThread;

import android.content.Context;

/**
 * @author Jean Marie Pascal
 */
public class SyncUpdateCallback extends AbstractSyncOperationCallback<Document>
{
    public SyncUpdateCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.sync_in_progress);
        complete = getBaseContext().getString(R.string.sync_complete);
    }

    // ////////////////////////////////////////////////////
    // LISTENERS + NOTIFIERS
    // ////////////////////////////////////////////////////
    @Override
    public void onProgressUpdate(Operation<Document> task, Long values)
    {
        if (values == PROGRESS_MAX)
        {
            NotificationHelper.createIndeterminateNotification(getBaseContext(), getNotificationId(),
                    ((AbstractSyncUpThread) task).getDocumentName(),
                    getBaseContext().getString(R.string.action_processing), totalItems - pendingItems + "/"
                            + totalItems);
        }
        else
        {
            NotificationHelper.createProgressNotification(getBaseContext(), getNotificationId(),
                    ((AbstractSyncUpThread) task).getDocumentName(),
                    getBaseContext().getString(R.string.upload_in_progress), totalItems - pendingItems + "/"
                            + totalItems, values,
                    ((AbstractSyncUpRequest) task.getOperationRequest()).getContentStreamLength());
        }
    }

    @Override
    public void onPostExecute(Operation<Document> task, Document results)
    {
        super.onPostExecute(task, results);
    }

    @Override
    public void onError(Operation<Document> task, Exception e)
    {
    }
}
