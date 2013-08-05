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

import java.util.List;

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.ProcessDefinition;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.content.Context;

/**
 * @author Jean Marie Pascal
 */
public class ProcessesDefinitionAdapter extends BaseListAdapter<ProcessDefinition, GenericViewHolder>
{
    protected Context context;

    public ProcessesDefinitionAdapter(Activity context, int textViewResourceId, List<ProcessDefinition> listItems)
    {
        super(context, textViewResourceId, listItems);
        this.context = context;
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, ProcessDefinition item)
    {
        vh.topText.setText(item.getName());
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, ProcessDefinition item)
    {
        vh.bottomText.setText((String) item.getData().get(OnPremiseConstant.DESCRIPTION_VALUE));
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, ProcessDefinition item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_validate));
    }
}
