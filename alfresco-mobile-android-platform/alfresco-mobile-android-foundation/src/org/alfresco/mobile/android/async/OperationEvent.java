/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.async;

public abstract class OperationEvent<T>
{
    public final String requestId;

    public final Exception exception;

    public final boolean hasException;

    public final T data;

    public OperationEvent(String requestId, LoaderResult<T> result)
    {
        this.requestId = requestId;
        if (result != null)
        {
            this.exception = result.getException();
            this.hasException = result.hasException();
            this.data = result.getData();
        }
        else
        {
            this.exception = null;
            this.hasException = false;
            this.data = null;
        }
    }

    public OperationEvent(String requestId, T data, Exception exception)
    {
        this.requestId = requestId;
        this.exception = exception;
        this.hasException = (exception != null);
        this.data = data;
    }

    public OperationEvent(String requestId, Exception exception)
    {
        this.requestId = requestId;
        this.exception = exception;
        this.hasException = true;
        this.data = null;
    }

    public OperationEvent(String requestId, T data)
    {
        this.requestId = requestId;
        this.exception = null;
        this.hasException = false;
        this.data = data;
    }
}
