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
package org.alfresco.mobile.android.sync;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.async.session.LoadSessionHelper;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.sync.operations.FavoriteSync;
import org.alfresco.mobile.android.sync.prepare.PrepareFavoriteHelper;
import org.alfresco.mobile.android.sync.prepare.PrepareFavoriteSyncHelper;
import org.alfresco.mobile.android.sync.prepare.PrepareSyncHelper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class FavoritesSyncAdapter extends AbstractThreadedSyncAdapter
{
    private static final String TAG = FavoritesSyncAdapter.class.getName();

    private final AccountManager mAccountManager;

    private final FavoritesSyncManager syncManager;

    private AlfrescoSession session;

    private AlfrescoAccount acc;

    private int mode = FavoritesSyncManager.MODE_BOTH;

    private boolean ignoreWarning = false;

    private Node node;

    private String nodeIdentifier;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public FavoritesSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
        syncManager = FavoritesSyncManager.getInstance(context);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SYNC
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
            SyncResult syncResult)
    {
        // Reset all previous values
        node = null;
        mode = FavoritesSyncManager.MODE_BOTH;

        Log.d("Alfresco", "onPerformSync for account[" + account.name + "]");
        try
        {
            // Retrieve account to sync
            acc = AlfrescoAccount.parse(mAccountManager, account);

            // Retrieve extra informations
            if (extras != null)
            {
                if (extras.containsKey(FavoritesSyncManager.ARGUMENT_MODE))
                {
                    mode = extras.getInt(FavoritesSyncManager.ARGUMENT_MODE);
                }

                if (extras.containsKey(FavoritesSyncManager.ARGUMENT_IGNORE_WARNING))
                {
                    ignoreWarning = extras.getBoolean(FavoritesSyncManager.ARGUMENT_IGNORE_WARNING);
                }

                if (extras.containsKey(FavoritesSyncManager.ARGUMENT_NODE))
                {
                    node = (Node) extras.getSerializable(FavoritesSyncManager.ARGUMENT_NODE);
                }

                if (extras.containsKey(FavoritesSyncManager.ARGUMENT_NODE_ID))
                {
                    nodeIdentifier = extras.getString(FavoritesSyncManager.ARGUMENT_NODE_ID);
                }
            }

            try
            {
                // Start Session
                session = requestSession(acc);
            }
            catch (Exception e)
            {
                syncResult.stats.numAuthExceptions++;
            }

            try
            {
                // Retrieve node
                if (nodeIdentifier != null && node == null)
                {
                    node = session.getServiceRegistry().getDocumentFolderService().getNodeByIdentifier(nodeIdentifier);
                }
            }
            catch (Exception e)
            {
                // Node has been deleted
                node = null;
            }

            // Sync
            sync(syncResult);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SYNC SCAN
    // ///////////////////////////////////////////////////////////////////////////
    protected void sync(SyncResult syncResult)
    {
        Log.d(TAG, "Sync Scan Started");

        // Timestamp the scan process
        syncManager.saveStartSyncPrepareTimestamp();
        long syncScanningTimeStamp = new GregorianCalendar(TimeZone.getTimeZone("GMT")).getTimeInMillis();

        // DISPATCHER
        // Depending on what we want to achieve we use the associated helper
        List<FavoriteSync> requests;
        if (syncManager.hasActivateSync(acc))
        {
            if (syncManager.canSyncEverything(acc))
            {
                // SYNC ANYTHING
                requests = new PrepareSyncHelper(getContext(), acc, session, mode, syncScanningTimeStamp, syncResult,
                        node).prepare();
            }
            else
            {
                if (node != null)
                {
                    // FAVORITE SYNC
                    requests = new PrepareFavoriteSyncHelper(getContext(), acc, session, mode, syncScanningTimeStamp,
                            syncResult, node).prepare();
                }
                else
                {
                    requests = new PrepareFavoriteSyncHelper(getContext(), acc, session, mode, syncScanningTimeStamp,
                            syncResult, nodeIdentifier).prepare();
                }
            }
        }
        else
        {
            // FAVORITE (WITHOUT CONTENT)
            requests = new PrepareFavoriteHelper(getContext(), acc, session, mode, syncScanningTimeStamp, syncResult,
                    node).prepare();
        }

        // Retrieve the result of the scan
        SyncScanInfo currentSyncScan = syncManager.getScanInfo(acc);

        switch (currentSyncScan.getScanResult())
        {
        // Normal Case
        // Scan is Success ==> Launch the sync
            case SyncScanInfo.RESULT_SUCCESS:
                // Start Execution
                for (FavoriteSync operation : requests)
                {
                    operation.execute();
                }
                break;
            // Warning Case
            // Scan raised a warning ==> request user decision
            case SyncScanInfo.RESULT_WARNING_LOW_STORAGE:
            case SyncScanInfo.RESULT_WARNING_MOBILE_DATA:
                if (ignoreWarning)
                {
                    currentSyncScan = new SyncScanInfo(currentSyncScan.getDeltaDataTransfer(),
                            currentSyncScan.getDataToTransfer(), SyncScanInfo.RESULT_SUCCESS);
                    for (FavoriteSync operation : requests)
                    {
                        operation.execute();
                    }
                }
                else
                {
                    syncResult.databaseError = true;
                }
                break;
            // ERROR Case
            // Scan raised an error ==> alert the user
            case SyncScanInfo.RESULT_ERROR_NOT_ENOUGH_STORAGE:
                syncResult.databaseError = true;
                break;
            default:
                break;
        }

        // Flag the execution of last sync
        currentSyncScan.save(getContext(), acc);
        syncManager.saveSyncPrepareTimestamp();

        EventBusManager.getInstance().post(new FavoritesSyncScanEvent());

        Log.d("SYNC", "Total:" + syncResult.stats.numEntries);
        Log.d("SYNC", "Skipped:" + syncResult.stats.numSkippedEntries);
        Log.d("SYNC", "Creation:" + syncResult.stats.numInserts);
        Log.d("SYNC", "Update:" + syncResult.stats.numUpdates);
        Log.d("SYNC", "Deletion:" + syncResult.stats.numDeletes);
        Log.d("SYNC", "Exceptions:" + syncResult.stats.numIoExceptions);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SESSION
    // ///////////////////////////////////////////////////////////////////////////
    protected AlfrescoSession requestSession(AlfrescoAccount acc)
    {
        if (SessionManager.getInstance(getContext()).hasSession(acc.getId()))
        {
            return SessionManager.getInstance(getContext()).getSession(acc.getId());
        }
        else
        {
            LoadSessionHelper helper = new LoadSessionHelper(getContext(), acc.getId());
            session = helper.requestSession();
            SessionManager.getInstance(getContext()).saveSession(helper.getAccount(), session);
            return session;
        }
    }
}
