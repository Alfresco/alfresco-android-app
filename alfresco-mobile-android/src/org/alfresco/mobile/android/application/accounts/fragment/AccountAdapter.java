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
package org.alfresco.mobile.android.application.accounts.fragment;

import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountAdapter extends ArrayAdapter<String>
{
    private Account item;

    private List<Account> accounts;

    public AccountAdapter(Context context, int textViewResourceId, List<Account> accounts)
    {
        super(context, textViewResourceId);
        this.accounts = accounts;
        refreshData(accounts);
    }

    public void refreshData(List<Account> accounts)
    {
        clear();
        this.accounts = accounts;
        for (int i = 0; i < accounts.size(); i++)
        {
            add(getLabel(accounts.get(i)));
        }
        add(getContext().getText(R.string.manage_accounts).toString());
        if (SessionUtils.getAccount(getContext()) != null)
        {
            long type = SessionUtils.getAccount(getContext()).getTypeId();
            if (type == Account.TYPE_ALFRESCO_CLOUD || type == Account.TYPE_ALFRESCO_TEST_OAUTH)
            {
                add(getContext().getText(R.string.cloud_networks_switch).toString());
            }
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getInternalView(position, convertView);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return getInternalView(position, convertView);
    }

    private View getInternalView(int position, View convertView)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.app_account_list_row, null);
        }

        TextView tv = (TextView) v.findViewById(R.id.toptext);
        ImageView iv = (ImageView) v.findViewById(R.id.icon);
        if (position == accounts.size())
        {
            tv.setText(getContext().getText(R.string.manage_accounts));
            iv.setVisibility(View.VISIBLE);
            iv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_settings_light));
        }
        else if (position == accounts.size() + 1)
        {
            tv.setText(getContext().getText(R.string.cloud_networks_switch));
            iv.setVisibility(View.VISIBLE);
            iv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_settings_light));
        }
        else
        {
            iv.setVisibility(View.VISIBLE);
            item = accounts.get(position);
            tv.setText(item.getDescription());
            iv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_account_light));
        }
        return v;
    }

    private String getLabel(Account account)
    {
        String label = account.getDescription();
        if (label == null || label.isEmpty())
        {
            label = account.getUsername();
        }
        return label;
    }
}
