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
package org.alfresco.mobile.android.platform.security;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.file.encryption.AccountProtectionRequest;
import org.alfresco.mobile.android.async.file.encryption.FileProtectionRequest;
import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.io.IOUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class DataProtectionManager extends Manager
{
    // private static final String TAG = DataProtectionManager.class.getName();
    // ////////////////////////////////////////////////////
    // CONSTANTS
    // ////////////////////////////////////////////////////
    public static final int ACTION_NONE = -1;

    public static final int ACTION_VIEW = 1;

    public static final int ACTION_OPEN_IN = 2;

    public static final int ACTION_SEND = 4;

    public static final int ACTION_SEND_ALFRESCO = 8;

    public static final int ACTION_COPY = 16;

    protected static final String REQUIRES_ENCRYPT = "RequiresEncrypt";

    protected static final String DATA_PROTECTION_USER_REQUEST = "EncryptionUserInteraction";

    protected static final String DATA_PROTECTION_ENABLE = "privatefolders";

    // ////////////////////////////////////////////////////
    // MEMBERS
    // ////////////////////////////////////////////////////
    protected static Manager mInstance;

    protected static final Object LOCK = new Object();

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public static DataProtectionManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = Manager.getInstance(context, DataProtectionManager.class.getSimpleName());
            }

            return (DataProtectionManager) mInstance;
        }
    }

    protected DataProtectionManager(Context applicationContext)
    {
        super(applicationContext);
    }

    // ////////////////////////////////////////////////////
    // ENCRYPTION
    // ////////////////////////////////////////////////////
    public void copyAndEncrypt(AlfrescoAccount account, List<File> sourceFiles, File folderStorage)
    {
        if (account == null) { return; }
        List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(sourceFiles.size());
        File destinationFile;
        for (File sourceFile : sourceFiles)
        {
            destinationFile = new File(folderStorage, sourceFile.getName());
            destinationFile = IOUtils.createFile(destinationFile);
            if (isEncryptionEnable())
            {
                requestsBuilder.add(new FileProtectionRequest.Builder(sourceFile, destinationFile, true, ACTION_NONE)
                        .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
            }
            else
            {
                requestsBuilder.add(new FileProtectionRequest.Builder(sourceFile, destinationFile, true, ACTION_COPY)
                        .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
            }
        }
        Operator.with(appContext, SessionUtils.getAccount(appContext)).load(requestsBuilder);
    }

    public void copyAndEncrypt(File sourceFile, File destinationFile)
    {
        List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>();
        if (isEncryptionEnable())
        {
            requestsBuilder.add(new FileProtectionRequest.Builder(sourceFile, destinationFile, true, ACTION_NONE)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
        }
        else
        {
            requestsBuilder.add(new FileProtectionRequest.Builder(sourceFile, destinationFile, true, ACTION_COPY)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
        }
        Operator.with(appContext, SessionUtils.getAccount(appContext)).load(requestsBuilder);
    }

    public void encrypt(AlfrescoAccount account)
    {
        File folder = AlfrescoStorageManager.getInstance(appContext).getPrivateFolder("", null);
        Operator.with(appContext, SessionUtils.getAccount(appContext)).load(
                new AccountProtectionRequest.Builder(folder, true));
    }

    public void encrypt(AlfrescoAccount account, File file)
    {
        Operator.with(appContext, SessionUtils.getAccount(appContext)).load(
                new FileProtectionRequest.Builder(file, true));
    }

    public void checkEncrypt(AlfrescoAccount account, File file)
    {
        if (account == null) { return; }
        if (isEncryptable(account, file) && !isEncrypted(file.getPath()))
        {
            encrypt(account, file);
        }
    }

    // ////////////////////////////////////////////////////
    // DECRYPTION
    // ////////////////////////////////////////////////////
    public void decrypt(AlfrescoAccount account)
    {
        File folder = AlfrescoStorageManager.getInstance(appContext).getPrivateFolder("", null);
        Operator.with(appContext, SessionUtils.getAccount(appContext)).load(
                new AccountProtectionRequest.Builder(folder, false));
    }

    public void checkDecrypt(AlfrescoAccount account, File file)
    {
        decrypt(account, file, ACTION_NONE);
    }

    public void decrypt(AlfrescoAccount account, File file, int intentAction)
    {
        if (account == null) { return; }
        if (isEncryptable(account, file) && isEncrypted(file.getPath()))
        {
            Operator.with(appContext, SessionUtils.getAccount(appContext)).load(
                    new FileProtectionRequest.Builder(file, false, intentAction));
        }
    }

    public boolean isEncrypted(String filePath)
    {
        try
        {
            return EncryptionUtils.isEncrypted(appContext, filePath);
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public boolean isEncryptionEnable()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        return prefs.getBoolean(DATA_PROTECTION_ENABLE, false);
    }

    public boolean isEncryptable(AlfrescoAccount account, File file)
    {
        return isEncryptionEnable() && isFileInProtectedFolder(account, file);
    }

    // ////////////////////////////////////////////////////
    // Internal Utils
    // ////////////////////////////////////////////////////
    private boolean isFileInProtectedFolder(AlfrescoAccount account, File f)
    {
        return (f.getPath().startsWith(
                AlfrescoStorageManager.getInstance(appContext).getDownloadFolder(account).getPath()) || f.getPath()
                .startsWith(SyncContentManager.getInstance(appContext).getSynchroFolder(account).getPath()));
    }

    // ////////////////////////////////////////////////////
    // SETTINGS
    // ////////////////////////////////////////////////////
    public boolean hasDataProtectionEnable()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        return sharedPref.getBoolean(DATA_PROTECTION_ENABLE, false);
    }

    public void setDataProtectionEnable(boolean isEnabled)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        sharedPref.edit().putBoolean(DATA_PROTECTION_ENABLE, isEnabled).commit();
    }

    public File getRequiredDataProtectionFile()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        String filePath = sharedPref.getString(REQUIRES_ENCRYPT, "");
        if (TextUtils.isEmpty(filePath)) { return null; }
        return new File(filePath);
    }

    public void setRequiredDataProtectionFile(File file)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        prefs.edit().putString(REQUIRES_ENCRYPT, file.getPath()).commit();
    }

    public boolean hasDataProtectionUserRequested()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        return sharedPref.getBoolean(DATA_PROTECTION_USER_REQUEST, false);
    }

    public void setDataProtectionUserRequested(boolean isRequested)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        sharedPref.edit().putBoolean(DATA_PROTECTION_USER_REQUEST, isRequested).commit();
    }
}
