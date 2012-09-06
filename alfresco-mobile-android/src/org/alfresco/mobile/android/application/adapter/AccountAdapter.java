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
package org.alfresco.mobile.android.application.adapter;

import java.util.List;

import org.alfresco.mobile.android.application.utils.SDKSessionParameter;
import org.alfresco.mobile.android.ui.utils.AdapterUtils;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class AccountAdapter extends ArrayAdapter<SDKSessionParameter>
{

    protected Activity context;

    protected SDKSessionParameter item;

    protected int textViewResourceId;

    public AccountAdapter(Activity context, int textViewResourceId, List<SDKSessionParameter> listItems)
    {
        super(context, textViewResourceId, listItems);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        View v = AdapterUtils.recycleOrCreateView(context, convertView, textViewResourceId);
        GenericViewHolder vh = (GenericViewHolder) v.getTag();

        item = getItem(position);
        updateControls(vh, item);
        return v;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = AdapterUtils.recycleOrCreateView(context, convertView, textViewResourceId);
        GenericViewHolder vh = (GenericViewHolder) v.getTag();

        item = getItem(position);
        updateControls(vh, item);
        return v;
    }

    private void updateControls(GenericViewHolder v, SDKSessionParameter item)
    {
        if (item != null)
        {
            v.topText.setText(item.getUrl());
            v.bottomText.setText(item.getUsername());
        }
    }
}
