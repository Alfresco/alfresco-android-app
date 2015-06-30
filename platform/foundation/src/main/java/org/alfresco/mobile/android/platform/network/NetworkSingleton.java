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
package org.alfresco.mobile.android.platform.network;

import java.net.URL;

import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public final class NetworkSingleton
{
    private static final String TAG = NetworkSingleton.class.getName();

    private static NetworkSingleton mInstance;

    private static final Object LOCK = new Object();

    private OkHttpClient httpClient;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static NetworkSingleton getInstance()
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new NetworkSingleton();
            }

            return mInstance;
        }
    }

    private NetworkSingleton()
    {
        httpClient = new OkHttpClient();
        httpClient.setConnectionPool(new ConnectionPool(1, 100));
    }

    public OkHttpClient getHttpClient()
    {
        return httpClient;
    }

}
