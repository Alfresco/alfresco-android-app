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
package org.alfresco.mobile.android.application.fragments.config;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.configuration.model.ViewConfigModel;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import androidx.fragment.app.Fragment;
import android.widget.Filter;

public class ViewConfigTypeItemAdapter extends BaseListAdapter<ViewConfigModel, TwoLinesViewHolder>
{
    private List<ViewConfigModel> fullList;

    private ArrayList<ViewConfigModel> mOriginalValues;

    private ArrayFilter mFilter;

    public ViewConfigTypeItemAdapter(Fragment fragment, int textViewResourceId, List<ViewConfigModel> objects)
    {
        super(fragment.getActivity(), textViewResourceId, objects);
        this.vhClassName = TwoLinesViewHolder.class.getCanonicalName();
        fullList = objects;
        mOriginalValues = new ArrayList<>(objects);

    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, ViewConfigModel item)
    {
        vh.topText.setText(item.getLabel(getContext()));
    }

    @Override
    protected void updateBottomText(TwoLinesViewHolder vh, ViewConfigModel item)
    {
        vh.bottomText.setText(item.getDescriptionId());
        HolderUtils.makeMultiLine(vh.bottomText, 3);
    }

    @Override
    protected void updateIcon(TwoLinesViewHolder vh, ViewConfigModel item)
    {
        vh.icon.setImageResource(item.getIconModelResId());
        vh.icon.setTag(item);
    }

    @Override
    public Filter getFilter()
    {
        if (mFilter == null)
        {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter
    {
        private Object lock = new Object();

        @Override
        protected FilterResults performFiltering(CharSequence prefix)
        {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null)
            {
                synchronized (lock)
                {
                    mOriginalValues = new ArrayList<>(fullList);
                }
            }

            if (prefix == null || prefix.length() == 0)
            {
                synchronized (lock)
                {
                    ArrayList<ViewConfigModel> list = new ArrayList<>(mOriginalValues);
                    results.values = list;
                    results.count = list.size();
                }
            }
            else
            {
                final String prefixString = prefix.toString().toLowerCase();

                ArrayList<ViewConfigModel> values = mOriginalValues;
                int count = values.size();

                ArrayList<ViewConfigModel> newValues = new ArrayList<>(count);

                for (int i = 0; i < count; i++)
                {
                    ViewConfigModel item = values.get(i);
                    if (item.getLabel(getContext()).toLowerCase().contains(prefixString))
                    {
                        newValues.add(item);
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            clear();
            if (results.values != null)
            {
                fullList = (ArrayList<ViewConfigModel>) results.values;
                addAll(fullList);
            }
            else
            {
                fullList = new ArrayList<>();
            }
            if (results.count > 0)
            {
                notifyDataSetChanged();
            }
            else
            {
                notifyDataSetInvalidated();
            }
        }
    }
}