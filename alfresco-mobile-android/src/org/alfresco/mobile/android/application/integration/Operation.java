package org.alfresco.mobile.android.application.integration;

import android.content.Intent;

public interface Operation<T>
{
    int STATUS_PENDING = 1;

    int STATUS_RUNNING = 2;

    int STATUS_PAUSED = 4;

    int STATUS_SUCCESSFUL = 8;

    int STATUS_FAILED = 16;

    int STATUS_CANCEL = 32;

    interface OperationCallBack<T>
    {
        void onPreExecute(Operation<T> task);

        void onPostExecute(Operation<T> task, T result);

        void onError(Operation<T> task, Exception e);

        void onProgressUpdate(Operation<T> task, Long values);
    }

    void setOperationCallBack(OperationCallBack<T> listener);

    Intent getCompleteBroadCastIntent();

    String getOperationId();

    OperationRequest getOperationRequest();

    Intent getStartBroadCastIntent();
}
