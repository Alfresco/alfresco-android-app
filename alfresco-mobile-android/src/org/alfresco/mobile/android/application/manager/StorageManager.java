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
import java.net.MalformedURLException;
import java.net.URL;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import org.alfresco.mobile.android.application.utils.IOUtils;


public class StorageManager extends org.alfresco.mobile.android.ui.manager.StorageManager
{
    private static final String TAG = "StorageManager";
    
    public static final String TEMPDIR = "Capture";     
    public static final String DLDIR = "Download";    
    public static final String ASSETDIR = "Assets";    
    
    
    private static boolean isExternalStorageAccessible()
    {
        return (Environment.getExternalStorageState().compareTo(Environment.MEDIA_MOUNTED) == 0);
    }

    public static File getDownloadFolder(Context context, String urlValue, String username)
    {
        return getPrivateFolder (context, DLDIR, urlValue, username);
    }
    
    public static File getTempFolder(Context context, String urlValue, String username)
    {
        return getPrivateFolder (context, TEMPDIR, urlValue, username);
    }
    
    public static File getCaptureFolder(Context context, String urlValue, String username)
    {
        return getPrivateFolder (context, TEMPDIR, urlValue, username);
    }
    
    public static File getAssetFolder(Context context, String urlValue, String username)
    {
        return getPrivateFolder (context, ASSETDIR, null, null);
    }
    
    public static File getPrivateFolder(Context context, String requestedFolder, String urlValue, String username)
    {
        File folder = null;
        try
        {
            //NOTE: We must have access to external storage in order to get a private folder for this Android logged in user.
            if (isExternalStorageAccessible())
            {
                folder = context.getExternalFilesDir(null);
                
                if (urlValue != null && urlValue.length() > 0 && username != null && username.length() > 0)
                    folder = IOUtils.createFolder(folder, getAccountFolder(urlValue, username) + File.separator + requestedFolder);
                else
                    folder = IOUtils.createFolder(folder, requestedFolder);
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
    
    /*
     * Retrieve < v1.1 download folder.
     * Used to migrate to new folder structures in a one-off operation.
     */
    public static File getOldDownloadFolder(Context context)
    {
        File folder = null;
        try
        {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            {
                folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            }
            
            folder = createFolder(
                    folder,
                    context.getResources()
                            .getText(
                                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.labelRes)
                            .toString());
        }
        catch (Exception e)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
        }

        return folder;
    }
}
