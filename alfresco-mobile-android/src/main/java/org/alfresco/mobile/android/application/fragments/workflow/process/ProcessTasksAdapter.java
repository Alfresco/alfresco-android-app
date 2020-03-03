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

import java.lang.ref.WeakReference;
import java.util.List;

import org.alfresco.mobile.android.api.constants.WorkflowModel;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.user.UserProfileFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;

import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * @author Jean Marie Pascal
 */
public class ProcessTasksAdapter extends BaseListAdapter<Task, ProcessViewHolder>
{
    protected WeakReference<FragmentActivity> activityRef;

    public ProcessTasksAdapter(FragmentActivity activity, int textViewResourceId, List<Task> listItems)
    {
        super(activity, textViewResourceId, listItems);
        this.activityRef = new WeakReference<>(activity);
        this.vhClassName = ProcessViewHolder.class.getCanonicalName();
    }

    @Override
    protected void updateTopText(ProcessViewHolder vh, Task item)
    {
        boolean isReviewTask = (WorkflowModel.TASK_REVIEW.equals(item.getKey()) || WorkflowModel.TASK_ACTIVITI_REVIEW
                .equals(item.getKey()));

        Boolean isApproved = null;

        // Task with outcome
        if (isReviewTask && item.getEndedAt() != null)
        {
            String approve = item.getVariableValue(WorkflowModel.PROP_OUTCOME);
            if (approve != null && !approve.isEmpty())
            {
                vh.icon_statut.setVisibility(View.VISIBLE);
                if (WorkflowModel.TRANSITION_APPROVE.equalsIgnoreCase(approve))
                {
                    vh.icon_statut.setBackgroundResource(R.drawable.sync_status_success);
                    isApproved = true;
                }
                else
                {
                    isApproved = false;
                    vh.icon_statut.setBackgroundResource(R.drawable.sync_status_failed);
                }
            }
            else
            {
                vh.icon_statut.setVisibility(View.GONE);
            }
        }
        else
        {
            vh.icon_statut.setVisibility(View.GONE);
        }

        StringBuilder builder = new StringBuilder();
        // Task is in progress
        if (item.getEndedAt() == null)
        {
            builder.append(String.format(getContext().getString(R.string.task_user_not_completed),
                    item.getAssigneeIdentifier()));
            if (!DisplayUtils.hasCentralPane(activityRef.get()))
            {
                vh.content.setVisibility(View.GONE);
            }
        }
        else
        {
            if (isReviewTask && isApproved != null)
            {
                // Task is approved or rejected
                builder.append(String.format(
                        getContext().getString(isApproved ? R.string.task_user_approved : R.string.task_user_rejected),
                        item.getAssigneeIdentifier()));
            }
            else
            {
                // Task is completed
                builder.append(String.format(getContext().getString(R.string.task_user_completed),
                        item.getAssigneeIdentifier()));
            }

            builder.append(" ");
            builder.append(formatDate(activityRef.get(), item.getEndedAt().getTime()));
        }

        if (item.hasAllVariables())
        {
            // Task with comment
            String comment = item.getVariableValue(WorkflowModel.PROP_COMMENT);
            if (comment != null && !comment.isEmpty())
            {
                builder.append(" ");
                builder.append(getContext().getString(R.string.task_user_commented));
                vh.content.setText(comment);
                vh.content.setVisibility(View.VISIBLE);
            }
            else if (!DisplayUtils.hasCentralPane(activityRef.get()))
            {
                vh.content.setVisibility(View.GONE);
            }
        }
        vh.topText.setText(Html.fromHtml(builder.toString()), TextView.BufferType.SPANNABLE);
    }

    @Override
    protected void updateBottomText(ProcessViewHolder vh, Task item)
    {
        vh.bottomText.setVisibility(View.GONE);
    }

    @Override
    protected void updateIcon(ProcessViewHolder vh, Task item)
    {
        final Task item2 = item;
        ((View) vh.icon.getParent()).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserProfileFragment.with(activityRef.get()).personId(item2.getAssigneeIdentifier()).display();

            }
        });
        RenditionManager.with(activityRef.get()).loadAvatar(item.getAssigneeIdentifier())
                .placeHolder(R.drawable.ic_person_light).into(vh.icon);
    }
}
