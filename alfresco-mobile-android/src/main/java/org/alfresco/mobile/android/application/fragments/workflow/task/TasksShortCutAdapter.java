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
package org.alfresco.mobile.android.application.fragments.workflow.task;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.workflow.SimpleViewHolder;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import androidx.fragment.app.FragmentActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TasksShortCutAdapter extends BaseListAdapter<Integer, SimpleViewHolder>
{
    private AlfrescoAccount account;

    public TasksShortCutAdapter(FragmentActivity context)
    {
        super(context, R.layout.app_header_row, SHORTCUTS);
        this.vhClassName = SimpleViewHolder.class.getCanonicalName();
        this.account = SessionUtils.getAccount(context);
    }

    @Override
    protected void updateTopText(SimpleViewHolder vh, Integer item)
    {
        vh.topText.setVisibility(View.GONE);
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getContext().getResources()
                .getDisplayMetrics());
        vh.bottomText.setMinHeight(px);
    }

    @Override
    protected void updateBottomText(SimpleViewHolder vh, Integer item)
    {
        vh.bottomText.setText(getContext().getString(item));
    }

    @Override
    protected void updateIcon(SimpleViewHolder vh, Integer item)
    {
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return super.getView(position, convertView, parent);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = super.getView(position, convertView, parent);
        if (AlfrescoAccountManager.getInstance(getContext()).hasMultipleAccount())
        {
            ((TextView) v.findViewById(R.id.toptext)).setText(UIUtils.getAccountLabel(account));
            v.findViewById(R.id.toptext).setVisibility(View.VISIBLE);
        }
        else
        {
            v.findViewById(R.id.toptext).setVisibility(View.GONE);
        }
        return v;
    }

    public static final int FILTER_ASSIGNED = 0;

    public static final int FILTER_INITIATOR = 1;

    public static final int FILTER_COMPLETED = 2;

    public static final int FILTER_HIGH_PRIORITY = 3;

    public static final int FILTER_DUE_TODAY = 4;

    public static final int FILTER_OVERDUE = 5;

    public static final int FILTER_ACTIVE = 6;

    public static final int FILTER_CUSTOM = 7;

    private static final List<Integer> SHORTCUTS = new ArrayList<Integer>(10)
    {
        private static final long serialVersionUID = 1L;
        {
            add(R.string.task_filter_assigned_me);
            add(R.string.task_filter_initiator);
            add(R.string.task_filter_completed);
            add(R.string.task_filter_high_priority);
            add(R.string.task_filter_due_today);
            add(R.string.task_filter_overdue);
            add(R.string.task_filter_active);
            add(R.string.task_filter_custom);
        }
    };
}
