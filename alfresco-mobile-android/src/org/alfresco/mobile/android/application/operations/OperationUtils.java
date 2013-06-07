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
package org.alfresco.mobile.android.application.operations;

import android.content.Context;
import android.net.Uri;

public final class OperationUtils
{
    private OperationUtils(){
    }

    public static void removeOperationUri(Context context, OperationsGroupResult result)
    {
        Uri operationUri = null;
        for (OperationRequest operationRequest : result.completeRequest)
        {
            operationUri = operationRequest.getNotificationUri();
            context.getContentResolver().delete(operationUri, null, null);
        }
        
        for (OperationRequest operationRequest : result.failedRequest)
        {
            operationUri = operationRequest.getNotificationUri();
            context.getContentResolver().delete(operationUri, null, null);
        }
    }
    
}
