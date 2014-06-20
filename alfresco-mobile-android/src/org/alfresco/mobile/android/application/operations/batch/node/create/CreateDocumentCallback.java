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
package org.alfresco.mobile.android.application.operations.batch.node.create;

import java.io.File;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.NotificationHelper;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;
import org.alfresco.mobile.android.application.operations.batch.node.AbstractUpRequest;
import org.alfresco.mobile.android.application.operations.batch.node.AbstractUpThread;
import org.alfresco.mobile.android.application.utils.IOUtils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

/**
 * @author Jean Marie Pascal
 */
public class CreateDocumentCallback extends AbstractBatchOperationCallback<Document>
{
    private static final String TAG = CreateDocumentCallback.class.getName();

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public CreateDocumentCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.upload_in_progress);
        complete = getBaseContext().getString(R.string.upload_complete);
        finalComplete = R.plurals.upload_complete_description;
    }

    // ////////////////////////////////////////////////////
    // LIFE CYCLE
    // ////////////////////////////////////////////////////
    @Override
    public void onProgressUpdate(Operation<Document> task, Long values)
    {
        groupRecord = BatchOperationManager.getInstance(context).getOperationGroup(task.getOperationId());
        if (groupRecord.totalRequests == 1)
        {
            if (values == ((AbstractUpRequest) task.getOperationRequest()).getContentStreamLength())
            {
                NotificationHelper.createIndeterminateNotification(getBaseContext(), getNotificationId(),
                        ((AbstractUpThread) task).getDocumentName(),
                        getBaseContext().getString(R.string.action_processing), groupRecord.completeRequest.size()
                                + "/" + totalItems);
            }
            else
            {
                NotificationHelper.createProgressNotification(getBaseContext(), getNotificationId(),
                        ((AbstractUpThread) task).getDocumentName(), inProgress, groupRecord.completeRequest.size()
                                + "/" + groupRecord.totalRequests, values,
                        ((AbstractUpRequest) task.getOperationRequest()).getContentStreamLength());
            }
        }
    }

    @Override
    public void onPostExecute(Operation<Document> task, Document results)
    {
        super.onPostExecute(task, results);
        if (task instanceof CreateDocumentThread && ((CreateDocumentThread) task).isCreation())
        {
            ((AbstractUpThread) task).getContentFile().getFile().delete();
        }
    }

    @Override
    public void onError(Operation<Document> task, Exception e)
    {
        // An error occurs, notify the user.
        if (((AbstractUpThread) task).getContentFile() != null)
        {
            NotificationHelper.createSimpleNotification(getBaseContext(), getNotificationId(),
                    ((AbstractUpThread) task).getDocumentName(), getBaseContext().getString(R.string.import_error),
                    totalItems - pendingItems + "/" + totalItems);

            // During creation process, the content must be available on
            // Download area.
            // The file is move from capture to download.
            if (task instanceof CreateDocumentThread && ((CreateDocumentThread) task).isCreation())
            {
                ContentFile contentFile = ((AbstractUpThread) task).getContentFile();
                final File folderStorage = StorageManager.getDownloadFolder(getBaseContext(),
                        ((CreateDocumentThread) task).getAccount());

                File dlFile = new File(folderStorage, contentFile.getFileName());
                if (dlFile.exists())
                {
                    dlFile = new File(folderStorage, contentFile.getFileName());
                    dlFile = IOUtils.createFile(dlFile);
                }

                if (!contentFile.getFile().renameTo(dlFile))
                {
                    Log.e(TAG, "Unable to rename file");
                }
                else
                {
                    // Update Referential with the new file
                    ContentValues cValues = new ContentValues();
                    cValues.put(BatchOperationSchema.COLUMN_LOCAL_URI, dlFile.getPath());
                    context.getContentResolver().update(
                            ((CreateDocumentThread) task).getOperationRequest().getNotificationUri(), cValues, null,
                            null);
                   ((CreateDocumentRequest)((CreateDocumentThread) task).getOperationRequest()).setContentFile(dlFile);
                }
            }
        }
    }

    protected int getNotificationId()
    {
        return NotificationHelper.UPLOAD_NOTIFICATION_ID;
    }
}
