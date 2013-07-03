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
package org.alfresco.mobile.android.application.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtils
{
    //private static final String TAG = "IOUtils";

    private IOUtils(){
    }
    
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

    // Helper method with string arguments
    public static boolean copyFile(String source, String dest) throws IOException
    {
        File destFile = new File(dest);
        InputStream sourceFile = new FileInputStream(source);

        // Fully qualified package due to IOUtils name conflict.
        boolean result = org.alfresco.mobile.android.api.utils.IOUtils.copyFile(sourceFile, destFile);

        sourceFile.close();

        return result;
    }

    public static boolean isFolderEmpty(File folder)
    {
        int nFiles = folder.listFiles().length;
        return (nFiles == 0);
    }

    public static String extractFileExtension(String fileName)
    {
        int dotInd = fileName.lastIndexOf('.');

        // if dot is in the first position,
        // we are dealing with a hidden file rather than an extension
        return (dotInd > 0 && dotInd < fileName.length()) ? fileName.substring(dotInd + 1) : "";
    }

    private static String getFileExtension(String fileName)
    {
        return "." + extractFileExtension(fileName);
    }

    /* XXX From libcore.io.IoUtils */
    public static void deleteContents(File dir) throws IOException
    {
        File[] files = dir.listFiles();
        if (files == null) { throw new IllegalArgumentException("not a directory: " + dir); }
        for (File file : files)
        {
            if (file.isDirectory())
            {
                deleteContents(file);
            }
            if (!file.delete()) { throw new IOException("failed to delete file: " + file); }
        }
    }

    private static File createUniqueName(File file)
    {
        String fileNameWithoutExtension = file.getName().replaceFirst("[.][^.]+$", "");
        File tmpFile = file;
        int index = 1;
        while (tmpFile.exists())
        {
            tmpFile = new File(tmpFile.getParentFile(), fileNameWithoutExtension + "-" + index
                    + getFileExtension(tmpFile.getName()));
            index++;
        }
        return tmpFile;
    }

    public static File createFile(File contentFile)
    {
        contentFile.getParentFile().mkdirs();
        return createUniqueName(contentFile);
    }

}
