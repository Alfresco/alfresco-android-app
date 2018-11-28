package org.alfresco.mobile.android.async.local;

import android.util.Log;

import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.io.IOUtils;

import java.io.File;

/**
 * Created by Bogdan Roatis on 11/28/2018.
 */
public class CleanLocalFilesOperation extends BaseOperation<Void> {

    private static final String TAG = CleanLocalFilesOperation.class.getName();

    public CleanLocalFilesOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action) {
        super(operator, dispatcher, action);

        if (request instanceof CleanLocalFilesRequest) {
            this.accountId = ((CleanLocalFilesRequest) request).account.getId();
            this.acc = ((CleanLocalFilesRequest) request).account;
        }
    }

    @Override
    protected LoaderResult<Void> doInBackground() {

        LoaderResult<Void> result = new LoaderResult<Void>();

        try {
            File downloadFolder = AlfrescoStorageManager.getInstance(context).getDownloadFolder(acc);

            // Delete All local files in sync folder
            if (downloadFolder != null && downloadFolder.exists()) {
                IOUtils.deleteContents(downloadFolder);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Void> result) {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new CleanLocalFilesEvent());
    }
}
