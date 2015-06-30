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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

public class NetworkHttpInvoker extends org.alfresco.mobile.android.api.network.NetworkHttpInvoker
{
    private OkHttpClient httpClient;

    private OkUrlFactory urlFactory;

    public NetworkHttpInvoker()
    {
        httpClient = NetworkSingleton.getInstance().getHttpClient();
        urlFactory = new OkUrlFactory(NetworkSingleton.getInstance().getHttpClient());
    }

    @Override
    protected HttpURLConnection getHttpURLConnection(URL url) throws IOException
    {
        return urlFactory.open(url);
    }
}
