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
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

@TargetApi(11)
@SuppressLint("UseSparseArrays")
public class ProgressNotification extends Service
{
    static Notification notification = null;
    static Activity parent = null;
    static Context ctxt = null;
    static Handler handler = null;
    static HashMap<String, progressItem> inProgressObjects = new HashMap<String, progressItem>();
    static progressItem newItem = null;
    private static String TAG = "ProgressNotification";

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    static boolean updateProgress(String name)
    {
        return updateProgress(name, null);
    }

    
    static synchronized boolean updateProgress(String name, Integer incrementBy)
    {
        if (ctxt != null)
        {
            NotificationManager notificationManager = (NotificationManager) ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

            progressItem progressItem = inProgressObjects.get(name);

            if (progressItem != null)
            {
                Bundle params = progressItem.bundle;
                int dataSize = params.getInt("dataSize");

                if (incrementBy == null)
                {
                    incrementBy = Integer.valueOf(params.getInt("dataIncrement"));
                }
                Log.d(TAG, progressItem.currentProgress + "");

                progressItem.currentProgress += incrementBy;
                progressItem.notification.contentView.setProgressBar(R.id.status_progress, dataSize,
                        progressItem.currentProgress, false);

                Notification tmpNotification = progressItem.notification;
                if (AndroidVersion.isICSOrAbove())
                {
                    tmpNotification = createNotification(ctxt, params, progressItem.currentProgress);
                }

                notificationManager.notify((int) progressItem.id, tmpNotification);

                if (progressItem.currentProgress >= dataSize - incrementBy - 1)
                {
                    // notificationManager.cancel((int) progressItem.id);
                    inProgressObjects.remove(name);
                    
                    progressItem.notification.contentView.setTextViewText(R.id.status_text, ctxt.getText(R.string.download_complete));
                    
                    progressItem.notification.contentView.setProgressBar(R.id.status_progress, dataSize, dataSize, false);
                    
                    if (AndroidVersion.isICSOrAbove())
                    {
                        tmpNotification = createNotification(ctxt, params, dataSize);
                    }
                    notificationManager.notify((int) progressItem.id, tmpNotification);
                    
                    //Ensure we do this in the UI thread.
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            MessengerManager.showLongToast(ctxt, ctxt.getString(R.string.upload_complete));
                            
                            //Refresh the main interface for newly uploaded file
                            ((RefreshFragment) parent.getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(parent))).refresh();
                        }
                    });
                    
                    if (inProgressObjects.size() == 0)
                        ctxt.stopService(new Intent (ctxt, ProgressNotification.class));
                }

                return true;
            }
        }

        return false;
    }

    
    @Override
    @Deprecated
    public void onStart(Intent intent, int startId)
    {
        if (inProgressObjects.size() > 0  &&  newItem != null)
        {
            ctxt = this;
            parent = MainActivity.activity;
            handler = new Handler();
            
            MessengerManager.showLongToast(this, getString(R.string.upload_in_progress));
            
            startForeground((int)newItem.id, newItem.notification);
        }
        
        super.onStart(intent, startId);
    }

    
    @TargetApi(16)
    private static Notification createNotification(Context c, Bundle params, int value)
    {
        Notification notification = null;

        // Get the builder to create notification.
        Builder builder = new Notification.Builder(c.getApplicationContext());
        builder.setContentTitle(c.getText(R.string.upload_in_progress));
        builder.setContentText(params.getString("name"));
        builder.setAutoCancel(false);
        if (params.getInt("dataSize") == value)
        {
            builder.setContentTitle(c.getText(R.string.upload_complete));
            builder.setAutoCancel(true);
        }
        builder.setNumber(0);
        builder.setTicker(params.getString("name"));
        builder.setSmallIcon(R.drawable.ic_alfresco);

        if (AndroidVersion.isICSOrAbove())
        {
            builder.setProgress(params.getInt("dataSize"), value, false);
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

        return notification;
    }

    
    @TargetApi(16)
    public static void createProgressNotification(Context c, Bundle params, Class clickActivity)
    {
        ctxt = c;
        parent = MainActivity.activity;
        
        long notificationID = System.currentTimeMillis();

        Intent intent = new Intent(c, clickActivity);
        final PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, 0);

        // Get the builder to create notification.
        Builder builder = new Notification.Builder(c.getApplicationContext());
        builder.setContentTitle(c.getText(R.string.upload) + params.getString("name"));
        builder.setContentText(params.getString("name"));
        builder.setNumber(0);
        builder.setTicker(params.getString("name"));
        builder.setSmallIcon(R.drawable.ic_alfresco);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);

        if (AndroidVersion.isICSOrAbove())
        {
            builder.setProgress(params.getInt("dataSize"), 0, false);
        }
        else
        {
            RemoteViews remote = new RemoteViews(c.getPackageName(), R.layout.app_download_progress);
            remote.setImageViewResource(R.id.status_icon, R.drawable.ic_alfresco);
            remote.setTextViewText(R.id.status_text, params.getString("name"));
            remote.setProgressBar(R.id.status_progress, params.getInt("dataSize"), 0, false);
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
        // builder.setDeleteIntent(pendingIntent);

        newItem = new progressItem(notificationID, notification, params);
        inProgressObjects.put(params.getString("name"), newItem);

        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).notify((int) notificationID, notification);
        
        c.startService(new Intent (c, ProgressNotification.class).putExtras(params));
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
