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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.alfresco.mobile.android.application.security.CipherUtils;

import android.content.Context;
import android.util.Log;

public class IOUtils
{
    private static final String TAG = "IOUtils";

    private static Vector<String> filesEncrypted = null;

    private static Vector<String> filesDecrypted = null;

    private static final String ENCRYPTION_EXTENSION = ".etmp";

    private static final String DECRYPTION_EXTENSION = ".utmp";

    public static final String TEMP_PREFIX = "Decrypted@";

    private static final String TEMP_FILESTAMP = "HH-mm MM-dd-yy ";

    private static final int TEMP_LEN = TEMP_PREFIX.length() + TEMP_FILESTAMP.length();

    public static File makeTempFile(File f)
    {
        String timeStamp = new SimpleDateFormat(TEMP_FILESTAMP).format(new Date());
        File newFile = new File(f.getParent() + "/" + TEMP_PREFIX + timeStamp + f.getName());

        if (f.renameTo(newFile)) return newFile;

        return f;
    }

    public static File returnTempFileToOriginal(File f)
    {
        if (f.getName().startsWith(TEMP_PREFIX))
        {
            String name = f.getName();
            File newFile = new File(f.getParent() + "/" + name.substring(TEMP_LEN, name.length()));

            if (f.renameTo(newFile)) { return newFile; }
        }

        return f;
    }

