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
package org.alfresco.mobile.android.application.operations.batch.node.update;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.NotificationHelper;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateDocumentCallback;

import android.content.Context;

/**
 * 
 * @author Jean Marie Pascal
 */
public class UpdateContentCallback extends CreateDocumentCallback
{
    public UpdateContentCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.update_in_progress);
        complete = getBaseContext().getString(R.string.update_complete);
        finalComplete = R.plurals.update_complete_description;
    }
    
    protected int getNotificationId()
    {
        return NotificationHelper.UPLOAD_NOTIFICATION_ID;
    }
}
