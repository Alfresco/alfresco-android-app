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

import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.ui.utils.Formatter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SyncScanInfo
{

    private static final String SYNCHRO_SCAN_DATA_DELTA_PREFIX = "SyncScanDelta-";

    private static final String SYNCHRO_SCAN_DATA_TRANSFER_PREFIX = "SyncScanTransfert-";

    private static final String SYNCHRO_SCAN_RESULT_PREFIX = "SyncScanResult-";

    private static final String SYNCHRO_SCAN_RESPONSE_PREFIX = "SyncScanResponse-";

    public static final int RESULT_SUCCESS = 0;

    public static final int RESULT_ERROR_NOT_ENOUGH_STORAGE = 1;

    public static final int RESULT_WARNING_LOW_STORAGE = 2;

    public static final int RESULT_WARNING_MOBILE_DATA = 4;

    public static final int RESPONSE_UNKNOWN = 0;

    public static final int RESPONSE_NOTHING = 1;

    public static final int RESPONSE_FORCE = 2;

    public static final int RESPONSE_AWAIT = 4;

    private long deltaDataTransfer;

    private long dataToTransfer;

    private int scanResult;

    private int scanResponse;

    public SyncScanInfo(long deltaDataTransfer, long dataToTransfer, int scanResult)
    {
        super();
        this.deltaDataTransfer = deltaDataTransfer;
        this.dataToTransfer = dataToTransfer;
        this.scanResult = scanResult;

        switch (scanResult)
        {
            case RESULT_ERROR_NOT_ENOUGH_STORAGE:
                this.scanResponse = RESPONSE_NOTHING;
                break;
            case RESULT_WARNING_LOW_STORAGE:
                this.scanResponse = RESPONSE_AWAIT;
                break;
            case RESULT_WARNING_MOBILE_DATA:
                this.scanResponse = RESPONSE_AWAIT;
                break;
            default:
                this.scanResponse = RESPONSE_UNKNOWN;
                break;
        }
    }

    public SyncScanInfo(long deltaDataTransfer, long dataToTransfer, int scanResult, int scanResponse)
    {
        super();
        this.deltaDataTransfer = deltaDataTransfer;
        this.dataToTransfer = dataToTransfer;
        this.scanResult = scanResult;
        this.scanResponse = scanResponse;
    }

    public long getDeltaDataTransfer()
    {
        return deltaDataTransfer;
    }

    public long getDateToTransfer()
    {
        return dataToTransfer;
    }

    public int getScanResult()
    {
        return scanResult;
    }

    public int getScanResponse()
    {
        return scanResponse;
    }

    public void forceScan(Context context, AlfrescoAccount account)
    {
        scanResponse = RESPONSE_FORCE;
        save(context, account);
    }

    public void waitSync(Context context, AlfrescoAccount account)
    {
        scanResponse = RESPONSE_AWAIT;
        save(context, account);
    }

    public void save(Context context, AlfrescoAccount account)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putLong(SYNCHRO_SCAN_DATA_DELTA_PREFIX + account.getId(), deltaDataTransfer)
                .putLong(SYNCHRO_SCAN_DATA_TRANSFER_PREFIX + account.getId(), dataToTransfer)
                .putInt(SYNCHRO_SCAN_RESULT_PREFIX + account.getId(), scanResult)
                .putInt(SYNCHRO_SCAN_RESPONSE_PREFIX + account.getId(), scanResponse).commit();
    }

    public void reset(Context context, AlfrescoAccount account)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().remove(SYNCHRO_SCAN_DATA_DELTA_PREFIX + account.getId())
                .remove(SYNCHRO_SCAN_DATA_TRANSFER_PREFIX + account.getId())
                .remove(SYNCHRO_SCAN_RESULT_PREFIX + account.getId()).commit();
    }

    public static SyncScanInfo getLastSyncScanData(Context context, AlfrescoAccount account)
    {
        SyncScanInfo info = null;
        if (account != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            info = new SyncScanInfo(sharedPref.getLong(SYNCHRO_SCAN_DATA_DELTA_PREFIX + account.getId(), 0),
                    sharedPref.getLong(SYNCHRO_SCAN_DATA_TRANSFER_PREFIX + account.getId(), 0), sharedPref.getInt(
                            SYNCHRO_SCAN_RESULT_PREFIX + account.getId(), 0), sharedPref.getInt(
                            SYNCHRO_SCAN_RESPONSE_PREFIX + account.getId(), 0));
        }
        return info;
    }

    public static void setLastSyncScanData(Context context, AlfrescoAccount account, long deltaDataTransfer,
            long dateToTransfer, int scanResult)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putLong(SYNCHRO_SCAN_DATA_DELTA_PREFIX + account.getId(), deltaDataTransfer)
                .putLong(SYNCHRO_SCAN_DATA_TRANSFER_PREFIX + account.getId(), dateToTransfer)
                .putInt(SYNCHRO_SCAN_RESULT_PREFIX + account.getId(), scanResult).commit();
    }

    public String getErrorMessage(Context context)
    {
        String error = "";
        switch (scanResult)
        {
            case RESULT_ERROR_NOT_ENOUGH_STORAGE:
                error = String.format(context.getString(R.string.sync_error_storage),
                        Formatter.formatFileSize(context, deltaDataTransfer),
                        Formatter.formatFileSize(context, (long) AlfrescoStorageManager.getInstance(context).getAvailableBytes()));
                break;
            case RESULT_WARNING_LOW_STORAGE:
                error = String.format(context.getString(R.string.sync_warning_storage),
                        Formatter.formatFileSize(context, deltaDataTransfer),
                        Formatter.formatFileSize(context, (long) AlfrescoStorageManager.getInstance(context).getAvailableBytes()));
                break;
            case RESULT_WARNING_MOBILE_DATA:
                error = String.format(context.getString(R.string.sync_warning_mobile_data),
                        Formatter.formatFileSize(context, dataToTransfer));
                break;
            default:
                break;
        }
        return error;
    }

    public String getErrorTitleMessage(Context context)
    {
        String error = context.getString(R.string.sync_warning);
        switch (scanResult)
        {
            case RESULT_ERROR_NOT_ENOUGH_STORAGE:
                error = context.getString(R.string.sync_error);
                break;
            case RESULT_WARNING_LOW_STORAGE:
            case RESULT_WARNING_MOBILE_DATA:
                error = context.getString(R.string.sync_warning);
                break;
            default:
                break;
        }

        return error;
    }

    public boolean hasWarning()
    {
        return (RESULT_SUCCESS != scanResult && scanResponse != RESPONSE_FORCE);
    }

    public boolean hasResponse()
    {
        return scanResponse == RESPONSE_FORCE;
    }

    public boolean hasError()
    {
        return (RESULT_ERROR_NOT_ENOUGH_STORAGE == scanResult);
    }
}
