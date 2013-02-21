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
package org.alfresco.mobile.android.application.integration;

import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public class SimpleAccountAdapter extends BaseListAdapter<Account, GenericViewHolder>
{
    public SimpleAccountAdapter(Activity context, int textViewResourceId, List<Account> listItems)
    {
        super(context, textViewResourceId, listItems);
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Account item)
    {
        vh.topText.setText(item.getDescription());
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, Account item)
    {
        vh.bottomText.setText(item.getUsername());
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, Account item)
    {
        int iconId = R.drawable.ic_onpremise;
        switch ((int) item.getTypeId())
        {
            case Account.TYPE_ALFRESCO_TEST_BASIC:
            case Account.TYPE_ALFRESCO_TEST_OAUTH:
                iconId = R.drawable.ic_cloud_alf;
                break;
            case Account.TYPE_ALFRESCO_CLOUD:
                iconId = R.drawable.ic_cloud;
                break;
            default:
                iconId = R.drawable.ic_onpremise;
                break;
        }
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(iconId));
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView, parent);
    }
}
