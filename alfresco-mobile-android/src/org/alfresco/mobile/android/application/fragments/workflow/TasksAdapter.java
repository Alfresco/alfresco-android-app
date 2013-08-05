/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

/**
 * @author Jean Marie Pascal
 */
public class TasksAdapter extends BaseListAdapter<Task, GenericViewHolder>
{
    private GregorianCalendar calendar = new GregorianCalendar();

    private List<Task> selectedItems;

    protected Context context;

    public TasksAdapter(Activity context, int textViewResourceId, List<Task> listItems, List<Task> selectedItems)
    {
        super(context, textViewResourceId, listItems);
        this.context = context;
        this.selectedItems = selectedItems;
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Task item)
    {
        vh.topText.setText(item.getDescription());
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, Task item)
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
            UIUtils.setBackground(((LinearLayout) vh.icon.getParent().getParent()),
                    context.getResources().getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(((LinearLayout) vh.icon.getParent().getParent()), null);
        }
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, Task item)
    {
        int iconId = R.drawable.ic_priority_medium;
        switch (item.getPriority())
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

        vh.icon.setImageDrawable(getContext().getResources().getDrawable(iconId));

        if (item.getEndedAt() == null && item.getDueAt() != null && item.getDueAt().before(calendar))
        {
            vh.choose.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_warning));
            vh.choose.setScaleType(ScaleType.CENTER_INSIDE);
        }
        else
        {
            vh.choose.setImageDrawable(null);
        }
    }
}
