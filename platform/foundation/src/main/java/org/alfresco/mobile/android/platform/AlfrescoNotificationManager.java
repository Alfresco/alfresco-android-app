/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.platform;

import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.OperationsContentProvider;
import org.alfresco.mobile.android.async.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.async.node.download.DownloadRequest;
import org.alfresco.mobile.android.async.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.platform.utils.SessionUtils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Manager responsible of all dialog, notification and warning
 * 
 * @author jpascal
 */
public class AlfrescoNotificationManager extends Manager
{
    // ///////////////////////////////////////////////////////////////////////////
    // CHANNELS
    // ///////////////////////////////////////////////////////////////////////////
    public static final int CHANNEL_DEFAULT = 500;

    protected static final int CHANNEL_DEFAULT_INDEX = 0;

    public static final int CHANNEL_UPLOAD = 501;

    protected static final int CHANNEL_UPLOAD_INDEX = 1;

    public static final int CHANNEL_DOWNLOAD = 502;

    protected static final int CHANNEL_DOWNLOAD_INDEX = 2;

    public static final int CHANNEL_SYNC = 510;

    protected static final int CHANNEL_SYNC_INDEX = 3;

    // ///////////////////////////////////////////////////////////////////////////
    // BUNDLE
    // ///////////////////////////////////////////////////////////////////////////
    public static final String ARGUMENT_TITLE = "title";

    public static final String ARGUMENT_DESCRIPTION = "description";

    public static final String ARGUMENT_PROGRESS_MAX = "size";

    public static final String ARGUMENT_INDETERMINATE = "indeterminate";

    public static final String ARGUMENT_CONTENT_INFO = "contentInfo";

    public static final String ARGUMENT_SMALL_ICON = "smallIcon";

    public static final String ARGUMENT_PROGRESS = "progress";

    public static final String ARGUMENT_CHANNEL = "channel";

    // ///////////////////////////////////////////////////////////////////////////
    // ARGUMENTS
    // ///////////////////////////////////////////////////////////////////////////
    protected static final Object LOCK = new Object();

    private static final String TAG = AlfrescoNotificationManager.class.getSimpleName();

    protected static Manager mInstance;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    protected AlfrescoNotificationManager(Context applicationContext)
    {
        super(applicationContext);
    }

