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

import java.util.List;

import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.View;

/**
 * @author Jean Marie Pascal
 */
public class ProcessTasksAdapter extends BaseListAdapter<Task, GenericViewHolder>
{
    protected Context context;

    private RenditionManager renditionManager;

    public ProcessTasksAdapter(Activity context, int textViewResourceId, List<Task> listItems)
    {
        super(context, textViewResourceId, listItems);
        this.context = context;
        renditionManager = ApplicationManager.getInstance(context).getRenditionManager(context);
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Task item)
    {
        vh.topText.setVisibility(View.GONE);
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, Task item)
    {
        String textSummary = item.getName();
        if (item.getEndedAt() == null)
        {
            textSummary = String.format(getContext().getString(R.string.task_user_not_completed),
                    item.getAssigneeIdentifier());
        }
        else
        {
            textSummary = String.format(getContext().getString(R.string.task_user_completed),
                    item.getAssigneeIdentifier());
        }
        vh.bottomText.setText(Html.fromHtml(textSummary));
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, Task item)
    {
        renditionManager.display(vh.icon, item.getAssigneeIdentifier(), R.drawable.ic_person);
    }
}
