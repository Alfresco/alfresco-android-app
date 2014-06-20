/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;

import android.content.Context;
import android.net.Uri;

import com.squareup.picasso.OkHttpDownloader;

/**
 * @author Jean Marie Pascal
 */
public class ImageDownloader extends OkHttpDownloader
{
    private AlfrescoSession alfSession;

    public ImageDownloader(Context context, AlfrescoSession alfSession)
    {
        super(context);
        this.alfSession = alfSession;
    }

    @Override
    protected HttpURLConnection openConnection(Uri uri) throws IOException
    {
        HttpURLConnection conn = super.openConnection(uri);

        Map<String, List<String>> httpHeaders = ((AbstractAlfrescoSessionImpl) alfSession).getAuthenticationProvider()
                .getHTTPHeaders();
        // set other headers
        if (httpHeaders != null)
        {
            for (Map.Entry<String, List<String>> header : httpHeaders.entrySet())
            {
                if (header.getValue() != null)
                {
                    for (String value : header.getValue())
                    {
                        conn.addRequestProperty(header.getKey(), value);
                    }
                }
            }
        }
        return conn;
    }
}
