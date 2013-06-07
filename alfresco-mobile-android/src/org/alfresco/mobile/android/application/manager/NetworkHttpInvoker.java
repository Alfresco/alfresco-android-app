package org.alfresco.mobile.android.application.manager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.squareup.okhttp.OkHttpClient;

public class NetworkHttpInvoker extends org.alfresco.mobile.android.api.network.NetworkHttpInvoker
{

    private OkHttpClient httpClient;
    
    public NetworkHttpInvoker()
    {
        httpClient = new OkHttpClient();
    }
    
    @Override
    protected HttpURLConnection getHttpURLConnection(URL url) throws IOException
    {
        return httpClient.open(url);
    }
    
}
