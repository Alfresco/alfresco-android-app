package org.alfresco.mobile.android.application.integration.node.download;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.OperationGroupResult;
import org.alfresco.mobile.android.application.integration.OperationService.BatchOperationCallBack;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationCallback;
import org.alfresco.mobile.android.application.manager.NotificationHelper;

import android.content.Context;
import android.os.Bundle;

public class DownloadCallBack extends AbstractOperationCallback<ContentFile> implements BatchOperationCallBack
{
    public DownloadCallBack(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
    }

    @Override
    public void onPreExecute(Operation<ContentFile> task)
    {
        NotificationHelper.createProgressNotification(getBaseContext(), ((DownloadTask) task).getDocument()
                .getName(), getBaseContext().getString(R.string.download_progress), totalItems - pendingItems + "/"
                + totalItems, 0, 100);
    }

    @Override
    public void onPostExecute(Operation<ContentFile> task, ContentFile results)
    {
        // Improvement : Better notification with share button or open in.
        NotificationHelper.createIndeterminateNotification(getBaseContext(), ((DownloadTask) task).getDocument()
                .getName(), getBaseContext().getString(R.string.download_progress), totalItems - pendingItems + "/"
                + totalItems);
    }

    @Override
    public void onProgressUpdate(Operation<ContentFile> task, Long values)
    {
        if (values == 100)
        {
            NotificationHelper.createIndeterminateNotification(getBaseContext(), ((DownloadTask) task).getDocument()
                    .getName(), getBaseContext().getString(R.string.download_progress), totalItems - pendingItems + "/"
                    + totalItems);
        }
        else
        {
            NotificationHelper.createProgressNotification(getBaseContext(), ((DownloadTask) task).getDocument()
                    .getName(), getBaseContext().getString(R.string.download_progress), totalItems - pendingItems + "/"
                    + totalItems, values, ((DownloadRequest)task.getOperationRequest()).getContentStreamLength());
        }
    }

    @Override
    public void onError(Operation<ContentFile> task, Exception e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPostBatchExecution(OperationGroupResult result)
    {
        Bundle b = new Bundle();
        b.putString(NotificationHelper.ARGUMENT_TITLE, getBaseContext().getString(R.string.download_complete));
        NotificationHelper.createNotification(getBaseContext(), b);
    }
}