    public static AlfrescoNotificationManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = Manager.getInstance(context, AlfrescoNotificationManager.class.getSimpleName());
            }
            return (AlfrescoNotificationManager) mInstance;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void shutdown()
    {
        mInstance = null;
    }

    // //////////////////////////////////////////////////////////////////////
    // TOAST
    // //////////////////////////////////////////////////////////////////////
    public void showToast(String text)
    {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(appContext, text, duration);
        toast.show();
    }

    public void showToast(int text)
    {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(appContext, text, duration);
        toast.show();
    }

    public void showLongToast(String text)
    {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(appContext, text, duration);
        toast.show();
    }

    public void showLongToast(int text)
    {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(appContext, text, duration);
        toast.show();
    }

    // //////////////////////////////////////////////////////////////////////
    // CROUTON
    // //////////////////////////////////////////////////////////////////////
    public void showInfoCrouton(FragmentActivity activity, String text)
    {
        // Implement in subclass
    }

    public void showAlertCrouton(FragmentActivity activity, String text)
    {
        // Implement in subclass
    }

    public void showInfoCrouton(FragmentActivity activity, int text)
    {
        // Implement in subclass
    }

    public void showAlertCrouton(FragmentActivity activity, int text)
    {
        // Implement in subclass
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONBAR NOTIFICATION METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public int createSimpleNotification(int notificationId, String title, String description, String contentInfo)
    {
        Bundle b = new Bundle();
        b.putString(ARGUMENT_TITLE, title);
        if (description != null)
        {
            b.putString(ARGUMENT_DESCRIPTION, description);
        }
        if (contentInfo != null)
        {
            b.putString(ARGUMENT_CONTENT_INFO, contentInfo);
        }
        return createNotification(notificationId, b);
    }

    public int createIndeterminateNotification(int notificationId, String title, String description, String contentInfo)
    {
        Bundle b = new Bundle();
        b.putString(ARGUMENT_TITLE, title);
        b.putBoolean(ARGUMENT_INDETERMINATE, true);
        if (description != null)
        {
            b.putString(ARGUMENT_DESCRIPTION, description);
        }
        if (contentInfo != null)
        {
            b.putString(ARGUMENT_CONTENT_INFO, contentInfo);
        }
        return createNotification(notificationId, b);
    }

    public int createProgressNotification(int notificationId, String title, String description, String contentInfo,
            long progress, long maxprogress)
    {
        Bundle b = new Bundle();
        b.putString(ARGUMENT_TITLE, title);
        b.putBoolean(ARGUMENT_INDETERMINATE, false);
        b.putLong(ARGUMENT_PROGRESS_MAX, maxprogress);
        b.putLong(ARGUMENT_PROGRESS, progress);
        if (description != null)
        {
            b.putString(ARGUMENT_DESCRIPTION, description);
        }
        if (contentInfo != null)
        {
            b.putString(ARGUMENT_CONTENT_INFO, contentInfo);
        }
        return createNotification(notificationId, b);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public int createNotification(int notificationId, Bundle params)
    {
        Notification notification;

        // Get the builder to create notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext.getApplicationContext());
        builder.setContentTitle(params.getString(ARGUMENT_TITLE));
        if (params.containsKey(ARGUMENT_DESCRIPTION))
        {
            builder.setContentText(params.getString(ARGUMENT_DESCRIPTION));
        }
        builder.setNumber(0);

        if (AndroidVersion.isLollipopOrAbove())
        {
            builder.setSmallIcon(R.drawable.ic_notification);
            builder.setColor(appContext.getResources().getColor(R.color.alfresco_dbp_blue));
        }
        else
        {
            builder.setSmallIcon(R.drawable.ic_notification);
        }

        if (params.containsKey(ARGUMENT_DESCRIPTION))
        {
            builder.setContentText(params.getString(ARGUMENT_DESCRIPTION));
        }

        if (params.containsKey(ARGUMENT_CONTENT_INFO))
        {
            builder.setContentInfo(params.getString(ARGUMENT_CONTENT_INFO));
        }

        if (params.containsKey(ARGUMENT_SMALL_ICON))
        {
            builder.setSmallIcon(params.getInt(ARGUMENT_SMALL_ICON));
        }

        Intent i;
        PendingIntent pIntent;
        switch (notificationId)
        {
            case CHANNEL_SYNC:
                i = new Intent(PrivateIntent.ACTION_SYNCHRO_DISPLAY);
                pIntent = PendingIntent.getActivity(appContext, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                break;

            default:
                i = new Intent(PrivateIntent.ACTION_DISPLAY_OPERATIONS);
                i.putExtra(PrivateIntent.EXTRA_OPERATIONS_TYPE, notificationId);
                if (SessionUtils.getAccount(appContext) != null)
                {
                    i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, SessionUtils.getAccount(appContext).getId());
                }
                pIntent = PendingIntent.getActivity(appContext, 0, i, 0);
                break;
        }
        builder.setContentIntent(pIntent);

        if (params.containsKey(ARGUMENT_PROGRESS_MAX) && params.containsKey(ARGUMENT_PROGRESS))
        {
            long max = params.getLong(ARGUMENT_PROGRESS_MAX);
            long progress = params.getLong(ARGUMENT_PROGRESS);
            float value = (((float) progress / ((float) max)) * 100);
            int percentage = Math.round(value);
            builder.setProgress(100, percentage, false);
        }

        if (params.getBoolean(ARGUMENT_INDETERMINATE))
        {
            builder.setProgress(0, 0, true);
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

        ((NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify(notificationId,
                notification);

        return notificationId;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // HANDLERS
    // ///////////////////////////////////////////////////////////////////////////
    protected class NotificationTimer implements Runnable
    {
        int channelId;

        public NotificationTimer(int channelId)
        {
            this.channelId = channelId;
        }

        @Override
        public void run()
        {
            createNotificationChannel(channelId);
            if (channelStatus[getIndex(channelId)])
            {
                // Log.d(TAG, "Refresh");
                downloadHandler.postDelayed(this,
                        1000 * (channelAttempt[getIndex(channelId)] == 0 ? 1 : channelAttempt[getIndex(channelId)]));
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CHANNELS
    // ///////////////////////////////////////////////////////////////////////////
    private Handler downloadHandler = new Handler();

    private NotificationTimer[] channelTimers = new NotificationTimer[4];

    private Boolean[] channelStatus = { false, false, false, false };

    private Long[] channelProgress = { 0L, 0L, 0L, 0L };

    private int[] channelAttempt = { 0, 0, 0, 0 };

    private Integer getIndex(int channelId)
    {
        try
        {
            int index = CHANNEL_DEFAULT_INDEX;
            switch (channelId)
            {
                case CHANNEL_DOWNLOAD:
                    index = CHANNEL_DOWNLOAD_INDEX;
                    break;
                case CHANNEL_SYNC:
                    index = CHANNEL_SYNC_INDEX;
                    break;
                case CHANNEL_UPLOAD:
                    index = CHANNEL_UPLOAD_INDEX;
                    break;
                default:
                    break;
            }
            return index;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private NotificationTimer getTimer(int channelId)
    {
        try
        {
            return channelTimers[getIndex(channelId)];
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public void startRefresh(int channelId)
    {
        if (getTimer(channelId) != null) { return; }
        NotificationTimer notificationTimer = new NotificationTimer(channelId);
        channelTimers[getIndex(channelId)] = notificationTimer;
        channelStatus[getIndex(channelId)] = true;
        channelProgress[getIndex(channelId)] = 0L;
        channelAttempt[getIndex(channelId)] = 0;

        // start it with
        downloadHandler.post(notificationTimer);
    }

    public void monitorChannel(int requestTypeId)
    {
        // Log.d(TAG, "[Refresh] START");

        int channelId = CHANNEL_DEFAULT;
        switch (requestTypeId)
        {
            case OperationRequestIds.ID_NODE_CREATE:
            case OperationRequestIds.ID_NODE_UPDATE_CONTENT:
            case OperationRequestIds.ID_NODE_UPDATE:
                channelId = CHANNEL_UPLOAD;
                break;
            case OperationRequestIds.ID_NODE_DOWNLOAD:
                channelId = CHANNEL_DOWNLOAD;
                break;
            default:

                break;
        }

        if (channelId != CHANNEL_DEFAULT)
        {
            startRefresh(channelId);
        }
    }

    public void cancelMonitorChannel(int channelId)
    {
        // Log.d(TAG, "[Refresh] STOP");
        channelStatus[getIndex(channelId)] = false;
        channelTimers[getIndex(channelId)] = null;
        channelProgress[getIndex(channelId)] = 0L;
        channelAttempt[getIndex(channelId)] = 0;
    }

    public void unMonitorChannel(int requestTypeId)
    {
        // Log.d(TAG, "[Refresh] STOP");
        int channelId = CHANNEL_DEFAULT;
        switch (requestTypeId)
        {
            case OperationRequestIds.ID_NODE_CREATE:
            case OperationRequestIds.ID_NODE_UPDATE_CONTENT:
            case OperationRequestIds.ID_NODE_UPDATE:
                channelId = CHANNEL_UPLOAD;
                break;
            case OperationRequestIds.ID_NODE_DOWNLOAD:
                channelId = CHANNEL_DOWNLOAD;
                break;
            default:

                break;
        }

        channelStatus[getIndex(channelId)] = false;
        channelTimers[getIndex(channelId)] = null;
        channelProgress[getIndex(channelId)] = 0L;
        channelAttempt[getIndex(channelId)] = 0;
    }

    private void createNotificationChannel(int channelId)
    {
        Cursor cursor = null;
        int inProgress = 0;
        int completed = 0;
        long downloadedSoFar = 0, totalSize = 0;
        int total;
        String title = null, description = null, contentInfo = null;
        try
        {
            // Retrieve Info
            cursor = createCursor(channelId);
            total = cursor.getCount();
            while (cursor.moveToNext())
            {
                int status = cursor.getInt(OperationSchema.COLUMN_STATUS_ID);
                totalSize += cursor.getLong(OperationSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                switch (status)
                {
                    case Operation.STATUS_PAUSED:
                    case Operation.STATUS_PENDING:
                        break;
                    case Operation.STATUS_RUNNING:
                        inProgress++;
                        downloadedSoFar += cursor.getLong(OperationSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID);
                        break;
                    case Operation.STATUS_SUCCESSFUL:
                        downloadedSoFar += cursor.getLong(OperationSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                        completed++;
                        break;
                    default:
                        break;
                }
            }

            // Create the associated
            switch (channelId)
            {
                case CHANNEL_DOWNLOAD:
                    if (total == 0)
                    {
                        cancelMonitorChannel(CHANNEL_DOWNLOAD);
                        return;
                    }
                    // COMPLETED
                    if (completed == total)
                    {
                        title = String.format(appContext.getResources().getQuantityString(
                                R.plurals.download_complete_description, completed), Integer.toString(completed));
                        description = appContext.getString(R.string.download_complete);
                        cancelMonitorChannel(CHANNEL_DOWNLOAD);
                    }
                    else
                    {
                        title = String.format(
                                appContext.getResources().getQuantityString(R.plurals.download_in_progress, total),
                                Integer.toString(total));
                        description = appContext.getString(R.string.download_progress);
                    }
                    contentInfo = completed + "/" + total;
                    break;
                case CHANNEL_UPLOAD:
                    if (total == 0)
                    {
                        cancelMonitorChannel(CHANNEL_UPLOAD);
                        return;
                    }
                    // COMPLETED
                    if (completed == total)
                    {
                        title = String.format(appContext.getResources().getQuantityString(
                                R.plurals.upload_complete_description, completed), Integer.toString(completed));
                        description = appContext.getString(R.string.upload_complete);
                        cancelMonitorChannel(CHANNEL_UPLOAD);
                    }
                    else
                    {
                        title = String.format(
                                appContext.getResources().getQuantityString(R.plurals.batch_in_progress, total),
                                Integer.toString(total));
                        description = appContext.getString(R.string.upload_in_progress);
                    }
                    contentInfo = completed + "/" + total;
                    break;
                case CHANNEL_SYNC:
                    break;
                default:
                    break;
            }

            if (completed == total)
            {
                createSimpleNotification(channelId, title, description, contentInfo);
            }
            else if (downloadedSoFar != totalSize)
            {
                createProgressNotification(channelId, title, description, contentInfo, downloadedSoFar, totalSize);
            }
            else
            {
                createIndeterminateNotification(channelId, title, description, contentInfo);
            }
            Log.d(TAG, title + " " + channelAttempt[getIndex(channelId)] + " " + channelProgress[getIndex(channelId)]
                    + " " + downloadedSoFar + " " + totalSize + " " + (downloadedSoFar / totalSize) * 100 + " " + 100);

            // Stop infinity loop
            if (channelProgress[getIndex(channelId)] == downloadedSoFar)
            {
                if (channelAttempt[getIndex(channelId)] >= 20)
                {
                    cancelMonitorChannel(channelId);
                    return;
                }
                channelAttempt[getIndex(channelId)] += 1;
            }
            else
            {
                channelAttempt[getIndex(channelId)] = 0;
            }
            channelProgress[getIndex(channelId)] = downloadedSoFar;
        }
        catch (Exception e)
        {
        }
        finally
        {
            CursorUtils.closeCursor(cursor);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    private static final String UPLOAD_REQUESTS = OperationSchema.COLUMN_REQUEST_TYPE + " IN ("
            + CreateDocumentRequest.TYPE_ID + "," + UpdateContentRequest.TYPE_ID + ")";

    private static final String DOWNLOAD_REQUESTS = OperationSchema.COLUMN_REQUEST_TYPE + " IN ("
            + DownloadRequest.TYPE_ID + ")";

    private static final String ALL_REQUESTS = OperationSchema.COLUMN_REQUEST_TYPE + " IN (" + DownloadRequest.TYPE_ID
            + "," + CreateDocumentRequest.TYPE_ID + "," + UpdateContentRequest.TYPE_ID + ")";

    public Cursor createCursor(int id)
    {
        if (id <= 0) { return null; }

        String request = ALL_REQUESTS;
        switch (id)
        {
            case AlfrescoNotificationManager.CHANNEL_DOWNLOAD:
                request = DOWNLOAD_REQUESTS;
                break;
            case AlfrescoNotificationManager.CHANNEL_UPLOAD:
                request = UPLOAD_REQUESTS;
                break;
            default:
                break;
        }

        if (SessionUtils.getAccount(appContext) != null)
        {
            request = OperationsContentProvider.getAccountFilter(SessionUtils.getAccount(appContext)) + " AND "
                    + request;
        }

        Uri baseUri = OperationsContentProvider.CONTENT_URI;
        return appContext.getContentResolver().query(baseUri, OperationSchema.COLUMN_ALL, request, null, null);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////

}
