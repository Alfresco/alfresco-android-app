package org.alfresco.mobile.android.application.utils;

import java.util.HashMap;

import org.alfresco.mobile.android.ui.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

@TargetApi(11)
@SuppressLint("UseSparseArrays")
public class ProgressNotification extends Activity
{
    static Context ctxt = null;

    static HashMap<String, progressItem> inProgressObjects = new HashMap<String, progressItem>();

    private static String TAG = "ProgressNotification";

    static boolean updateProgress(String name)
    {
        return updateProgress(name, null);
    }

    static synchronized boolean updateProgress(String name, Integer incrementBy)
    {
        if (ctxt != null)
        {
            NotificationManager notificationManager = (NotificationManager) ctxt
                    .getSystemService(Context.NOTIFICATION_SERVICE);

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

                if (progressItem.currentProgress >= dataSize - incrementBy)
                {
                    // notificationManager.cancel((int) progressItem.id);
                    inProgressObjects.remove(name);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Implemented as an Activity so that we can display a full screen or
        // dialog based progress as well if we wish.
        // The current implementation simply initiates the Notification with
        // progress in, then finish()es. We could easily however
        // expand this class to show a dialog of progress that shows initially
        // for a few seconds then disappears leaving the notification ongoing.

        super.onCreate(savedInstanceState);

        setContentView(R.layout.download_progress);

        try
        {
            createProgressNotification(this, getIntent().getExtras(), ProgressNotification.class);
        }
        catch (Exception e)
        {
            // Someone didn't set the bundle up correctly before calling.
            finish();
            return;
        }

        // Remove this if we want the activity displayed with progress of its
        // own.
        finish();
    }

    @TargetApi(16)
    private static Notification createNotification(Context c, Bundle params, int value)
    {
        Notification notification = null;
        
        // Get the builder to create notification.
        Builder builder = new Notification.Builder(c.getApplicationContext());
        builder.setContentTitle("Upload");
        builder.setContentText(params.getString("name"));
        builder.setNumber(0);
        builder.setTicker(params.getString("name"));
        builder.setSmallIcon(R.drawable.ic_alfresco);
        builder.setAutoCancel(false);

        if (AndroidVersion.isICSOrAbove())
        {
            builder.setProgress(params.getInt("dataSize"), value, false);
        }

        if (AndroidVersion.isJBOrAbove())
        {
            builder.setPriority(0);
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }

        return notification;
    }

    @TargetApi(16)
    public static void createProgressNotification(Context c, Bundle params, Class clickActivity)
    {
        ctxt = c;

        Notification notification;
        long notificationID = System.currentTimeMillis();

        Intent intent = new Intent(c, clickActivity);
        final PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, 0);

        // Get the builder to create notification.
        Builder builder = new Notification.Builder(c.getApplicationContext());
        builder.setContentTitle("Upload " + params.getString("name"));
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
            RemoteViews remote = new RemoteViews(c.getPackageName(), R.layout.download_progress);
            remote.setImageViewResource(R.id.status_icon, R.drawable.ic_alfresco);
            remote.setTextViewText(R.id.status_text, params.getString("name"));
            remote.setProgressBar(R.id.status_progress, params.getInt("dataSize"), 0, false);
            builder.setContent(remote);
        }

        if (AndroidVersion.isJBOrAbove())
        {
            builder.setPriority(0);
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        // builder.setDeleteIntent(pendingIntent);

        progressItem newItem = new progressItem(notificationID, notification, params);
        inProgressObjects.put(params.getString("name"), newItem);

        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).notify((int) notificationID,
                notification);
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