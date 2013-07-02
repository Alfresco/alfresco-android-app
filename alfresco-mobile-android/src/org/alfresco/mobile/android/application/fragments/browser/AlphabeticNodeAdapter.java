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
package org.alfresco.mobile.android.application.fragments.browser;

import java.util.HashMap;
import java.util.List;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AlphabeticNodeAdapter extends ProgressNodeAdapter
{
    private static final int ITEM_TYPE = 0;

    private static final int HEADING_TYPE = 1;

    private HashMap<String, Integer> alphaIndexer = new HashMap<String, Integer>();

    private HashMap<Integer, String> positionIndexer = new HashMap<Integer, String>();

    private LayoutInflater mInflater;

    public AlphabeticNodeAdapter(Activity context, int textViewResourceId, List<Node> objects)
    {
        super(context, textViewResourceId, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public AlphabeticNodeAdapter(Activity context, int textViewResourceId, Node parent, List<Node> listItems,
            List<Node> selectedItems, int mode)
    {
        super(context, textViewResourceId, parent, listItems, selectedItems, mode);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void add(Node object)
    {
        int idx = getCount();
        if (object == null) { return; }
        String element = object.getName();
        String key = element.substring(0, 1).toUpperCase();

        if (!alphaIndexer.containsKey(key))
        {
            alphaIndexer.put(key, idx);
            positionIndexer.put(idx, key);

            super.add(null); 
        }

        // Add the real new item
        super.add(object);
    }

    @Override
    public void clear()
    {
        alphaIndexer.clear();
        positionIndexer.clear();
        super.clear();
    }

    public int getItemViewType(int position)
    {
        return positionIndexer.containsKey(position) ? HEADING_TYPE : ITEM_TYPE;
    }

    public int getViewTypeCount()
    {
        return 2;
    }
   
    @Override
    public boolean isEnabled(int position)
    {
        return getItemViewType(position) == ITEM_TYPE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        int type;

        type = getItemViewType(position);

        if (type == ITEM_TYPE)
        {
            return super.getView(position, convertView, parent);
        }
        else
        {
            ViewHolder holder = null;

            if (convertView == null)
            {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.app_list_header, null);
                holder.textView = (TextView) convertView.findViewById(R.id.list_header_title);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.textView.setText(positionIndexer.get(position));
            return convertView;
        }
    }

    public static class ViewHolder
    {
        public TextView textView;
    }
}
