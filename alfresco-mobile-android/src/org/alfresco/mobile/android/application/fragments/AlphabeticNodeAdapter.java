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

package org.alfresco.mobile.android.application.fragments;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


@SuppressLint("UseSparseArrays")
public abstract class AlphabeticNodeAdapter extends BaseListAdapter<Node, GenericViewHolder>
{ 
    private final int ITEM_TYPE = 0;
    private final int HEADING_TYPE = 1;
    
    HashMap<String, Integer> alphaIndexer;
    HashMap<Integer, String> positionIndexer;
    LayoutInflater mInflater;

    protected abstract void updateTopText(GenericViewHolder vh, Node item2);
    protected abstract void updateBottomText(GenericViewHolder vh, Node item2);
    protected abstract void updateIcon(GenericViewHolder vh, Node item2);
    
    public AlphabeticNodeAdapter(Context context, int textViewResourceId, List<Node> objects)
    {
        super(context, textViewResourceId, objects);
        
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        alphaIndexer = new HashMap<String, Integer>();
        positionIndexer = new HashMap<Integer, String>();
    }

    @Override
    public void add(Node object)
    {
        int idx = getCount();
        String element = object.getName();
        String key = element.substring(0, 1).toUpperCase();
        
        if (alphaIndexer.containsKey(key) == false)
        {
            alphaIndexer.put(key, idx);
            positionIndexer.put(idx, key);
            
            super.add(object);  //Add a dummy at the point of the header to keep in sync.
        }
        
        //Add the real new item
        super.add(object);
    }
    
    @Override
    public void addAll(Collection<? extends Node> collection)
    {
        Node objects[] = (Node[])collection.toArray(new Node[0]);
        
        int size = objects.length;
        for (int i = 0;  i < size;  i++)
        {
            add(objects[i]);
        }
    }
    
    public int getItemViewType(int position)
    {
        return  positionIndexer.containsKey(position) ? HEADING_TYPE : ITEM_TYPE;
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
                holder.textView = (TextView)convertView.findViewById(R.id.list_header_title);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)convertView.getTag();
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
