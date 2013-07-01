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
package org.alfresco.mobile.android.application.operations.batch.file.encryption;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;
import org.alfresco.mobile.android.application.security.DataProtectionManager;

import android.content.Context;

/**
 * @author Jean Marie Pascal
 */
public class DataProtectionCallback extends AbstractBatchOperationCallback<Void>
{
    public DataProtectionCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.data_protection);
        complete = getBaseContext().getString(R.string.data_protection);
    }
    
    
    @Override
    public void onPreExecute(Operation<Void> task)
    {
        if (task.getOperationRequest() instanceof DataProtectionRequest){
            int intentAction = ((DataProtectionRequest)task.getOperationRequest()).getIntentAction();
            switch (intentAction)
            {
                case DataProtectionManager.ACTION_COPY:
                    inProgress = getBaseContext().getString(R.string.copy_file_title);
                    complete = getBaseContext().getString(R.string.copy_file_completed);
                    finalComplete = R.plurals.download_complete_description;
                    break;
                default:
                    break;
            }
        }
        super.onPreExecute(task);
    }
}
