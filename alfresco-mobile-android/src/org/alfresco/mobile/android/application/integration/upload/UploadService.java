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
package org.alfresco.mobile.android.application.integration.upload;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.application.utils.ProgressNotification;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/**
 * UploadService is responsible to upload document from the device to the
 * repository.
 * 
 * @author Jean Marie Pascal
 */
public class UploadService extends Service
{

    public static final String ARGUMENT_SESSION = "session";

    public static final String ARGUMENT_FOLDER = "folder";

    public static final String ARGUMENT_CONTENT_FILE = "contentFileURI";

    public static final String ARGUMENT_CONTENT_NAME = "contentName";

    public static final String ARGUMENT_CONTENT_DESCRIPTION = "contentDescription";

    public static final String ARGUMENT_CONTENT_TAGS = "contentTags";

    public static final String ARGUMENT_TASK_ID = "taskId";

    /**
     * List of all current/pending upload task.
     */
    private Map<String, UploadDocumentTask> tasks = new HashMap<String, UploadDocumentTask>();

    public static void updateImportService(Context c, Bundle params)
    {
        c.startService(new Intent(c, UploadService.class).putExtras(params));
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        MessengerManager.showLongToast(this, getString(R.string.upload_in_progress));
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //If the intent has ARGUMENT_TASK_ID, it means the upload task has been done.
        if (intent.getExtras() != null && intent.getExtras().containsKey(ARGUMENT_TASK_ID))
        {
            tasks.remove(intent.getExtras().get(ARGUMENT_TASK_ID));
            if (tasks.isEmpty())
            {
                stopSelf();
            }
        } //In other case, we want to start another upload task.
        else if (intent.getExtras() != null)
        {
            handleUpload(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Responsible to init an upload task based on intent.getExtra values.
     * @param intent
     */
    private void handleUpload(Intent intent)
    {
        Bundle args = intent.getExtras();
        Map<String, Serializable> props = new HashMap<String, Serializable>(3);
        props.put(ContentModel.PROP_DESCRIPTION, args.getString(ARGUMENT_CONTENT_DESCRIPTION));
        props.put(ContentModel.PROP_TAGS, args.getStringArrayList(ARGUMENT_CONTENT_TAGS));
        props.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.DOCUMENT_BASETYPE_ID);

        String name = args.getString(ARGUMENT_CONTENT_NAME);
        ContentFile contentFile = (ContentFile) args.getSerializable(ARGUMENT_CONTENT_FILE);
        AlfrescoSession alfSession = args.getParcelable(ARGUMENT_SESSION);

        // Create the first Creation Notification.
        // Improvement : Manage multiple upload notification at once.
        // Improvement : merge with notificationService
        if (contentFile != null && name != null)
        {
            Bundle progressBundle = new Bundle();
            if (contentFile.getClass() == ContentFileProgressImpl.class)
            {
                ((ContentFileProgressImpl) contentFile).setFilename(name);
                progressBundle.putString(ProgressNotification.PARAM_DATA_NAME, name);
            }
            else
            {
                progressBundle.putString(ProgressNotification.PARAM_DATA_NAME, contentFile.getFile().getName());
            }

            progressBundle.putInt(ProgressNotification.PARAM_DATA_SIZE, (int) contentFile.getFile().length());
            progressBundle.putInt(ProgressNotification.PARAM_DATA_INCREMENT,
                    (int) (contentFile.getFile().length() / 10));

            ProgressNotification.createProgressNotification(getBaseContext(), progressBundle, getBaseContext()
                    .getClass());
        }

        // Start the Asynctask to do the upload in background.
        UploadDocumentTask task = (UploadDocumentTask) new UploadDocumentTask(getBaseContext(), alfSession,
                (Folder) args.get(ARGUMENT_FOLDER), args.getString(ARGUMENT_CONTENT_NAME), props,
                (ContentFile) args.getSerializable(ARGUMENT_CONTENT_FILE)).execute();
        tasks.put(task.getId(), task);
    }

}
