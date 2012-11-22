/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.accounts.fragment;

import java.util.List;

import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.utils.AdapterUtils;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

public class AccountDetailsAdapter extends ArrayAdapter<Account>
{

    protected Activity context;

    protected Account item;

    private List<Account> selectedItems;

    protected int textViewResourceId;

    public AccountDetailsAdapter(Activity context, int textViewResourceId, List<Account> listItems,
            List<Account> selectedItems)
    {
        super(context, textViewResourceId, listItems);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        this.selectedItems = selectedItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = AdapterUtils.recycleOrCreateView(context, convertView, textViewResourceId);
        GenericViewHolder vh = (GenericViewHolder) v.getTag();

        item = getItem(position);
        if (item != null)
        {
            vh.icon.setTag(position);
        }
        updateControls(vh, item);
        return v;
    }

    private void updateControls(GenericViewHolder v, Account item)
    {
        if (item != null)
        {
            v.topText.setText(item.getDescription());
            updateControlIcon(v, item);
            if (item.getActivation() != null)
            {
                v.bottomText.setText(context.getText(R.string.sign_up_cloud_awaiting_email));
            }
            else
            {
                v.bottomText.setText(item.getUsername());
            }

            if (selectedItems != null && selectedItems.contains(item))
            {
                ((LinearLayout) v.icon.getParent()).setBackgroundDrawable(getContext().getResources().getDrawable(
                        R.drawable.list_longpressed_holo));
            }
            else
            {
                ((LinearLayout) v.icon.getParent()).setBackgroundDrawable(null);
            }

        }
    }

    protected void updateControlIcon(GenericViewHolder vh, Account item)
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
}
