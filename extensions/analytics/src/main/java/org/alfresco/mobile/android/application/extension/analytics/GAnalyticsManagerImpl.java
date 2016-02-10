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
package org.alfresco.mobile.android.application.extension.analytics;

import java.util.List;

import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.SparseArray;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class GAnalyticsManagerImpl extends AnalyticsManager
{
    private Tracker mTracker;

    private GoogleAnalytics analytics;

    private boolean hasOptOut = false;

    private static final boolean DISPATCH_MANUALLY = true;

    protected SharedPreferences.Editor editor;

    protected Integer status = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static GAnalyticsManagerImpl getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new GAnalyticsManagerImpl(context);
            }

            return (GAnalyticsManagerImpl) mInstance;
        }
    }

    protected GAnalyticsManagerImpl(Context context)
    {
        super(context);
        analytics = GoogleAnalytics.getInstance(context);
        mTracker = analytics.newTracker(context.getString(R.string.ga_trackId));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // O
    // ///////////////////////////////////////////////////////////////////////////
    public void optOut(Activity activity, AlfrescoAccount account)
    {
        opt(activity, STATUS_DISABLE, account);
        status = getStatus();
    }

    public void optOutByConfig(Context context, AlfrescoAccount account)
    {
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(ANALYTICS_PREFIX + account.getId(), STATUS_BLOCKED).apply();
        status = getStatus();
    }

    public void optInByConfig(Context context, AlfrescoAccount account)
    {
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(ANALYTICS_PREFIX + account.getId(), STATUS_ENABLE).apply();
        status = getStatus();
    }

    public void optIn(Activity activity, AlfrescoAccount account)
    {
        opt(activity, STATUS_ENABLE, account);
        status = getStatus();
    }

    public void cleanOptInfo(Context context, AlfrescoAccount account)
    {
        if (editor == null)
        {
            editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        }
        if (account != null)
        {
            editor.remove(ANALYTICS_PREFIX + account.getId());
            editor.apply();
        }
        status = getStatus();
    }

    private void opt(Context context, int value, AlfrescoAccount account)
    {
        if (editor == null)
        {
            editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        }
        if (account != null)
        {
            editor.putInt(ANALYTICS_PREFIX + account.getId(), value);
            editor.apply();
        }
    }

    public boolean isEnable()
    {
        if (status == null)
        {
            getStatus();
        }
        return status == STATUS_ENABLE;
    }

    public boolean isEnable(AlfrescoAccount account)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        int tempStatus = sharedPref.getInt(ANALYTICS_PREFIX + account.getId(), STATUS_ENABLE);
        return tempStatus == STATUS_ENABLE;
    }

    public boolean isBlocked()
    {
        if (status == null)
        {
            getStatus();
        }
        return status == STATUS_BLOCKED;
    }

    public int getStatus()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        List<AlfrescoAccount> accounts = AlfrescoAccountManager.retrieveAccounts(appContext);
        int tempStatus = STATUS_ENABLE;
        Integer resultStatus = null;
        for (AlfrescoAccount account : accounts)
        {
            tempStatus = sharedPref.getInt(ANALYTICS_PREFIX + account.getId(), STATUS_ENABLE);
            switch (tempStatus)
            {
                case STATUS_BLOCKED:
                    resultStatus = STATUS_BLOCKED;
                    break;
                case STATUS_DISABLE:
                    resultStatus = STATUS_DISABLE;
                    break;
                default:
                    continue;
            }
        }
        status = resultStatus != null ? resultStatus : STATUS_ENABLE;
        return status;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REPORT
    // ///////////////////////////////////////////////////////////////////////////
    public void startReport(Activity activity)
    {
        analytics.enableAutoActivityReports(activity.getApplication());
        mTracker.send(new HitBuilders.ScreenViewBuilder().setNewSession().build());
    }

    public void reportScreen(String name)
    {
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        if (DISPATCH_MANUALLY)
        {
            analytics.dispatchLocalHits();
        }
    }

    public void reportEvent(String category, String action, String label, int value)
    {
        mTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label)
                .setValue(value).build());
        if (DISPATCH_MANUALLY)
        {
            analytics.dispatchLocalHits();
        }
    }

    public void reportEvent(String category, String action, String label, int value, int customMetricId,
            Long customMetricValue)
    {
        mTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label)
                .setValue(value).setCustomMetric(customMetricId, customMetricValue).build());
        if (DISPATCH_MANUALLY)
        {
            analytics.dispatchLocalHits();
        }
    }

    public void reportInfo(String label, SparseArray<String> dimensions, SparseArray<Long> metrics)
    {
        reportEvent(CATEGORY_SESSION, ACTION_INFO, label, 1, dimensions, metrics);
    }

    public void reportEvent(String category, String action, String label, int eventValue,
            SparseArray<String> dimensions, SparseArray<Long> metrics)
    {
        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder().setCategory(category).setAction(action)
                .setLabel(label).setValue(eventValue);

        if (dimensions != null)
        {
            for (int i = 0; i < dimensions.size(); i++)
            {
                int dimensionId = dimensions.keyAt(i);
                String value = dimensions.get(dimensionId);
                builder.setCustomDimension(dimensionId, value);
            }
        }

        if (metrics != null)
        {
            for (int i = 0; i < metrics.size(); i++)
            {
                int dimensionId = metrics.keyAt(i);
                Long value = metrics.get(dimensionId);
                builder.setCustomMetric(dimensionId, value);
            }
        }

        mTracker.send(builder.build());
        if (DISPATCH_MANUALLY)
        {
            analytics.dispatchLocalHits();
        }
    }

    public void reportError(boolean isFatal, String description)
    {
        mTracker.send(new HitBuilders.ExceptionBuilder().setFatal(isFatal).setDescription(description).build());
        if (DISPATCH_MANUALLY)
        {
            analytics.dispatchLocalHits();
        }
    }
}
