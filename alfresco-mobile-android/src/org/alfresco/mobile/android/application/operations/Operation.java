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
