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
package org.alfresco.mobile.android.application.fragments.workflow.process;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.api.model.impl.ProcessImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
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
public class ProcessesAdapter extends BaseListAdapter<Process, TwoLinesViewHolder>
{
    private GregorianCalendar calendar = new GregorianCalendar();

    private List<Process> selectedItems;

    protected Context context;

    public ProcessesAdapter(FragmentActivity context, int textViewResourceId, List<Process> listItems,
            List<Process> selectedItems)
    {
        super(context, textViewResourceId, listItems);
        this.vhClassName = TwoLinesViewHolder.class.getCanonicalName();
        this.context = context;
        this.selectedItems = selectedItems;
    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, Process item)
    {
        vh.topText.setText(item.getDescription() != null ? item.getDescription() : context
                .getString(R.string.process_no_description));
    }

    @Override
    protected void updateBottomText(TwoLinesViewHolder vh, Process item)
    {
        StringBuilder bottomText = new StringBuilder(item.getName() != null ? item.getName() : getName(context,
                item.getKey()));

        if (item.getEndedAt() == null && ((ProcessImpl) item).getDueAt() != null
                && ((ProcessImpl) item).getDueAt().before(calendar))
        {
            if (bottomText.length() > 0)
            {
                bottomText.append(" - ");
            }
            bottomText.append("<b>");
            bottomText.append("<font color='#9F000F'>");
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM");
            bottomText.append(formatter.format(((ProcessImpl) item).getDueAt().getTime()));
            bottomText.append("</font>");
            bottomText.append("</b>");
        }

        if (item.getInitiatorIdentifier() == null)
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
    protected void updateIcon(TwoLinesViewHolder vh, Process item)
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

    public static String getName(Context context, String keyId)
    {
        int nameId = -1;

        if (WorkflowModel.FAMILY_PROCESS_ADHOC.contains(keyId))
        {
            nameId = R.string.process_adhoc;
        }
        else if (WorkflowModel.FAMILY_PROCESS_PARALLEL_GROUP_REVIEW.contains(keyId))
        {
            nameId = R.string.process_review;
        }
        else if (WorkflowModel.FAMILY_PROCESS_REVIEW.contains(keyId))
        {
            nameId = R.string.process_review;
        }
        else if (WorkflowModel.FAMILY_PROCESS_POOLED_REVIEW.contains(keyId))
        {
            nameId = R.string.process_pooled_review;
        }
        else if (WorkflowModel.FAMILY_PROCESS_PARALLEL_REVIEW.contains(keyId))
        {
            nameId = R.string.process_parallel_review;
        }
        return (nameId != -1) ? context.getString(nameId) : "";
    }
}