    public static String getOriginalFromTempFilename(String filename)
    {
        File origFile = new File(filename);
        String name = origFile.getName();

        if (name.startsWith(TEMP_PREFIX))
        {
            return origFile.getParent() + "/" + name.substring(TEMP_LEN, name.length());
        }
        else
        {
            return filename;
        }
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

    public static boolean transferFiles(String sourceFolder, String destFolder, boolean move, boolean recursive)
    {
        return transferFilesUnderNewStructure(sourceFolder, destFolder, "", move, recursive);
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
                            destFile = createFolder(destFile.getParentFile(), additionalFolder);
                            destFile = new File(destFile, file[i].getName());
                        }

                        result = copyFile(sourceFile.getPath(), destFile.getPath());
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
     * Encrypt an entire folder, recursively if required. Rollback is
     * implemented if any failures occur. NOTE: This method is not thread-safe.
     */
    public static boolean encryptFiles(Context ctxt, String sourceFolder, boolean recursive)
    {
        return decryptFiles(ctxt, sourceFolder, null, recursive);
    }

    public static boolean encryptFiles(Context ctxt, String sourceFolder, Vector<String> withinFolders,
            boolean recursive)
    {
        boolean startPoint = false;
        boolean result = true;

        if (filesEncrypted == null)
        {
            filesEncrypted = new Vector<String>();
            startPoint = true;
        }
        try
        {
            File f = new File(sourceFolder);
            File file[] = f.listFiles();

            for (int i = 0; i < file.length; i++)
            {
                File sourceFile = file[i];
                String destFilename = file[i].getPath() + ENCRYPTION_EXTENSION;

                if (!sourceFile.isHidden())
                {
                    if (sourceFile.isFile())
                    {
                        for (String item : withinFolders)
                        {
                            if (item.equals(sourceFile.getParentFile().getName()))
                            {
                                result = CipherUtils.encryptFile(ctxt, sourceFile.getPath(), destFilename, true);
                                if (result == true) filesEncrypted.add(sourceFile.getPath());
                            }
                        }
                    }
                    else
                    {
                        if (sourceFile.isDirectory() && recursive && !sourceFile.getName().equals(".")
                                && !sourceFile.getName().equals(".."))
                        {
                            result = encryptFiles(ctxt, sourceFile.getPath(), withinFolders, recursive);
                        }
                    }

                    if (!result)
                    {
                        if (filesEncrypted != null)
                        {
                            Log.e("Alfresco", "Folder encryption failed for " + sourceFile.getName());

                            // Remove the encrypted versions done so far.
                            Log.i("Alfresco", "Encryption rollback in progress...");
                            for (int j = 0; j < filesEncrypted.size(); j++)
                            {
                                if (new File(filesEncrypted.get(j) + ENCRYPTION_EXTENSION).delete())
                                    Log.i("Alfresco", "Deleted encrypted version of " + filesEncrypted.get(j));
                            }
                            filesEncrypted.clear();
                            filesEncrypted = null;
                        }

                        break;
                    }
                }
            }

            if (result && startPoint)
            {
                // Whole folder encrypt succeeded. Move over to new encrypted
                // versions.

                for (int j = 0; j < filesEncrypted.size(); j++)
                {
                    File src = new File(filesEncrypted.get(j));
                    File dest = new File(filesEncrypted.get(j) + ENCRYPTION_EXTENSION);

                    //
                    // Two-stage delete for failsafe operation.
                    //
                    File tempSrc = new File(filesEncrypted.get(j) + ".mov");
                    if (src.renameTo(tempSrc))
                    {
                        // Put encrypted version in originals place.
                        if (dest.renameTo(src))
                        {
                            // Delete the original unencrypted temp file.
                            if (!tempSrc.delete())
                            {
                                // At least rename it out of the way with a temp
                                // extension, and nuke its content.
                                Log.w("Alfresco",
                                        "Could not delete original file. Nuking and renaming it " + tempSrc.getPath());
                                CipherUtils.nukeFile(tempSrc, -1);
                            }
                        }
                        else
                        {
                            tempSrc.renameTo(src);
                        }
                    }
                }
                filesEncrypted.clear();
                filesEncrypted = null;
            }

            return result;
        }
        catch (Exception e)
        {
            Log.e("Alfresco", "Error during folder encryption: " + e.getMessage());
            Log.d(TAG, Log.getStackTraceString(e));

            return false;
        }
    }

    /*
     * Encrypt an entire folder, recursively if required. Rollback is
     * implemented if any failures occur. NOTE: This method is not thread-safe.
     */
    public static boolean decryptFiles(Context ctxt, String sourceFolder, boolean recursive)
    {
        return decryptFiles(ctxt, sourceFolder, null, recursive);
    }

    public static boolean decryptFiles(Context ctxt, String sourceFolder, Vector<String> withinFolders,
            boolean recursive)
    {
        boolean startPoint = false;
        boolean result = true;

        if (filesDecrypted == null)
        {
            filesDecrypted = new Vector<String>();
            startPoint = true;
        }
        try
        {
            File f = new File(sourceFolder);
            File file[] = f.listFiles();

            for (int i = 0; i < file.length; i++)
            {
                File sourceFile = file[i];
                String destFilename = file[i].getPath() + DECRYPTION_EXTENSION;

                if (!sourceFile.isHidden())
                {
                    if (sourceFile.isFile())
                    {
                        for (String item : withinFolders)
                        {
                            if (item.equals(sourceFile.getParentFile().getName()))
                            {
                                result = CipherUtils.decryptFile(ctxt, sourceFile.getPath(), destFilename);
                                if (result == true) filesDecrypted.add(sourceFile.getPath());
                            }
                        }
                    }
                    else
                    {
                        if (sourceFile.isDirectory() && recursive && !sourceFile.getName().equals(".")
                                && !sourceFile.getName().equals(".."))
                        {
                            result = decryptFiles(ctxt, sourceFile.getPath(), withinFolders, recursive);
                        }
                    }

                    if (!result)
                    {
                        if (filesDecrypted != null)
                        {
                            Log.e("Alfresco", "Folder decryption failed for " + sourceFile.getName());

                            // Remove the decrypted versions done so far.
                            Log.i("Alfresco", "Decryption rollback in progress...");
                            for (int j = 0; j < filesDecrypted.size(); j++)
                            {
                                if (new File(filesDecrypted.get(j) + DECRYPTION_EXTENSION).delete())
                                    Log.i("Alfresco", "Deleted decrypted version of " + filesDecrypted.get(j));
                            }
                            filesDecrypted.clear();
                            filesDecrypted = null;
                        }

                        break;
                    }
                }
            }

            if (result && startPoint)
            {
                // Whole folder decrypt succeeded. Move over to new decrypted
                // versions.

                for (int j = 0; j < filesDecrypted.size(); j++)
                {
                    File src = new File(filesDecrypted.get(j));
                    File dest = new File(filesDecrypted.get(j) + DECRYPTION_EXTENSION);

                    //
                    // Two-stage delete for failsafe operation.
                    //
                    File tempSrc = new File(filesDecrypted.get(j) + ".mov");
                    if (src.renameTo(tempSrc))
                    {
                        // Put decrypted version in originals place.
                        if (dest.renameTo(src))
                        {
                            // Delete the original decrypted temp file.
                            if (!tempSrc.delete())
                            {
                                Log.w("Alfresco", "Could not delete original file " + tempSrc.getPath());
                            }
                        }
                        else
                        {
                            tempSrc.renameTo(src);
                        }
                    }
                }
                filesDecrypted.clear();
                filesDecrypted = null;
            }

            return result;
        }
        catch (Exception e)
        {
            Log.e("Alfresco", "Error during folder decryption: " + e.getMessage());
            Log.d(TAG, Log.getStackTraceString(e));

            return false;
        }
    }

    public static String extractFileExtension(String fileName)
    {
        int dotInd = fileName.lastIndexOf('.');

        // if dot is in the first position,
        // we are dealing with a hidden file rather than an extension
        return (dotInd > 0 && dotInd < fileName.length()) ? fileName.substring(dotInd + 1) : null;
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
        int index = 0;
        while (tmpFile.exists())
        {
            index++;
            tmpFile = new File(tmpFile.getParentFile(), fileNameWithoutExtension + "-" + index
                    + getFileExtension(tmpFile.getName()));
        }
        return tmpFile;
    }

    public static File createFile(File contentFile)
    {
        contentFile.getParentFile().mkdirs();
        return createUniqueName(contentFile);
    }

}
