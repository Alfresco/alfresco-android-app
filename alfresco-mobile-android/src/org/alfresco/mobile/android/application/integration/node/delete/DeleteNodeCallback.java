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
package org.alfresco.mobile.android.application.integration.node.delete;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationCallback;

import android.content.Context;

/**
 * UploadService is responsible to upload document from the device to the
 * repository.
 * 
 * @author Jean Marie Pascal
 */
public class DeleteNodeCallback extends AbstractOperationCallback<Void>
{
    public DeleteNodeCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.delete_in_progress);
        complete = getBaseContext().getString(R.string.delete_complete);
    }
}
