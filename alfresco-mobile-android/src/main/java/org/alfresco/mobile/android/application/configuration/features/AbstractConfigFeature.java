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

package org.alfresco.mobile.android.application.configuration.features;

import java.util.List;

import org.alfresco.mobile.android.api.model.config.FeatureConfig;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.services.impl.AlfrescoServiceRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jpascal on 25/04/2016.
 */
public abstract class AbstractConfigFeature
{

    // ////////////////////////////////////////////////////
    // SETTINGS
    // ////////////////////////////////////////////////////
    public static final int STATUS_BLOCKED = -1;

    public static final int STATUS_DISABLE = 0;

    public static final int STATUS_ENABLE = 1;

    protected SharedPreferences.Editor editor;

    protected Integer status = null;

    protected final Activity activity;

    // ////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ////////////////////////////////////////////////////
    public AbstractConfigFeature(Activity appContext)
    {
        this.activity = appContext;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // O
    // ///////////////////////////////////////////////////////////////////////////
    public void userProtectedByConfig(AlfrescoAccount account)
    {
        editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
        editor.putInt(getFeaturePrefix() + account.getId(), STATUS_BLOCKED).apply();
        status = getStatus();
    }

    public void userVisibileByConfig(AlfrescoAccount account)
    {
        editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
        editor.putInt(getFeaturePrefix() + account.getId(), STATUS_ENABLE).apply();
        status = getStatus();
    }

    public void enable()
    {
        setStatus(STATUS_ENABLE);
        status = getStatus();
    }

    public void disable()
    {
        setStatus(STATUS_DISABLE);
        status = getStatus();
    }

    public void cleanUserVisibility(AlfrescoAccount account)
    {
        if (editor == null)
        {
            editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
        }
        if (account != null)
        {
            editor.remove(getFeaturePrefix() + account.getId());
            editor.apply();
        }
        status = getStatus();
    }

    private void changeUserVisibility(Context context, int value, AlfrescoAccount account)
    {
        if (editor == null)
        {
            editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        }
        if (account != null)
        {
            editor.putInt(getFeaturePrefix() + account.getId(), value);
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
        return PreferenceManager.getDefaultSharedPreferences(activity).getInt(getFeaturePrefix() + account.getId(),
                STATUS_ENABLE) == STATUS_ENABLE;
    }

    public boolean isProtected(AlfrescoAccount account)
    {
        return PreferenceManager.getDefaultSharedPreferences(activity).getInt(getFeaturePrefix() + account.getId(),
                STATUS_ENABLE) == STATUS_BLOCKED;
    }

    public boolean isProtected()
    {
        if (status == null)
        {
            getStatus();
        }
        return status == STATUS_BLOCKED;
    }

    private int getStatus()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        List<AlfrescoAccount> accounts = AlfrescoAccountManager.retrieveAccounts(activity);
        int tempStatus = STATUS_ENABLE;
        Integer resultStatus = null;
        for (AlfrescoAccount account : accounts)
        {
            tempStatus = sharedPref.getInt(getFeaturePrefix() + account.getId(), STATUS_ENABLE);
            switch (tempStatus)
            {
                case STATUS_BLOCKED:
                    status = STATUS_BLOCKED;
                    return STATUS_BLOCKED;
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

    private void setStatus(int status)
    {
        List<AlfrescoAccount> accounts = AlfrescoAccountManager.retrieveAccounts(activity);
        for (AlfrescoAccount account : accounts)
        {
            changeUserVisibility(activity, status, account);
        }
    }

    public abstract String getFeatureType();

    public abstract String getFeaturePrefix();

    public void check(AlfrescoSession session, AlfrescoAccount acc, FeatureConfig feature)
    {
        try
        {
            // Analytics
            if (session instanceof RepositorySession && session.getServiceRegistry() instanceof AlfrescoServiceRegistry)
            {
                ConfigService configService = ((AlfrescoServiceRegistry) session.getServiceRegistry())
                        .getConfigService();
                if (configService != null)
                {
                    if (feature == null && isProtected(acc))
                    {
                        // When server config has been removed we revert
                        userVisibileByConfig(acc);
                    }
                    else if (feature != null)
                    {
                        checkFeatureState(feature, acc);
                    }
                }
                else if (isProtected(acc))
                {
                    // When server config has been removed we revert analytics
                    // to true
                    userVisibileByConfig(acc);
                }
            }
        }
        catch (Exception e)
        {
            // DO Nothing
        }
    }

    public void checkFeatureState(FeatureConfig feature, AlfrescoAccount acc)
    {
    }

}
