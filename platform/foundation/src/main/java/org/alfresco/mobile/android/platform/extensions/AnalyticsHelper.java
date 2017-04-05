/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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

package org.alfresco.mobile.android.platform.extensions;

import java.util.List;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.services.impl.AlfrescoServiceRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.session.authentication.SamlAuthenticationProvider;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.sync.SyncContentProvider;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.SparseArray;

/**
 * Created by jpascal on 29/01/2016.
 */
public class AnalyticsHelper
{

    protected static SparseArray<String> retrieveSessionInfo(AlfrescoSession repoSession)
    {
        SparseArray<String> customDimensions = new SparseArray<>();

        if (repoSession != null)
        {
            if (repoSession instanceof CloudSession)
            {
                customDimensions.append(AnalyticsManager.INDEX_SERVER_VERSION, AnalyticsManager.SERVER_TYPE_CLOUD);
                customDimensions.append(AnalyticsManager.INDEX_SERVER_TYPE, AnalyticsManager.SERVER_TYPE_CLOUD);
                customDimensions.append(AnalyticsManager.INDEX_SERVER_EDITION,
                        ((CloudSession) repoSession).getNetwork().getSubscriptionLevel());
            }
            else
            {
                customDimensions.append(AnalyticsManager.INDEX_SERVER_VERSION,
                        repoSession.getRepositoryInfo().getVersion());
                customDimensions.append(AnalyticsManager.INDEX_SERVER_TYPE, AnalyticsManager.SERVER_TYPE_ONPREMISE);
                customDimensions.append(AnalyticsManager.INDEX_SERVER_EDITION,
                        repoSession.getRepositoryInfo().getEdition());
            }
        }
        return customDimensions;
    }

    protected static SparseArray<Long> retrieveSyncInfo(Context context, AlfrescoAccount account)
    {
        SparseArray<Long> customMetrics = new SparseArray<>();

        Integer syncedFile = getNumberOfSyncedFiles(context, account);
        Integer syncedFolder = getNumberOfSyncedFolder(context, account);

        // Via Custom Metric
        customMetrics.append(AnalyticsManager.INDEX_SYNC_CREATION, 1L);
        customMetrics.append(AnalyticsManager.INDEX_SYNCED_FOLDERS, syncedFolder.longValue());
        customMetrics.append(AnalyticsManager.INDEX_SYNCED_FILES, syncedFile.longValue());
        customMetrics.append(AnalyticsManager.INDEX_SYNCED_SIZE, getSizeOfSyncedFiles(context, account));

        return customMetrics;
    }

    public static void analyzeSync(Context context, AlfrescoAccount account, String analyticInfo)
    {
        if (AnalyticsManager.getInstance(context) == null
                || !AnalyticsManager.getInstance(context).isEnable()) { return; }
        try
        {
            SparseArray<Long> customMetrics = new SparseArray<>();
            SparseArray<String> customDimensions = new SparseArray<>();
            SparseArray<String> customInfo = retrieveSessionInfo(
                    SessionManager.getInstance(context).getSession(account.getId()));
            for (int i = 0; i < customInfo.size(); i++)
            {
                int key = customInfo.keyAt(i);
                customDimensions.append(key, customInfo.get(key));
            }

            Integer syncedFile = getNumberOfSyncedFiles(context, account);
            Integer syncedFolder = getNumberOfSyncedFolder(context, account);
            customMetrics.append(AnalyticsManager.INDEX_SYNCED_SIZE, getSizeOfSyncedFiles(context, account));

            // Via EVENT
            AnalyticsManager.getInstance(context).reportEvent(AnalyticsManager.CATEGORY_SYNC, analyticInfo,
                    AnalyticsManager.SYNCED_FILES, syncedFile, customDimensions, customMetrics);
            AnalyticsManager.getInstance(context).reportEvent(AnalyticsManager.CATEGORY_SYNC, analyticInfo,
                    AnalyticsManager.SYNCED_FOLDERS, syncedFolder, customDimensions, null);

        }
        catch (Exception e)
        {
            Log.d("Analytics Global", Log.getStackTraceString(e));
        }
    }

