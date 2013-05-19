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
package org.alfresco.mobile.android.application.integration.node.create;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationCallback;
import org.alfresco.mobile.android.application.integration.node.AbstractUpRequest;
import org.alfresco.mobile.android.application.integration.node.AbstractUpTask;
import org.alfresco.mobile.android.application.manager.NotificationHelper;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.content.Context;

/**
 * UploadService is responsible to upload document from the device to the
 * repository.
 * 
 * @author Jean Marie Pascal
 */
public class CreateDocumentCallback extends AbstractOperationCallback<Document>
{
    //private static final String TAG = CreateDocumentCallback.class.getName();

    public CreateDocumentCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.upload_in_progress);
        complete = getBaseContext().getString(R.string.upload_complete);
    }

    // ////////////////////////////////////////////////////
    // LISTENERS + NOTIFIERS
    // ////////////////////////////////////////////////////
    @Override
    public void onProgressUpdate(Operation<Document> task, Long values)
    {
        if (values == 100)
        {
            NotificationHelper.createIndeterminateNotification(getBaseContext(),
                    ((AbstractUpTask) task).getDocumentName(), getBaseContext().getString(R.string.action_processing),
                    totalItems - pendingItems + "/" + totalItems);
        }
        else
        {
            NotificationHelper.createProgressNotification(getBaseContext(),
                    ((AbstractUpTask) task).getDocumentName(), getBaseContext().getString(R.string.upload_in_progress),
                    totalItems - pendingItems + "/" + totalItems, values,
                    ((AbstractUpRequest) task.getOperationRequest()).getContentStreamLength());
        }
    }

    @Override
    public void onPostExecute(Operation<Document> task, Document results)
    {
        super.onPostExecute(task, results);
        if (task instanceof CreateDocumentTask && ((CreateDocumentTask) task).isCreation())
        {
            ((AbstractUpTask) task).getContentFile().getFile().delete();
        }
    }

    @Override
    public void onError(Operation<Document> task, Exception e)
    {
        // An error occurs, notify the user.
        if (((AbstractUpTask) task).getContentFile() != null)
        {
            NotificationHelper.createSimpleNotification(getBaseContext(), ((AbstractUpTask) task).getDocumentName(),
                    getBaseContext().getString(R.string.import_error), totalItems - pendingItems + "/" + totalItems);

            // During creation process, the content must be available on
            // Download area.
            // The file is move from capture to download.
            if (task instanceof CreateDocumentTask && ((CreateDocumentTask) task).isCreation())
            {
                ContentFile contentFile = ((AbstractUpTask) task).getContentFile();
                // TODO Identifier is wrong when switching!
                final File folderStorage = StorageManager.getDownloadFolder(getBaseContext(),
                        ((CreateDocumentTask) task).getSession().getBaseUrl(), ((CreateDocumentTask) task).getSession()
                                .getPersonIdentifier());

                File dlFile = new File(folderStorage, contentFile.getFileName());
                if (dlFile.exists())
                {
                    String timeStamp = new SimpleDateFormat("yyyyddMM_HHmmss-").format(new Date());
                    dlFile = new File(folderStorage, timeStamp + contentFile.getFileName());
                }

                if (contentFile.getFile().renameTo(dlFile))
                {
                    MessengerManager.showLongToast(getBaseContext(),
                            getBaseContext().getString(R.string.create_document_save));
                }
                else
                {
                    MessengerManager.showToast(getBaseContext(), R.string.error_general);
                }
            }
        }
    }
}
