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
package org.alfresco.mobile.android.application.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.mobile.android.application.manager.StorageManager;

import android.content.Context;
import android.os.Environment;

public class IOUtils
{
    public static File createFolder(File f, String extendedPath)
    {
        File tmpFolder = null;
        tmpFolder = new File(f, extendedPath);
        if (!tmpFolder.exists())
        {
            tmpFolder.mkdirs();
        }

        return tmpFolder;
    }
    
    public static String writeAsset(Context c, String assetFilename) throws IOException
    {
        String newFilename = "";

        File folder = StorageManager.getAssetFolder(c, SessionUtils.getAccount(c).getUrl(), SessionUtils.getAccount(c).getUsername());
        if (folder != null)
        {
            newFilename = folder.getPath() + File.separator + assetFilename;
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;

            try
            {
                InputStream is = c.getAssets().open(assetFilename);
                OutputStream os = new FileOutputStream(newFilename);

                bis = new BufferedInputStream(is);
                bos = new BufferedOutputStream(os);
                byte[] buf = new byte[1024];

                int n = 0;
                int o = 0;
                while ((n = bis.read(buf, o, buf.length)) > 0)
                {
                    bos.write(buf, 0, n);
                }
            }
            catch (IOException e)
            {
                newFilename = "";
            }

            if (bis != null)
            {
                bis.close();
            }
            if (bos != null)
            {
                bos.close();
            }
        }

        return newFilename;
    }

    //Helper method with string arguments
    public static boolean copyFile(String source, String dest) throws IOException
    {
        File destFile = new File (dest);
        InputStream sourceFile = new FileInputStream(source);
        
        //Fully qualified package due to IOUtils name conflict.
        boolean result = org.alfresco.mobile.android.api.utils.IOUtils.copyFile(sourceFile, destFile);
        
        sourceFile.close();
        
        return result;
    }
    
    public static boolean isFolderEmpty (File folder)
    {        
        int nFiles = folder.listFiles().length;
        return (nFiles == 0);
    }
    
    public static void transferFilesBackground (final String sourceFolder, final String destFolder, final String additionalFolder, final boolean move, final boolean recursive)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                transferFilesUnderNewStructure (sourceFolder, destFolder, additionalFolder, move, recursive);
                super.run();
            }
        }.start();
    }
    
    public static boolean transferFiles (String sourceFolder, String destFolder, boolean move, boolean recursive)
    {
        return transferFilesUnderNewStructure (sourceFolder, destFolder, "", move, recursive);
    }
    
    public static boolean transferFilesUnderNewStructure (String sourceFolder, String destFolder, String additionalFolder, boolean move, boolean recursive)
    {
        boolean result = true;
        
        try
        {
            File f = new File(sourceFolder);        
            File file[] = f.listFiles();
            
            for (int i = 0;  i < file.length; i++)
            {
                File sourceFile = file[i];
                File destFile = new File(destFolder + File.separator + file[i].getName());
                
                if (sourceFile.isFile())
                {
                    if (additionalFolder != null  &&  additionalFolder.length() > 0)
                    {
                        destFile = createFolder (destFile.getParentFile(), additionalFolder);
                        destFile = new File(destFile, file[i].getName());
                    }
                    
                    result = copyFile (sourceFile.getPath(), destFile.getPath() );
                }
                else
                {
                    if (sourceFile.isDirectory()  &&  recursive  &&
                       !sourceFile.getName().equals(".")  &&  !sourceFile.getName().equals("..") )
                    {
                        result = transferFilesUnderNewStructure (sourceFile.getPath(), destFile.getPath(), additionalFolder, move, recursive);
                    }
                }
                
                if (!result)
                    break;
                
                if (move)
                    sourceFile.delete();
            }
            
            return result;
        }
        catch (Exception e)
        {
            return false;
        }   
    }
}
