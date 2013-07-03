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
package org.alfresco.mobile.android.application.manager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class StorageManager extends org.alfresco.mobile.android.ui.manager.StorageManager
{
    private static final String TAG = "StorageManager";

    private static final String CAPTURE_DIRECTORY = "Capture";

    public static final String TEMPDIR = "Tmp";

    public static final String DLDIR = "Download";

    private static final String SYNCHRO_DIRECTORY = "Synchro";

    private static final String SHARE_DIRECTORY = "Share";

    private static final String ASSETDIR = "Assets";

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
    public static File getShareFolder(Context context, Account acc)
    {
        return getPrivateFolder(context, SHARE_DIRECTORY, acc);
    }

    public static File getDownloadFolder(Context context, Account acc)
    {
        return getPrivateFolder(context, DLDIR, acc);
    }

    public static File getTempFolder(Context context, Account acc)
    {
        return getPrivateFolder(context, TEMPDIR, acc);
    }

    public static File getCaptureFolder(Context context, Account acc)
    {
        return getPrivateFolder(context, CAPTURE_DIRECTORY, acc);
    }

    public static File getSynchroFolder(Context context, Account acc)
    {
        return getPrivateFolder(context, SYNCHRO_DIRECTORY, acc);
    }

    public static File getSynchroFile(Context context, Account acc, Document doc)
    {
        if (context != null && doc != null) { return getSynchroFile(context, acc, doc.getName(), doc.getIdentifier()); }
        return null;
    }

    public static File getSynchroFile(Context context, Account acc, String documentName, String nodeIdentifier)
    {
        if (context != null && acc != null)
        {
            File synchroFolder = StorageManager.getSynchroFolder(context, acc);
            File uuidFolder = new File(synchroFolder, NodeRefUtils.getNodeIdentifier(nodeIdentifier));
            uuidFolder.mkdirs();
            return new File(uuidFolder, documentName);
        }
        return null;
    }

    public static File getAssetFolder(Context context)
    {
        return getPrivateFolder(context, ASSETDIR, null);
    }

    /**
     * Returns a specific file/folder inside the private area of the
     * application.
     * 
     * @param context : Android context.
     * @param filePath : extended Path relative to the private folder.
     * @return the file object. This file might be exist.
     */
    public static File getFileInPrivateFolder(Context context, String filePath)
    {
        File file = null;
        try
        {
            if (isExternalStorageAccessible() && filePath != null && !filePath.isEmpty())
            {
                file = new File(context.getExternalFilesDir(null), filePath);
            }
        }
        catch (Exception e)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
        }

        return file;
    }
    
    public static File getRootPrivateFolder(Context context)
    {
        File file = null;
        try
        {
            if (isExternalStorageAccessible())
            {
                file = context.getExternalFilesDir(null);
            }
        }
        catch (Exception e)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.GENERAL_IO, e);
        }

        return file;
    }

    public static File getPrivateFolder(Context context, String requestedFolder, Account acc)
    {
        File folder = null;
        try
        {
            // NOTE: We must have access to external storage in order to get a
            // private folder for this Android logged in user.
            if (isExternalStorageAccessible())
            {
                folder = context.getExternalFilesDir(null);

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
    public static boolean isTempFile(Context c, File file)
    {
        if (SessionUtils.getAccount(c) == null) { return true; }
        File tempFolder = StorageManager.getTempFolder(c, SessionUtils.getAccount(c));

        return (tempFolder != null && file.getParent().compareTo(tempFolder.getPath()) == 0);
    }

    public static boolean isSyncFile(Context c, File file)
    {
        if (SessionUtils.getAccount(c) == null) { return true; }
        File tempFolder = StorageManager.getSynchroFolder(c, SessionUtils.getAccount(c));

        return (tempFolder != null && file.getParentFile().getParent().compareTo(tempFolder.getPath()) == 0);
    }
    
    public static boolean isSynchroFile(Context c, File file)
    {
        File tempFolder = c.getExternalFilesDir(null);
        String path = file.getPath();
        String[] pathS = path.split("/");
        return (tempFolder != null && file.getPath().startsWith(tempFolder.getPath()) &&  pathS[pathS.length - 3].contains(SYNCHRO_DIRECTORY));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MANAGE FILE
    // ///////////////////////////////////////////////////////////////////////////
    public static void manageFile(Context c, File file)
    {
        if (isTempFile(c, file))
        {
            file.delete();
            return;
        }

        if (DataProtectionManager.getInstance(c).isEncryptionEnable())
        {
            DataProtectionManager.getInstance(c).checkEncrypt(SessionUtils.getAccount(c), file);
        }
    }
}
