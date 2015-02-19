/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.platform.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.platform.utils.SessionUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class AlfrescoStorageManager extends Manager
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTANTS
    // ///////////////////////////////////////////////////////////////////////////
    private static final String TAG = AlfrescoStorageManager.class.getName();

    private static final String DIRECTORY_CAPTURE = "Capture";

    private static final String DIRECTORY_TEMP = "Tmp";

    public static final String DIRECTORY_DOWNLOAD = "Download";

    private static final String DIRECTORY_SHARE = "Share";

    private static final String DIRECTORY_CONFIGURATION = "Config";

    private static final String DIRECTORY_CUSTOMIZATION = "Custom";

    private static final String DIRECTORY_ASSET = "Assets";

    // ///////////////////////////////////////////////////////////////////////////
    // MEMBERS
    // ///////////////////////////////////////////////////////////////////////////
    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    protected AlfrescoStorageManager(Context applicationContext)
    {
        super(applicationContext);
    }

    public static AlfrescoStorageManager getInstance(Context appContext)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = Manager.getInstance(appContext, AlfrescoStorageManager.class.getSimpleName());
            }
            return (AlfrescoStorageManager) mInstance;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void shutdown()
    {
        mInstance = null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // STATUS SDCARD
    // ///////////////////////////////////////////////////////////////////////////
    private static boolean isExternalStorageAccessible()
    {
        return (Environment.getExternalStorageState().compareTo(Environment.MEDIA_MOUNTED) == 0);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SHORTCUT
    // ///////////////////////////////////////////////////////////////////////////
    public File getConfigurationFolder(AlfrescoAccount acc)
    {
        return getPrivateFolder(DIRECTORY_CONFIGURATION, acc);
    }

    public File getCustomFolder(AlfrescoAccount acc)
    {
        return getPrivateFolder(DIRECTORY_CUSTOMIZATION, acc);
    }

    public File getConfigurationFolder()
    {
        return getPrivateFolder(DIRECTORY_CONFIGURATION, null);
    }

    public File getShareFolder(AlfrescoAccount acc)
    {
        return getPrivateFolder(DIRECTORY_SHARE, acc);
    }

    public File getDownloadFolder(AlfrescoAccount acc)
    {
        return getPrivateFolder(DIRECTORY_DOWNLOAD, acc);
    }

    public File getTempFolder(AlfrescoAccount acc)
    {
        return getPrivateFolder(DIRECTORY_TEMP, acc);
    }

    public File getCaptureFolder(AlfrescoAccount acc)
    {
        return getPrivateFolder(DIRECTORY_CAPTURE, acc);
    }

    public File getAssetFolder()
    {
        return getPrivateFolder(DIRECTORY_ASSET, null);
    }

    /**
     * Returns a specific file/folder inside the private area of the
     * application.
     * 
     * @param filePath : extended Path relative to the private folder.
     * @return the file object. This file might be exist.
     */
    public File getFileInPrivateFolder(String filePath)
    {
        File file = null;
        try
        {
            if (isExternalStorageAccessible() && filePath != null && !filePath.isEmpty())
            {
                file = new File(appContext.getExternalFilesDir(null), filePath);
            }
        }
        catch (Exception e)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
        }

        return file;
    }

    public File getRootPrivateFolder()
    {
        File file = null;
        try
        {
            if (isExternalStorageAccessible())
            {
                file = appContext.getExternalFilesDir(null);
            }
        }
        catch (Exception e)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
        }

        return file;
    }

    public File getPrivateFolder(AlfrescoAccount acc)
    {
        File folder = null;
        try
        {
            if (isExternalStorageAccessible())
            {
                folder = appContext.getExternalFilesDir(null);
                folder = new File(folder, getAccountFolder(acc.getUrl(), acc.getUsername()));
            }
        }
        catch (Exception e)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
        }

        return folder;
    }

    public File getPrivateFolder(String requestedFolder, AlfrescoAccount acc)
    {
        File folder = null;
        try
        {
            // NOTE: We must have access to external storage in order to get a
            // private folder for this Android logged in user.
            if (isExternalStorageAccessible())
            {
                folder = appContext.getExternalFilesDir(null);

                if (acc != null && acc.getUrl() != null && acc.getUrl().length() > 0 && acc.getUsername() != null
                        && acc.getUsername().length() > 0)
                {
                    folder = IOUtils.createFolder(folder, getAccountFolder(acc.getUrl(), acc.getUsername())
                            + File.separator + requestedFolder);
                }
                else
                {
                    folder = IOUtils.createFolder(folder, requestedFolder);
                }
            }
        }
        catch (Exception e)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
        }

        return folder;
    }

    public File getPrivateFolder(String requestedFolder, String username, String url)
    {
        File folder = null;
        try
        {
            // NOTE: We must have access to external storage in order to get a
            // private folder for this Android logged in user.
            if (isExternalStorageAccessible())
            {
                folder = appContext.getExternalFilesDir(null);

                if (url != null && url.length() > 0 && username != null && username.length() > 0)
                {
                    folder = IOUtils.createFolder(folder, getAccountFolder(url, username) + File.separator
                            + requestedFolder);
                }
                else
                {
                    folder = IOUtils.createFolder(folder, requestedFolder);
                }
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

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean isTempFile(File file)
    {
        if (SessionUtils.getAccount(appContext) == null) { return true; }
        File tempFolder = getTempFolder(SessionUtils.getAccount(appContext));

        return (tempFolder != null && file.getParent().compareTo(tempFolder.getPath()) == 0);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MANAGE FILE
    // ///////////////////////////////////////////////////////////////////////////
    public void manageFile(File file)
    {
        if (isTempFile(file))
        {
            file.delete();
        }

        /*
         * if (DataProtectionManager.getInstance(c).isEncryptionEnable()) {
         * DataProtectionManager
         * .getInstance(c).checkEncrypt(SessionUtils.getAccount(c), file); }
         */
    }

    // ///////////////////////////////////////////////////////////////////////////
    // STORAGE SPACE
    // ///////////////////////////////////////////////////////////////////////////
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressWarnings("deprecation")
    public float getAvailableBytesByPath(File f)
    {
        StatFs stat = new StatFs(f.getPath());
        if (AndroidVersion.isJBMR2OrAbove())
        {
            return stat.getAvailableBytes();
        }
        else
        {
            return (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        }
    }

    @SuppressWarnings("deprecation")
    public float getTotalBytesByPath(File f)
    {
        StatFs stat = new StatFs(f.getPath());
        if (AndroidVersion.isJBMR2OrAbove())
        {
            return stat.getTotalBytes();
        }
        else
        {
            return (long) stat.getBlockSize() * (long) stat.getBlockCount();
        }
    }

    public float getAvailableBytes()
    {
        return getAvailableBytesByPath(appContext.getExternalFilesDir(null));
    }

    public float getTotalBytes()
    {
        return getTotalBytesByPath(appContext.getExternalFilesDir(null));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // STORAGE SPACE
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Return the cache dir and check if exists
     * 
     * @param extendedPath
     * @return
     * @throws java.io.IOException
     */
    public File getCacheDir(String extendedPath)
    {
        File folder;
        try
        {
            folder = createFolder(appContext.getCacheDir(), extendedPath);
        }
        catch (Exception e)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
        }

        return folder;
    }

    protected static File createFolder(File f, String extendedPath)
    {
        File tmpFolder;
        tmpFolder = new File(f, extendedPath);
        if (!tmpFolder.exists())
        {
            tmpFolder.mkdirs();
            try
            {
                new File(tmpFolder, ".nomedia").createNewFile();
            }
            catch (IOException e)
            {
                throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
            }
        }

        return tmpFolder;
    }

    /**
     * Create the MD5 representation of a string
     * 
     * @param s
     * @return
     */
    public String md5(String s)
    {
        try
        {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
            {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            return hexString.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return "";
    }

    /**
     * Get specific access to DownloadFolder
     * 
     * @return
     * @throws java.io.IOException
     */
    public File getDownloadFolder(String urlValue, String username)
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
                    appContext
                            .getResources()
                            .getText(
                                    appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0).applicationInfo.labelRes)
                            .toString());
            folder = createFolder(folder, getDownloadAccountFolder(urlValue, username));
        }
        catch (Exception e)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
        }

        return folder;
    }

    /**
     * Return null if already exists
     * 
     * @param fileName
     * @return
     * @throws java.io.IOException
     */
    public File getDownloadFile(String urlValue, String username, String fileName)
    {
        File f = new File(getDownloadFolder(urlValue, username), fileName);
        if (!f.exists())
        {
            return f;
        }
        else
        {
            return null;
        }// trhow excception
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