    protected static Integer getNumberOfSyncedFolder(Context context, AlfrescoAccount account)
    {
        if (account == null) { return 0; }
        Integer syncedFolders = 0;
        try
        {
            Cursor syncedCursor = context.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                    SyncContentSchema.COLUMN_IDS,
                    SyncContentProvider.getAccountFilter(account) + " AND " + SyncContentSchema.COLUMN_MIMETYPE
                            + " LIKE '" + ContentModel.TYPE_FOLDER + "' AND " + SyncContentSchema.COLUMN_STATUS
                            + " NOT IN ( " + SyncContentStatus.STATUS_HIDDEN + ")",
                    null, null);
            syncedFolders = syncedCursor != null ? syncedCursor.getCount() : 0;
            CursorUtils.closeCursor(syncedCursor);
        }
        catch (Exception e)
        {
            Log.d("Analytics Folders", Log.getStackTraceString(e));
        }
        return syncedFolders;
    }

    protected static Integer getNumberOfSyncedFiles(Context context, AlfrescoAccount account)
    {
        if (account == null) { return 0; }

        Integer syncedFiles = 0;
        try
        {
            Cursor syncedCursor = context.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                    SyncContentSchema.COLUMN_IDS,
                    SyncContentProvider.getAccountFilter(account) + " AND " + SyncContentSchema.COLUMN_MIMETYPE
                            + " NOT LIKE  '" + ContentModel.TYPE_FOLDER + "' AND " + SyncContentSchema.COLUMN_STATUS
                            + " NOT IN ( " + SyncContentStatus.STATUS_HIDDEN + ")",
                    null, null);
            syncedFiles = syncedCursor != null ? syncedCursor.getCount() : 0;
            CursorUtils.closeCursor(syncedCursor);
        }
        catch (Exception e)
        {
            Log.d("Analytics Files", Log.getStackTraceString(e));
        }
        return syncedFiles;
    }

    protected static Long getSizeOfSyncedFiles(Context context, AlfrescoAccount account)
    {
        if (account == null) { return 0L; }

        Long syncedSize = 0L;
        String[] columns = new String[] { SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES };
        try
        {
            Cursor syncedCursor = context.getContentResolver().query(SyncContentProvider.CONTENT_URI, columns,
                    SyncContentProvider.getAccountFilter(account) + " AND " + SyncContentSchema.COLUMN_MIMETYPE
                            + " NOT LIKE  '" + ContentModel.TYPE_FOLDER + "' AND " + SyncContentSchema.COLUMN_STATUS
                            + " NOT IN ( " + SyncContentStatus.STATUS_HIDDEN + ")",
                    null, null);
            if (syncedCursor != null)
            {
                while (syncedCursor.moveToNext())
                {
                    syncedSize = syncedSize + syncedCursor.getLong(0);
                }
            }
            CursorUtils.closeCursor(syncedCursor);
        }
        catch (Exception e)
        {
            Log.d("Analytics Size Files", Log.getStackTraceString(e));
        }
        return syncedSize;
    }

    protected static Long getDownloadedFilesCount(Context context, AlfrescoAccount account)
    {
        int size = 0;
        try
        {
            size = AlfrescoStorageManager.getInstance(context).getDownloadFolder(account).list().length;
        }
        catch (Exception e)
        {
            Log.d("Analytics DL Files", Log.getStackTraceString(e));
        }
        return (long) size;
    }

    public static void reportScreen(Context context, String screenName)
    {
        if (AnalyticsManager.getInstance(context) == null
                || !AnalyticsManager.getInstance(context).isEnable()) { return; }
        AnalyticsManager.getInstance(context).reportScreen(screenName);
    }

    public static void reportOperationEvent(Context context, String category, String action, String label, int value,
            boolean hasException)
    {
        if (AnalyticsManager.getInstance(context) == null
                || !AnalyticsManager.getInstance(context).isEnable()) { return; }
        AnalyticsManager.getInstance(context).reportEvent(category, action,
                (hasException) ? AnalyticsManager.LABEL_FAILED : label, value);
    }

    public static void reportOperationEvent(Context context, String category, String action, String label, int value,
            boolean hasException, int customMetricId, Long customMetricValue)
    {
        if (AnalyticsManager.getInstance(context) == null
                || !AnalyticsManager.getInstance(context).isEnable()) { return; }
        AnalyticsManager.getInstance(context).reportEvent(category, action,
                (hasException) ? AnalyticsManager.LABEL_FAILED : label, value, customMetricId, customMetricValue);
    }

    public static void checkServerConfiguration(Context context, AlfrescoSession session, AlfrescoAccount acc)
    {
        try
        {
            // Analytics
            if (session instanceof RepositorySession && AnalyticsManager.getInstance(context) != null
                    && session.getServiceRegistry() instanceof AlfrescoServiceRegistry)
            {
                ConfigService configService = ((AlfrescoServiceRegistry) session.getServiceRegistry())
                        .getConfigService();
                if (configService != null)
                {
                    List<FeatureConfig> configs = configService.getFeatureConfig();

                    if (configs.isEmpty() && AnalyticsManager.getInstance(context).isBlocked(acc))
                    {
                        // When server config has been removed we revert
                        // analytics
                        // to true
                        AnalyticsManager.getInstance(context).optInByConfig(context, acc);
                    }

                    for (FeatureConfig feature : configs)
                    {
                        if (FeatureConfig.FEATURE_ANALYTICS.equals(feature.getType()))
                        {
                            // When analytics enable via server config
                            if (feature.isEnable() && !AnalyticsManager.getInstance(context).isEnable(acc))
                            {
                                AnalyticsManager.getInstance(context).optInByConfig(context, acc);
                            }
                            // When analytics disable via server config
                            else if (!feature.isEnable())
                            {
                                if (AnalyticsManager.getInstance(context).isEnable())
                                {
                                    AnalyticsManager.getInstance(context).reportEvent(
                                            AnalyticsManager.CATEGORY_SETTINGS, AnalyticsManager.ACTION_ANALYTICS,
                                            AnalyticsManager.LABEL_DISABLE_BY_CONFIG, 1);
                                }

                                if (!AnalyticsManager.getInstance(context).isBlocked(acc))
                                {
                                    AnalyticsManager.getInstance(context).optOutByConfig(context, acc);
                                }
                            }
                            break;
                        }
                    }
                }
                else if (AnalyticsManager.getInstance(context).isBlocked(acc))
                {
                    // When server config has been removed we revert analytics
                    // to true
                    AnalyticsManager.getInstance(context).optInByConfig(context, acc);
                }
            }
        }
        catch (Exception e)
        {
            // DO Nothing
        }
    }

    public static String getAccountType(int typeId)
    {
        switch (typeId)
        {
            case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                return AnalyticsManager.SERVER_TYPE_CLOUD;
            case AlfrescoAccount.TYPE_ALFRESCO_CMIS:
                return AnalyticsManager.SERVER_TYPE_ONPREMISE;
            case AlfrescoAccount.TYPE_ALFRESCO_CMIS_SAML:
                return AnalyticsManager.SERVER_TYPE_ONPREMISE_SAML;
            default:
                return AnalyticsManager.SERVER_TYPE_ONPREMISE;
        }
    }

    public static String getAccountType(AlfrescoSession session)
    {
        if (session instanceof RepositorySession
                && ((RepositorySession) session).getAuthenticationProvider() instanceof SamlAuthenticationProvider)
        {
            return AnalyticsManager.SERVER_TYPE_ONPREMISE_SAML;
        }
        else if (session instanceof CloudSession)
        {
            return AnalyticsManager.SERVER_TYPE_CLOUD;
        }
        else
        {
            return AnalyticsManager.SERVER_TYPE_ONPREMISE;
        }
    }
}
