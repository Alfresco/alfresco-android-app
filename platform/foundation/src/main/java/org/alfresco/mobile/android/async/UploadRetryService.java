/*******************************************************************************
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.mobile.android.async;

import java.util.Calendar;
import java.util.Stack;

import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

public class UploadRetryService extends Service
{
    private static final String TAG = UploadRetryService.class.getName();

    public static final int DEFAULT_DELAY = 2;

    private static final int DEFAULT_DELAY_MAX = 240;

    private Stack<Intent> intents = new Stack<Intent>();

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null)
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
            if (intent.getAction().equals(PrivateIntent.ACTION_RETRY_OPERATIONS))
            {
                retryOperations(intent);
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
    private void retryOperations(Intent pIntent)
    {
        Uri uri = null;
        if (pIntent.getExtras() != null && pIntent.hasExtra(PrivateIntent.EXTRA_OPERATION_ID))
        {
            uri = Uri.parse(pIntent.getStringExtra(PrivateIntent.EXTRA_OPERATION_ID));
        }

        if (uri == null)
        {
            stopSelf();
        }

        // Postpone if still offline (double time for each attempt)
        if (!ConnectivityUtils.hasInternetAvailable(getApplicationContext()))
        {
            if (OperationsFactory.canRetry(getApplicationContext(), uri))
            {
                int delay = pIntent.getExtras().getInt(PrivateIntent.EXTRA_OPERATION_DELAY);
                pIntent.getExtras().clear();
                if (delay * 2 < DEFAULT_DELAY_MAX)
                {
                    postpone(uri.toString(), delay * 2);
                }
            }
        }
        else
        {
            // Check if still present ?
            if (OperationsFactory.canRetry(getApplicationContext(), uri))
            {
                ContentValues cValues = new ContentValues();
                cValues.put(OperationSchema.COLUMN_NOTIFICATION_VISIBILITY, OperationRequest.VISIBILITY_HIDDEN);
                getApplicationContext().getContentResolver().update(uri, cValues, null, null);

                Operator.with(getApplicationContext()).retry(uri);
            }
        }

        stopSelf();
    }

    private void postpone(String operationId, int delay)
    {
        retryDelay(getApplicationContext(), operationId, delay);
    }

    public static void retryDelay(Context context, String operationId, int min)
    {
        // Start alarm to retry upload
        Intent postPoneIntent = new Intent(context, UploadRetryService.class);
        postPoneIntent.setAction(PrivateIntent.ACTION_RETRY_OPERATIONS);
        postPoneIntent.putExtra(PrivateIntent.EXTRA_OPERATION_ID, operationId);
        postPoneIntent.putExtra(PrivateIntent.EXTRA_OPERATION_DELAY, min);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pIntent = PendingIntent.getService(context, uniqueInt, postPoneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, min);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC, cal.getTimeInMillis(), pIntent);
    }
}
