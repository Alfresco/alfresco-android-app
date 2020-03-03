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
package org.alfresco.mobile.android.ui.fragments;

import java.lang.ref.WeakReference;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Base Fragment for All fragments available inside the UI Library.
 * 
 * @author Jean Marie Pascal
 */
public abstract class AlfrescoFragment extends DialogFragment implements AnalyticsManager.FragmentAnalyzed
{
    protected static final String ARGUMENT_BASED_ON_TEMPLATE = "basedOnTemplate";

    /** RepositorySession */
    private WeakReference<AlfrescoSession> alfSession;

    /** Flag to retrieve or not alfresco session during onActivityCreate. */
    protected boolean requiredSession = true;

    /** Flag to display an error if the session is not present. */
    protected boolean checkSession = true;

    /** Flag to display an error if the session is not present. */
    protected boolean eventBusRequired = true;

    /** Root View */
    private WeakReference<View> vRoot;

    protected String title;

    protected String screenName;

    /** Flag to send screen event with analytics. */
    protected boolean reportAtCreation = true;

    // /////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Check and retrieve Session
        checkSession();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // Check and retrieve Session
        checkSession();
    }

    @Override
    public void onStart()
    {
        if (eventBusRequired)
        {
            EventBusManager.getInstance().register(this);
        }
        super.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        displayTitle();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (eventBusRequired)
        {
            try
            {
                EventBusManager.getInstance().unregister(this);
            }
            catch (Exception e)
            {
                // Do nothing
            }
        }
    }

    // /////////////////////////////////////////////////////////////
    // SESSION MANAGEMENT
    // ////////////////////////////////////////////////////////////
    public void setSession(AlfrescoSession session)
    {
        this.alfSession = new WeakReference<AlfrescoSession>(session);
    }

    public AlfrescoSession getSession()
    {
        if (alfSession == null || alfSession.get() == null)
        {
            alfSession = new WeakReference<>(SessionUtils.getSession(getActivity()));
        }
        return alfSession.get();
    }

    public boolean hasSession()
    {
        return alfSession != null && alfSession.get() != null;
    }

    protected void checkSession()
    {
        if (requiredSession)
        {
            setSession(SessionUtils.getSession(getActivity()));
        }

        if (checkSession && requiredSession && !hasSession())
        {
            onSessionMissing();
        }
    }

    /**
     * Use this method when the session is missing to display for example an
     * error.
     */
    protected void onSessionMissing()
    {
        AlfrescoNotificationManager.getInstance(getActivity()).showToast(R.string.empty_session);
    }

    // /////////////////////////////////////////////////////////////
    // ACCOUNT MANAGEMENT
    // ////////////////////////////////////////////////////////////
    protected AlfrescoAccount getAccount()
    {
        return SessionUtils.getAccount(getActivity());
    }

    // /////////////////////////////////////////////////////////////
    // VIEW MANAGEMENT
    // ////////////////////////////////////////////////////////////
    protected View getRootView()
    {
        if (vRoot == null) { return null; }
        return vRoot.get();
    }

    protected Fragment getFragmentByTag(String tag)
    {
        return getActivity().getSupportFragmentManager().findFragmentByTag(tag);
    }

    protected void setRootView(View rootView)
    {
        this.vRoot = new WeakReference<View>(rootView);
    }

    protected View viewById(int id)
    {
        if (getRootView() == null) { return null; }
        return getRootView().findViewById(id);
    }

    protected void hide(int id)
    {
        if (getRootView() == null) { return; }
        if (getRootView().findViewById(id) == null) { return; }
        getRootView().findViewById(id).setVisibility(View.GONE);
    }

    protected void show(int id)
    {
        if (getRootView() == null) { return; }
        if (getRootView().findViewById(id) == null) { return; }
        getRootView().findViewById(id).setVisibility(View.VISIBLE);
    }

    protected ActionBar getActionBar()
    {
        if (getActivity() instanceof AlfrescoActivity)
        {
            return ((AlfrescoActivity) getActivity()).getAppActionBar();
        }
        else
        {
            return null;
        }
    }

    // /////////////////////////////////////////////////////////////
    // TITLE
    // ////////////////////////////////////////////////////////////
    /**
     * Used to refresh Title & ActionBar content after sliding menu event.
     */
    public void displayTitle()
    {
        title = TextUtils.isEmpty(title) ? onPrepareTitle() : title;
        if (TextUtils.isEmpty(title)) { return; }
        UIUtils.displayTitle(getActivity(), title);
    }

    public String onPrepareTitle()
    {
        return title;
    }

    // /////////////////////////////////////////////////////////////
    // ANLYTICS
    // ////////////////////////////////////////////////////////////
    public String getScreenName()
    {
        return TextUtils.isEmpty(screenName) ? getClass().getSimpleName() : screenName;
    }

    @Override
    public boolean reportAtCreationEnable()
    {
        return reportAtCreation;
    }
}
