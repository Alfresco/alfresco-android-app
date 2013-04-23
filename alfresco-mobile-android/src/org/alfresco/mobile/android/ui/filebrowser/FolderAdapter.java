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
package org.alfresco.mobile.android.ui.filebrowser;

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

public class FolderAdapter extends ArrayAdapter<String>
{
    static public class FolderBookmark
    {
        String name;
        String location;
        int icon;
    };
    
    private List<FolderBookmark> folders;

    
    public FolderAdapter(Context context, int textViewResourceId, List<FolderBookmark> folders)
    {
        super(context, textViewResourceId);
        this.folders = folders;
        refreshData(folders);
    }

    public void refreshData(List<FolderBookmark> folders)
    {
        clear();
        this.folders = folders;
        for (int i = 0; i < folders.size(); i++)
        {
            add(folders.get(i).name);
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
            v = vi.inflate(R.layout.app_folderbookmark_list_row, null);
        }

        TextView tv = (TextView) v.findViewById(R.id.toptext);
        ImageView iv = (ImageView) v.findViewById(R.id.icon);
    
        iv.setVisibility(View.VISIBLE);
        FolderBookmark item = folders.get(position);
        tv.setText(item.name);
        iv.setImageResource(item.icon);
        
        return v;
    }
}
