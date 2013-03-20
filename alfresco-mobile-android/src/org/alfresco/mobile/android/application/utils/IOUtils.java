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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.alfresco.mobile.android.application.manager.StorageManager;

import android.content.Context;
import android.util.Log;

public class IOUtils
{
    static final private String TAG = "IOUtils";

    static Vector<String> filesEncrypted = null;

    static Vector<String> filesDecrypted = null;

    static final String encryptionExtension = ".etmp";

    static final String decryptionExtension = ".utmp";

    private static final String TEMP_FILESTAMP = "-yyyyddMM-HHmmss";

    private static final String TEMP_FILE_EXT = ".tmp";

    private static final int TEMP_LEN = TEMP_FILESTAMP.length() + TEMP_FILE_EXT.length();

    public static File makeTempFile(File f)
    {
        String timeStamp = new SimpleDateFormat(TEMP_FILESTAMP).format(new Date());
        File newFile = new File(f.getPath() + timeStamp + TEMP_FILE_EXT);

        if (f.renameTo(newFile)) return newFile;

        return f;
    }

    public static File returnTempFileToOriginal(File f)
    {
        if (f.getName().endsWith(".tmp"))
        {
            String name = f.getPath();
            File newFile = new File(name.substring(0, name.length() - TEMP_LEN));

            if (f.renameTo(newFile)) return newFile;
        }

        return f;
    }
    
    public static String getOriginalFromTempFilename (String filename)
    {
        if (filename.endsWith(".tmp"))
        {
            return filename.substring(0, filename.length() - TEMP_LEN);
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

    public static String writeAsset(Context c, String assetFilename) throws IOException
    {
        String newFilename = "";

        File folder = StorageManager.getAssetFolder(c, SessionUtils.getAccount(c).getUrl(), SessionUtils.getAccount(c)
                .getUsername());
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
                        Log.e("Alfresco", "File copy failed for " + sourceFile.getName());
                        break;
                    }

                    if (move) sourceFile.delete();
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
                String destFilename = file[i].getPath() + encryptionExtension;

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
                                if (new File(filesEncrypted.get(j) + encryptionExtension).delete())
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
                    File dest = new File(filesEncrypted.get(j) + encryptionExtension);

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
                String destFilename = file[i].getPath() + decryptionExtension;

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
                                if (new File(filesDecrypted.get(j) + decryptionExtension).delete())
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
                    File dest = new File(filesDecrypted.get(j) + decryptionExtension);

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
}
