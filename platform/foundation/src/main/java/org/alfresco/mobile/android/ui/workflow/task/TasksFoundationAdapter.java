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
package org.alfresco.mobile.android.ui.workflow.task;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.TwoLinesCaptionViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author Jean Marie Pascal
 */
public class TasksFoundationAdapter extends BaseListAdapter<Task, TwoLinesCaptionViewHolder>
{
    private GregorianCalendar calendar = new GregorianCalendar();

    private List<Task> selectedItems;

    protected Context context;

    public TasksFoundationAdapter(FragmentActivity context, int textViewResourceId, List<Task> listItems,
            List<Task> selectedItems)
    {
        super(context, textViewResourceId, listItems);
        this.vhClassName = TwoLinesCaptionViewHolder.class.getCanonicalName();
        this.context = context;
        this.selectedItems = selectedItems;
    }

    @Override
    protected void updateTopText(TwoLinesCaptionViewHolder vh, Task item)
    {
        vh.topText.setText(item.getDescription());
    }

    @Override
    protected void updateBottomText(TwoLinesCaptionViewHolder vh, Task item)
    {
        StringBuilder bottomText = new StringBuilder(item.getName());

        if (item.getEndedAt() == null && item.getDueAt() != null && item.getDueAt().before(calendar))
        {
            bottomText.append(" - ");
            bottomText.append("<b>");
            bottomText.append("<font color='#9F000F'>");
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM");
            bottomText.append(formatter.format(item.getDueAt().getTime()));
            bottomText.append("</font>");
            bottomText.append("</b>");
        }

        if (item.getAssigneeIdentifier() == null)
        {
            bottomText.append(" - ");
            bottomText.append("<b>");
            bottomText.append(getContext().getString(R.string.tasks_assignee_unassigned));
            bottomText.append("</b>");
        }

        vh.bottomText.setText(Html.fromHtml(bottomText.toString()), TextView.BufferType.SPANNABLE);

        if (selectedItems != null && selectedItems.contains(item))
        {
            UIUtils.setBackground(((RelativeLayout) vh.icon.getParent()),
                    context.getResources().getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(((RelativeLayout) vh.icon.getParent()), null);
        }
    }

    @Override
    protected void updateIcon(TwoLinesCaptionViewHolder vh, Task item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(getPriorityIconId(item.getPriority())));
        vh.choose.setVisibility(View.GONE);
    }

    public static int getPriorityIconId(int priority)
    {
        int iconId = R.drawable.ic_priority_medium;
        switch (priority)
        {
            case 3:
                iconId = R.drawable.ic_priority_low;
                break;
            case 2:
                iconId = R.drawable.ic_priority_medium;
                break;
            case 1:
                iconId = R.drawable.ic_priority_high;
                break;
            default:
                iconId = R.drawable.ic_workflow;
                break;
        }
        return iconId;
    }
}
