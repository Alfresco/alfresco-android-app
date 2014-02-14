package org.alfresco.mobile.android.application.fragments.favorites;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.manager.StorageManager;
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

    public void forceScan(Context context, Account account)
    {
        scanResponse = RESPONSE_FORCE;
        save(context, account);
    }

    public void waitSync(Context context, Account account)
    {
        scanResponse = RESPONSE_AWAIT;
        save(context, account);
    }

    public void save(Context context, Account account)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putLong(SYNCHRO_SCAN_DATA_DELTA_PREFIX + account.getId(), deltaDataTransfer)
                .putLong(SYNCHRO_SCAN_DATA_TRANSFER_PREFIX + account.getId(), dataToTransfer)
                .putInt(SYNCHRO_SCAN_RESULT_PREFIX + account.getId(), scanResult)
                .putInt(SYNCHRO_SCAN_RESPONSE_PREFIX + account.getId(), scanResponse).commit();
    }

    public void reset(Context context, Account account)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().remove(SYNCHRO_SCAN_DATA_DELTA_PREFIX + account.getId())
                .remove(SYNCHRO_SCAN_DATA_TRANSFER_PREFIX + account.getId())
                .remove(SYNCHRO_SCAN_RESULT_PREFIX + account.getId()).commit();
    }

    public static SyncScanInfo getLastSyncScanData(Context context, Account account)
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

    public static void setLastSyncScanData(Context context, Account account, long deltaDataTransfer,
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
                        Formatter.formatFileSize(context, (long) StorageManager.getAvailableBytes(context)));
                break;
            case RESULT_WARNING_LOW_STORAGE:
                error = String.format(context.getString(R.string.sync_warning_storage),
                        Formatter.formatFileSize(context, deltaDataTransfer),
                        Formatter.formatFileSize(context, (long) StorageManager.getAvailableBytes(context)));
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
