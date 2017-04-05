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
package org.alfresco.mobile.android.ui.activity;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.extensions.HockeyAppManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.ui.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.ui.operation.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;

import com.mattprecious.telescope.EmailDeviceInfoLens;
import com.mattprecious.telescope.TelescopeLayout;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Base class for all activities.
 *
 * @author Jean Marie Pascal
 */
public abstract class AlfrescoActivity extends AppCompatActivity
{
    // SESSION FLAG
    public static final int SESSION_LOADING = 1;

    public static final int SESSION_ACTIVE = 2;

    public static final int SESSION_INACTIVE = 4;

    public static final int SESSION_ERROR = 8;

    protected LocalBroadcastManager broadcastManager;

    protected SessionManager sessionManager;

    protected BroadcastReceiver receiver;

    protected BroadcastReceiver utilsReceiver;

    protected List<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>(2);

    protected List<BroadcastReceiver> publicReceivers = new ArrayList<BroadcastReceiver>(2);

    protected RenditionManager renditionManager;

    protected int telescopeId;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        broadcastManager = LocalBroadcastManager.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        IntentFilter filters = new IntentFilter();
        filters.addAction(PrivateIntent.ACTION_DISPLAY_ERROR);
        broadcastManager.registerReceiver(utilsReceiver, filters);

        // HockeyApp
        if (HockeyAppManager.getInstance(this) != null)
        {
            HockeyAppManager.getInstance(this).checkForUpdates(this);
        }
    }

    @Override
    protected void onStart()
    {
        if (sessionManager == null)
        {
            sessionManager = SessionManager.getInstance(this);
        }

        EventBusManager.getInstance().register(this);

        initBugReport();

        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // HockeyApp
        if (HockeyAppManager.getInstance(this) != null)
        {
            HockeyAppManager.getInstance(this).checkForCrashes(this);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        for (BroadcastReceiver bReceiver : receivers)
        {
            broadcastManager.unregisterReceiver(bReceiver);
        }

        for (BroadcastReceiver bReceiver : publicReceivers)
        {
            unregisterReceiver(bReceiver);
        }

        receivers.clear();
        publicReceivers.clear();
        try
        {
            EventBusManager.getInstance().unregister(this);
        }
        catch (Exception e)
        {
            // DO Nothing
        }
    }

    @Override
    protected void onDestroy()
    {
        cleanUp();
        super.onDestroy();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public Fragment getFragment(String tag)
    {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    protected boolean isVisible(String tag)
    {
        return getSupportFragmentManager().findFragmentByTag(tag) != null
                && getSupportFragmentManager().findFragmentByTag(tag).isAdded();
    }

    public void displayWaitingDialog()
    {
        if (getSupportFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null)
        {
            new WaitingDialogFragment().show(getSupportFragmentManager(), WaitingDialogFragment.TAG);
        }
    }

    public void removeWaitingDialog()
    {
        if (getSupportFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) != null)
        {
            ((DialogFragment) getSupportFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG)).dismiss();
        }

        if (getSupportFragmentManager().findFragmentByTag(OperationWaitingDialogFragment.TAG) != null)
        {
            ((DialogFragment) getSupportFragmentManager().findFragmentByTag(OperationWaitingDialogFragment.TAG))
                    .dismiss();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACCOUNTS / SESSION MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public void swapAccount(AlfrescoAccount account)
    {
        // Analytics
        AnalyticsHelper.reportOperationEvent(this, AnalyticsManager.CATEGORY_SESSION, AnalyticsManager.ACTION_SWITCH,
                AnalyticsHelper.getAccountType(account.getTypeId()), 1, false);

        setCurrentAccount(account);
        SessionManager.getInstance(this).loadSession(account);
    }

    public void setCurrentAccount(AlfrescoAccount account)
    {
        sessionManager.saveAccount(account);
    }

    public void setCurrentAccount(long accountId)
    {
        sessionManager.saveAccount(AlfrescoAccountManager.getInstance(this).retrieveAccount(accountId));
    }

    public AlfrescoAccount getCurrentAccount()
    {
        if (sessionManager == null)
        {
            sessionManager = SessionManager.getInstance(this);
        }

        return sessionManager.getCurrentAccount();
    }

    public AlfrescoSession getCurrentSession()
    {
        if (sessionManager == null)
        {
            sessionManager = SessionManager.getInstance(this);
            return null;
        }

        return getCurrentAccount() != null ? sessionManager.getSession(getCurrentAccount().getId()) : null;
    }

    public void setSessionState(int state)
    {

    }

    public void setSessionState(int state, int sessionMessageId)
    {

    }

    // ///////////////////////////////////////////////////////////////////////////
    // MANAGERS
    // ///////////////////////////////////////////////////////////////////////////
    public RenditionManager getRenditionManager()
    {
        return renditionManager;
    }

    public void setRenditionManager(RenditionManager renditionManager)
    {
        this.renditionManager = renditionManager;
    }

    public ActionBar getAppActionBar()
    {
        return getSupportActionBar();
    }

    // ////////////////////////////////////////////////////////
    // BUG REPORTING
    // ///////////////////////////////////////////////////////
    public void initBugReport()
    {
        try
        {
            TelescopeLayout telescopeView = (TelescopeLayout) findViewById(telescopeId);
            telescopeView.setPointerCount(getResources().getInteger(R.integer.bugreport_pointer));
            telescopeView.setVibrate(false);
            telescopeView.setLens(new EmailDeviceInfoLens(this, getResources().getStringArray(R.array.bugreport_email),
                    getString(R.string.bug_report_title),
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName,
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionCode));
        }
        catch (Exception e)
        {

        }
    }

    // ////////////////////////////////////////////////////////
    // Clean Up
    // ///////////////////////////////////////////////////////
    public void cleanUp()
    {
        TelescopeLayout.cleanUp(this);
    }

    // ////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////
    public void registerPublicReceiver(BroadcastReceiver receiver, IntentFilter filter)
    {
        if (receiver != null && filter != null)
        {
            registerReceiver(receiver, filter);
            publicReceivers.add(receiver);
        }
    }

}
