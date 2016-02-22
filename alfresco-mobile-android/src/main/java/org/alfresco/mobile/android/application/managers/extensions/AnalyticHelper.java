/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
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

package org.alfresco.mobile.android.application.managers.extensions;

import java.util.List;

import org.alfresco.mobile.android.api.model.config.ProfileConfig;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

/**
 * Created by jpascal on 29/01/2016.
 */
public class AnalyticHelper extends AnalyticsHelper
{

    public static void optIn(Activity activity, AlfrescoAccount account)
    {
        AnalyticsManager.getInstance(activity).optIn(activity);
        AnalyticsManager.getInstance(activity).startReport(activity);
        try
        {
            analyzeSession(activity, ((AlfrescoActivity) activity).getCurrentAccount(),
                    ((AlfrescoActivity) activity).getCurrentSession());
        }
        catch (Exception e)
        {

        }
        AnalyticsManager.getInstance(activity).reportEvent(AnalyticsManager.CATEGORY_SETTINGS,
                AnalyticsManager.ACTION_ANALYTICS, AnalyticsManager.LABEL_ENABLE, 1);
    }

    public static void optOut(Activity activity, AlfrescoAccount account)
    {
        AnalyticsManager.getInstance(activity).reportEvent(AnalyticsManager.CATEGORY_SETTINGS,
                AnalyticsManager.ACTION_ANALYTICS, AnalyticsManager.LABEL_DISABLE, 1);
        AnalyticsManager.getInstance(activity).optOut(activity);
    }

    public static void cleanOpt(Activity activity, AlfrescoAccount account)
    {
        if (AnalyticsManager.getInstance(activity) == null) { return; }
        AnalyticsManager.getInstance(activity).cleanOptInfo(activity, account);
    }

    public static void analyzeSession(Context context, AlfrescoAccount account, AlfrescoSession repoSession)
    {
        if (repoSession == null || account == null) { return; }
        if (AnalyticsManager.getInstance(context) == null
                || !AnalyticsManager.getInstance(context).isEnable()) { return; }
        try
        {
            SparseArray<String> customDimensions = new SparseArray<>();
            SparseArray<Long> customMetrics = new SparseArray<>();

            // Accounts Info
            List<AlfrescoAccount> accounts = AlfrescoAccountManager.retrieveAccounts(context);
            customMetrics.append(AnalyticsManager.INDEX_ACCOUNT_NUMBER, (long) accounts.size());
            customDimensions.append(AnalyticsManager.INDEX_ACCOUNT_COUNT, getAccountLabel(accounts.size()));

            // Settings Info
            boolean dataProtectionEnable = DataProtectionManager.getInstance(context).hasDataProtectionEnable();
            boolean passcodeEnable = PasscodePreferences.hasPasscode(context);
            Boolean syncWifiEnable = SyncContentManager.getInstance(context).hasWifiOnlySync(account);
            customMetrics.append(AnalyticsManager.INDEX_PASSCODE, passcodeEnable ? 1L : 0L);
            customMetrics.append(AnalyticsManager.INDEX_DATA_PROTECTION, dataProtectionEnable ? 1L : 0L);
            customMetrics.append(AnalyticsManager.INDEX_SYNC_CELLULAR, syncWifiEnable ? 0L : 1L);

            // Server Info
            customMetrics.append(AnalyticsManager.INDEX_SESSION_CREATION, 1L);
            SparseArray<String> customInfo = retrieveSessionInfo(repoSession);
            for (int i = 0; i < customInfo.size(); i++)
            {
                int key = customInfo.keyAt(i);
                customDimensions.append(key, customInfo.get(key));
            }

            // Sync Info
            SparseArray<Long> syncInfo = retrieveSyncInfo(context, account);
            for (int i = 0; i < syncInfo.size(); i++)
            {
                int key = syncInfo.keyAt(i);
                if (key == AnalyticsManager.INDEX_SYNCED_FILES)
                {
                    customDimensions.append(AnalyticsManager.INDEX_SYNC_FILE_COUNT,
                            getSyncFileLabel(syncInfo.get(key)));
                }
                customMetrics.append(key, syncInfo.get(key));
            }

            // Download Info
            customMetrics.append(AnalyticsManager.INDEX_LOCAL_FILES, getDownloadedFilesCount(context, account));

            // Profiles
            customMetrics.append(AnalyticsManager.INDEX_PROFILES, getProfilesCount(context, account));

            AnalyticsManager.getInstance(context).reportInfo(AnalyticsManager.ACTION_INFO, customDimensions,
                    customMetrics);
        }
        catch (Exception e)
        {
            // Do Nothing
        }
    }

    protected static Long getProfilesCount(Context context, AlfrescoAccount account)
    {
        int size = 0;
        try
        {
            if (ConfigManager.getInstance(context) != null
                    && ConfigManager.getInstance(context).hasConfig(account.getId()))
            {
                List<ProfileConfig> profileListing = ConfigManager.getInstance(context).getConfig(account.getId())
                        .getProfiles();
                size = profileListing.size();
            }

        }
        catch (Exception e)
        {
            Log.d("Analytics Profiles", Log.getStackTraceString(e));
        }
        return (long) size;
    }

    protected static String getAccountLabel(int value)
    {
        String label = AnalyticsManager.INDEX_ACCOUNT_COUNT_1;
        switch (value)
        {
            case 1:
                return AnalyticsManager.INDEX_ACCOUNT_COUNT_1;
            case 2:
                return AnalyticsManager.INDEX_ACCOUNT_COUNT_2;
            case 3:
                return AnalyticsManager.INDEX_ACCOUNT_COUNT_3;
            case 4:
                return AnalyticsManager.INDEX_ACCOUNT_COUNT_4;
            case 5:
                return AnalyticsManager.INDEX_ACCOUNT_COUNT_5;
        }
        return label;
    }

    protected static String getSyncFileLabel(long value)
    {
        if (value <= 0)
        {
            return AnalyticsManager.INDEX_SYNC_FILE_COUNT_0;
        }
        else if (value <= 1)
        {
            return AnalyticsManager.INDEX_SYNC_FILE_COUNT_1;
        }
        else if (value <= 5)
        {
            return AnalyticsManager.INDEX_SYNC_FILE_COUNT_5;
        }
        else if (value <= 10)
        {
            return AnalyticsManager.INDEX_SYNC_FILE_COUNT_10;
        }
        else if (value <= 20)
        {
            return AnalyticsManager.INDEX_SYNC_FILE_COUNT_20;
        }
        else if (value <= 50)
        {
            return AnalyticsManager.INDEX_SYNC_FILE_COUNT_50;
        }
        else if (value <= 100)
        {
            return AnalyticsManager.INDEX_SYNC_FILE_COUNT_100;
        }
        else if (value <= 250)
        {
            return AnalyticsManager.INDEX_SYNC_FILE_COUNT_250;
        }
        else if (value <= 500)
        {
            return AnalyticsManager.INDEX_SYNC_FILE_COUNT_500;
        }
        else if (value <= 1000)
        {
            return AnalyticsManager.INDEX_SYNC_FILE_COUNT_1000;
        }
        else
        {
            return AnalyticsManager.INDEX_SYNC_FILE_COUNT_1001;
        }
    }

}
