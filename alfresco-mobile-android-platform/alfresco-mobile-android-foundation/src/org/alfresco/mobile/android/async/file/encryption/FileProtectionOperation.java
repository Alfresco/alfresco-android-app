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
package org.alfresco.mobile.android.async.file.encryption;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.file.FileOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.io.FileCleanerService;
import org.alfresco.mobile.android.platform.io.IOUtils;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.security.EncryptionUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FileProtectionOperation extends FileOperation<Void>
{
    private static final String TAG = FileProtectionOperation.class.getName();

    private boolean doEncrypt;

    private int intentAction;

    private File copiedFile;

    private File tmpFile;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public FileProtectionOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof FileProtectionRequest)
        {
            doEncrypt = ((FileProtectionRequest) request).doEncrypt;
            intentAction = ((FileProtectionRequest) request).intentAction;
            copiedFile = ((FileProtectionRequest) request).copiedFile;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Void> doInBackground()
    {
        LoaderResult<Void> result = new LoaderResult<Void>();
        try
        {
            result = super.doInBackground();

            tmpFile = file;

            // IF we want to decrypt a file and send to another application
            // we need to keep this file unprotected until the 3rd party app
            // executes its own action
            // That's why we copy the file to a dedicated folder
            // and run an alarm to delete it after X minutes
            if (intentAction == DataProtectionManager.ACTION_SEND
                    || intentAction == DataProtectionManager.ACTION_SEND_ALFRESCO)
            {
                File folder = AlfrescoStorageManager.getInstance(context).getShareFolder(acc);
                copiedFile = new File(folder, file.getName());
                IOUtils.createFile(copiedFile);

                // Start an alarm to delete the file after Xx minutes
                Intent intent = new Intent(context, FileCleanerService.class);
                intent.setAction(PrivateIntent.ACTION_CLEAN_SHARE_FILE);
                intent.putExtra(PrivateIntent.EXTRA_FILE_PATH, copiedFile.getPath());
                PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);

                // Transfert rate : 1Mb/1min
                int securedTime = Math.round(copiedFile.length() / 1048576) + 1;
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, securedTime);
                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarm.set(AlarmManager.RTC, cal.getTimeInMillis(), pintent);
            }

            if (copiedFile != null)
            {
                IOUtils.copyFile(file.getPath(), copiedFile.getPath());
                tmpFile = copiedFile;
                if (intentAction == DataProtectionManager.ACTION_COPY) { return result; }
            }

            if (tmpFile.isFile())
            {
                boolean isEncrypted = DataProtectionManager.getInstance(context).isEncrypted(tmpFile.getPath());
                if (doEncrypt && !isEncrypted)
                {
                    EncryptionUtils.encryptFile(context, tmpFile.getPath(), true);
                }
                else if (!doEncrypt && isEncrypted)
                {
                    EncryptionUtils.decryptFile(context, tmpFile.getPath());
                }
            }
        }
        catch (IOException e)
        {
            if (e.getMessage().contains("last block incomplete in decryption"))
            {
                // Do Nothing.
            }
            else
            {
                Log.e(TAG, Log.getStackTraceString(e));
                result.setException(e);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Void> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new FileProtectionEvent(getRequestId(), result, tmpFile, doEncrypt, intentAction));
    }
}
