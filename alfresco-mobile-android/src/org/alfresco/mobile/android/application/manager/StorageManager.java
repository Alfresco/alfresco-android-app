/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
    
    private static final String tempDir = "Capture";     
    private static final String dlDir = "Download";    
    private static final String assetDir = "Assets";    
    private static final String syncDir = "Sync";  
    
    
    private static boolean isExternalStorageAccessible()
    {
        return (Environment.getExternalStorageState().compareTo(Environment.MEDIA_MOUNTED) == 0);
    }
    
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

    public static File getDownloadFolder(Context context, String urlValue, String username)
    {
        return getPrivateFolder (context, dlDir, urlValue, username);
    }
    
    public static File getTempFolder(Context context, String urlValue, String username)
    {
        return getPrivateFolder (context, tempDir, urlValue, username);
    }
    
    public static File getCaptureFolder(Context context, String urlValue, String username)
    {
        return getPrivateFolder (context, tempDir, urlValue, username);
    }
    
    public static File getAssetFolder(Context context, String urlValue, String username)
    {
        return getPrivateFolder (context, assetDir, null, null);
    }
    
    public static File getSyncFolder(Context context, String urlValue, String username)
    {
        return getPrivateFolder (context, syncDir, urlValue, username);
    }
    
    private static File getPrivateFolder(Context context, String requestedFolder, String urlValue, String username)
    {
        File folder = null;
        try
        {
            //NOTE: We must have access to external storage in order to get a private folder for this Android logged in user.
            if (isExternalStorageAccessible())
            {
                folder = context.getExternalFilesDir(null);
                
                if (urlValue != null && urlValue.length() > 0 && username != null && username.length() > 0)
                    folder = createFolder(folder, getAccountFolder(urlValue, username) + File.separator + requestedFolder);
                else
                    folder = createFolder(folder, requestedFolder);
            }
        }
        catch (Exception e)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
        }

        return folder;
    }

    private static String getAccountFolder(String urlValue, String username)
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
