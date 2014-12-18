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
import java.util.Stack;

import org.alfresco.mobile.android.platform.intent.PrivateIntent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class FileCleanerService extends Service
{
    private static final String TAG = FileCleanerService.class.getName();

    private Stack<Intent> intents = new Stack<Intent>();

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null && intent.getExtras() != null)
        {
            intents.push(intent);
            startService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // ////////////////////////////////////////////////////
    // Service lifecycle
    // ////////////////////////////////////////////////////
    private void startService()
    {
        try
        {
            Intent intent = intents.pop();
            if (intent.getAction() == PrivateIntent.ACTION_CLEAN_SHARE_FILE)
            {
                deleteShareFile(intent.getExtras().getString(PrivateIntent.EXTRA_FILE_PATH));
            }
            else
            {
                stopSelf();
            }
        }
        catch (Exception e)
        {
            stopSelf();
        }
    }

    // ////////////////////////////////////////////////////
    // ACTIONS
    // ////////////////////////////////////////////////////
    private void deleteShareFile(String filePath)
    {
        Log.d(TAG, "Clean");
        if (filePath != null)
        {
            File removeFile = new File(filePath);
            if (removeFile.exists())
            {
                removeFile.delete();
            }
        }
        stopSelf();
        return;
    }
}
