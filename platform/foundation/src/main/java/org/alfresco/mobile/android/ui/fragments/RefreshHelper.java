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

import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.RefreshFragment;

import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;

public class RefreshHelper implements OnRefreshListener
{
    private SwipeRefreshLayout swipeLayout;

    private WeakReference<RefreshFragment> fragmentRef;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public RefreshHelper(FragmentActivity activity, final RefreshFragment fragment, View rootView)
    {
        if (rootView == null) { return; }
        fragmentRef = new WeakReference<RefreshFragment>(fragment);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.ptr_layout);

        if (AccessibilityUtils.isEnabled(activity))
        {
            swipeLayout.setEnabled(false);
            swipeLayout.setRefreshing(false);
        }
        else
        {
            swipeLayout.setOnRefreshListener(this);
            swipeLayout.setColorSchemeResources(R.color.alfresco_dbp_green_dark, R.color.alfresco_dbp_orange,
                    R.color.alfresco_dbp_blue_dark, R.color.alfresco_dbp_orange_dark);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // METHODS
    // //////////////////////////////////////////////////////////////////////
    public void setRefreshComplete()
    {
        if (swipeLayout != null)
        {
            swipeLayout.setRefreshing(false);
        }
    }

    public void setRefreshing()
    {
        if (swipeLayout != null)
        {
            swipeLayout.setRefreshing(true);
        }
    }

    public void setEnabled(boolean isEnable)
    {
        if (swipeLayout != null)
        {
            swipeLayout.setEnabled(isEnable);
        }
    }

    @Override
    public void onRefresh()
    {
        if (fragmentRef != null && fragmentRef.get() != null)
        {
            fragmentRef.get().refresh();
        }
    }
}
