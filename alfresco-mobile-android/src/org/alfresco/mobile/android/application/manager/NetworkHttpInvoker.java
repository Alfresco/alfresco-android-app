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
package org.alfresco.mobile.android.application.manager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.alfresco.mobile.android.application.ApplicationManager;

import android.util.Log;

import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;

public class NetworkHttpInvoker extends org.alfresco.mobile.android.api.network.NetworkHttpInvoker
{
    private OkHttpClient httpClient;
    
    public NetworkHttpInvoker()
    {
        httpClient = NetworkSingleton.getInstance().getHttpClient();
    }
    
    @Override
    protected HttpURLConnection getHttpURLConnection(URL url) throws IOException
    {
        return httpClient.open(url);
    }
    
}
