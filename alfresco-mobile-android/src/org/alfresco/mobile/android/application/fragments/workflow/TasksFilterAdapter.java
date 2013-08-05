/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.workflow;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public class TasksFilterAdapter extends BaseListAdapter<Integer, GenericViewHolder>
{
    public TasksFilterAdapter(Activity context)
    {
        super(context, R.layout.sdk_list_row, SHORTCUTS);
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Integer item)
    {
        vh.topText.setText(getContext().getString(item));
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, Integer item)
    {
        vh.bottomText.setVisibility(View.GONE);
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, Integer item)
    {
        vh.icon.setVisibility(View.GONE);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView, parent);
    }

    private static final List<Integer> SHORTCUTS = new ArrayList<Integer>(10)
    {
        private static final long serialVersionUID = 1L;
        {
            add(R.string.task_filter_active);
            add(R.string.task_filter_completed);
            add(R.string.task_filter_high_priority);
            add(R.string.task_filter_due_today);
            add(R.string.task_filter_overdue);
            add(R.string.task_filter_assigned_me);
            add(R.string.task_filter_custom);
        }
    };
}
