/*
 *  Copyright (C) 2005-2020 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.alfresco.mobile.android.platform.network;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import org.alfresco.mobile.android.api.network.NetworkHttpInvoker;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConnectionProvider implements NetworkHttpInvoker.ConnectionProvider {
    protected static final Object LOCK = new Object();
    protected static ConnectionProvider mInstance;

    private OkHttpClient httpClient;
    private OkUrlFactory urlFactory;
    private CookieManager cookieManager;

    public static ConnectionProvider getInstance()
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new ConnectionProvider();
            }

            return mInstance;
        }
    }

    private ConnectionProvider()
    {
        httpClient = NetworkSingleton.getInstance().getHttpClient();

        // Create cookie handler to ensure sticky sessions work
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        httpClient.setCookieHandler(cookieManager);

        urlFactory = new OkUrlFactory(httpClient);
    }

    public void putCookies(String url, String cookies)
    {
        URI uri = null;
        try
        {
            uri = new URI(url);
        } catch (URISyntaxException ignored) { return; }

        HashMap<String, List<String>>  headers = new HashMap<>();
        headers.put("Set-Cookie", Arrays.asList(cookies.split("; ")));

        CookieStore cookieStore = cookieManager.getCookieStore();
        for (HttpCookie cookie : cookieStore.get(uri))
        {
            cookieStore.remove(uri, cookie);
        }


        try
        {
            cookieManager.put(uri, headers);
        } catch (IOException ignore) { }
    }

    @Override
    public HttpURLConnection getHttpURLConnection(URL url) throws IOException
    {
        return urlFactory.open(url);
    }
}
