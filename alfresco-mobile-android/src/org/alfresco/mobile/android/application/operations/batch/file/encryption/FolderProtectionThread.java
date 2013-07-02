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

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.batch.file.FileOperationThread;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.security.EncryptionUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class FolderProtectionThread extends FileOperationThread<Void>
{
    private static final String TAG = FolderProtectionThread.class.getName();

    private boolean doEncrypt;

    private int intentAction;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public FolderProtectionThread(Context ctx, AbstractBatchOperationRequestImpl request)
    {
        super(ctx, request);
        if (request instanceof DataProtectionRequest)
        {
            doEncrypt = ((DataProtectionRequest) request).doEncrypt();
            intentAction = ((DataProtectionRequest) request).getIntentAction();
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

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            
            if (file.isDirectory())
            {
                if (doEncrypt)
                {
                    if (EncryptionUtils.encryptFiles(context, file.getPath(), true))
                    {
                        prefs.edit().putBoolean(GeneralPreferences.PRIVATE_FOLDERS, true).commit();
                    }
                }
                else if (!doEncrypt)
                {
                    if (EncryptionUtils.decryptFiles(context, file.getPath(), true))
                    {
                        prefs.edit().putBoolean(GeneralPreferences.PRIVATE_FOLDERS, false).commit();
                    }
                }
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
            broadcastIntent.setAction(IntentIntegrator.ACTION_ENCRYPT_ALL_COMPLETED);
        }
        else
        {
            broadcastIntent.setAction(IntentIntegrator.ACTION_DECRYPT_ALL_COMPLETED);
        }
        Bundle b = new Bundle();
        if (intentAction != DataProtectionManager.ACTION_NONE)
        {
            b.putInt(IntentIntegrator.EXTRA_INTENT_ACTION, intentAction);
        }
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, parentFile);
        b.putSerializable(IntentIntegrator.EXTRA_FILE, file);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
}
