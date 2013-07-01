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
package org.alfresco.mobile.android.application.upgrade;

import java.io.File;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.application.utils.IOUtils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public final class UpgradeVersion110
{

    private UpgradeVersion110(){
    }
    
    private static final String TAG = UpgradeVersion110.class.getName();

    public static void transferFilesBackground(final String sourceFolder, final String destFolder,
            final String additionalFolder, final boolean move, final boolean recursive)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                transferFilesUnderNewStructure(sourceFolder, destFolder, additionalFolder, move, recursive);
                super.run();
            }
        }.start();
    }

    public static boolean transferFilesUnderNewStructure(String sourceFolder, String destFolder,
            String additionalFolder, boolean move, boolean recursive)
    {
        boolean result = true;

        try
        {
            File f = new File(sourceFolder);
            File file[] = f.listFiles();

            for (int i = 0; i < file.length; i++)
            {
                File sourceFile = file[i];
                File destFile = new File(destFolder + File.separator + file[i].getName());

                if (!sourceFile.isHidden())
                {
                    if (sourceFile.isFile())
                    {
                        if (additionalFolder != null && additionalFolder.length() > 0)
                        {
                            destFile = IOUtils.createFolder(destFile.getParentFile(), additionalFolder);
                            destFile = new File(destFile, file[i].getName());
                        }

                        result = IOUtils.copyFile(sourceFile.getPath(), destFile.getPath());
                    }
                    else
                    {
                        if (sourceFile.isDirectory() && recursive && !sourceFile.getName().equals(".")
                                && !sourceFile.getName().equals(".."))
                        {
                            result = transferFilesUnderNewStructure(sourceFile.getPath(), destFile.getPath(),
                                    additionalFolder, move, recursive);
                        }
                    }

                    if (!result)
                    {
                        Log.e(TAG, "File copy failed for " + sourceFile.getName());
                        break;
                    }

                    if (move)
                    {
                        sourceFile.delete();
                    }
                }
            }

            return result;
        }
        catch (Exception e)
        {
            Log.e("Alfresco", "Error during file transfer: " + e.getMessage());
            Log.d(TAG, Log.getStackTraceString(e));

            return false;
        }
    }

    /*
     * Retrieve < v1.1 download folder. Used to migrate to new folder structures
     * in a one-off operation.
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

            folder = IOUtils
                    .createFolder(
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
