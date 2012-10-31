/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.manager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class StorageManager extends org.alfresco.mobile.android.ui.manager.StorageManager
{

    private static final String TAG = "StorageManager";
    
    protected static File createFolder(File f, String extendedPath)
    {
        File tmpFolder = null;
        tmpFolder = new File(f, extendedPath);
        if (!tmpFolder.exists())
        {
            tmpFolder.mkdirs();
        }

        return tmpFolder;
    }

    /**
     * Get specific access to DownloadFolder
     * 
     * @param context
     * @param extendedPath
     * @return
     * @throws IOException
     */
    public static File getDownloadFolder(Context context, String urlValue, String username)
    {
        File folder = null;
        try
        {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            {
                folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            }
            else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState()))
            {
                folder = Environment.getDownloadCacheDirectory();
            }

            folder = createFolder(
                    folder,
                    context.getResources()
                            .getText(
                                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.labelRes)
                            .toString());
            folder = createFolder(folder, getDownloadAccountFolder(urlValue, username));
        }
        catch (Exception e)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
        }

        return folder;
    }


    private static String getDownloadAccountFolder(String urlValue, String username)
    {
        String name = null;
        try
        {
            URL url = new URL(urlValue);
            name = url.getHost();
        }
        catch (MalformedURLException e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return name + "-" + username;
    }

}
