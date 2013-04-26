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
package org.alfresco.mobile.android.application.manager;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.AndroidVersion;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class NotificationHelper
{
    public static final String ARGUMENT_TITLE = "title";

    public static final String ARGUMENT_DESCRIPTION = "description";

    public static final String ARGUMENT_PROGRESS_MAX = "size";

    public static final String ARGUMENT_INDETERMINATE = "indeterminate";

    public static final String ARGUMENT_CONTENT_INFO = "contentInfo";

    public static final String ARGUMENT_PROGRESS = "progress";

    public static final int DEFAULT_NOTIFICATION_ID = 500;

    public static int createSimpleNotification(Context c, String title, String description, String contentInfo)
    {
        Bundle b = new Bundle();
        b.putString(NotificationHelper.ARGUMENT_TITLE, title);
        if (description != null)
        {
            b.putString(NotificationHelper.ARGUMENT_DESCRIPTION, description);
        }
        if (contentInfo != null)
        {
            b.putString(NotificationHelper.ARGUMENT_CONTENT_INFO, contentInfo);
        }
        return NotificationHelper.createNotification(c, b);
    }

    public static int createIndeterminateNotification(Context c, String title, String description, String contentInfo)
    {
        Bundle b = new Bundle();
        b.putString(NotificationHelper.ARGUMENT_TITLE, title);
        b.putBoolean(NotificationHelper.ARGUMENT_INDETERMINATE, true);
        if (description != null)
        {
            b.putString(NotificationHelper.ARGUMENT_DESCRIPTION, description);
        }
        if (contentInfo != null)
        {
            b.putString(NotificationHelper.ARGUMENT_CONTENT_INFO, contentInfo);
        }
        return NotificationHelper.createNotification(c, b);
    }

    public static int createProgressNotification(Context c, String title, String description, String contentInfo,
            long progress, long maxprogress)
    {
        Bundle b = new Bundle();
        b.putString(NotificationHelper.ARGUMENT_TITLE, title);
        b.putBoolean(NotificationHelper.ARGUMENT_INDETERMINATE, false);
        b.putLong(NotificationHelper.ARGUMENT_PROGRESS_MAX, maxprogress);
        b.putLong(NotificationHelper.ARGUMENT_PROGRESS, progress);
        if (description != null)
        {
            b.putString(NotificationHelper.ARGUMENT_DESCRIPTION, description);
        }
        if (contentInfo != null)
        {
            b.putString(NotificationHelper.ARGUMENT_CONTENT_INFO, contentInfo);
        }
        return NotificationHelper.createNotification(c, b);
    }

    public static int createNotification(Context c, Bundle params)
    {
        Notification notification = null;
        

        // Get the builder to create notification.
        Builder builder = new Notification.Builder(c.getApplicationContext());
        builder.setContentTitle(params.getString(ARGUMENT_TITLE));
        if (params.containsKey(ARGUMENT_DESCRIPTION))
        {
            builder.setContentText(params.getString(ARGUMENT_DESCRIPTION));
        }
        builder.setNumber(0);
        builder.setSmallIcon(R.drawable.ic_notif_alfresco);

        if (params.containsKey(ARGUMENT_DESCRIPTION))
        {
            builder.setContentText(params.getString(ARGUMENT_DESCRIPTION));
        }

        if (params.containsKey(ARGUMENT_CONTENT_INFO))
        {
            builder.setContentInfo(params.getString(ARGUMENT_CONTENT_INFO));
        }

        Intent i = new Intent(IntentIntegrator.ACTION_DISPLAY_OPERATIONS);
        PendingIntent pIntent = PendingIntent.getActivity(c, 0, i, 0);
        builder.setContentIntent(pIntent);
        
        if (AndroidVersion.isICSOrAbove())
        {
            if (params.containsKey(ARGUMENT_PROGRESS_MAX) && params.containsKey(ARGUMENT_PROGRESS))
            {
                long max = params.getLong(ARGUMENT_PROGRESS_MAX);
                long progress = params.getLong(ARGUMENT_PROGRESS);
                float value = (((float) progress/ ((float) max)) * 100);
                int percentage = Math.round(value);
                builder.setProgress(100, percentage, false);
            }

            if (params.getBoolean(ARGUMENT_INDETERMINATE))
            {
                builder.setProgress(0, 0, true);
            }
        }
        else
        {
            RemoteViews remote = new RemoteViews(c.getPackageName(), R.layout.app_download_progress);
            remote.setImageViewResource(R.id.status_icon, R.drawable.ic_alfresco);
            remote.setTextViewText(R.id.status_text, params.getString(ARGUMENT_TITLE));
            remote.setProgressBar(R.id.status_progress, params.getInt(ARGUMENT_PROGRESS_MAX), 0, false);
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

        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(DEFAULT_NOTIFICATION_ID,
                notification);

        return DEFAULT_NOTIFICATION_ID;
    }

}
