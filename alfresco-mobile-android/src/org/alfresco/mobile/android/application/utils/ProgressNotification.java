/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.util.HashMap;

import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Responsible to display notification during content upload.
 */
@TargetApi(11)
@SuppressLint("UseSparseArrays")
public class ProgressNotification extends Service
{
    public static final int FLAG_UPLOAD_COMPLETED = -1;

    private static final int FLAG_UPLOAD_PROCESSING = -2;

    private static Notification notification = null;

    private static Activity parent = null;

    private static Context ctxt = null;

    private static HashMap<String, progressItem> inProgressObjects = null;

    private static progressItem newItem = null;

    private static String TAG = "ProgressNotification";

    public static String PARAM_DATA_SIZE = "dataSize";

    public static String PARAM_DATA_INCREMENT = "dataIncrement";

    public static String PARAM_DATA_NAME = "name";

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        return null;
    }

    static public boolean updateProgress(String name)
    {
        return updateProgress(name, null);
    }

    static public synchronized boolean updateProgress(String name, Integer incrementBy)
    {
        if (ctxt != null && inProgressObjects != null && parent != null)
        {
            NotificationManager notificationManager = (NotificationManager) ctxt
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            progressItem progressItem = inProgressObjects.get(name);

            if (progressItem != null)
            {
                Notification tmpNotification = progressItem.notification;
                Bundle params = progressItem.bundle;
                int dataSize = params.getInt(PARAM_DATA_SIZE);

                // UPLOAD PROCESS IS FINISHED : Notification Upload Complete<
                if (incrementBy != null && incrementBy == FLAG_UPLOAD_COMPLETED)
                {
                    if (AndroidVersion.isICSOrAbove())
                    {
                        tmpNotification = createNotification(ctxt, params, FLAG_UPLOAD_COMPLETED);
                    }
                    else
                    {
                        tmpNotification.contentView.setTextViewText(R.id.status_text,
                                ctxt.getText(R.string.download_complete));
                        tmpNotification.contentView.setProgressBar(R.id.status_progress, dataSize, dataSize, false);
                    }
                    notificationManager.notify((int) progressItem.id, tmpNotification);

                    // Stop Service
                    inProgressObjects.remove(name);

                    if (inProgressObjects.isEmpty())
                    {
                        parent.stopService(new Intent(parent, ProgressNotification.class).putExtras(params));
                    }
                }
                else
                {
                    // HTTP Data transfert in progress : Notification in
                    // progress.
                    if (incrementBy == null)
                    {
                        incrementBy = Integer.valueOf(params.getInt(PARAM_DATA_INCREMENT));
                    }
                    Log.d(TAG, progressItem.currentProgress + "");

                    progressItem.currentProgress += incrementBy;

                    if (AndroidVersion.isICSOrAbove())
                    {
                        tmpNotification = createNotification(ctxt, params, progressItem.currentProgress);
                    }
                    else
                    {
                        tmpNotification.contentView.setProgressBar(R.id.status_progress, dataSize,
                                progressItem.currentProgress, false);
                    }

                    notificationManager.notify((int) progressItem.id, tmpNotification);

                    if (progressItem.currentProgress >= dataSize - incrementBy - 1)
                    {
                        // Data Transfert Complete : Notification processing...
                        if (AndroidVersion.isICSOrAbove())
                        {
                            tmpNotification = createNotification(ctxt, params, FLAG_UPLOAD_PROCESSING);
                        }
                        else
                        {
                            tmpNotification.contentView.setTextViewText(R.id.status_text,
                                    ctxt.getText(R.string.action_processing));
                            tmpNotification.contentView.setProgressBar(R.id.status_progress, dataSize, dataSize, false);
                        }
                        notificationManager.notify((int) progressItem.id, tmpNotification);
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void onCreate()
    {
        if (inProgressObjects != null && inProgressObjects.size() > 0 && newItem != null)
        {
            ctxt = this;
            parent = MainActivity.activity;

            MessengerManager.showLongToast(this, getString(R.string.upload_in_progress));
        }
        super.onCreate();
    }

    @TargetApi(16)
    private static Notification createNotification(Context c, Bundle params, int value)
    {
        Notification notification = null;

        // Get the builder to create notification.
        Builder builder = new Notification.Builder(c.getApplicationContext());
        builder.setContentText(params.getString(PARAM_DATA_NAME));
        if (FLAG_UPLOAD_COMPLETED == value)
        {
            builder.setContentTitle(c.getText(R.string.upload_complete));
            builder.setTicker(c.getString(R.string.upload_complete) + " " + params.getString(PARAM_DATA_NAME));
            builder.setAutoCancel(true);
        }
        else if (FLAG_UPLOAD_PROCESSING == value)
        {
            builder.setContentTitle(c.getText(R.string.action_processing));
        }
        else
        {
            builder.setContentTitle(c.getText(R.string.upload_in_progress));
            builder.setTicker(params.getString(PARAM_DATA_NAME));
        }
        builder.setNumber(0);
        builder.setSmallIcon(R.drawable.ic_alfresco);

        if (AndroidVersion.isICSOrAbove())
        {
            if (FLAG_UPLOAD_COMPLETED == value)
            {
                builder.setProgress(0, 0, false);
            }
            else if (FLAG_UPLOAD_PROCESSING == value)
            {
                builder.setProgress(0, 0, true);
            }
            else
            {
                builder.setProgress(params.getInt(PARAM_DATA_SIZE), value, false);
            }
        }

        if (AndroidVersion.isJBOrAbove())
        {
            builder.setPriority(0);
            notification = builder.build();
        }
        else
        {
            notification = builder.getNotification();
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        return notification;
    }

    @TargetApi(16)
    public static void createProgressNotification(Context c, Bundle params, Class clickActivity)
    {
        ctxt = c;
        parent = MainActivity.activity;

        if (inProgressObjects == null)
        {
            inProgressObjects = new HashMap<String, progressItem>();
        }

        long notificationID = System.currentTimeMillis();

        Intent intent = new Intent(c, clickActivity);
        final PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, 0);

        // Get the builder to create notification.
        Builder builder = new Notification.Builder(c.getApplicationContext());
        builder.setContentTitle(c.getString(R.string.upload) + " " + params.getString(PARAM_DATA_NAME));
        builder.setContentText(params.getString(PARAM_DATA_NAME));
        builder.setNumber(0);
        // builder.setTicker(c.getString(R.string.upload) +
        // params.getString(PARAM_DATA_NAME));
        builder.setSmallIcon(R.drawable.ic_alfresco);
        builder.setContentIntent(pendingIntent);

        if (AndroidVersion.isICSOrAbove())
        {
            builder.setProgress(params.getInt(PARAM_DATA_SIZE), 0, false);
        }
        else
        {
            RemoteViews remote = new RemoteViews(c.getPackageName(), R.layout.app_download_progress);
            remote.setImageViewResource(R.id.status_icon, R.drawable.ic_alfresco);
            remote.setTextViewText(R.id.status_text, params.getString(PARAM_DATA_NAME));
            remote.setProgressBar(R.id.status_progress, params.getInt(PARAM_DATA_SIZE), 0, false);
            builder.setContent(remote);
        }

        if (AndroidVersion.isJBOrAbove())
        {
            builder.setPriority(0);
            notification = builder.build();
        }
        else
        {
            notification = builder.getNotification();
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

        newItem = new progressItem(notificationID, notification, params);
        inProgressObjects.put(params.getString(PARAM_DATA_NAME), newItem);

        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).notify((int) notificationID,
                notification);

        c.startService(new Intent(c, ProgressNotification.class).putExtras(params));
    }

    static class progressItem
    {
        progressItem(long id, Notification notification, Bundle bundle)
        {
            this.id = id;
            this.notification = notification;
            this.bundle = bundle;
        }

        long id = 0;

        Notification notification = null;

        Bundle bundle = null;

        int currentProgress = 0;
    }
}
