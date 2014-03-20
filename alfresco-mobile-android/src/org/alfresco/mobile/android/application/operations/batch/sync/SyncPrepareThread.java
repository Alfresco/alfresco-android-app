/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.operations.batch.sync;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.fragments.favorites.SyncScanInfo;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.node.NodeOperationThread;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.utils.CursorUtils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class SyncPrepareThread extends NodeOperationThread<Void>
{
    private static final String TAG = SyncPrepareThread.class.getName();

    private int mode = SyncPrepareRequest.MODE_DOCUMENTS;

    private OperationsRequestGroup group;

    private long syncScanningTimeStamp;

    private Cursor localSyncCursor;

    private SynchroManager syncManager;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncPrepareThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof SyncPrepareRequest)
        {
            this.mode = ((SyncPrepareRequest) request).getMode();
        }
        syncManager = SynchroManager.getInstance(context);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_SYNC_SCAN_STARTED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }

    @Override
    protected LoaderResult<Void> doInBackground()
    {
        LoaderResult<Void> result = new LoaderResult<Void>();
        try
        {
            Log.d(TAG, "Sync Scan Started");
            result = super.doInBackground();

            // Timestamp the scan process
            SynchroManager.saveStartSyncPrepareTimestamp(context);
            syncScanningTimeStamp = new GregorianCalendar(TimeZone.getTimeZone("GMT")).getTimeInMillis();

            // DISPATCHER
            // Depending on what we want to achieve we use the associated helper
            if (syncManager.hasActivateSync(acc))
            {
                if (syncManager.canSyncEverything(acc))
                {
                    // SYNC ANYTHING
                    group = new PrepareSyncHelper(context, this, syncScanningTimeStamp).prepare();
                }
                else
                {
                    // FAVORITE SYNC
                    group = new PrepareFavoriteSyncHelper(context, this, syncScanningTimeStamp).prepare();
                }
            }
            else
            {
                // FAVORITE (WITHOUT CONTENT)
                group = new PrepareFavoriteHelper(context, this, syncScanningTimeStamp).prepare();
            }

            // Retrieve the result of the scan
            SyncScanInfo currentSyncScan = syncManager.getScanInfo(getAccount());

            switch (currentSyncScan.getScanResult())
            {
            // Normal Case
            // Scan is Success ==> Launch the sync
                case SyncScanInfo.RESULT_SUCCESS:
                    if (group != null && !group.getRequests().isEmpty())
                    {
                        syncManager.enqueue(group);
                    }
                    break;
                // Warning Case
                // Scan raised a warning ==> request user decision
                case SyncScanInfo.RESULT_WARNING_LOW_STORAGE:
                case SyncScanInfo.RESULT_WARNING_MOBILE_DATA:
                    syncManager.saveOperationGroup(group);
                    break;
                // ERROR Case
                // Scan raised an error ==> alert the user
                case SyncScanInfo.RESULT_ERROR_NOT_ENOUGH_STORAGE:
                    group = null;
                    break;
                default:
                    break;
            }

            // Flag the execution of last sync
            currentSyncScan.save(context, acc);
            SynchroManager.saveSyncPrepareTimestamp(context);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        finally
        {
            CursorUtils.closeCursor(localSyncCursor);
        }
        return result;
    }

    @Override
    protected void onPostExecute(LoaderResult<Void> result)
    {
        super.onPostExecute(result);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_SYNC_SCAN_COMPLETED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public int getMode()
    {
        return mode;
    }
}
