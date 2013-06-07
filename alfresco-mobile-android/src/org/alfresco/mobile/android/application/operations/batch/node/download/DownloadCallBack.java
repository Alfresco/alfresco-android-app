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
package org.alfresco.mobile.android.application.operations.batch.node.download;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.NotificationHelper;
import org.alfresco.mobile.android.application.operations.OperationsGroupCallBack;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationsGroupResult;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class DownloadCallBack extends AbstractBatchOperationCallback<ContentFile> implements OperationsGroupCallBack
{
    public DownloadCallBack(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
    }

    @Override
    public void onPreExecute(Operation<ContentFile> task)
    {
        NotificationHelper.createProgressNotification(getBaseContext(), NotificationHelper.DEFAULT_NOTIFICATION_ID,
                getBaseContext().getString(R.string.download_progress), ((DownloadThread) task).getDocument().getName(),
                totalItems - pendingItems + "/" + totalItems, 0, 100);
    }

    @Override
    public void onPostExecute(Operation<ContentFile> task, ContentFile results)
    {
        if (task.getCompleteBroadCastIntent() != null)
        {
            LocalBroadcastManager.getInstance(context).sendBroadcast(task.getCompleteBroadCastIntent());
        }
        
        // Improvement : Better notification with share button or open in.
        NotificationHelper.createIndeterminateNotification(getBaseContext(), NotificationHelper.DEFAULT_NOTIFICATION_ID,
                getBaseContext().getString(R.string.download_progress), ((DownloadThread) task).getDocument().getName(),
                totalItems - pendingItems + "/" + totalItems);
        /*getBaseContext().sendBroadcast(
                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(results.getFile())));*/
    }

    @Override
    public void onProgressUpdate(Operation<ContentFile> task, Long values)
    {
        if (values == 100)
        {
            NotificationHelper.createIndeterminateNotification(getBaseContext(),NotificationHelper.DEFAULT_NOTIFICATION_ID,
                    getBaseContext().getString(R.string.download_progress), ((DownloadThread) task).getDocument()
                            .getName(), totalItems - pendingItems + "/" + totalItems);
        }
        else
        {
            NotificationHelper.createProgressNotification(getBaseContext(),NotificationHelper.DEFAULT_NOTIFICATION_ID,
                    getBaseContext().getString(R.string.download_progress), ((DownloadThread) task).getDocument()
                            .getName(), totalItems - pendingItems + "/" + totalItems, values, ((DownloadRequest) task
                            .getOperationRequest()).getContentStreamLength());
        }
    }

    @Override
    public void onError(Operation<ContentFile> task, Exception e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPostExecution(OperationsGroupResult result)
    {
        Bundle b = new Bundle();
        b.putString(NotificationHelper.ARGUMENT_TITLE, getBaseContext().getString(R.string.download_complete));
        if (result.failedRequest.isEmpty())
        {
            b.putString(
                    NotificationHelper.ARGUMENT_DESCRIPTION,
                    String.format(getBaseContext().getString(R.string.batch_download_complete),
                            Integer.toString(result.totalRequests)));
        }
        else
        {
            b.putString(NotificationHelper.ARGUMENT_DESCRIPTION, result.failedRequest.size() + "/"
                    + result.totalRequests);
        }
        NotificationHelper.createNotification(getBaseContext(), NotificationHelper.DEFAULT_NOTIFICATION_ID, b);
    }
}
