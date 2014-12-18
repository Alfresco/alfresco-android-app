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

/**
 * Wrapper that contains data or error state from the remote API layer.
 * 
 * @author Jean Marie Pascal
 */
public class LoaderResult<T>
{
    /** Exception from remote API. */
    private Exception exception;

    /** Result data of loader. */
    private T data;

    /**
     * Flag to indicate if an error has been encounter during the proces.
     * 
     * @return true if something goes wrong in remote side.
     */
    public boolean hasException()
    {
        return (exception != null);
    }

    /**
     * @return Returns the exception raised during the execution.
     */
    public Exception getException()
    {
        return exception;
    }

    /**
     * @param Exception
     */
    public void setException(Exception exception)
    {
        this.exception = exception;
    }

    /**
     * @return the data from the remote service layer.
     */
    public T getData()
    {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(T data)
    {
        this.data = data;
    }

}
