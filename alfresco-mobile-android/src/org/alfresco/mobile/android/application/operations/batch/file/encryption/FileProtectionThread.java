/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.file.encryption;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.batch.file.FileOperationThread;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.security.DataCleanerService;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.security.EncryptionUtils;
import org.alfresco.mobile.android.application.utils.IOUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FileProtectionThread extends FileOperationThread<Void>
{
    private static final String TAG = FileProtectionThread.class.getName();

    private boolean doEncrypt;

    private int intentAction;

    private File copiedFile;

    private File tmpFile;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public FileProtectionThread(Context ctx, AbstractBatchOperationRequestImpl request)
    {
        super(ctx, request);
        if (request instanceof DataProtectionRequest)
        {
            doEncrypt = ((DataProtectionRequest) request).doEncrypt();
            intentAction = ((DataProtectionRequest) request).getIntentAction();
            copiedFile = ((DataProtectionRequest) request).getCopiedFile();
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
            if (intentAction == DataProtectionManager.ACTION_SEND || intentAction == DataProtectionManager.ACTION_SEND_ALFRESCO)
            {
                File folder = StorageManager.getShareFolder(context, acc);
                copiedFile = new File(folder, file.getName());
                IOUtils.createFile(copiedFile);
                
                //Start an alarm to delete the file after Xx minutes
                Intent intent = new Intent(context, DataCleanerService.class);
                intent.setAction(IntentIntegrator.ACTION_CLEAN_SHARE_FILE);
                intent.putExtra(IntentIntegrator.EXTRA_FILE_PATH, copiedFile.getPath());
                PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);
                
                //Transfert rate : 1Mb/1min
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
            if (e.getMessage().contains("last block incomplete in decryption")){
                //Do Nothing.
            } else {
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
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        if (doEncrypt)
        {
            broadcastIntent.setAction(IntentIntegrator.ACTION_ENCRYPT_COMPLETED);
        }
        else
        {
            broadcastIntent.setAction(IntentIntegrator.ACTION_DECRYPT_COMPLETED);
        }
        Bundle b = new Bundle();
        if (intentAction != DataProtectionManager.ACTION_NONE)
        {
            b.putInt(IntentIntegrator.EXTRA_INTENT_ACTION, intentAction);
        }
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, parentFile);
        b.putSerializable(IntentIntegrator.EXTRA_FILE, tmpFile);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public boolean isDoEncrypt()
    {
        return doEncrypt;
    }

    public int getIntentAction()
    {
        return intentAction;
    }

    public File getTmpFile()
    {
        return tmpFile;
    }

}
