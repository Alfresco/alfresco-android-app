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
package org.alfresco.mobile.android.application.fragments.node.browser;

import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FolderPathAdapter extends ArrayAdapter<String>
{
    private String item;

    private AlfrescoAccount account;

    public FolderPathAdapter(FragmentActivity context, int textViewResourceId, List<String> objects)
    {
        super(context, textViewResourceId, objects);
        this.account = SessionUtils.getAccount(context);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.app_path_shortcut, null);
        }
        item = getItem(position);
        if (item != null)
        {
            ((TextView) v.findViewById(R.id.bottomtext)).setText(item + "  ");
            v.findViewById(R.id.toptext).setVisibility(View.GONE);
            v.findViewById(R.id.icon).setVisibility(View.VISIBLE);
        }
        return v;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.app_header_row, null);
        }
        item = getItem(position);
        if (item != null && v != null)
        {
            if (AlfrescoAccountManager.getInstance(getContext()).hasMultipleAccount())
            {
                ((TextView) v.findViewById(R.id.toptext)).setText(UIUtils.getAccountLabel(account));
                v.findViewById(R.id.toptext).setVisibility(View.VISIBLE);
            }
            else
            {
                v.findViewById(R.id.toptext).setVisibility(View.GONE);
            }

            ((TextView) v.findViewById(R.id.bottomtext)).setText(getItem(position));
            v.findViewById(R.id.icon).setVisibility(View.GONE);
        }
        return v;
    }
}
