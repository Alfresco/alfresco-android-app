package org.alfresco.mobile.android.application.manager;

import java.io.File;

import org.alfresco.mobile.android.application.VersionNumber;
import org.alfresco.mobile.android.application.utils.IOUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

public class UpgradeManager implements VersionNumber
{
    private static final String TAG = UpgradeManager.class.getName();

    private static final String VERSION_NUMBER = "applicationVersionNumber";

    private Context context;

    private SharedPreferences prefs;

    private int versionNumber;

    private int currentVersionNumber;

    private boolean canUpgrade = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public UpgradeManager(Context context)
    {
        this.context = context.getApplicationContext();
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Check if application has been updated
        checkVersionCode();
        
        // Upgrade Manager
        if (canUpgrade())
        {
            upgrade();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CHECK UPGRADE
    // ///////////////////////////////////////////////////////////////////////////
    private void checkVersionCode()
    {
        try
        {
            // Retrieve current version Number
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            currentVersionNumber = info.versionCode;

            // Check if application has been updated
            if (prefs.contains(VERSION_NUMBER))
            {
                // Retrieve previous version Number
                versionNumber = prefs.getInt(VERSION_NUMBER, LATEST_VERSION);

                // Determine if upgrade process is necessary
                if (currentVersionNumber > versionNumber)
                {
                    canUpgrade = true;
                }
            }
            else
            {
                //Save status
                prefs.edit().putInt(VERSION_NUMBER, currentVersionNumber).commit();
            }
        }
        catch (NameNotFoundException e)
        {
            Log.w(TAG, "Error during upgrade process");
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UPGRADE PROCESS
    // ///////////////////////////////////////////////////////////////////////////
    public void upgrade()
    {
        if (canUpgrade)
        {
            upgradeVersion110();
            
            //Upgrade done. Save current state.
            prefs.edit().putInt(VERSION_NUMBER, currentVersionNumber).commit();
            versionNumber = currentVersionNumber;
            canUpgrade = false;
        }
    }

    public boolean canUpgrade()
    {
        return canUpgrade;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UPGRADE TASK 1.1.0
    // ///////////////////////////////////////////////////////////////////////////
    private static final String UPGRADE_MIGRATION_FILES = "filesmigrated";

    /**
     * If from 1.1.0 and before to 1.1.0 and after
     */
    private void upgradeVersion110()
    {
        if (!prefs.getBoolean(UPGRADE_MIGRATION_FILES, false) && currentVersionNumber >= VERSION_1_1_0 && versionNumber < VERSION_1_1_0)
        {
            Log.i(TAG, "[upgradeVersion110] : Start");
            // Transfer downloads to new folder structure if they haven't been
            // already.
            if (!prefs.getBoolean(UPGRADE_MIGRATION_FILES, false))
            {
                File oldDownloads = StorageManager.getOldDownloadFolder(context);
                File newDownloads = StorageManager.getPrivateFolder(context, "", null);

                if (IOUtils.isFolderEmpty(oldDownloads) == false)
                {
                    if (oldDownloads != null && newDownloads != null)
                    {
                        IOUtils.transferFilesBackground(oldDownloads.getPath(), newDownloads.getPath(),
                                StorageManager.DLDIR, true, true);
                        prefs.edit().putBoolean(UPGRADE_MIGRATION_FILES, true).commit();
                    }
                }
                else
                {
                    prefs.edit().putBoolean(UPGRADE_MIGRATION_FILES, true).commit();
                }
            }
            Log.i(TAG, "[upgradeVersion110] : Completed");
        }
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // UPGRADE TASK 1.2.0
    // ///////////////////////////////////////////////////////////////////////////

}
